package com.biodataai.backend.dto;

import com.biodataai.backend.entity.BiodataStatus;
import com.biodataai.backend.entity.LanguagePref;
import java.util.UUID;

/**
 * All fields are optional; the service only updates sections that are present so a client can
 * PUT a single section (e.g. just {@code lifestyle}) without resending the whole biodata.
 */
public record BiodataUpdateRequest(
        String title,
        LanguagePref language,
        UUID templateId,
        BiodataStatus status,
        PersonalDetailsDto personalDetails,
        FamilyDetailsDto familyDetails,
        EducationCareerDto educationCareer,
        LifestyleDto lifestyle,
        AstrologyDto astrology,
        ContactInfoDto contactInfo) {
}
