package com.otavio.aifoodapp.filter;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

    public void setSecureCookies(boolean secure) {
        this.secure = secure;
        log.debug("Set secure cookies to: {}", secure);
    }

    @Override
    public void addHeader(String name, String value) {
        if (name != null && name.equalsIgnoreCase("Set-Cookie") && value != null) {
            value = addSameSiteAttribute(value);
            log.debug("Modified cookie header: {}", value);
        }

        Collection<String> values = headers.computeIfAbsent(name, k -> new ArrayList<>());
        values.add(value);

        super.addHeader(name, value);
    }

    @Override
    public void setHeader(String name, String value) {
        if (name != null && name.equalsIgnoreCase("Set-Cookie") && value != null) {
            value = addSameSiteAttribute(value);
            log.debug("Modified cookie header: {}", value);
        }

        Collection<String> values = new ArrayList<>();
        values.add(value);
        headers.put(name, values);

        super.setHeader(name, value);
    }

    private String addSameSiteAttribute(String cookieHeader) {
        if (cookieHeader == null) {
            return null;
        }

        if (cookieHeader.contains("SameSite=")) {
            return cookieHeader;
        }

        boolean hasDomain = cookieHeader.contains("Domain=");
        StringBuilder modifiedCookie = new StringBuilder(cookieHeader);

        if (secure && !cookieHeader.contains("Secure")) {
            modifiedCookie.append("; Secure");
        }

        if (!cookieHeader.contains("HttpOnly")) {
            modifiedCookie.append("; HttpOnly");
        }

        modifiedCookie.append("; SameSite=").append(sameSiteValue);

        if (!hasDomain && cookieDomain != null && !cookieDomain.isEmpty()) {
            modifiedCookie.append("; Domain=").append(cookieDomain);
        }

        return modifiedCookie.toString();
    }

    @Override
    public Collection<String> getHeaderNames() {
        return headers.keySet();
    }

    /**
     * ✅ CORREÇÃO APLICADA AQUI
     * Garante que o método nunca retorne null, em conformidade com o contrato
     * da interface HttpServletResponse. Isso resolve o NullPointerException.
     */
    @Override
    public Collection<String> getHeaders(String name) {
        Collection<String> values = headers.get(name);
        return values != null ? values : Collections.emptyList();
    }
}
