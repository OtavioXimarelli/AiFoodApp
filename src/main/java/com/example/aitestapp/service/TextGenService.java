package com.example.aitestapp.service;


import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class TextGenService {

    Dotenv dotenv = Dotenv.load();
    private final WebClient webClient;
    private final String apiKey = dotenv.get("MARITAL_KEY");
    private String aiModel = dotenv.get("MARI_MODEL");

    @Autowired
    public TextGenService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<String> fetchResponseFromApi(Map<String, Object> requestBody) {
        return webClient.post()
                .uri("https://chat.maritaca.ai/api/chat/completions")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                    if (choices != null && !choices.isEmpty()) {
                        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                        return (String) message.get("content");
                    } else {
                        return "Resposta da API inesperada ou vazia. Verifique os detalhes da solicitação.";
                    }
                })
                .onErrorResume(e -> {
                    System.err.println("Erro ao chamar a API: " + e.getMessage());
                    return Mono.just("Erro ao processar a solicitação. Por favor, tente novamente mais tarde.");
                });
    }

    public Mono<String> generateRecipe() {
        Map<String, Object> requestBody = Map.of(
                "model", aiModel,
                "messages", List.of(
                        Map.of("role", "system", "content", "Você é um chefe de cozinha que sugere receitas."),
                        Map.of("role", "user", "content", "Sugira receitas com os seguintes ingredientes, arroz, feijao, linguiça toscana, batata: ")
                ),
                "temperature", 0.7
        );
        return fetchResponseFromApi(requestBody);
    }}



