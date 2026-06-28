package com.biodataai.backend.service;

import com.biodataai.backend.entity.GenerationType;
import com.biodataai.backend.repository.AiAdGrantRepository;
import com.biodataai.backend.repository.AiGenerationRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Active AI quota policy: each user gets {@code ai.quota.daily-free-summaries} free AI summary
 * generations per day, and each verified rewarded-ad unlock ({@link com.biodataai.backend.entity.AiAdGrant})
 * adds one more for that day. The cap applies to SUMMARY generations only — field suggestions are
 * unmetered so form-filling stays responsive.
 *
 * <p>"Today" is computed in {@code ai.quota.zone} (default UTC) so the reset boundary is
 * deterministic regardless of server timezone.
 */
@Component
@Primary
public class DailyAiQuotaPolicy implements AiQuotaPolicy {

    private final AiGenerationRepository generationRepository;
    private final AiAdGrantRepository adGrantRepository;
    private final int dailyFreeLimit;
    private final ZoneId zone;

    public DailyAiQuotaPolicy(
            AiGenerationRepository generationRepository,
            AiAdGrantRepository adGrantRepository,
            @Value("${ai.quota.daily-free-summaries:3}") int dailyFreeLimit,
            @Value("${ai.quota.zone:UTC}") String zoneId) {
        this.generationRepository = generationRepository;
        this.adGrantRepository = adGrantRepository;
        this.dailyFreeLimit = dailyFreeLimit;
        this.zone = ZoneId.of(zoneId);
    }

    @Override
    public AiQuotaStatus getStatus(UUID userId) {
        Instant startOfDay = LocalDate.now(zone).atStartOfDay(zone).toInstant();
        long used = generationRepository.countByUserAndTypeSince(userId, GenerationType.SUMMARY, startOfDay);
        long adGrants = adGrantRepository.countByUser_IdAndGrantedAtAfter(userId, startOfDay);
        long limit = (long) dailyFreeLimit + adGrants;
        long remaining = Math.max(0, limit - used);
        // Unlimited ad unlocks: a user out of generations can always earn another by watching an ad.
        return new AiQuotaStatus(dailyFreeLimit, adGrants, used, remaining, true);
    }
}
