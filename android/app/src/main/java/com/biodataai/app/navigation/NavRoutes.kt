package com.biodataai.app.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class NavRoute {
    @Serializable
    data object Splash : NavRoute()

    @Serializable
    data object Auth : NavRoute() {
        @Serializable
        data object Login : NavRoute()

        @Serializable
        data object PhoneOtp : NavRoute()
    }

    @Serializable
    data object Home : NavRoute()

    @Serializable
    data class BiodataCreate(val biodataId: String? = null) : NavRoute()

    @Serializable
    data class FormStep(val biodataId: String, val step: Int) : NavRoute()

    @Serializable
    data class AiSummaryReview(val biodataId: String) : NavRoute()

    @Serializable
    data class TemplatePicker(val biodataId: String) : NavRoute()

    @Serializable
    data class BiodataPreview(val biodataId: String, val templateId: String) : NavRoute()

    @Serializable
    data class PdfExport(val biodataId: String, val templateId: String) : NavRoute()

    @Serializable
    data object Settings : NavRoute()
}
