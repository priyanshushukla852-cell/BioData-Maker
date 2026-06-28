package com.biodataai.backend.service.ad;

import com.biodataai.backend.entity.AiAdGrant;
import com.biodataai.backend.entity.User;
import com.biodataai.backend.repository.AiAdGrantRepository;
import com.biodataai.backend.repository.UserRepository;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Records a rewarded-ad unlock that grants the user one extra AI summary generation for the day.
 * Only called after {@link AdMobSsvVerifier} has confirmed the SSV signature. Idempotent on the
 * AdMob {@code transaction_id} so Google's callback retries don't double-grant.
 */
@Service
public class AiAdRewardService {

    private static final Logger log = LoggerFactory.getLogger(AiAdRewardService.class);

    private final UserRepository userRepository;
    private final AiAdGrantRepository adGrantRepository;

    public AiAdRewardService(UserRepository userRepository, AiAdGrantRepository adGrantRepository) {
        this.userRepository = userRepository;
        this.adGrantRepository = adGrantRepository;
    }

    /**
     * @param firebaseUid SSV custom data set by the app (its Firebase uid)
     * @param transactionId AdMob SSV transaction id (dedup key)
     * @return true if a grant was recorded (or already existed) for a known user; false if the user
     *     could not be resolved
     */
    @Transactional
    public boolean grant(String firebaseUid, String transactionId) {
        if (firebaseUid == null || firebaseUid.isBlank() || transactionId == null || transactionId.isBlank()) {
            return false;
        }
        if (adGrantRepository.existsByTransactionId(transactionId)) {
            return true; // already processed — idempotent success
        }
        Optional<User> user = userRepository.findByFirebaseUid(firebaseUid);
        if (user.isEmpty()) {
            log.warn("AdMob SSV grant: no user for the supplied custom data");
            return false;
        }
        AiAdGrant grant = new AiAdGrant();
        grant.setUser(user.get());
        grant.setTransactionId(transactionId);
        try {
            adGrantRepository.save(grant);
        } catch (DataIntegrityViolationException e) {
            // Concurrent duplicate (unique transaction_id) — treat as already granted.
            return true;
        }
        return true;
    }
}
