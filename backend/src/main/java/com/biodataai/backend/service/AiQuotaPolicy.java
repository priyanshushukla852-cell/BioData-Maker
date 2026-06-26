package com.biodataai.backend.service;

import java.util.UUID;

/**
 * Pluggable per-user AI usage cap. Default implementation is unlimited; CLAUDE.md §14 flags
 * "should /api/ai/* be rate-limited per user per month for cost control" as an open question
 * for the user to decide — this seam lets that policy be swapped in later without touching
 * {@link GeminiProxyService}.
 */
public interface AiQuotaPolicy {
    boolean isAllowed(UUID userId);
}
