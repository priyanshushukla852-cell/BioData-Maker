package com.biodataai.backend.service;

/**
 * Snapshot of a user's AI summary quota for the current day.
 *
 * @param dailyFreeLimit base free generations per day (before ads); -1 means unlimited
 * @param adGrants extra generations earned today via rewarded ads
 * @param used summary generations already consumed today
 * @param remaining generations still available today (never negative)
 * @param adRewardAvailable whether watching a rewarded ad can unlock another generation
 */
public record AiQuotaStatus(
        int dailyFreeLimit, long adGrants, long used, long remaining, boolean adRewardAvailable) {

    public static AiQuotaStatus unlimited() {
        return new AiQuotaStatus(-1, 0, 0, Long.MAX_VALUE, false);
    }
}
