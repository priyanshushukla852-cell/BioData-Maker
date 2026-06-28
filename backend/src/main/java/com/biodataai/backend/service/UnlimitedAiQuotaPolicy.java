package com.biodataai.backend.service;

import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Opt-in no-cap policy. Enabled only when {@code ai.quota.mode=unlimited}; otherwise
 * {@link DailyAiQuotaPolicy} is the active bean.
 */
@Component
@ConditionalOnProperty(name = "ai.quota.mode", havingValue = "unlimited")
public class UnlimitedAiQuotaPolicy implements AiQuotaPolicy {

    @Override
    public AiQuotaStatus getStatus(UUID userId) {
        return AiQuotaStatus.unlimited();
    }
}
