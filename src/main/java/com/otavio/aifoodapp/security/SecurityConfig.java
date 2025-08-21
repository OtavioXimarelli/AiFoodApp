package com.otavio.aifoodapp.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import com.otavio.aifoodapp.filter.SameSiteCookieFilter;

import jakarta.servlet.DispatcherType;
import lombok.extern.slf4j.Slf4j;


@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    @Value("${app.frontend.url}")
    private String frontEndUrl;
    
    private final OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;
    private final TokenRefreshFilter tokenRefreshFilter;
    private final JsonAuthenticationEntryPoint jsonAuthenticationEntryPoint;
    private final SameSiteCookieFilter sameSiteCookieFilter;
    
    @Autowired
    private CorsConfigurationSource corsConfigurationSource;
    
    public SecurityConfig(
            OAuth2LoginSuccessHandler oauth2LoginSuccessHandler,
            TokenRefreshFilter tokenRefreshFilter,
            JsonAuthenticationEntryPoint jsonAuthenticationEntryPoint,
            SameSiteCookieFilter sameSiteCookieFilter) {
        this.oauth2LoginSuccessHandler = oauth2LoginSuccessHandler;
        this.tokenRefreshFilter = tokenRefreshFilter;
        this.jsonAuthenticationEntryPoint = jsonAuthenticationEntryPoint;
        this.sameSiteCookieFilter = sameSiteCookieFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Reabilitar CORS com configuração corrigida
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                
                // Disable CSRF for API endpoints (appropriate for stateless API with token-based auth)
                .csrf(csrf -> csrf.disable())
                
                // Use custom JSON authentication entry point for better API handling
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jsonAuthenticationEntryPoint))
                
                // Configure session management for OAuth2 authentication
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
                
                // Configure authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Allow error resources
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        
                        // Allow OAuth and login endpoints
                        .requestMatchers("/oauth2/**", "/login/**").permitAll()
                        
                        // Allow OPTIONS requests for CORS preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        
                        // Allow error endpoint
                        .requestMatchers("/error").permitAll()
                        
                        // Allow health check endpoint
                        .requestMatchers("/health").permitAll()
                        
                        // Allow test authentication endpoints for debugging
                        .requestMatchers("/api/foods/test-auth").permitAll()
                        
                        // Allow auth status endpoint for checking authentication (with and without duplicate 'api')
                        .requestMatchers("/api/auth/status", "/api/api/auth/status").permitAll()
                        
                        // Debug endpoints are conditionally available based on app.debug.enabled property
                        .requestMatchers("/api/debug/**").permitAll()
                        
                        // Require authentication for all other requests
                        .anyRequest().authenticated()
                )
                
                // Configure OAuth2 login with persistent tokens
                .oauth2Login(oauth2 -> oauth2
                        // Use the consolidated OAuth2LoginSuccessHandler
                        .successHandler(oauth2LoginSuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            log.error("OAuth2 login failure: {}", exception.getMessage(), exception);
                            response.sendRedirect(frontEndUrl + "/login?error=true&message=" + exception.getMessage());
                        })
                        // Use custom repository for authorization requests to maintain state
                        .authorizationEndpoint(auth -> auth
                            .authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository())
                            .baseUri("/oauth2/authorization")
                        )
                        .redirectionEndpoint(redirect -> 
                            redirect.baseUri("/login/oauth2/code/*")
                        )
                )
                
                // Configure logout
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessUrl(frontEndUrl + "/home")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                )
                // Add token refresh filter
                .addFilterBefore(tokenRefreshFilter, UsernamePasswordAuthenticationFilter.class)
                // Add SameSiteCookieFilter to set SameSite attribute for all cookies
                .addFilterBefore(sameSiteCookieFilter, UsernamePasswordAuthenticationFilter.class)
                .build();


    }

    // O Bean CorsConfigurationSource foi movido para CorsConfig

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    
    @Bean
    public AuthorizationRequestRepository<OAuth2AuthorizationRequest> httpCookieOAuth2AuthorizationRequestRepository() {
        // Usar nosso repositório personalizado baseado em cookies para maior robustez
        return new HttpCookieOAuth2AuthorizationRequestRepository();
    }

}

