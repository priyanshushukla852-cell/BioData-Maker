package com.biodataai.app.repository

import android.content.Context
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
                        templateId = templateId,
                        language = languagePref
                    )
                )
            } catch (e: Exception) {
                // Offline or backend error — user can still work on local draft
            }

            Result.Success(biodataEntity)
        } catch (e: Exception) {
            Result.Error(e, "Failed to create biodata: ${e.message}")
        }
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
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to delete biodata: ${e.message}")
        }
    }
}
