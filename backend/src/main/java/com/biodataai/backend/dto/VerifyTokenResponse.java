package com.biodataai.backend.dto;

import java.util.UUID;

public record VerifyTokenResponse(UUID userId, boolean isNewUser, String displayName) {
}
