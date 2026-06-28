package com.biodataai.backend.service;

import java.util.UUID;

/**
 * Pluggable per-user AI usage cap (applies to summary generations only; field suggestions are
 * unmetered). The active policy ({@link DailyAiQuotaPolicy}) enforces a daily free limit that can
 * be extended by rewarded-ad unlocks; {@link UnlimitedAiQuotaPolicy} is available for environments
 * that opt out via {@code ai.quota.mode=unlimited}.
 */
public interface AiQuotaPolicy {

    /** True if the user may generate another AI summary right now. */
    default boolean isAllowed(UUID userId) {
        return getStatus(userId).remaining() > 0;
    }

    /** Current-day quota snapshot, used both for enforcement and to inform the client UI. */
    AiQuotaStatus getStatus(UUID userId);
}
