package com.biodataai.backend.service;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UnlimitedAiQuotaPolicy implements AiQuotaPolicy {
    @Override
    public boolean isAllowed(UUID userId) {
        return true;
    }
}
