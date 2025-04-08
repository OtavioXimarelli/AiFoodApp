package com.otavio.aifoodapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class MaritacaAiConfig {

    @Value("${maritaca.api.url:https://api.maritaca.ai/chat/completions}")
    private String apiUrl;

    @Value("${maritaca.api.key}")
    private String apiKey;

    @Value("${maritaca.model:sabiazinho-3}")
    private String model;

    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }

    @Bean
    public MaritacaChatClient maritacaChatClient(WebClient webClient) {
        return new MaritacaChatClient(webClient, apiUrl, apiKey, model);
    }
}