package com.biodataai.backend.repository;

import com.biodataai.backend.entity.FamilyDetails;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyDetailsRepository extends JpaRepository<FamilyDetails, UUID> {
    Optional<FamilyDetails> findByBiodataId(UUID biodataId);
}
