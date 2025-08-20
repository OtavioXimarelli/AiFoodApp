package com.otavio.aifoodapp.filter;

import java.io.IOException;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Filtro para adicionar atributos SameSite aos cookies.
 * Necessário pois o atributo SameSite não está disponível na API padrão de cookies do Jakarta Servlet.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class SameSiteCookieFilter implements Filter {

    @Value("${COOKIE_SAME_SITE:lax}")
    private String sameSite;

    @Value("${COOKIE_DOMAIN:.aifoodapp.site}")
    private String cookieDomain;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Wrapper personalizado para interceptar os cookies e adicionar o atributo SameSite
        SameSiteResponseWrapper wrappedResponse = new SameSiteResponseWrapper(httpResponse, sameSite, cookieDomain);
        
        try {
            chain.doFilter(request, wrappedResponse);
        } finally {
            // Adicionar os cabeçalhos de cookie modificados ao response original
            Collection<String> headers = wrappedResponse.getHeaderNames();
            for (String header : headers) {
                if (header.equalsIgnoreCase("Set-Cookie")) {
                    Collection<String> cookieHeaders = wrappedResponse.getHeaders(header);
                    if (cookieHeaders != null) {
                        // Remover os cabeçalhos de cookie originais
                        httpResponse.setHeader(header, null);
                        
                        // Adicionar os cabeçalhos de cookie modificados
                        for (String cookieHeader : cookieHeaders) {
                            httpResponse.addHeader(header, cookieHeader);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("Inicializando SameSiteCookieFilter com sameSite={}, domain={}", sameSite, cookieDomain);
    }

    @Override
    public void destroy() {
        // Nada a fazer
    }
}
