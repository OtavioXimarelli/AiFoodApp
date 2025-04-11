package com.otavio.aifoodapp.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.ai.chat.client.ChatClient.Builder;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MaritacaChatClient implements ChatClient {

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

    @Override
    public ChatClientRequestSpec prompt() {
        throw new UnsupportedOperationException("Método não implementado");
    }

    @Override
    public ChatClientRequestSpec prompt(String prompt) {
        throw new UnsupportedOperationException("Método não implementado");
    }

    @Override
    public ChatClientRequestSpec prompt(Prompt prompt) {
        throw new UnsupportedOperationException("Método não implementado");
    }

    @Override
    public Builder mutate() {
        throw new UnsupportedOperationException("Método não implementado");
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        Map<String, Object> requestBody = createRequestBody(prompt);
        try {
            // Realiza a chamada à API com o WebClient
            Map response = webClient.post()
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
                    generations.add(new Generation(new AssistantMessage(content)));
                }
            }
            return new ChatResponse(generations);
        } catch (Exception e) {
            System.err.println("Erro ao chamar a API Maritaca: " + e.getMessage());
            e.printStackTrace();
            AssistantMessage errorMessage = new AssistantMessage(
                    "Erro ao processar a solicitação. Por favor, tente novamente mais tarde."
            );
            List<Generation> errorGenerations = List.of(new Generation(errorMessage));
            return new ChatResponse(errorGenerations);
        }
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        return Flux.just(call(prompt));
    }

    private Map<String, Object> createRequestBody(Prompt prompt) {
        List<Map<String, String>> messages;
        try {
            // Tentamos primeiro com getMessages()
            List<Message> messageList = (List<Message>) prompt.getClass().getMethod("getMessages").invoke(prompt);
            messages = messageList.stream()
                    .map(this::convertMessage)
                    .collect(Collectors.toList());
        } catch (Exception e1) {
            try {
                // Tentamos com getContents()
                Object contents = prompt.getClass().getMethod("getContents").invoke(prompt);
                if (contents instanceof List) {
                    messages = ((List<Message>) contents).stream()
                            .map(this::convertMessage)
                            .collect(Collectors.toList());
                } else {
                    // Assumimos que é um Stream ou outra estrutura iterável
                    messages = new ArrayList<>();
                    for (Message m : prompt.toIterable()) {
                        messages.add(convertMessage(m));
                    }
                }
            } catch (Exception e2) {
                // Último recurso: obtemos diretamente os campos necessários
                messages = new ArrayList<>();
                for (Message m : prompt.toIterable()) {
                    messages.add(convertMessage(m));
                }
            }
        }

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

        // Tentamos diferentes métodos para obter o conteúdo da mensagem
        String content;
        try {
            // Tentamos com getContent()
            content = (String) message.getClass().getMethod("getContent").invoke(message);
        } catch (Exception e1) {
            try {
                // Tentamos com getText()
                content = (String) message.getClass().getMethod("getText").invoke(message);
            } catch (Exception e2) {
                // Último recurso: toString()
                content = message.toString();
            }
        }

        result.put("content", content);
        return result;
    }
}