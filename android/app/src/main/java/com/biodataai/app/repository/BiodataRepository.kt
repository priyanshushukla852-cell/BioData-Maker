package com.biodataai.app.repository

import android.content.Context
import android.util.Log
import com.biodataai.app.core.Result
import com.biodataai.app.db.BioDataDatabase
import com.biodataai.app.db.entity.BiodataEntity
import com.biodataai.app.db.entity.BiodataStatus
import com.biodataai.app.db.entity.LanguagePref
import com.biodataai.app.db.entity.UserEntity
import com.biodataai.app.network.RetrofitClient
import com.biodataai.app.network.api.BiodataService
import com.biodataai.app.network.api.CreateBiodataRequest
import com.biodataai.app.network.api.toUpdateRequest
import com.biodataai.app.ui.viewmodel.FormState
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.time.Instant
import java.util.UUID

class BiodataRepository(
    private val context: Context,
    private val database: BioDataDatabase
) {

    private val biodataService: BiodataService by lazy {
        RetrofitClient.getRetrofit(context).create(BiodataService::class.java)
    }

    private val biodataDao = database.biodataDao()

    // Server-sync failures used to be swallowed silently, which let a draft live only in Room and
    // then made the AI summary call 500 on an id the server never received. Log them (metadata
    // only — no PII per CLAUDE.md rule 2: just the operation, the biodata UUID, and the error type).
    private fun logSyncFailure(op: String, biodataId: String, e: Exception) {
        val detail = if (e is HttpException) "HTTP ${e.code()}" else e.javaClass.simpleName
        Log.w(TAG, "Backend sync failed [$op] for biodata=$biodataId: $detail")
    }

    // Create a new biodata locally and sync to backend
    suspend fun createBiodata(
        userFirebaseUid: String,
        templateId: String,
        languagePref: String,
        displayName: String? = null,
        email: String? = null,
        phoneNumber: String? = null
    ): Result<BiodataEntity> {
        return try {
            // The biodatas.userFirebaseUid FK requires the local users row to exist first.
            // Sign-in only authenticates with Firebase/backend; it doesn't populate Room, so
            // ensure the row is present (no-op if it already is — see insertUserIfAbsent).
            biodataDao.insertUserIfAbsent(
                UserEntity(
                    firebaseUid = userFirebaseUid,
                    displayName = displayName,
                    phoneNumber = phoneNumber,
                    email = email,
                    profilePhotoUrl = null,
                    createdAt = Instant.now(),
                    updatedAt = Instant.now()
                )
            )

            // Create locally first (Room as source of truth)
            val biodataEntity = BiodataEntity(
                id = UUID.randomUUID().toString(),
                userFirebaseUid = userFirebaseUid,
                title = "", // Will be filled in during form steps
                templateId = templateId,
                language = LanguagePref.valueOf(languagePref),
                status = BiodataStatus.DRAFT,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                deletedAt = null
            )
            biodataDao.insertBiodata(biodataEntity)

            // Sync to backend (non-blocking — continue if offline or backend fails). The local id
            // is sent so the server row shares it (offline-first id alignment), making the AI
            // summary call resolvable by the same biodataId.
            try {
                biodataService.createBiodata(
                    CreateBiodataRequest(
                        id = biodataEntity.id,
                        title = biodataEntity.title,
                        // templateId is a client-side design-asset key ("classic"/"modern"), NOT a
                        // server template UUID. Sending it made the server reject the create as an
                        // invalid UUID (400) — silently — so the row never synced. Omit it here;
                        // the template choice only matters to client-side PDF rendering.
                        templateId = null,
                        language = languagePref
                    )
                )
            } catch (e: Exception) {
                // Offline or backend error — user can still work on the local draft, but record it
                // so a persistent sync failure is visible (Crashlytics) instead of disappearing.
                logSyncFailure("create", biodataEntity.id, e)
            }

            Result.Success(biodataEntity)
        } catch (e: Exception) {
            Result.Error(e, "Failed to create biodata: ${e.message}")
        }
    }

    /**
     * Ensure this biodata exists on the backend before a server-side operation that resolves it by
     * id (e.g. AI summary). Drafts are created offline-first in Room and may never have synced (or
     * the original create may have failed), so the server can 404/500 on an id it never received.
     *
     * Create is idempotent on the client-supplied id server-side, so this is safe to call every
     * time. Returns [Result.Error] if the row can't be synced so the caller can surface a real
     * message instead of the old misleading "write your own" fallback.
     */
    suspend fun ensureSyncedToServer(biodataId: String): Result<Unit> {
        val biodata = biodataDao.getBiodataById(biodataId)
            ?: return Result.Error(
                IllegalStateException("Biodata $biodataId not found locally"),
                "Biodata not found"
            )

        // Create-if-missing must succeed — without the server row the AI call can't resolve the id.
        try {
            biodataService.createBiodata(
                CreateBiodataRequest(
                    id = biodata.id,
                    title = biodata.title,
                    templateId = null, // see createBiodata: client template keys aren't server UUIDs
                    language = biodata.language.name
                )
            )
        } catch (e: Exception) {
            logSyncFailure("ensureSynced.create", biodataId, e)
            return Result.Error(e, "Couldn't sync your biodata to the server")
        }

        // Best-effort: push the latest form fields so the summary reflects current input. The row
        // already exists at this point, so a failure here shouldn't block generation.
        try {
            val formState = try {
                Gson().fromJson(biodata.formDataJson ?: "{}", FormState::class.java) ?: FormState()
            } catch (e: Exception) {
                FormState()
            }
            biodataService.updateBiodata(
                biodata.id,
                formState.toUpdateRequest(title = biodata.title.ifBlank { null })
            )
            biodataDao.updateBiodata(biodata.copy(syncedAt = Instant.now()))
        } catch (e: Exception) {
            logSyncFailure("ensureSynced.update", biodataId, e)
        }

        return Result.Success(Unit)
    }

    // Get user's biodatas from Room (offline-first)
    fun getUserBiodatas(userId: String): Flow<List<BiodataEntity>> {
        return biodataDao.getBiodatasByUser(userId)
    }

    // Get a single biodata by ID
    suspend fun getBiodata(id: String): Result<BiodataEntity> {
        return try {
            val biodata = biodataDao.getBiodataById(id)
                ?: return Result.Error(
                    Exception("Biodata not found"),
                    "Biodata with ID $id does not exist"
                )
            Result.Success(biodata)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get biodata: ${e.message}")
        }
    }

    // Update a biodata (e.g., form data, title, status)
    suspend fun updateBiodata(biodata: BiodataEntity): Result<Unit> {
        return try {
            biodataDao.updateBiodata(biodata)
            // Sync update to backend (non-blocking). Parse the cached FormState blob and map it to
            // the backend's structured sections so the server can build a properly-redacted AI
            // prompt (income/caste/Manglik are stripped server-side per CLAUDE.md).
            try {
                val formState = try {
                    Gson().fromJson(biodata.formDataJson ?: "{}", FormState::class.java) ?: FormState()
                } catch (e: Exception) {
                    FormState()
                }
                val request = formState.toUpdateRequest(title = biodata.title.ifBlank { null })
                biodataService.updateBiodata(biodata.id, request)
            } catch (e: Exception) {
                // Offline or backend error — update will sync on next connectivity
                logSyncFailure("update", biodata.id, e)
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to update biodata: ${e.message}")
        }
    }

    // Soft delete a biodata
    suspend fun deleteBiodata(id: String): Result<Unit> {
        return try {
            biodataDao.softDeleteBiodata(id, Instant.now())
            // Sync deletion to backend (non-blocking)
            try {
                biodataService.deleteBiodata(id)
            } catch (e: Exception) {
                // Offline — deletion will sync on next connectivity
                logSyncFailure("delete", id, e)
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to delete biodata: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "BiodataRepository"
    }
}
