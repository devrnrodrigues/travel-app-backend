package com.devrenanrodrigues.travelapi.ratelimit;

import com.devrenanrodrigues.travelapi.exception.ErrorResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String uri = request.getRequestURI();
        if (uri == null || !uri.startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        RateLimitTier tier = resolveTier(request.getMethod(), uri);
        String clientIp = extractClientIp(request);

        boolean allowed = rateLimitService.tryAcquire(clientIp, tier);
        if (!allowed) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            ErrorResponseDTO errorBody = ErrorResponseDTO.of(
                    HttpStatus.TOO_MANY_REQUESTS.value(),
                    "Too Many Requests",
                    "Muitas requisições. Por favor, aguarde alguns instantes antes de tentar novamente.",
                    uri
            );

            response.getWriter().write(objectMapper.writeValueAsString(errorBody));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private RateLimitTier resolveTier(String method, String uri) {
        if (uri.startsWith("/api/auth/")) {
            return RateLimitTier.AUTH;
        }

        if (uri.startsWith("/api/flights/") || uri.matches("^/api/destinations/[^/]+/weather.*$")) {
            return RateLimitTier.EXTERNAL;
        }

        if (uri.equals("/api/users/avatar")
                || (uri.startsWith("/api/collections") && ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)))
                || (uri.matches("^/api/destinations/[^/]+/comments$") && "POST".equalsIgnoreCase(method))
                || (uri.startsWith("/api/comments/") && ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)))) {
            return RateLimitTier.UPLOAD;
        }

        return RateLimitTier.GENERAL;
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr();
    }
}
