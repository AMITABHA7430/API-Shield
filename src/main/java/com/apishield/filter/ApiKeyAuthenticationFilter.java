
package com.apishield.filter;

import com.apishield.model.ApiClient;
import com.apishield.repository.ApiClientRepository;
import com.apishield.service.ApiKeyService;
import com.apishield.service.RateLimiterService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final ApiClientRepository repository;
    private final ApiKeyService apiKeyService;
    private final RateLimiterService rateLimiterService;


    public ApiKeyAuthenticationFilter(
            ApiClientRepository repository,
            ApiKeyService apiKeyService,
            RateLimiterService rateLimiterService) {
        this.repository = repository;
        this.apiKeyService = apiKeyService;
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String apiKey = request.getHeader("X-API-Key");

        // No key: let Spring Security reject protected requests.
        if (apiKey == null || apiKey.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        String hash = apiKeyService.hashApiKey(apiKey);
        Optional<ApiClient> result = repository.findByApiKeyHash(hash);

        if (result.isEmpty() || !result.get().isActive()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\":\"Invalid or inactive API key\"}");
            return;
        }


        ApiClient client = result.get();

        var authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        client,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_API_CLIENT"))
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);


        boolean allowed = rateLimiterService.isAllowed(
                client.getId(),
                client.getRateLimitPerMinute()
        );

        if (!allowed) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\":\"Rate limit exceeded. Try again later.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
