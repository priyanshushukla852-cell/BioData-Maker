package com.biodataai.backend.repository;

import com.biodataai.backend.entity.Biodata;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BiodataRepository extends JpaRepository<Biodata, UUID> {
    List<Biodata> findAllByUserId(UUID userId);

    Optional<Biodata> findByIdAndUserId(UUID id, UUID userId);
}
