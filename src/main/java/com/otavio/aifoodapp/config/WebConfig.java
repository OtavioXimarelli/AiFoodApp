package com.otavio.aifoodapp.config;

import java.io.IOException;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuração de filtros da web para monitoramento e diagnóstico
 */
@Configuration
@Slf4j
public class WebConfig {

    /**
     * Filtro para monitorar o tempo de execução das requisições
     */
    @Bean
    public FilterRegistrationBean<RequestMonitorFilter> requestMonitorFilter() {
        FilterRegistrationBean<RequestMonitorFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RequestMonitorFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1); // Logo após o rate limiting
        return registrationBean;
    }
    
    /**
     * Filtro que monitora o tempo de execução das requisições
     */
    @Slf4j
    public static class RequestMonitorFilter extends OncePerRequestFilter {
        
        private static final long SLOW_REQUEST_THRESHOLD_MS = 1000; // 1 segundo
        
        @Override
        protected void doFilterInternal(
                @org.springframework.lang.NonNull HttpServletRequest request,
                @org.springframework.lang.NonNull HttpServletResponse response,
                @org.springframework.lang.NonNull FilterChain filterChain) throws ServletException, IOException {
            
            long startTime = System.currentTimeMillis();
            String path = request.getRequestURI();
            
            try {
                // Executar a requisição
                filterChain.doFilter(request, response);
            } finally {
                // Calcular o tempo de execução
                long duration = System.currentTimeMillis() - startTime;
                
                // Registrar requisições lentas
                if (duration > SLOW_REQUEST_THRESHOLD_MS) {
                    log.warn("Requisição lenta: {} {} - {}ms", request.getMethod(), path, duration);
                    
                    // Log detalhado para requisições muito lentas
                    if (duration > SLOW_REQUEST_THRESHOLD_MS * 5) {
                        log.warn("Requisição muito lenta: {} {} - {}ms - Referer: {} - User-Agent: {}", 
                                request.getMethod(), 
                                path, 
                                duration,
                                request.getHeader("Referer"),
                                request.getHeader("User-Agent"));
                    }
                }
            }
        }
    }
}
