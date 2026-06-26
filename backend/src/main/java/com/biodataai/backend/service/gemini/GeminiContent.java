package com.biodataai.backend.service.gemini;

import java.util.List;

public record GeminiContent(List<GeminiPart> parts) {
    public static GeminiContent text(String text) {
        return new GeminiContent(List.of(new GeminiPart(text)));
    }
}
