package com.biodataai.backend.repository;

import com.biodataai.backend.entity.ContactInfo;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactInfoRepository extends JpaRepository<ContactInfo, UUID> {
    Optional<ContactInfo> findByBiodataId(UUID biodataId);
}
