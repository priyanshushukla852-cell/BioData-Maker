package com.biodataai.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "idempotency_keys")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdempotencyKeyEntity {

    @Id
    private String idempotencyKey;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String endpoint;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiresAt;
}
