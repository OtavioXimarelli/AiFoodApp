package com.otavio.aifoodapp.security;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * Service to handle OAuth2 token refreshing
 * This ensures users remain logged in using refresh tokens similar to YouTube
 */
@Service
@Slf4j
public class TokenService {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final RestTemplate restTemplate;
    
    // Cache para evitar múltiplos refreshes para o mesmo token em curto período
    private final Map<String, Long> lastRefreshAttempts = new ConcurrentHashMap<>();
    private static final long REFRESH_THROTTLE_MS = 30000; // 30 segundos
    
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;
    
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;
    
    @Value("${spring.security.oauth2.client.provider.google.token-uri}")
    private String tokenUri;

    public TokenService(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Refresh the OAuth2 access token using the refresh token
     * @param authentication The OAuth2 authentication token
     * @return true if the token was refreshed successfully
     */
    public boolean refreshAccessTokenIfNeeded(OAuth2AuthenticationToken authentication) {
        try {
            String clientRegistrationId = authentication.getAuthorizedClientRegistrationId();
            String principalName = authentication.getName();
            
            // Verificar se houve uma atualização recente para este usuário
            String cacheKey = clientRegistrationId + ":" + principalName;
            Long lastAttempt = lastRefreshAttempts.get(cacheKey);
            long currentTime = System.currentTimeMillis();
            
            if (lastAttempt != null && currentTime - lastAttempt < REFRESH_THROTTLE_MS) {
                log.debug("Pulando verificação de token para {} - última verificação foi há menos de 30 segundos", principalName);
                return false;
            }
            
            // Atualizar timestamp da última verificação
            lastRefreshAttempts.put(cacheKey, currentTime);
            
            // Limpar cache periodicamente (probabilidade 1%)
            if (lastRefreshAttempts.size() > 100 && Math.random() < 0.01) {
                cleanRefreshCache();
            }
            
            OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                    clientRegistrationId, principalName);
            
            if (authorizedClient == null) {
                log.warn("No authorized client found for {}", principalName);
                return false;
            }
            
            OAuth2RefreshToken refreshToken = authorizedClient.getRefreshToken();
            if (refreshToken == null) {
                log.warn("No refresh token available for {}", principalName);
                return false;
            }
            
            OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
            
            // Check if token is expired or will expire soon (within 5 minutes)
            java.time.Instant expiresAt = accessToken != null ? accessToken.getExpiresAt() : null;
            if (accessToken != null && expiresAt != null && 
                expiresAt.isAfter(java.time.Instant.now().plusSeconds(300))) {
                // Token is still valid
                log.debug("Access token still valid for user {}", principalName);
                return true;
            }
            
            // Need to refresh the token
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "refresh_token");
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("refresh_token", refreshToken.getTokenValue());
            
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
            
            ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(
                    tokenUri,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {});
            
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> tokenResponse = responseEntity.getBody();
                
                if (tokenResponse != null) {
                    // Extract new token details
                    String newAccessToken = (String) tokenResponse.get("access_token");
                    Integer expiresIn = (Integer) tokenResponse.get("expires_in");
                
                    if (newAccessToken == null || expiresIn == null) {
                        log.error("Invalid token response for user {}", principalName);
                        return false;
                    }
                    
                    // Calculate expiry time
                    java.time.Instant newExpiresAt = java.time.Instant.now().plusSeconds(expiresIn);
                    
                    // Create new access token with the refreshed details
                    OAuth2AccessToken newToken = new OAuth2AccessToken(
                        OAuth2AccessToken.TokenType.BEARER,
                        newAccessToken,
                        java.time.Instant.now(),
                        newExpiresAt
                    );
                    
                    // Create a new OAuth2AuthorizedClient with the new access token and original refresh token
                    OAuth2AuthorizedClient updatedClient = new OAuth2AuthorizedClient(
                        authorizedClient.getClientRegistration(),
                        principalName,
                        newToken,
                        authorizedClient.getRefreshToken()
                    );
                    
                    // Save the updated authorized client
                    authorizedClientService.saveAuthorizedClient(updatedClient, authentication);
                    
                    // Log successful token refresh
                    log.info("Successfully refreshed access token for user {}", principalName);
                    
                    return true;
                }
                
                log.error("Token response body is null for user {}", principalName);
                return false;
            } else {
                log.error("Failed to refresh token. Status code: {}", responseEntity.getStatusCode());
                return false;
            }
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("HTTP client error while refreshing token: {}", e.getMessage(), e);
            return false;
        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.error("Resource access error while refreshing token: {}", e.getMessage(), e);
            return false;
        } catch (org.springframework.web.client.RestClientException e) {
            log.error("REST client error while refreshing token: {}", e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("Unexpected error refreshing access token: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Limpa entradas antigas do cache de verificações de token
     */
    private void cleanRefreshCache() {
        long currentTime = System.currentTimeMillis();
        long threshold = currentTime - (REFRESH_THROTTLE_MS * 10); // 10x o tempo de throttle
        
        lastRefreshAttempts.entrySet().removeIf(entry -> entry.getValue() < threshold);
        log.debug("Cache de verificações de token limpo. Tamanho atual: {}", lastRefreshAttempts.size());
    }
}
