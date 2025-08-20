package com.otavio.aifoodapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Debug configuration for Spring Security
 * Provides additional diagnostic tools for authentication issues
 */
@Configuration
@Slf4j
public class SecurityDebugConfig {

    @PostConstruct
    public void init() {
        log.info("Security debug configuration initialized");
    }
    
    /**
     * Creates a request logging filter that logs details about HTTP requests
     * This is helpful for diagnosing cookie and session issues
     */
    @Bean
    @Profile({"dev", "prod"})  // Only enable in dev and prod profiles
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(false);
        filter.setIncludeHeaders(true);  // Will include cookies as they are headers
        filter.setMaxPayloadLength(10000);
        filter.setBeforeMessagePrefix("REQUEST DATA: [");
        filter.setAfterMessagePrefix("REQUEST PROCESSED: [");
        filter.setAfterMessageSuffix("]");
        return filter;
    }
}
