package com.biodataai.app.debug

import android.os.StrictMode

object StrictModeConfig {
    /**
     * Enable StrictMode for development builds only
     * Catches:
     * - Disk I/O on main thread
     * - Network calls on main thread
     * - Custom violations
     */
    fun enableStrictMode(isDebug: Boolean) {
        if (!isDebug) return

        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .detectCustomSlowCalls()
                .penaltyLog()
                .penaltyFlashScreen()
                .build()
        )

        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .build()
        )
    }

    fun enableDiskReadPolicy() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .penaltyLog()
                .build()
        )
    }

    fun enableNetworkPolicy() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectNetwork()
                .penaltyLog()
                .build()
        )
    }
}
