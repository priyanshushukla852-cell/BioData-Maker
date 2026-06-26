package com.biodataai.backend.dto;

import com.biodataai.backend.entity.LanguagePref;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BiodataCreateRequest(
        @NotBlank String title,
        @NotNull LanguagePref language) {
}
