package com.otavio.aifoodapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
public class CookieConfig {

    @Value("${spring.session.cookie.domain:aifoodapp.site}")
    private String cookieDomain;

    @Value("${spring.session.cookie.secure:true}")
    private boolean secure;

    @Value("${spring.session.cookie.same-site:lax}")
    private String sameSite;

    @Value("${spring.session.cookie.http-only:true}")
    private boolean httpOnly;

    @Value("${spring.session.cookie.max-age:2592000}")
    private int maxAge;

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();

        // Clean domain - remove leading dot if present as Spring Security doesn't accept it
        String cleanDomain = cookieDomain;
        if (cleanDomain != null && cleanDomain.startsWith(".")) {
            cleanDomain = cleanDomain.substring(1);
        }

        // Only set domain if it's not localhost and not empty
        if (cleanDomain != null && !cleanDomain.isEmpty() &&
            !cleanDomain.equals("localhost") && !cleanDomain.equals("127.0.0.1")) {
            serializer.setDomainName(cleanDomain);
        }

        serializer.setCookieName("JSESSIONID");
        serializer.setCookieMaxAge(maxAge);
        serializer.setUseSecureCookie(secure);
        serializer.setUseHttpOnlyCookie(httpOnly);
        serializer.setSameSite(sameSite);

        return serializer;
    }
}
