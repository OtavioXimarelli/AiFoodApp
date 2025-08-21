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
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        this.webClient = webClientBuilder.baseUrl(apiUrl).build();
    }

    public Mono<ChatResponse> call(Prompt prompt) {
        Map<String, Object> requestBody = createRequestBody(prompt);

        log.debug("Enviando requisição para Maritaca API: {}", requestBody);

        return webClient.post()
                .uri(apiUrl)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("Erro da API Maritaca. Status: {}, Body: {}", clientResponse.statusCode(), errorBody);
                                    return Mono.error(new WebClientResponseException(
                                            "API Error: " + clientResponse.statusCode() + " Body: " + errorBody,
                                            clientResponse.statusCode().value(),
                                            errorBody,
                                            clientResponse.headers().asHttpHeaders(),
                                            null, null
                                    ));
                                })
                )
                .bodyToMono(Map.class)
                .map(responseMap -> {
                    log.debug("Resposta recebida da Maritaca API: {}", responseMap);
                    Object choicesObj = responseMap.get("choices");
                    List<Generation> generations = new ArrayList<>();

                    if (choicesObj instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> choices = (List<Map<String, Object>>) choicesObj;

                        if (!choices.isEmpty()) {
                            for (Map<String, Object> choice : choices) {
                                Object messageObj = choice.get("message");
                                if (messageObj instanceof Map) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> message = (Map<String, Object>) messageObj;
                                    String content = (String) message.get("content");
                                    if (content != null) {
                                        generations.add(new Generation(new AssistantMessage(content)));
                                    } else {
                                        log.warn("Campo 'content' nulo na mensagem da API: {}", message);
                                    }
                                } else {
                                    log.warn("Campo 'message' nulo ou não é um Map na escolha da API: {}", choice);
                                }
                            }
                        } else {
                            log.warn("Lista 'choices' vazia na resposta da API: {}", responseMap);
                        }
                    } else {
                        log.warn("Campo 'choices' não é uma lista ou está nulo na resposta da API: {}", responseMap);
                    }

                    return new ChatResponse(generations);
                })
                .onErrorResume(e -> {
                    log.error("Falha ao chamar a API Maritaca ou processar a resposta: {}", e.getMessage(), e);

                    AssistantMessage errorMessage = new AssistantMessage(
                            "Desculpe, ocorreu um erro ao processar sua solicitação. Tente novamente mais tarde."
                    );

                    List<Generation> errorGenerations = List.of(new Generation(errorMessage));
                    return Mono.just(new ChatResponse(errorGenerations));
                });
    }

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
        String role = "unknown";

        if (message instanceof SystemMessage) {
            role = "system";
        } else if (message instanceof UserMessage) {
            role = "user";
        } else if (message instanceof AssistantMessage) {
            role = "assistant";
        }
        result.put("role", role);

        String content = message.getText();
        result.put("content", content != null ? content : "");

        return result;
    }

    @Override
    @NonNull
    public ChatClientRequestSpec prompt() {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    @Override
    @NonNull
    public ChatClientRequestSpec prompt(@NonNull String content) {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    @Override
    @NonNull
    public ChatClientRequestSpec prompt( @NonNull Prompt prompt) {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    @Override
    @NonNull
    public Builder mutate() {
        throw new UnsupportedOperationException("Method not implemented.");
    }
}