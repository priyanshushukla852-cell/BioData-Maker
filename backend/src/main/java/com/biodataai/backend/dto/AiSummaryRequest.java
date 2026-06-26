package com.biodataai.backend.dto;

import com.biodataai.backend.entity.LanguagePref;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AiSummaryRequest(@NotNull UUID biodataId, @NotNull LanguagePref language) {
}
