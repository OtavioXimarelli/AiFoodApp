package com.otavio.aifoodapp.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Configuração de filtros da web para CORS e monitoramento.
 */
@Configuration
@Slf4j
public class WebConfig {

    @Value("${app.frontend.url}")
    private String frontEndUrl;

    /**
     * ✅ BEAN DE FILTRO CORS COM PRIORIDADE MÁXIMA
     * Esta é a única fonte de configuração de CORS para toda a aplicação.
     * Ao registrá-lo como um FilterRegistrationBean com a maior precedência,
     * garantimos que ele seja executado antes de qualquer filtro do Spring Security,
     * resolvendo o NullPointerException durante o fluxo OAuth2.
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Permite que o frontend envie credenciais (cookies)
        config.setAllowCredentials(true);

        // Define as origens permitidas - incluindo portas de desenvolvimento local
        config.setAllowedOrigins(List.of(
            frontEndUrl, 
            "https://aifoodapp.site", 
            "https://www.aifoodapp.site",
            "http://localhost:8082",  // Frontend development port
            "http://localhost:3000",  // Alternative React development port
            "http://127.0.0.1:8082",  // Alternative localhost format
            "http://127.0.0.1:3000"   // Alternative localhost format
        ));

        // Permite todos os cabeçalhos e métodos
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Registra a configuração para todos os paths
        source.registerCorsConfiguration("/**", config);

        // Cria o bean do filtro com a maior prioridade
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);

        return bean;
    }

    /**
     * Filtro para monitorar o tempo de execução das requisições
     */
    @Bean
    public FilterRegistrationBean<RequestMonitorFilter> requestMonitorFilter() {
        FilterRegistrationBean<RequestMonitorFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RequestMonitorFilter());
        registrationBean.addUrlPatterns("/*");
        // Executa logo após o filtro de CORS e outros filtros de alta prioridade
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
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
