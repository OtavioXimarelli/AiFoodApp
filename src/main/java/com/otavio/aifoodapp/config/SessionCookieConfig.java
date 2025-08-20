package com.otavio.aifoodapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.server.CookieSameSiteSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuration for session cookies
 * This ensures cookies are properly set in production environment
 */
@Configuration
@Slf4j
public class SessionCookieConfig {

    @Value("${COOKIE_SECURE:true}")
    private boolean cookieSecure;
    
    @Value("${COOKIE_SAME_SITE:lax}")
    private String cookieSameSite;
    
    @Value("${COOKIE_DOMAIN:aifoodapp.site}")
    private String cookieDomain;

    @PostConstruct
    public void init() {
        log.info("Initializing SessionCookieConfig with secure={}, sameSite={}, domain={}", 
                cookieSecure, cookieSameSite, cookieDomain);
    }

    /**
     * Configure cookie serializer for Spring Session
     */
    @Bean
    @Primary
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        
        log.info("Configuring session cookies with secure={}, sameSite={}, domain={}",
                cookieSecure, cookieSameSite, cookieDomain);
        
        // Set secure flag based on environment
        serializer.setUseSecureCookie(cookieSecure);
        
        // Set cookie name
        serializer.setCookieName("JSESSIONID");
        
        // Set domain to ensure cookies work across subdomains
        serializer.setDomainName(cookieDomain);
        
        // Set cookie path to root to be accessible from all paths
        serializer.setCookiePath("/");
        
        // Set SameSite attribute
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
        if ("none".equalsIgnoreCase(cookieSameSite)) {
            return CookieSameSiteSupplier.ofNone();
        } else if ("strict".equalsIgnoreCase(cookieSameSite)) {
            return CookieSameSiteSupplier.ofStrict();
        } else {
            return CookieSameSiteSupplier.ofLax();
        }
    }
}
