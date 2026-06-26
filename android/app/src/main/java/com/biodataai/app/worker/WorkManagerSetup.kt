package com.biodataai.app.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WorkManagerSetup {
    fun scheduleBiodataSyncWorker(context: Context) {
        val syncWorkerRequest = PeriodicWorkRequestBuilder<BiodataSyncWorkerWrapper>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES,
            flexTimeInterval = 5,
            flexTimeIntervalUnit = TimeUnit.MINUTES
        ).setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(false)
                .build()
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            BiodataSyncWorkerWrapper.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncWorkerRequest
        )
    }

    fun cancelBiodataSyncWorker(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(BiodataSyncWorkerWrapper.WORK_NAME)
    }
}
