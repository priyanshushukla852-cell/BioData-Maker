package com.biodataai.backend.service.gemini;

import java.util.List;

public record GeminiGenerateRequest(List<GeminiContent> contents) {
    public static GeminiGenerateRequest ofPrompt(String prompt) {
        return new GeminiGenerateRequest(List.of(GeminiContent.text(prompt)));
    }
}
