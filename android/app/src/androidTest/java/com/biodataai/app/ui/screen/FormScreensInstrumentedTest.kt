package com.biodataai.app.ui.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose UI tests for standalone components that have no Firebase/Room dependency.
 *
 * Note: full-screen composables like [TemplatePickerScreen] and [BiodataPreviewScreen]
 * are intentionally NOT tested here — they call FirebaseAuth.getInstance() and
 * BioDataDatabase.getInstance() directly, which throw "Default FirebaseApp is not
 * initialized" in a test process. Testing those requires DI / a Firebase test harness,
 * tracked separately. Offline-first persistence is covered by BiodataDaoTest instead.
 */
@RunWith(AndroidJUnit4::class)
class FormScreensInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun offlineBanner_isDisplayed() {
        composeTestRule.setContent {
            com.biodataai.app.ui.component.OfflineStateBanner()
        }
        // R.string.offline_banner = "Offline - Changes will sync when connection returns".
        // Use substring match so the test survives minor copy tweaks and stays locale-tolerant.
        composeTestRule
            .onNodeWithText("Offline", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun offlineBanner_explainsSyncBehavior() {
        composeTestRule.setContent {
            com.biodataai.app.ui.component.OfflineStateBanner()
        }
        composeTestRule
            .onNodeWithText("sync when connection returns", substring = true)
            .assertExists()
    }
}
