package com.biodataai.backend.controller;

import com.biodataai.backend.service.ad.AdMobSsvVerifier;
import com.biodataai.backend.service.ad.AiAdRewardService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public endpoint that AdMob calls (server-to-server) when a user finishes a rewarded ad. It is
 * intentionally outside {@code /api/ai} and {@code /api/biodatas} so {@code FirebaseAuthFilter}
 * does not require a bearer token — the request comes from Google, not the app. Authenticity is
 * established by the SSV signature instead.
 *
 * <p>The app sets its Firebase uid as the SSV custom data, which arrives here as {@code custom_data}
 * and is used to credit the right user.
 */
@RestController
@RequestMapping("/api/admob")
public class AdMobSsvController {

    private static final Logger log = LoggerFactory.getLogger(AdMobSsvController.class);
    private static final String SIGNATURE_DELIMITER = "&signature=";

    private final AdMobSsvVerifier verifier;
    private final AiAdRewardService rewardService;
    private final String expectedAdUnitId;

    public AdMobSsvController(
            AdMobSsvVerifier verifier,
            AiAdRewardService rewardService,
            @Value("${admob.rewarded-ad-unit-id:}") String expectedAdUnitId) {
        this.verifier = verifier;
        this.rewardService = rewardService;
        this.expectedAdUnitId = expectedAdUnitId;
    }

    @GetMapping("/ssv")
    public ResponseEntity<Void> rewardCallback(HttpServletRequest request) {
        String queryString = request.getQueryString();
        if (queryString == null) {
            return ResponseEntity.badRequest().build();
        }

        int delimiterIndex = queryString.indexOf(SIGNATURE_DELIMITER);
        if (delimiterIndex < 0) {
            return ResponseEntity.badRequest().build();
        }
        // Content to verify is everything before "&signature=" (signature and key_id are last).
        String content = queryString.substring(0, delimiterIndex);

        String signature = request.getParameter("signature");
        String keyId = request.getParameter("key_id");
        if (!verifier.verify(content, signature, keyId)) {
            // Forged or corrupted callback — reject so it isn't credited.
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Optional defense-in-depth: only honor callbacks for our configured rewarded ad unit.
        String adUnit = request.getParameter("ad_unit");
        if (expectedAdUnitId != null && !expectedAdUnitId.isBlank()
                && adUnit != null && !expectedAdUnitId.equals(adUnit)) {
            log.warn("AdMob SSV: ad_unit mismatch; ignoring callback");
            return ResponseEntity.ok().build();
        }

        String firebaseUid = request.getParameter("custom_data");
        String transactionId = request.getParameter("transaction_id");
        boolean granted = rewardService.grant(firebaseUid, transactionId);
        if (!granted) {
            // Signature was valid but we couldn't credit a user; 200 so Google stops retrying.
            log.warn("AdMob SSV: verified callback could not be credited (unknown user/custom_data)");
        }
        return ResponseEntity.ok().build();
    }
}
