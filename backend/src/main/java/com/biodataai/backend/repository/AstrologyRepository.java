package com.biodataai.backend.repository;

import com.biodataai.backend.entity.Astrology;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AstrologyRepository extends JpaRepository<Astrology, UUID> {
    Optional<Astrology> findByBiodataId(UUID biodataId);
}
