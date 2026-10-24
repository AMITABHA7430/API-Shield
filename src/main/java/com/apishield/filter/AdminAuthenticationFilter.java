package com.apishield.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

@Component
public class AdminAuthenticationFilter extends OncePerRequestFilter {

    private final String adminToken;

    public AdminAuthenticationFilter(
            @Value("${APISHIELD_ADMIN_TOKEN}") String adminToken) {
        this.adminToken = adminToken;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        return !path.equals("/api/v1/clients/register")
                && !path.matches(
                "/api/v1/clients/\\d+/(revoke|rotate-key)");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String suppliedToken = request.getHeader("X-Admin-Token");

        boolean valid = suppliedToken != null
                && MessageDigest.isEqual(
                suppliedToken.getBytes(StandardCharsets.UTF_8),
                adminToken.getBytes(StandardCharsets.UTF_8));

        if (!valid) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\":\"Invalid or missing admin token\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}