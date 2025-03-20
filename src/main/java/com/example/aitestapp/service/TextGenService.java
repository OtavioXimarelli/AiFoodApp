package com.example.aitestapp.service;


import com.example.aitestapp.model.FoodItem;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TextGenService {

    Dotenv dotenv = Dotenv.load();
    private final WebClient webClient;
    private final String apiKey = dotenv.get("MARITAL_KEY");
    private FoodItem foodItem;
    private String aiModel = dotenv.get("MARI_MODEL");

    @Autowired
    public TextGenService(WebClient webClient, FoodItem foodItem) {
        this.webClient = webClient;
        this.foodItem = foodItem;
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

    String food = foodItem.stream()
            .map(item -> String.format("%s (%s) - Quantidade: %d, Validade: %s",
            item.getName(),
            item.getQuantity(),
            item.getExpiration()))
            .collect(Collectors.joining("\n"));

    String prompt = "Baseado no meu banco de dados, sugira receitas com os seguintes ingredientes: " + food;

    public Mono<String> generateRecipe(List<FoodItem>  foodItems) {
        Map<String, Object> requestBody = Map.of(
                "model", aiModel,
                "messages", List.of(
                        Map.of("role", "system", "content", "Você é um chefe de cozinha que sugere receitas."),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.7
        );
        return fetchResponseFromApi(requestBody);
    }}



