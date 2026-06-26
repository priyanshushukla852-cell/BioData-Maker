package com.biodataai.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyTokenRequest(@NotBlank String idToken) {
}
