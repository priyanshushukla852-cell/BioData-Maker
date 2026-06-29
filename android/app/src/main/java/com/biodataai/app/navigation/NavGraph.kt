package com.biodataai.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.biodataai.app.ui.screen.SplashScreen
import com.biodataai.app.ui.screen.LoginScreen
import com.biodataai.app.ui.screen.HomeScreen
import com.biodataai.app.ui.screen.BiodataCreateScreen
import com.biodataai.app.ui.screen.FormStepScreen
import com.biodataai.app.ui.screen.AiSummaryReviewScreen
import com.biodataai.app.ui.screen.TemplatePickerScreen
import com.biodataai.app.ui.screen.BiodataPreviewScreen
import com.biodataai.app.ui.screen.PdfExportScreen

@Composable
fun BioDataNavGraph(
    navController: NavHostController,
    startDestination: NavRoute = NavRoute.Splash
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<NavRoute.Splash> {
            SplashScreen(navController)
        }

        composable<NavRoute.Auth.Login> {
            LoginScreen(navController)
        }

        composable<NavRoute.Auth.PhoneOtp> {
            // PhoneOtpScreen placeholder
        }

        composable<NavRoute.Home> {
            HomeScreen(navController)
        }

        composable<NavRoute.BiodataCreate> { backStackEntry ->
            val biodataId = backStackEntry.arguments?.getString("biodataId")
            BiodataCreateScreen(navController, biodataId)
        }

        composable<NavRoute.FormStep> { backStackEntry ->
            val biodataId = backStackEntry.arguments?.getString("biodataId")
            val step = backStackEntry.arguments?.getInt("step") ?: 1
            if (biodataId != null) {
                FormStepScreen(navController, biodataId, step)
            }
        }

        composable<NavRoute.AiSummaryReview> { backStackEntry ->
            val biodataId = backStackEntry.arguments?.getString("biodataId")
            if (biodataId != null) {
                AiSummaryReviewScreen(navController, biodataId)
            }
        }

        composable<NavRoute.TemplatePicker> { backStackEntry ->
            val biodataId = backStackEntry.arguments?.getString("biodataId")
            if (biodataId != null) {
                TemplatePickerScreen(navController, biodataId)
            }
        }

        composable<NavRoute.BiodataPreview> { backStackEntry ->
            val biodataId = backStackEntry.arguments?.getString("biodataId")
            val templateId = backStackEntry.arguments?.getString("templateId") ?: "classic"
            if (biodataId != null) {
                BiodataPreviewScreen(navController, biodataId, templateId)
            }
        }

        composable<NavRoute.PdfExport> { backStackEntry ->
            val biodataId = backStackEntry.arguments?.getString("biodataId")
            val templateId = backStackEntry.arguments?.getString("templateId") ?: "classic"
            if (biodataId != null) {
                PdfExportScreen(navController, biodataId, templateId)
            }
        }
    }
}
