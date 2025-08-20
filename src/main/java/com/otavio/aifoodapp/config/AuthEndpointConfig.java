package com.otavio.aifoodapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.HeaderWriterFilter;

import com.otavio.aifoodapp.filter.SameSiteCookieFilter;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuração especial para endpoints de autenticação para garantir que eles funcionem
 * corretamente com CORS e cookies em requisições cross-origin.
 */
@Configuration
@Slf4j
public class AuthEndpointConfig {

    @Value("${app.frontend.url}")
    private String frontendUrl;
    
    private final SameSiteCookieFilter sameSiteCookieFilter;
    
    public AuthEndpointConfig(SameSiteCookieFilter sameSiteCookieFilter) {
        this.sameSiteCookieFilter = sameSiteCookieFilter;
    }
    
    @PostConstruct
    public void init() {
        log.info("Inicializando configuração de endpoints de autenticação");
        log.info("Frontend URL configurada: {}", frontendUrl);
    }
    
    /**
     * Cria uma cadeia de filtros de segurança específica para endpoints de autenticação
     * com prioridade maior que a configuração de segurança principal
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authFilterChain(HttpSecurity http) throws Exception {
        return http
            // Aplicar apenas a endpoints de autenticação
            .securityMatcher("/api/auth/**", "/oauth2/**", "/login/**")
            
            // Desativar CSRF para endpoints de autenticação
            .csrf(AbstractHttpConfigurer::disable)
            
            // Desativar CORS padrão (usaremos nosso filtro personalizado)
            .cors(AbstractHttpConfigurer::disable)
            
            // Garantir criação de sessão em todas as requisições
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
            
            // Permitir todas as requisições para esses endpoints
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            
            // Adicionar filtro de debug APENAS para informações detalhadas sobre requisições de autenticação
            // .addFilterBefore(authDebugFilter, HeaderWriterFilter.class)
            
            // Adicionar filtro SameSite para garantir atributos corretos em cookies
            .addFilterBefore(sameSiteCookieFilter, HeaderWriterFilter.class)
            
            // NÃO adicionar o filtro CORS aqui pois pode estar causando problemas
            // .addFilterBefore(authStatusCorsFilter, sameSiteCookieFilter.getClass())
            
            .build();
    }
}
