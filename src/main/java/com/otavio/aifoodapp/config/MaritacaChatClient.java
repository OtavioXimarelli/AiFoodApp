package com.otavio.aifoodapp.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MaritacaChatClient implements ChatClient {

    private final WebClient webClient;
    private final String apiUrl;
    private final String apiKey;
    private final String model;

    public MaritacaChatClient(WebClient webClient, String apiUrl, String apiKey, String model) {
        this.webClient = webClient;
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        Map<String, Object> requestBody = createRequestBody(prompt);

        try {
            Map<String, Object> response = webClient.post()
                    .uri(apiUrl)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            List<Generation> generations = new ArrayList<>();

            if (choices != null && !choices.isEmpty()) {
                for (Map<String, Object> choice : choices) {
                    Map<String, Object> message = (Map<String, Object>) choice.get("message");
                    String content = (String) message.get("content");
                    generations.add(new Generation(content));
                }
            }

            return new ChatResponse(generations);
        } catch (Exception e) {
            System.err.println("Erro ao chamar a API Maritaca: " + e.getMessage());
            e.printStackTrace();
            List<Generation> errorGenerations = List.of(
                    new Generation("Erro ao processar a solicitação. Por favor, tente novamente mais tarde.")
            );
            return new ChatResponse(errorGenerations);
        }
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        // Implementação simples para streaming, retornando o resultado como um único item no fluxo
        return Flux.just(call(prompt));
    }

    private Map<String, Object> createRequestBody(Prompt prompt) {
        List<Map<String, String>> messages = prompt.getMessages().stream()
                .map(this::convertMessage)
                .collect(Collectors.toList());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);

        return requestBody;
    }

    private Map<String, String> convertMessage(Message message) {
        Map<String, String> result = new HashMap<>();

        if (message instanceof SystemMessage) {
            result.put("role", "system");
        } else if (message instanceof UserMessage) {
            result.put("role", "user");
        } else if (message instanceof AssistantMessage) {
            result.put("role", "assistant");
        } else {
            // Default fallback
            result.put("role", "user");
        }

        result.put("content", message.getContent());
        return result;
    }
}