package com.biodataai.app.repository

import android.content.Context
import com.biodataai.app.network.RetrofitClient
import com.biodataai.app.network.api.TemplateApiResponse
import com.biodataai.app.network.api.TemplateService
import com.biodataai.app.ui.viewmodel.TemplateOption

/**
 * Fetches the template catalogue from the backend (`GET /api/templates`) and adapts it to
 * the picker's [TemplateOption] model.
 *
 * v1 product rules applied here (per CLAUDE.md):
 *  - premium templates are excluded (premium is v1.1+ scope)
 *  - only templates whose backend `style` maps to a render key the client can actually
 *    produce are shown. Styles without a client renderer yet (e.g. FLORAL) are excluded
 *    until their renderer exists, so we never offer a template we can't draw.
 *
 * Offline-first: if the network call fails, the picker falls back to the built-in default
 * set so the user can still proceed (the templates are static design assets, not user data).
 */
class TemplateRepository(
    private val context: Context,
    private val service: TemplateService =
        RetrofitClient.getRetrofit(context).create(TemplateService::class.java)
) {

    suspend fun getTemplates(): List<TemplateOption> {
        return try {
            service.listTemplates()
                .filter { !it.premium }
                .mapNotNull { it.toOptionOrNull() }
                .ifEmpty { DEFAULT_TEMPLATES }
        } catch (e: Exception) {
            // Offline or backend unavailable — keep the picker usable.
            DEFAULT_TEMPLATES
        }
    }

    private fun TemplateApiResponse.toOptionOrNull(): TemplateOption? {
        val renderKey = RENDER_KEYS[style.uppercase()] ?: return null
        return TemplateOption(
            id = renderKey,
            name = name,
            description = DESCRIPTIONS[renderKey] ?: ""
        )
    }

    companion object {
        /** Backend TemplateStyle -> client render key consumed by BiodataPreviewViewModel. */
        private val RENDER_KEYS = mapOf(
            "TRADITIONAL" to "classic",
            "MODERN" to "modern",
            "MINIMAL" to "minimal"
        )

        private val DESCRIPTIONS = mapOf(
            "classic" to "Traditional wedding biodata format",
            "modern" to "Contemporary professional layout",
            "minimal" to "Clean and simple design"
        )

        /** Fallback used when the backend can't be reached. */
        val DEFAULT_TEMPLATES = listOf(
            TemplateOption("classic", "Classic", DESCRIPTIONS["classic"]!!),
            TemplateOption("modern", "Modern", DESCRIPTIONS["modern"]!!),
            TemplateOption("minimal", "Minimal", DESCRIPTIONS["minimal"]!!)
        )
    }
}
