package com.biodataai.app.ui.screen

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FormScreensInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testOfflineBannerVisibility() {
        composeTestRule.setContent {
            com.biodataai.app.ui.component.OfflineStateBanner()
        }

        composeTestRule.onNodeWithText("Offline").assertExists()
    }

    @Test
    fun testOfflineBannerMessage() {
        composeTestRule.setContent {
            com.biodataai.app.ui.component.OfflineStateBanner()
        }

        composeTestRule.onNodeWithText("Changes will sync when connection returns").assertExists()
    }

    @Test
    fun testTemplatePickerScreenRendering() {
        composeTestRule.setContent {
            TemplatePickerScreen(
                navController = androidx.navigation.compose.rememberNavController(),
                biodataId = "test-biodata-123"
            )
        }

        composeTestRule.onNodeWithText("Choose Template").assertExists()
    }

    @Test
    fun testBiodataPreviewScreenRendering() {
        composeTestRule.setContent {
            BiodataPreviewScreen(
                navController = androidx.navigation.compose.rememberNavController(),
                biodataId = "test-biodata-123",
                templateId = "classic"
            )
        }

        composeTestRule.onNodeWithText("Preview").assertExists()
    }
}
