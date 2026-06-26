package com.biodataai.backend.repository;

import com.biodataai.backend.entity.BiodataPhoto;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BiodataPhotoRepository extends JpaRepository<BiodataPhoto, UUID> {
    List<BiodataPhoto> findAllByBiodataIdOrderBySortOrderAsc(UUID biodataId);
}
