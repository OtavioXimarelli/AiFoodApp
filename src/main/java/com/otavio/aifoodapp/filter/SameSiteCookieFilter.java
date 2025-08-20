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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Filter to add SameSite attributes to cookies.
 * Necessary because the SameSite attribute is not available in the standard Jakarta Servlet cookie API.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class SameSiteCookieFilter implements Filter {

    @Value("${COOKIE_SAME_SITE:lax}")
    private String sameSite;

    @Value("${COOKIE_DOMAIN:.aifoodapp.site}")
    private String cookieDomain;
    
    @Value("${COOKIE_SECURE:true}")
    private boolean secure;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        // Debug request info
        if (httpRequest.getRequestURI().contains("/auth/status")) {
            log.debug("Processing auth status request");
            log.debug("Request URI: {}, Origin: {}, Referer: {}", 
                    httpRequest.getRequestURI(),
                    httpRequest.getHeader("Origin"),
                    httpRequest.getHeader("Referer"));
        }
        
        // Custom wrapper to intercept cookies and add the SameSite attribute
        SameSiteResponseWrapper wrappedResponse = new SameSiteResponseWrapper(httpResponse, sameSite, cookieDomain);
        // Pass secure setting to the wrapper
        wrappedResponse.setSecureCookies(secure);
        
        try {
            chain.doFilter(request, wrappedResponse);
        } finally {
            // Add modified cookie headers to original response
            Collection<String> headers = wrappedResponse.getHeaderNames();
            for (String header : headers) {
                if (header.equalsIgnoreCase("Set-Cookie")) {
                    Collection<String> cookieHeaders = wrappedResponse.getHeaders(header);
                    if (cookieHeaders != null) {
                        // Remove original cookie headers
                        httpResponse.setHeader(header, null);
                        
                        // Add modified cookie headers
                        for (String cookieHeader : cookieHeaders) {
                            if (httpRequest.getRequestURI().contains("/auth/status")) {
                                log.debug("Setting cookie: {}", cookieHeader);
                            }
                            httpResponse.addHeader(header, cookieHeader);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("Initializing SameSiteCookieFilter with sameSite={}, domain={}, secure={}", 
                sameSite, cookieDomain, secure);
    }

    @Override
    public void destroy() {
        // Nothing to do
    }
}
