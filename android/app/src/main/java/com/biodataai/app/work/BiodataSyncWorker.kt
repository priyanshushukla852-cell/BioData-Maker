package com.biodataai.app.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.biodataai.app.db.BioDataDatabase
import com.biodataai.app.repository.BiodataRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Background sync worker that pushes queued local biodatas to the backend.
 * Scheduled when connectivity returns to sync offline drafts that couldn't be uploaded.
 */
class BiodataSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            val firebaseAuth = FirebaseAuth.getInstance()
            val userId = firebaseAuth.currentUser?.uid
                ?: return@withContext Result.retry()

            val database = BioDataDatabase.getInstance(applicationContext)
            val biodataRepository = BiodataRepository(applicationContext, database)

            // Attempt to sync biodatas from Room to backend
            // In a full implementation, this would:
            // 1. Query Room for biodatas with syncedAt = null or syncError != null
            // 2. Retry POSTing them to the backend
            // 3. Mark as synced on success, log error on failure
            // For MVP, this is a placeholder that will be expanded in Phase 3/4

            Result.success()
        } catch (e: Exception) {
            // Retry with exponential backoff (WorkManager handles this)
            Result.retry()
        }
    }

    companion object {
        const val WORK_TAG = "biodataSyncWorker"
    }
}
