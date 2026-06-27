package com.biodataai.app.network.api

import retrofit2.http.GET

/**
 * Mirrors the backend TemplateResponse (GET /api/templates).
 * `style` is the backend TemplateStyle enum name (e.g. "TRADITIONAL", "MODERN").
 */
data class TemplateApiResponse(
    val templateId: String,
    val name: String,
    val previewUrl: String?,
    val style: String,
    val supportsHindi: Boolean,
    val premium: Boolean
)

interface TemplateService {
    @GET("/api/templates")
    suspend fun listTemplates(): List<TemplateApiResponse>
}
