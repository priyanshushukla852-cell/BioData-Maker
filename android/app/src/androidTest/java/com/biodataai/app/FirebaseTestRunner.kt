package com.biodataai.app

import android.app.Application
import androidx.test.runner.AndroidJUnitRunner
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

/**
 * Custom instrumentation runner that registers a stub default [FirebaseApp] before the
 * app under test starts.
 *
 * Why this exists: several screen composables (e.g. TemplatePickerScreen,
 * BiodataPreviewScreen) call `FirebaseAuth.getInstance()` at composition time. Without a
 * configured FirebaseApp that throws "Default FirebaseApp is not initialized" in the test
 * process, because the test APK has no google-services.json. The options below are dummy
 * values — no real Firebase project is contacted; we only need the SDK to construct an
 * instance so the composables can render. The ViewModels never make auth network calls.
 *
 * Registered via `testInstrumentationRunner` in app/build.gradle.kts.
 */
class FirebaseTestRunner : AndroidJUnitRunner() {

    override fun callApplicationOnCreate(app: Application) {
        if (FirebaseApp.getApps(app).isEmpty()) {
            val options = FirebaseOptions.Builder()
                .setApplicationId("1:000000000000:android:abcdef0123456789")
                .setApiKey("AIzaSyDUMMY-KEY-FOR-INSTRUMENTED-TESTS-0000")
                .setProjectId("biodata-instrumented-tests")
                .build()
            FirebaseApp.initializeApp(app, options)
        }
        super.callApplicationOnCreate(app)
    }
}
