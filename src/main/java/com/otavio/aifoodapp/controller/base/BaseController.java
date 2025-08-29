package com.otavio.aifoodapp.controller.base;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.otavio.aifoodapp.enums.UserRoles;
import com.otavio.aifoodapp.model.User;
import com.otavio.aifoodapp.service.UserService;

import lombok.extern.slf4j.Slf4j;

/**
 * Base controller with common functionality for all controllers
 * Consolidates duplicate code related to authentication, response formatting,
 * and error handling
 */
@Slf4j
public abstract class BaseController {

    @Autowired
    private UserService userService;

    /**
     * Check if a user is authenticated
     *
     * @return true if the user is authenticated, false otherwise
     */
    protected boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() &&
               !"anonymousUser".equals(auth.getPrincipal());
    }

    /**
     * Get the current authenticated user
     *
     * @return the authenticated user, or null if not authenticated
     */
    protected User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            log.debug("No authenticated user found");
            return null;
        }

        try {
            // Handle OAuth2 authentication
            if (auth.getPrincipal() instanceof OAuth2User oauth2User) {
                String email = oauth2User.getAttribute("email");
                if (email != null) {
                    log.debug("OAuth2 user found with email: {}", email);
                    return userService.findUserByEmail(email);
                }
            }

            // Handle direct User authentication (if any)
            if (auth.getPrincipal() instanceof User user) {
                return user;
            }

            // Try to find user by authentication name (email)
            String username = auth.getName();
            if (username != null) {
                return userService.findUserByEmail(username);
            }

        } catch (Exception e) {
            log.error("Error getting current user: {}", e.getMessage(), e);
        }

        log.debug("Authentication principal type not supported: {}", 
                 auth.getPrincipal() != null ? auth.getPrincipal().getClass().getName() : "null");
        return null;
    }

    /**
     * Extract user information from OAuth2 authentication
     *
     * @param authentication the OAuth2 authentication
     * @return map of user information
     */
    protected Map<String, Object> extractOAuth2UserInfo(Authentication authentication) {
        Map<String, Object> userInfo = new HashMap<>();

        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
                userInfo.put("email", oauth2User.getAttribute("email"));
                userInfo.put("name", oauth2User.getAttribute("name"));
                userInfo.put("picture", oauth2User.getAttribute("picture"));
            }
        }

        return userInfo;
    }

    /**
     * Create a success response with data
     *
     * @param data the data to include in the response
     * @return ResponseEntity with status 200 and the data
     */
    protected <T> ResponseEntity<T> success(T data) {
        return ResponseEntity.ok(data);
    }

    /**
     * Create a response with a specific status and data
     *
     * @param status the HTTP status
     * @param data the data to include in the response
     * @return ResponseEntity with the specified status and data
     */
    protected <T> ResponseEntity<T> response(HttpStatus status, T data) {
        return ResponseEntity.status(status).body(data);
    }

    /**
     * Create an error response
     *
     * @param status the HTTP status
     * @param message the error message
     * @return ResponseEntity with the error details
     */
    protected ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);

        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * Check if user is authorized to access a resource
     *
     * @param resourceOwnerId the ID of the resource owner
     * @return true if authorized, false otherwise
     */
    protected boolean isAuthorized(Long resourceOwnerId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return false;
        }

        // Admin can access any resource
        if (UserRoles.ADMIN.equals(currentUser.getRole())) {
            return true;
        }

        // Users can access their own resources
        return currentUser.getId().equals(resourceOwnerId);
    }

    /**
     * Create a not found response
     *
     * @param resourceName the name of the resource
     * @param id the ID of the resource
     * @return ResponseEntity with status 404
     */
    protected ResponseEntity<Map<String, Object>> notFound(String resourceName, Long id) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.NOT_FOUND.value());
        errorResponse.put("error", HttpStatus.NOT_FOUND.getReasonPhrase());
        errorResponse.put("message", String.format("%s with id %d not found", resourceName, id));

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Create an unauthorized response
     *
     * @return ResponseEntity with status 401
     */
    protected ResponseEntity<Map<String, Object>> unauthorized() {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
        errorResponse.put("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());
        errorResponse.put("message", "You are not authorized to perform this action");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Log and handle exceptions
     *
     * @param e the exception
     * @return ResponseEntity with error details
     */
    protected ResponseEntity<Map<String, Object>> handleException(Exception e) {
        log.error("Error processing request", e);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.put("error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        errorResponse.put("message", "An error occurred while processing your request");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
