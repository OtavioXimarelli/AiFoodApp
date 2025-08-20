package com.otavio.aifoodapp.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class CorsConfig {

    @Value("${app.frontend.url}")
    private String frontendUrl;
    
    @PostConstruct
    public void init() {
        log.info("Initializing CORS configuration with frontendUrl: {}", frontendUrl);
    }

    /**
     * Configuração CORS principal para toda a aplicação
     * 
     * FIXED: Complete CORS configuration to prevent NullPointerException
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Começar com valores padrão seguros
        configuration.applyPermitDefaultValues();
        
        // Configuração explícita de origens permitidas (sem usar "*")
        List<String> allowedOrigins = Arrays.asList(
            frontendUrl, 
            "https://aifoodapp.site",
            "https://www.aifoodapp.site"
        );
        configuration.setAllowedOrigins(allowedOrigins);
        
        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        
        // Cabeçalhos permitidos
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Type", 
            "X-Requested-With", 
            "Accept", 
            "Origin", 
            "Access-Control-Request-Method", 
            "Access-Control-Request-Headers"
        ));
        
        // Cabeçalhos expostos
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization", 
            "Set-Cookie",
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials"
        ));
        
        // Permitir credenciais (cookies, autenticação)
        configuration.setAllowCredentials(true);
        
        // Tempo de cache (em segundos)
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        log.info("Configuração CORS principal criada com origens: {}", allowedOrigins);
        return source;
    }
    
    /**
     * Filtro CORS específico para endpoints de autenticação
     * Isso é necessário para evitar NullPointerException no DefaultCorsProcessor
     * 
     * TEMPORARILY DISABLED TO DEBUG OAUTH2 ISSUE
     */
    /*
    @Bean
    public CorsFilter authStatusCorsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Permitir credenciais
        config.setAllowCredentials(true);
        
        // Origens permitidas específicas (não use "*" quando credentials=true)
        List<String> allowedOrigins = Arrays.asList(
            frontendUrl, 
            "https://aifoodapp.site",
            "https://www.aifoodapp.site"
        );
        config.setAllowedOrigins(allowedOrigins);
        
        // Cabeçalhos e métodos permitidos
        config.setAllowedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Type", 
            "X-Requested-With", 
            "Accept", 
            "Origin", 
            "Access-Control-Request-Method", 
            "Access-Control-Request-Headers"
        ));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        
        // Cabeçalhos expostos
        config.setExposedHeaders(Arrays.asList(
            "Authorization", 
            "Set-Cookie",
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials",
            "Vary"
        ));
        
        // Adicionar cabeçalho Vary explicitamente para corrigir o problema do DefaultCorsProcessor
        config.addExposedHeader("Vary");
        
        // Tempo de cache
        config.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/auth/status", config);
        source.registerCorsConfiguration("/api/auth/**", config);
        source.registerCorsConfiguration("/oauth2/**", config);
        source.registerCorsConfiguration("/login/**", config);
        
        log.info("Filtro CORS de autenticação criado com origens: {}", allowedOrigins);
        return new CorsFilter(source);
    }
    */
}
