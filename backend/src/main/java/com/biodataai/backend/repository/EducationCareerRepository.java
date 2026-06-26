package com.biodataai.backend.repository;

import com.biodataai.backend.entity.EducationCareer;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EducationCareerRepository extends JpaRepository<EducationCareer, UUID> {
    Optional<EducationCareer> findByBiodataId(UUID biodataId);
}
