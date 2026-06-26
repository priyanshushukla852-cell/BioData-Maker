package com.biodataai.backend.controller;

import com.biodataai.backend.config.FirebaseAuthFilter;
import com.biodataai.backend.dto.AiSuggestRequest;
import com.biodataai.backend.dto.AiSuggestResponse;
import com.biodataai.backend.dto.AiSummaryRequest;
import com.biodataai.backend.dto.AiSummaryResponse;
import com.biodataai.backend.dto.BiodataResponse;
import com.biodataai.backend.entity.Biodata;
import com.biodataai.backend.exception.AiServiceException;
import com.biodataai.backend.service.AiQuotaPolicy;
import com.biodataai.backend.service.BiodataService;
import com.biodataai.backend.service.GeminiProxyService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final GeminiProxyService geminiProxyService;
    private final BiodataService biodataService;
    private final AiQuotaPolicy aiQuotaPolicy;

    public AiController(
            GeminiProxyService geminiProxyService, BiodataService biodataService, AiQuotaPolicy aiQuotaPolicy) {
        this.geminiProxyService = geminiProxyService;
        this.biodataService = biodataService;
        this.aiQuotaPolicy = aiQuotaPolicy;
    }

    @PostMapping("/summary")
    public AiSummaryResponse summary(
            @RequestAttribute(FirebaseAuthFilter.USER_ID_ATTRIBUTE) UUID userId,
            @Valid @RequestBody AiSummaryRequest request) {
        checkQuota(userId);
        Biodata biodata = biodataService.getOwnedEntity(userId, request.biodataId());
        BiodataResponse snapshot = biodataService.getById(userId, request.biodataId());
        AiSummaryResponse response = geminiProxyService.generateSummary(biodata, snapshot, request.language());
        biodataService.attachAiSummary(request.biodataId(), response.summaryText());
        return response;
    }

    @PostMapping("/suggest")
    public AiSuggestResponse suggest(
            @RequestAttribute(FirebaseAuthFilter.USER_ID_ATTRIBUTE) UUID userId,
            @Valid @RequestBody AiSuggestRequest request) {
        checkQuota(userId);
        Biodata biodata = biodataService.getOwnedEntity(userId, request.biodataId());
        return geminiProxyService.suggestField(biodata, request.fieldName(), request.currentValue());
    }

    private void checkQuota(UUID userId) {
        if (!aiQuotaPolicy.isAllowed(userId)) {
            throw new AiServiceException("Monthly AI usage limit reached.");
        }
    }
}
