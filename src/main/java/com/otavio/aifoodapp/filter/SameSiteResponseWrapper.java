package com.otavio.aifoodapp.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import lombok.extern.slf4j.Slf4j;

/**
 * Wrapper para HttpServletResponse que intercepta os cabeçalhos Set-Cookie
 * e adiciona o atributo SameSite aos cookies.
 */
@Slf4j
public class SameSiteResponseWrapper extends HttpServletResponseWrapper {

    private final String sameSiteValue;
    private final String cookieDomain;
    private final Map<String, Collection<String>> headers = new HashMap<>();

    public SameSiteResponseWrapper(HttpServletResponse response, String sameSiteValue, String cookieDomain) {
        super(response);
        this.sameSiteValue = sameSiteValue != null ? sameSiteValue : "lax";
        this.cookieDomain = cookieDomain;
    }

    @Override
    public void addHeader(String name, String value) {
        if (name != null && name.equalsIgnoreCase("Set-Cookie") && value != null) {
            // Modificar o cabeçalho do cookie para incluir o atributo SameSite
            value = addSameSiteAttribute(value);
        }
        
        // Armazenar o cabeçalho localmente
        Collection<String> values = headers.computeIfAbsent(name, k -> new ArrayList<>());
        values.add(value);
        
        // Passar o cabeçalho modificado para o response original
        super.addHeader(name, value);
    }

    @Override
    public void setHeader(String name, String value) {
        if (name != null && name.equalsIgnoreCase("Set-Cookie") && value != null) {
            // Modificar o cabeçalho do cookie para incluir o atributo SameSite
            value = addSameSiteAttribute(value);
        }
        
        // Armazenar o cabeçalho localmente
        Collection<String> values = new ArrayList<>();
        values.add(value);
        headers.put(name, values);
        
        // Passar o cabeçalho modificado para o response original
        super.setHeader(name, value);
    }

    /**
     * Adiciona o atributo SameSite ao cookie se ainda não estiver presente.
     * 
     * @param cookieHeader O cabeçalho do cookie a ser modificado
     * @return O cabeçalho do cookie modificado
     */
    private String addSameSiteAttribute(String cookieHeader) {
        if (cookieHeader == null) {
            return null;
        }
        
        // Se o SameSite já estiver presente, não modificar
        if (cookieHeader.contains("SameSite=")) {
            return cookieHeader;
        }
        
        // Se o Domain já estiver presente, não adicionar novamente
        boolean hasDomain = cookieHeader.contains("Domain=");
        
        StringBuilder modifiedCookie = new StringBuilder(cookieHeader);
        
        // Adicionar atributo Secure se não estiver presente
        if (!cookieHeader.contains("Secure")) {
            modifiedCookie.append("; Secure");
        }
        
        // Adicionar atributo HttpOnly se não estiver presente
        if (!cookieHeader.contains("HttpOnly")) {
            modifiedCookie.append("; HttpOnly");
        }
        
        // Adicionar atributo SameSite
        modifiedCookie.append("; SameSite=").append(sameSiteValue);
        
        // Adicionar atributo Domain se não estiver presente e cookieDomain for fornecido
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
