package com.otavio.aifoodapp.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.slf4j.Logger; 
import org.slf4j.LoggerFactory; 


import org.springframework.web.reactive.function.client.WebClientResponseException; 



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MaritacaChatClient implements ChatClient {

    private static final Logger log = LoggerFactory.getLogger(MaritacaChatClient.class);

    private final WebClient webClient;

    @Value("${maritaca.api.url}")
    private String apiUrl;

    @Value("${maritaca.api.key}")
    private String apiKey;

    @Value("${maritaca.api.model}")
    private String model;

    public MaritacaChatClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

   
    public Mono<ChatResponse> call(Prompt prompt) {
        Map<String, Object> requestBody = createRequestBody(prompt);

        log.debug("Enviando requisição para Maritaca API: {}", requestBody);

        try {
            Map response = webClient.post()
                    .uri(apiUrl)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            List<Generation> generations = new ArrayList<>();

            if (choices != null && !choices.isEmpty()) {
                for (Map<String, Object> choice : choices) {
                    Map<String, Object> message = (Map<String, Object>) choice.get("message");
                    String content = (String) message.get("content");
                    generations.add(new Generation(new AssistantMessage(content)));
                }
            }
            return new ChatResponse(generations);
        } catch (Exception e) {
            System.err.println("Error calling the Maritaca API: " + e.getMessage());
            e.printStackTrace();
            AssistantMessage errorMessage = new AssistantMessage(
                    "Error processing the request. Please try again later."
            );
            List<Generation> errorGenerations = List.of(new Generation(errorMessage));
            return new ChatResponse(errorGenerations);
        }
    }

    // Removed @Override since stream(Prompt) is not part of ChatClient
    public Flux<ChatResponse> stream(Prompt prompt) {
        throw new UnsupportedOperationException("Streaming not implemented.");
    }

    private Map<String, Object> createRequestBody(Prompt prompt) {
        List<Map<String, String>> messages = prompt.getInstructions().stream()
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
            result.put("role", "unknown");
        }
        // Changed getContent() to getText()
        String content = message.getText();
        result.put("content", content);
        return result;
    }

    @Override
    public ChatClientRequestSpec prompt() {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    @Override
    public ChatClientRequestSpec prompt(String content) {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    @Override
    public ChatClientRequestSpec prompt(Prompt prompt) {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    @Override
    public Builder mutate() {
        throw new UnsupportedOperationException("Method not implemented.");
    }
}