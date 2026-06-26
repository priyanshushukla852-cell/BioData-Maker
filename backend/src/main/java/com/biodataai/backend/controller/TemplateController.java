package com.biodataai.backend.controller;

import com.biodataai.backend.dto.TemplateResponse;
import com.biodataai.backend.repository.TemplateRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {

    private final TemplateRepository templateRepository;

    public TemplateController(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @GetMapping
    public List<TemplateResponse> list() {
        return templateRepository.findAllByOrderBySortOrderAsc().stream().map(TemplateResponse::from).toList();
    }
}
