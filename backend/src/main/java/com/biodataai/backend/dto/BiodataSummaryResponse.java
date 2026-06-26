package com.biodataai.backend.dto;

import com.biodataai.backend.entity.Biodata;
import com.biodataai.backend.entity.BiodataStatus;
import com.biodataai.backend.entity.LanguagePref;
import java.time.Instant;
import java.util.UUID;

public record BiodataSummaryResponse(
        UUID biodataId, String title, LanguagePref language, BiodataStatus status, Instant updatedAt) {

    public static BiodataSummaryResponse from(Biodata biodata) {
        return new BiodataSummaryResponse(
                biodata.getId(), biodata.getTitle(), biodata.getLanguage(), biodata.getStatus(), biodata.getUpdatedAt());
    }
}
