package com.otavio.aifoodapp.security;

import com.otavio.aifoodapp.filter.SameSiteCookieFilter;
import jakarta.servlet.DispatcherType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.server.CookieSameSiteSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    @Value("${app.frontend.url}")
    private String frontEndUrl;

    @Value("${COOKIE_SECURE:true}")
    private boolean cookieSecure;

    @Value("${COOKIE_SAME_SITE:lax}")
    private String cookieSameSite;

    @Value("${COOKIE_DOMAIN:aifoodapp.site}")
    private String cookieDomain;

    private final OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;
    private final JsonAuthenticationEntryPoint jsonAuthenticationEntryPoint;
    private final SameSiteCookieFilter sameSiteCookieFilter;

    // Removed TokenRefreshFilter dependency - using standard OAuth2 flow only
    public SecurityConfig(
            OAuth2LoginSuccessHandler oauth2LoginSuccessHandler,
            JsonAuthenticationEntryPoint jsonAuthenticationEntryPoint,
            SameSiteCookieFilter sameSiteCookieFilter) {
        this.oauth2LoginSuccessHandler = oauth2LoginSuccessHandler;
        this.jsonAuthenticationEntryPoint = jsonAuthenticationEntryPoint;
        this.sameSiteCookieFilter = sameSiteCookieFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Standard CORS configuration
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration configuration = new CorsConfiguration();
                    configuration.setAllowedOrigins(List.of(frontEndUrl, "https://aifoodapp.site", "https://www.aifoodapp.site"));
                    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                    configuration.setAllowedHeaders(List.of("*"));
                    configuration.setAllowCredentials(true);
                    configuration.setMaxAge(3600L);
                    return configuration;
                }))
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jsonAuthenticationEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .requestCache(cache -> cache.requestCache(new NullRequestCache()))

                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers("/oauth2/**", "/login/**", "/error", "/health").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/status", "/api/debug/**").permitAll()
                        .anyRequest().authenticated()
                )

                // Standard OAuth2 login configuration
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oauth2LoginSuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            log.error("OAuth2 login failure: {}", exception.getMessage(), exception);
                            response.sendRedirect(frontEndUrl + "/login?error=true&message=" + exception.getMessage());
                        })
                        .authorizationEndpoint(auth -> auth
                                .authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository())
                                .baseUri("/oauth2/authorization")
                        )
                        .redirectionEndpoint(redirect ->
                                redirect.baseUri("/login/oauth2/code/*")
                        )
                )

                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessUrl(frontEndUrl + "/home")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                )

                // Only add the SameSite cookie filter - removed TokenRefreshFilter
                .addFilterBefore(sameSiteCookieFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthorizationRequestRepository<OAuth2AuthorizationRequest> httpCookieOAuth2AuthorizationRequestRepository() {
        return new HttpCookieOAuth2AuthorizationRequestRepository();
    }

    @Bean
    @Primary
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        log.info("Configuring session cookies with secure={}, sameSite={}, domain={}",
                cookieSecure, cookieSameSite, cookieDomain);
        serializer.setUseSecureCookie(cookieSecure);
        serializer.setCookieName("JSESSIONID");
        serializer.setDomainName(cookieDomain);
        serializer.setCookiePath("/");
        serializer.setSameSite(cookieSameSite);
        serializer.setUseHttpOnlyCookie(true);
        return serializer;
    }

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
