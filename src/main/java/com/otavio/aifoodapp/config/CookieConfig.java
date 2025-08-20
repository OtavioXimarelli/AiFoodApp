package com.otavio.aifoodapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

import lombok.extern.slf4j.Slf4j;

/**
 * This class has been deprecated in favor of SessionCookieConfig
 * Keeping it for backward compatibility, but SessionCookieConfig should be used
 */
@Configuration
@Slf4j
@Deprecated
public class CookieConfig {

    @Value("${COOKIE_DOMAIN:aifoodapp.site}")
    private String cookieDomain;
    
    @Value("${COOKIE_SECURE:true}")
    private boolean cookieSecure;
    
    @Value("${COOKIE_SAME_SITE:lax}")
    private String cookieSameSite;

    /**
     * Renamed from cookieSerializer to defaultCookieSerializer to avoid conflict
     * with the bean of the same name in SessionCookieConfig
     */
    @Bean
    public CookieSerializer defaultCookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        
        log.info("CookieConfig (deprecated): Configuring with domain={}, secure={}, sameSite={}",
                cookieDomain, cookieSecure, cookieSameSite);
                
        // We don't set cookie name here to avoid conflict with SessionCookieConfig
        serializer.setCookiePath("/");
        
        // Use domain value from environment
        serializer.setDomainName(cookieDomain);
        
        // Set SameSite attribute
        serializer.setSameSite(cookieSameSite);
        
        serializer.setUseHttpOnlyCookie(true);
        serializer.setUseSecureCookie(cookieSecure);
        
        return serializer;
    }
}
