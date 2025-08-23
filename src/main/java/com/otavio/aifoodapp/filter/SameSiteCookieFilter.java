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
 * Filter to add SameSite attributes to cookies.
 * Necessary because the SameSite attribute is not available in the standard Jakarta Servlet cookie API.
 *
 * DISABLED: Using CookieConfig bean instead to avoid conflicts with Spring Session
 */
// @Component  // DISABLED - Commented out to prevent conflicts
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class SameSiteCookieFilter implements Filter {

    @Value("${COOKIE_SAME_SITE:lax}")
    private String sameSite;

    @Value("${COOKIE_DOMAIN:aifoodapp.site}")
    private String cookieDomain;
    
    @Value("${COOKIE_SECURE:true}")
    private boolean secure;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        // DISABLED: This filter is no longer active
        // Using CookieConfig bean instead for proper cookie handling
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) {
        log.info("SameSiteCookieFilter is DISABLED - Using CookieConfig bean instead");
    }

    @Override
    public void destroy() {
        // Nothing to do
    }
}
