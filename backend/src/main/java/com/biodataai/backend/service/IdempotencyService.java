package com.biodataai.backend.service;

import com.biodataai.backend.entity.IdempotencyKeyEntity;
import com.biodataai.backend.repository.IdempotencyKeyRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages idempotency keys for POST endpoints.
 * Stores each key with a 24h expiration window to prevent duplicate processing after client retries.
 */
@Service
public class IdempotencyService {

    private final IdempotencyKeyRepository repository;
    private static final long IDEMPOTENCY_KEY_TTL_HOURS = 24;

    public IdempotencyService(IdempotencyKeyRepository repository) {
        this.repository = repository;
    }

    /**
     * Records a new idempotency key for a request.
     * Throws IllegalArgumentException if the key already exists (duplicate request).
     */
    @Transactional
    public void recordKey(String idempotencyKey, UUID userId, String endpoint) {
        Optional<IdempotencyKeyEntity> existing = repository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Duplicate idempotency key: " + idempotencyKey);
        }

        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(IDEMPOTENCY_KEY_TTL_HOURS * 3600);

        IdempotencyKeyEntity entity = IdempotencyKeyEntity.builder()
                .idempotencyKey(idempotencyKey)
                .userId(userId)
                .endpoint(endpoint)
                .createdAt(now)
                .expiresAt(expiresAt)
                .build();

        repository.save(entity);
    }

    /**
     * Returns true if the idempotency key is known and not expired.
     */
    public boolean isDuplicate(String idempotencyKey) {
        Optional<IdempotencyKeyEntity> existing = repository.findByIdempotencyKey(idempotencyKey);
        if (existing.isEmpty()) {
            return false;
        }
        IdempotencyKeyEntity entity = existing.get();
        return entity.getExpiresAt().isAfter(Instant.now());
    }
}
