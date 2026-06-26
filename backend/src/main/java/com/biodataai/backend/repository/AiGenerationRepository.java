package com.biodataai.backend.repository;

import com.biodataai.backend.entity.AiGeneration;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiGenerationRepository extends JpaRepository<AiGeneration, UUID> {
}
