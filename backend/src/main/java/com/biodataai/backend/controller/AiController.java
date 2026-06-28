package com.biodataai.backend.controller;

import com.biodataai.backend.config.FirebaseAuthFilter;
import com.biodataai.backend.dto.AiSuggestRequest;
import com.biodataai.backend.dto.AiSuggestResponse;
import com.biodataai.backend.dto.AiSummaryRequest;
import com.biodataai.backend.dto.AiSummaryResponse;
import com.biodataai.backend.dto.BiodataResponse;
import com.biodataai.backend.entity.Biodata;
import com.biodataai.backend.exception.AiQuotaExceededException;
import com.biodataai.backend.service.AiQuotaPolicy;
import com.biodataai.backend.service.AiQuotaStatus;
import com.biodataai.backend.service.BiodataService;
import com.biodataai.backend.service.GeminiProxyService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
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
        // Daily cap applies to summary generations only (field suggestions stay unmetered).
        AiQuotaStatus quota = aiQuotaPolicy.getStatus(userId);
        if (quota.remaining() <= 0) {
            throw new AiQuotaExceededException(
                    "Daily AI summary limit reached. Watch an ad to generate one more.",
                    quota.adRewardAvailable());
        }
        Biodata biodata = biodataService.getOwnedEntity(userId, request.biodataId());
        BiodataResponse snapshot = biodataService.getById(userId, request.biodataId());
        AiSummaryResponse response = geminiProxyService.generateSummary(biodata, snapshot, request.language());
        biodataService.attachAiSummary(request.biodataId(), response.summaryText());
        return response;
    }

    @GetMapping("/quota")
    public AiQuotaStatus quota(@RequestAttribute(FirebaseAuthFilter.USER_ID_ATTRIBUTE) UUID userId) {
        return aiQuotaPolicy.getStatus(userId);
    }

    @PostMapping("/suggest")
    public AiSuggestResponse suggest(
            @RequestAttribute(FirebaseAuthFilter.USER_ID_ATTRIBUTE) UUID userId,
            @Valid @RequestBody AiSuggestRequest request) {
        Biodata biodata = biodataService.getOwnedEntity(userId, request.biodataId());
        return geminiProxyService.suggestField(biodata, request.fieldName(), request.currentValue());
    }
}
