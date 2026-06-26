package com.biodataai.app.repository

import android.content.Context
import com.biodataai.app.core.Result
import com.biodataai.app.db.BioDataDatabase
import com.biodataai.app.db.entity.BiodataEntity
import com.biodataai.app.db.entity.BiodataStatus
import com.biodataai.app.db.entity.LanguagePref
import com.biodataai.app.network.RetrofitClient
import com.biodataai.app.network.api.BiodataService
import com.biodataai.app.network.api.CreateBiodataRequest
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
        languagePref: String
    ): Result<BiodataEntity> {
        return try {
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

            // Sync to backend (non-blocking — continue if offline or backend fails)
            try {
                biodataService.createBiodata(
                    CreateBiodataRequest(templateId, languagePref)
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
            // Sync update to backend (non-blocking)
            try {
                // TODO: Create PUT endpoint for updates
            } catch (e: Exception) {
                // Offline — update will sync on next connectivity
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
