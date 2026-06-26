package com.biodataai.backend.repository;

import com.biodataai.backend.entity.IdempotencyKeyEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKeyEntity, String> {
    Optional<IdempotencyKeyEntity> findByIdempotencyKey(String idempotencyKey);
}
