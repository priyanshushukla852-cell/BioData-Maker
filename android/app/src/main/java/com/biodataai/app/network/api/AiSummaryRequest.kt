package com.biodataai.app.network.api

/**
 * Request to generate AI summary from form data
 * Sent to POST /api/ai/summary
 */
data class AiSummaryRequest(
    val formDataJson: String, // Serialized FormState
    val languagePref: String // "EN" or "HI"
)

/**
 * AI-generated summary response
 */
data class AiSummaryResponse(
    val summary: String,
    val keywords: List<String>,
    val generatedAt: String
)
