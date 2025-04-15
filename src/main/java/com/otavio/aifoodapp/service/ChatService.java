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


        String prompt = "Gere uma receita seguindo estritamente os critérios abaixo.\n\n" +
                "**Ingredientes Disponíveis:**\n" + // Markdown para negrito pode ou não ser interpretado, mas ajuda na clareza
                food + "\n\n" +
                "**Critérios da Receita:**\n" +
                "1.  **Idioma:** Português\n" +
                "2.  **Porções:** 1 pessoas\n" +
                "3.  **Estilo:** Prática (passos simples), rápida (com foco fitness), saudável e saborosa.\n" + // Exemplo de tempo limite
                "4.  **Output Obrigatório:**\n" + // Ajustado para melhor estrutura de lista
                "    * Nome do Prato (ou o que ele contém)\n" +
                "    * Tempo Total de Preparo Estimado\n" +
                "    * Modo de Preparo Detalhado (passo a passo)\n" +
                "    * Estimativa Nutricional (Calorias, Proteínas, Carboidratos, Gorduras)"; // Macros especificados


        Prompt chatPrompt = new Prompt(List.of(
                new SystemMessage(systemPrompt),
                new UserMessage(prompt)
        ));


        ChatResponse response = maritacaChatClient.call(chatPrompt);
        return response.getResult().getOutput().getText();
    }
}