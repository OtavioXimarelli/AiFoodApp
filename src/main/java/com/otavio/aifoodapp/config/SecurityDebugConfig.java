package com.otavio.aifoodapp.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Debug configuration for Spring Security
 * Provides additional diagnostic tools for authentication issues
 * Only active when app.debug.enabled=true
 */
@Configuration
@ConditionalOnProperty(name = "app.debug.enabled", havingValue = "true", matchIfMissing = false)
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
