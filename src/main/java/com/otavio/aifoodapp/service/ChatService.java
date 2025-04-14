package com.otavio.aifoodapp.service;


import com.otavio.aifoodapp.config.MaritacaChatClient;
import com.otavio.aifoodapp.model.FoodItem;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {
    private final MaritacaChatClient maritacaChatClient;


    @Value("${maritaca.system.prompt}")
    private String systemPrompt;

    public ChatService(MaritacaChatClient maritacaChatClient) {
        this.maritacaChatClient = maritacaChatClient;
    }

    public String generateRecipe(List<FoodItem> foodItems) {
        String food = foodItems.stream()
                .map(item -> String.format("%s- Quantidade: %d, Validade: %s",
                        item.getName(), item.getQuantity(),
                        item.getExpiration()))
                .collect(Collectors.joining("\n"));


        String prompt = "Baseado nos ingredientes do meu banco de dados, crie uma receita:\n" +
                food + "\n" +
                "A receita deve conter o nome do prato, o modo de preparo e o tempo de preparo.\n" +
                "A receita deve ser em português.\n" +
                "A receita deve ser prática e rápida.\n" +
                "A receita deve ser saudável e saborosa.\n" +
                "A receita deve ser para 2 pessoas.\n" +
                "A receita deve ter as calorias e macro-nutrientes estimados.";


        Prompt chatPrompt = new Prompt(List.of(
                new SystemMessage(systemPrompt),
                new UserMessage(prompt)
        ));


        ChatResponse response = maritacaChatClient.call(chatPrompt);
        return response.getResult().getOutput().getText();
    }
}