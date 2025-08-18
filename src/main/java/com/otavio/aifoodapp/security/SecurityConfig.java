package com.otavio.aifoodapp.security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.DispatcherType;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.frontend.url}")
    private String frontEndUrl;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, @Qualifier("configurationSource") CorsConfigurationSource corsConfigurationSource) throws Exception {
        return http
                // Configure CORS with the specified source
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                
                // Temporarily disable CSRF for all requests to make debugging easier
                .csrf(csrf -> csrf.disable())
                
                // Configure session management
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                
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
                        
                        // Allow test authentication endpoint for debugging
                        .requestMatchers("/api/foods/test-auth").permitAll()
                        
                        // Require authentication for all other requests
                        .anyRequest().authenticated()
                )
                
                // Configure OAuth2 login
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl(frontEndUrl + "/dashboard", true)
                        .failureUrl(frontEndUrl + "/login?error=true")
                )
                
                // Configure logout
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessUrl(frontEndUrl + "/home")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                )
                .build();


    }

    @Bean
    public CorsConfigurationSource configurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:8082", 
            "http://localhost:5173",
            "http://192.168.5.19:8082", 
            "https://www.aifoodapp.site", 
            "https://aifoodapp.site"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Type", 
            "X-Requested-With", 
            "Accept", 
            "Origin", 
            "X-XSRF-TOKEN",
            "X-CSRF-TOKEN"
        ));
        configuration.setExposedHeaders(Arrays.asList("X-XSRF-TOKEN"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}

