package com.biodataai.backend.service;

import com.biodataai.backend.config.GeminiClientConfig;
import com.biodataai.backend.dto.AiSummaryResponse;
import com.biodataai.backend.dto.AiSuggestResponse;
import com.biodataai.backend.dto.BiodataResponse;
import com.biodataai.backend.entity.AiGeneration;
import com.biodataai.backend.entity.Biodata;
import com.biodataai.backend.entity.GenerationType;
import com.biodataai.backend.entity.LanguagePref;
import com.biodataai.backend.exception.AiServiceException;
import com.biodataai.backend.repository.AiGenerationRepository;
import com.biodataai.backend.service.gemini.GeminiGenerateRequest;
import com.biodataai.backend.service.gemini.GeminiGenerateResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Thin proxy to the Gemini 2.5 Flash API. Never called directly from the Android client — only
 * this backend holds the API key, per CLAUDE.md's hard architecture rule.
 */
@Service
public class GeminiProxyService {

    private static final Logger log = LoggerFactory.getLogger(GeminiProxyService.class);
    private static final String RESILIENCE_INSTANCE = "gemini";

    private final RestClient summaryClient;
    private final RestClient suggestClient;
    private final String apiKey;
    private final String model;
    private final AiGenerationRepository aiGenerationRepository;
    private final ObjectMapper objectMapper;

    public GeminiProxyService(
            @Qualifier(GeminiClientConfig.SUMMARY_CLIENT) RestClient summaryClient,
            @Qualifier(GeminiClientConfig.SUGGEST_CLIENT) RestClient suggestClient,
            @Value("${gemini.api-key:}") String apiKey,
            @Value("${gemini.model:gemini-2.5-flash}") String model,
            AiGenerationRepository aiGenerationRepository,
            ObjectMapper objectMapper) {
        this.summaryClient = summaryClient;
        this.suggestClient = suggestClient;
        this.apiKey = apiKey;
        this.model = model;
        this.aiGenerationRepository = aiGenerationRepository;
        this.objectMapper = objectMapper;
    }

    @CircuitBreaker(name = RESILIENCE_INSTANCE)
    @Retry(name = RESILIENCE_INSTANCE)
    public AiSummaryResponse generateSummary(Biodata biodata, BiodataResponse snapshot, LanguagePref language) {
        requireApiKey();
        String prompt = buildSummaryPrompt(snapshot, language);
        String responseText = callGemini(summaryClient, prompt);

        AiGeneration generation = new AiGeneration();
        generation.setBiodata(biodata);
        generation.setGenerationType(GenerationType.SUMMARY);
        generation.setInputSnapshot(toJson(Map.of("language", language, "biodataId", biodata.getId())));
        generation.setAiResponse(responseText);
        AiGeneration saved = aiGenerationRepository.save(generation);

        return new AiSummaryResponse(responseText, saved.getId());
    }

    @CircuitBreaker(name = RESILIENCE_INSTANCE)
    @Retry(name = RESILIENCE_INSTANCE)
    public AiSuggestResponse suggestField(Biodata biodata, String fieldName, String currentValue) {
        requireApiKey();
        String prompt = buildSuggestPrompt(fieldName, currentValue);
        String responseText = callGemini(suggestClient, prompt);

        AiGeneration generation = new AiGeneration();
        generation.setBiodata(biodata);
        generation.setGenerationType(GenerationType.FIELD_SUGGEST);
        generation.setFieldName(fieldName);
        generation.setInputSnapshot(toJson(Map.of("fieldName", fieldName, "currentValue", currentValue)));
        generation.setAiResponse(responseText);
        aiGenerationRepository.save(generation);

        return new AiSuggestResponse(parseSuggestions(responseText));
    }

    private void requireApiKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new AiServiceException("AI service is not configured yet.");
        }
    }

    private String callGemini(RestClient client, String prompt) {
        try {
            GeminiGenerateResponse response = client
                    .post()
                    .uri("/models/{model}:generateContent?key={key}", model, apiKey)
                    .body(GeminiGenerateRequest.ofPrompt(prompt))
                    .retrieve()
                    .body(GeminiGenerateResponse.class);
            String text = response != null ? response.firstText() : null;
            if (text == null || text.isBlank()) {
                throw new AiServiceException("AI service returned an empty response.");
            }
            return text.trim();
        } catch (AiServiceException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Gemini call failed: {}", e.getClass().getSimpleName());
            throw new AiServiceException("AI service is temporarily unavailable.");
        }
    }

    private String buildSummaryPrompt(BiodataResponse biodata, LanguagePref language) {
        String languageName = language == LanguagePref.HI ? "Hindi" : "English";
        String name = biodata.personalDetails() != null ? biodata.personalDetails().fullName() : "the candidate";
        return """
                You are helping create a marriage biodata for %s.
                Here are their details (JSON): %s
                Write a warm, respectful personal introduction paragraph in %s suitable for an
                Indian arranged marriage biodata. Keep it 4-6 sentences. Mention their family
                background, education, career, and personality briefly.
                Do NOT include income, caste, or Manglik status in the summary.
                """
                .formatted(name, toJson(redactForAi(biodata)), languageName);
    }

    private String buildSuggestPrompt(String fieldName, String currentValue) {
        return """
                Suggest 2-3 short, comma-separated options for the "%s" field of an Indian
                marriage biodata. The user's current input (may be blank): "%s".
                Respond with ONLY the suggestions as a single comma-separated line, no preamble.
                """
                .formatted(fieldName, currentValue == null ? "" : currentValue);
    }

    /** Strips income/caste/manglik before anything is sent to the AI provider, per CLAUDE.md / PRD §7.1. */
    private Map<String, Object> redactForAi(BiodataResponse biodata) {
        Map<String, Object> snapshot = new java.util.HashMap<>();
        snapshot.put("title", biodata.title());
        if (biodata.personalDetails() != null) {
            var p = biodata.personalDetails();
            // caste and Manglik (the latter lives on Astrology, already excluded) must never
            // reach the AI provider, per CLAUDE.md / PRD §7.1.
            snapshot.put(
                    "personalDetails",
                    Map.of(
                            "fullName", String.valueOf(p.fullName()),
                            "dob", String.valueOf(p.dob()),
                            "gender", String.valueOf(p.gender()),
                            "religion", String.valueOf(p.religion()),
                            "heightCm", String.valueOf(p.heightCm()),
                            "complexion", String.valueOf(p.complexion())));
        }
        snapshot.put("familyDetails", biodata.familyDetails());
        if (biodata.educationCareer() != null) {
            snapshot.put(
                    "educationCareer",
                    Map.of(
                            "highestQualification", String.valueOf(biodata.educationCareer().highestQualification()),
                            "college", String.valueOf(biodata.educationCareer().college()),
                            "jobTitle", String.valueOf(biodata.educationCareer().jobTitle()),
                            "company", String.valueOf(biodata.educationCareer().company()),
                            "workLocation", String.valueOf(biodata.educationCareer().workLocation())));
        }
        snapshot.put("lifestyle", biodata.lifestyle());
        return snapshot;
    }

    private List<String> parseSuggestions(String responseText) {
        return Arrays.stream(responseText.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .limit(3)
                .toList();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return "{}";
        }
    }
}
