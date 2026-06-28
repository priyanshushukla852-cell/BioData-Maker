package com.biodataai.backend.repository;

import com.biodataai.backend.entity.AiGeneration;
import com.biodataai.backend.entity.GenerationType;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AiGenerationRepository extends JpaRepository<AiGeneration, UUID> {

    /**
     * Counts a user's generations of a given type since {@code start} (i.e. today), resolved
     * through the owning biodata. Used by the daily AI quota: the cap applies to SUMMARY
     * generations only, so field suggestions stay unmetered.
     */
    @Query("""
            select count(g) from AiGeneration g
            where g.biodata.user.id = :userId
              and g.generationType = :type
              and g.generatedAt >= :start
            """)
    long countByUserAndTypeSince(
            @Param("userId") UUID userId,
            @Param("type") GenerationType type,
            @Param("start") Instant start);
}
