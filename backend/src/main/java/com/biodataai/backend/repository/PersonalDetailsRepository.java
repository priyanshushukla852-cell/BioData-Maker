package com.biodataai.backend.repository;

import com.biodataai.backend.entity.PersonalDetails;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonalDetailsRepository extends JpaRepository<PersonalDetails, UUID> {
    Optional<PersonalDetails> findByBiodataId(UUID biodataId);
}
