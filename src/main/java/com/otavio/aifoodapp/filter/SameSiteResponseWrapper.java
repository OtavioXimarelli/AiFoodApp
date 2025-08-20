package com.otavio.aifoodapp.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import lombok.extern.slf4j.Slf4j;

/**
 * HttpServletResponse wrapper that intercepts Set-Cookie headers
 * and adds the SameSite attribute to cookies.
 */
@Slf4j
public class SameSiteResponseWrapper extends HttpServletResponseWrapper {

    private final String sameSiteValue;
    private final String cookieDomain;
    private final Map<String, Collection<String>> headers = new HashMap<>();
    private boolean secure = true; // Default to secure cookies

    public SameSiteResponseWrapper(HttpServletResponse response, String sameSiteValue, String cookieDomain) {
        super(response);
        this.sameSiteValue = sameSiteValue != null ? sameSiteValue : "lax";
        this.cookieDomain = cookieDomain;
    }
    
    /**
     * Set whether cookies should be marked as Secure
     * @param secure true to add the Secure attribute, false otherwise
     */
    public void setSecureCookies(boolean secure) {
        this.secure = secure;
        log.debug("Set secure cookies to: {}", secure);
    }

    @Override
    public void addHeader(String name, String value) {
        if (name != null && name.equalsIgnoreCase("Set-Cookie") && value != null) {
            // Modify cookie header to include SameSite attribute
            value = addSameSiteAttribute(value);
            log.debug("Modified cookie header: {}", value);
        }
        
        // Store header locally
        Collection<String> values = headers.computeIfAbsent(name, k -> new ArrayList<>());
        values.add(value);
        
        // Pass modified header to original response
        super.addHeader(name, value);
    }

    @Override
    public void setHeader(String name, String value) {
        if (name != null && name.equalsIgnoreCase("Set-Cookie") && value != null) {
            // Modify cookie header to include SameSite attribute
            value = addSameSiteAttribute(value);
            log.debug("Modified cookie header: {}", value);
        }
        
        // Store header locally
        Collection<String> values = new ArrayList<>();
        values.add(value);
        headers.put(name, values);
        
        // Pass modified header to original response
        super.setHeader(name, value);
    }

    /**
     * Add the SameSite attribute to the cookie if not already present.
     * 
     * @param cookieHeader The cookie header to modify
     * @return The modified cookie header
     */
    private String addSameSiteAttribute(String cookieHeader) {
        if (cookieHeader == null) {
            return null;
        }
        
        // If SameSite is already present, don't modify
        if (cookieHeader.contains("SameSite=")) {
            return cookieHeader;
        }
        
        // Check if Domain is already present
        boolean hasDomain = cookieHeader.contains("Domain=");
        
        StringBuilder modifiedCookie = new StringBuilder(cookieHeader);
        
        // Add Secure attribute if not present and secure is true
        if (secure && !cookieHeader.contains("Secure")) {
            modifiedCookie.append("; Secure");
        }
        
        // Add HttpOnly attribute if not present
        if (!cookieHeader.contains("HttpOnly")) {
            modifiedCookie.append("; HttpOnly");
        }
        
        // Add SameSite attribute
        modifiedCookie.append("; SameSite=").append(sameSiteValue);
        
        // Add Domain attribute if not present and cookieDomain is provided
        if (!hasDomain && cookieDomain != null && !cookieDomain.isEmpty()) {
            modifiedCookie.append("; Domain=").append(cookieDomain);
        }
        
        return modifiedCookie.toString();
    }

    /**
     * Obtém os nomes de todos os cabeçalhos armazenados.
     * 
     * @return Uma coleção com os nomes dos cabeçalhos
     */
    @Override
    public Collection<String> getHeaderNames() {
        return headers.keySet();
    }

    /**
     * Obtém todos os valores para um determinado cabeçalho.
     * 
     * @param name O nome do cabeçalho
     * @return Uma coleção com os valores do cabeçalho
     */
    @Override
    public Collection<String> getHeaders(String name) {
        return headers.get(name);
    }
}
