package com.biodataai.backend.dto;

import com.biodataai.backend.entity.LanguagePref;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Create a biodata. The client (offline-first) generates the biodata UUID locally and sends it as
 * {@code id} so the local Room row and the server row share one identifier; the create is then
 * idempotent on that id (a retried sync returns the existing row instead of duplicating). {@code id}
 * may be null, in which case the server assigns one. {@code title} is optional — drafts start
 * untitled.
 */
public record BiodataCreateRequest(
        UUID id,
        String title,
        UUID templateId,
        @NotNull LanguagePref language) {
}
