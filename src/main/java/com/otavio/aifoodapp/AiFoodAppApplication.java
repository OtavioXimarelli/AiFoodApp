package com.otavio.aifoodapp;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(exclude = {OpenAiAutoConfiguration.class})
public class AiFoodAppApplication {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        // don't know if is the best way to do this, but for now is what works
        System.setProperty("MARITACA_API_KEY", dotenv.get("MARITACA_API_KEY", ""));
        System.setProperty("MARITACA_API_URL", dotenv.get("MARITACA_API_URL", ""));
        System.setProperty("MARITACA_API_MODEL", dotenv.get("MARITACA_API_MODEL", ""));
        System.setProperty("MARITACA_SYSTEM_PROMPT", dotenv.get("MARITACA_SYSTEM_PROMPT", ""));
        System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET", ""));
        SpringApplication.run(AiFoodAppApplication.class, args);
    }
}
