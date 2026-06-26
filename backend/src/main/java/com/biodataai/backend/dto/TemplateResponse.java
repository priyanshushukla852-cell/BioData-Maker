package com.biodataai.backend.dto;

import com.biodataai.backend.entity.Template;
import com.biodataai.backend.entity.TemplateStyle;
import java.util.UUID;

public record TemplateResponse(
        UUID templateId,
        String name,
        String previewUrl,
        TemplateStyle style,
        boolean supportsHindi) {

    public static TemplateResponse from(Template template) {
        return new TemplateResponse(
                template.getId(),
                template.getName(),
                template.getPreviewUrl(),
                template.getStyle(),
                template.isSupportsHindi());
    }
}
