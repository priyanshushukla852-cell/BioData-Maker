package com.biodataai.app.work

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Manages background sync scheduling for offline biodatas.
 * Schedules a sync worker to run when the device regains connectivity.
 */
object SyncScheduler {

    /**
     * Schedule a one-time background sync that requires network connectivity.
     * If a sync is already scheduled, it's replaced (to avoid duplicate work).
     */
    fun scheduleSync(context: Context) {
        val syncRequest = OneTimeWorkRequestBuilder<BiodataSyncWorker>()
            .addTag(BiodataSyncWorker.WORK_TAG)
            .setConstraints(
                androidx.work.Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            BiodataSyncWorker.WORK_TAG,
            ExistingWorkPolicy.KEEP, // If sync is already scheduled, keep it (don't duplicate)
            syncRequest
        )
    }

    /**
     * Cancel any pending sync work.
     */
    fun cancelSync(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(BiodataSyncWorker.WORK_TAG)
    }
}
