package com.biodataai.backend.repository;

import com.biodataai.backend.entity.Template;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemplateRepository extends JpaRepository<Template, UUID> {
    List<Template> findAllByOrderBySortOrderAsc();
}
