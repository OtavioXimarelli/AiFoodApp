package com.example.aitestapp.config;


import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    Dotenv dotenv = Dotenv.load();
    private final String chatGptUrl = dotenv.get("MARITALK_URL") ;


    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.baseUrl(chatGptUrl).build();
    }

}
