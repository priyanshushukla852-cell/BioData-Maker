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
 * Full-screen composables ([TemplatePickerScreen], [BiodataPreviewScreen]) that call
 * FirebaseAuth.getInstance() are covered in [BiodataScreenInstrumentedTest], which relies
 * on the stub FirebaseApp registered by [com.biodataai.app.FirebaseTestRunner].
 * Offline-first persistence is covered by [com.biodataai.app.db.BiodataDaoTest].
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
