package com.biodataai.backend.dto;

import com.biodataai.backend.entity.BiodataStatus;
import com.biodataai.backend.entity.LanguagePref;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BiodataResponse(
        UUID biodataId,
        String title,
        LanguagePref language,
        BiodataStatus status,
        UUID templateId,
        String aiSummary,
        PersonalDetailsDto personalDetails,
        FamilyDetailsDto familyDetails,
        EducationCareerDto educationCareer,
        LifestyleDto lifestyle,
        AstrologyDto astrology,
        ContactInfoDto contactInfo,
        List<PhotoDto> photos,
        Instant createdAt,
        Instant updatedAt) {
}
