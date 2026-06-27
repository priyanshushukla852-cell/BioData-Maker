package com.biodataai.backend.controller;

import com.biodataai.backend.annotation.Idempotent;
import com.biodataai.backend.config.FirebaseAuthFilter;
import com.biodataai.backend.dto.BiodataCreateRequest;
import com.biodataai.backend.dto.BiodataResponse;
import com.biodataai.backend.dto.BiodataSummaryResponse;
import com.biodataai.backend.dto.BiodataUpdateRequest;
import com.biodataai.backend.service.BiodataService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/biodatas")
public class BiodataController {

    private final BiodataService biodataService;

    public BiodataController(BiodataService biodataService) {
        this.biodataService = biodataService;
    }

    @PostMapping
    @Idempotent
    public ResponseEntity<BiodataResponse> create(
            @RequestAttribute(FirebaseAuthFilter.USER_ID_ATTRIBUTE) UUID userId,
            @Valid @RequestBody BiodataCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(biodataService.create(userId, request));
    }

    @GetMapping
    public List<BiodataSummaryResponse> list(
            @RequestAttribute(FirebaseAuthFilter.USER_ID_ATTRIBUTE) UUID userId) {
        return biodataService.list(userId);
    }

    @GetMapping("/{id}")
    public BiodataResponse getById(
            @RequestAttribute(FirebaseAuthFilter.USER_ID_ATTRIBUTE) UUID userId, @PathVariable UUID id) {
        return biodataService.getById(userId, id);
    }

    @PutMapping("/{id}")
    @Idempotent
    public BiodataResponse update(
            @RequestAttribute(FirebaseAuthFilter.USER_ID_ATTRIBUTE) UUID userId,
            @PathVariable UUID id,
            @RequestBody BiodataUpdateRequest request) {
        return biodataService.update(userId, id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @RequestAttribute(FirebaseAuthFilter.USER_ID_ATTRIBUTE) UUID userId, @PathVariable UUID id) {
        biodataService.softDelete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
