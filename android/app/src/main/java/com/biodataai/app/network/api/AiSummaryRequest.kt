package com.biodataai.app.network.api

/**
 * Request to generate an AI summary. Sent to POST /api/ai/summary.
 * biodataId is the shared client/server UUID; language is "EN" or "HI".
 */
data class AiSummaryRequest(
    val biodataId: String,
    val language: String
)

/**
 * AI-generated summary response from the backend.
 */
data class AiSummaryResponse(
    val summaryText: String,
    val generationId: String
)
