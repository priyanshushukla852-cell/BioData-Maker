package com.biodataai.backend.repository;

import com.biodataai.backend.entity.AiAdGrant;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiAdGrantRepository extends JpaRepository<AiAdGrant, UUID> {

    /** Number of rewarded-ad grants this user has earned since {@code start} (i.e. today). */
    long countByUser_IdAndGrantedAtAfter(UUID userId, Instant start);

    /** True if this AdMob SSV transaction has already been recorded (idempotency guard). */
    boolean existsByTransactionId(String transactionId);
}
