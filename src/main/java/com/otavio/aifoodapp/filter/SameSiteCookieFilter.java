package com.otavio.aifoodapp.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Filter to add SameSite attributes to cookies.
 * Necessary because the SameSite attribute is not available in the standard Jakarta Servlet cookie API.
 *
 * DISABLED: Using CookieConfig bean instead to avoid conflicts with Spring Session
 */
// @Component  // DISABLED - Commented out to prevent conflicts
@Slf4j
public class SameSiteCookieFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws java.io.IOException, ServletException {
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
