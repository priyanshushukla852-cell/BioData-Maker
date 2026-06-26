package com.biodataai.app

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics

class BioDataApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeFirebase()
    }

    private fun initializeFirebase() {
        try {
            Firebase.crashlytics.setCrashlyticsCollectionEnabled(true)
        } catch (e: Exception) {
            // Firebase not configured yet (dev without google-services.json is fine)
        }
    }
}
