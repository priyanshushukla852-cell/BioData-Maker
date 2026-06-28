package com.biodataai.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a user has used up their daily AI summary quota. Maps to HTTP 429 so the client can
 * distinguish "out of quota" (offer a rewarded ad) from a generic AI failure. Carries whether a
 * rewarded-ad unlock is available so the app knows whether to show the "watch ad to continue" path.
 */
public class AiQuotaExceededException extends ApiException {

    private final boolean adRewardAvailable;

    public AiQuotaExceededException(String message, boolean adRewardAvailable) {
        super(HttpStatus.TOO_MANY_REQUESTS, message);
        this.adRewardAvailable = adRewardAvailable;
    }

    public boolean isAdRewardAvailable() {
        return adRewardAvailable;
    }
}
