package com.biodataai.backend.dto;

import java.util.UUID;

public record AiSummaryResponse(String summaryText, UUID generationId) {
}
