package com.otavio.aifoodapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.server.CookieSameSiteSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

import lombok.extern.slf4j.Slf4j;

/**
 * Configuration for session cookies
 * This ensures cookies are properly set in production environment
 */
@Configuration
@Slf4j
public class SessionCookieConfig {

    @Value("${COOKIE_SECURE:false}")
    private boolean cookieSecure;
    
    @Value("${COOKIE_SAME_SITE:lax}")
    private String cookieSameSite;
    
    @Value("${COOKIE_DOMAIN:}")
    private String cookieDomain;

    /**
     * Configure cookie serializer for Spring Session
     */
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        
        log.info("Configuring session cookies with secure={}, sameSite={}, domain={}",
                cookieSecure, cookieSameSite, cookieDomain.isEmpty() ? "default" : cookieDomain);
        
        // Set secure flag based on environment
        serializer.setUseSecureCookie(cookieSecure);
        
        // Set cookie name
        serializer.setCookieName("JSESSIONID");
        
        // Set domain if provided
        if (!cookieDomain.isEmpty()) {
            serializer.setDomainName(cookieDomain);
            log.info("Setting cookie domain to: {}", cookieDomain);
        }
        
        // Set cookie path to root to be accessible from all paths
        serializer.setCookiePath("/");
        
        // Set SameSite attribute
        // Note: DefaultCookieSerializer doesn't support SameSite directly in older Spring versions
        serializer.setSameSite(cookieSameSite);
        
        // Configure session cookie for maximum browser compatibility
        serializer.setUseHttpOnlyCookie(true);
        
        return serializer;
    }
    
    /**
     * Configure SameSite attribute for all cookies
     */
    @Bean
    public CookieSameSiteSupplier applicationCookieSameSiteSupplier() {
        return CookieSameSiteSupplier.ofLax();
    }
}
