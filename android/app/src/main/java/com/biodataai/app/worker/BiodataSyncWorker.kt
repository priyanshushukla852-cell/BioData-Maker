package com.biodataai.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.biodataai.app.db.BioDataDatabase
import com.biodataai.app.network.api.BiodataService
import com.biodataai.app.network.api.UpdateBiodataRequest
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant

class BiodataSyncWorker(
    context: Context,
    params: WorkerParameters,
    private val database: BioDataDatabase,
    private val biodataService: BiodataService,
    private val firebaseAuth: FirebaseAuth
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val userId = firebaseAuth.currentUser?.uid ?: return@withContext Result.retry()
                
                val unsyncedBiodatas = database.biodataDao().getUnsyncedBiodatas(userId)
                
                for (biodata in unsyncedBiodatas) {
                    val formDataJson = biodata.formDataJson ?: continue
                    
                    try {
                        biodataService.updateBiodata(
                            biodata.id,
                            UpdateBiodataRequest(formDataJson)
                        )
                        
                        val updatedBiodata = biodata.copy(syncedAt = Instant.now())
                        database.biodataDao().updateBiodata(updatedBiodata)
                    } catch (e: Exception) {
                        return@withContext Result.retry()
                    }
                }
                
                Result.success()
            } catch (e: Exception) {
                Result.retry()
            }
        }
    }

    companion object {
        const val WORK_NAME = "biodata_sync"
    }
}
