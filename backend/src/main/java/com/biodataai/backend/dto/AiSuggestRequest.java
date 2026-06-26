package com.biodataai.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AiSuggestRequest(@NotNull UUID biodataId, @NotBlank String fieldName, String currentValue) {
}
