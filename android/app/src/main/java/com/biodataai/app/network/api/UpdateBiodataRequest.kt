package com.biodataai.app.network.api

/**
 * Request body for PUT /api/biodatas/{id} to sync form data to backend.
 * Carries serialized FormState as JSON.
 */
data class UpdateBiodataRequest(
    val formDataJson: String
)
