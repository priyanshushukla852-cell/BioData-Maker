package com.biodataai.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Basic instrumentation sanity check — confirms the test APK installs against the
 * app under test and the instrumentation target resolves.
 *
 * Real coverage lives in:
 *  - [com.biodataai.app.db.BiodataDaoTest] — offline-first Room persistence
 *  - [com.biodataai.app.ui.screen.FormScreensInstrumentedTest] — Compose components
 *
 * A full end-to-end golden-path test (auth -> 7 steps -> AI summary -> template ->
 * PDF export) is intentionally NOT stubbed here; it requires a Firebase test harness
 * and is tracked as follow-up work rather than faked with a passing no-op.
 */
@RunWith(AndroidJUnit4::class)
class InstrumentedTestRunner {

    @Test
    fun instrumentationTargetsAppUnderTest() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.biodataai.app", appContext.packageName)
    }
}
