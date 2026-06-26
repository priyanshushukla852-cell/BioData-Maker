package com.biodataai.backend.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class GeminiClientConfig {

    public static final String SUMMARY_CLIENT = "geminiSummaryClient";
    public static final String SUGGEST_CLIENT = "geminiSuggestClient";

    @Bean
    @Qualifier(SUMMARY_CLIENT)
    public RestClient geminiSummaryClient(@Value("${gemini.base-url:https://generativelanguage.googleapis.com/v1beta}") String baseUrl) {
        return buildClient(baseUrl, 2_000, 8_000);
    }

    @Bean
    @Qualifier(SUGGEST_CLIENT)
    public RestClient geminiSuggestClient(@Value("${gemini.base-url:https://generativelanguage.googleapis.com/v1beta}") String baseUrl) {
        return buildClient(baseUrl, 1_000, 3_000);
    }

    private RestClient buildClient(String baseUrl, int connectTimeoutMs, int readTimeoutMs) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);
        return RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
    }
}
