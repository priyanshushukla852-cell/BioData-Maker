package com.biodataai.app.network.api

/**
 * Request body for PUT /api/biodatas/{id}. Mirrors the backend's structured update contract: all
 * sections are optional, so a client can sync any subset. Built from the local FormState via
 * [FormState.toUpdateRequest]. The Idempotency-Key header (IdempotencyInterceptor) prevents
 * duplicates on retry.
 */
data class UpdateBiodataRequest(
    val title: String? = null,
    val personalDetails: PersonalDetailsDto? = null,
    val familyDetails: FamilyDetailsDto? = null,
    val educationCareer: EducationCareerDto? = null,
    val lifestyle: LifestyleDto? = null,
    val astrology: AstrologyDto? = null,
    val contactInfo: ContactInfoDto? = null
)
