package com.biodataai.backend.service.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeminiGenerateResponse(List<Candidate> candidates) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Candidate(GeminiContent content) {
    }

    public String firstText() {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        GeminiContent content = candidates.get(0).content();
        if (content == null || content.parts() == null || content.parts().isEmpty()) {
            return null;
        }
        return content.parts().get(0).text();
    }
}
