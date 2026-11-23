package com.devrenanrodrigues.travelapi.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private FilterChain filterChain;

    private RateLimitFilter rateLimitFilter;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        rateLimitFilter = new RateLimitFilter(rateLimitService, objectMapper);
    }

    @Test
    @DisplayName("Deve permitir requisições OPTIONS sem aplicar rate limit")
    void shouldAllowOptionsRequestsWithoutRateLimit() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/destinations");
        MockHttpServletResponse response = new MockHttpServletResponse();

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(rateLimitService, never()).tryAcquire(any(), any());
    }

    @Test
    @DisplayName("Deve ignorar rotas fora de /api/")
    void shouldBypassNonApiRoutes() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/swagger-ui/index.html");
        MockHttpServletResponse response = new MockHttpServletResponse();

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(rateLimitService, never()).tryAcquire(any(), any());
    }

    @Test
    @DisplayName("Deve permitir requisição quando o rate limit não for excedido")
    void shouldAllowRequestWhenRateLimitAllows() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/destinations");
        request.setRemoteAddr("192.168.1.50");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(rateLimitService.tryAcquire("192.168.1.50", RateLimitTier.GENERAL)).thenReturn(true);

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("Deve retornar 429 Too Many Requests quando limite for excedido")
    void shouldReturn429WhenRateLimitExceeded() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setRemoteAddr("192.168.1.50");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(rateLimitService.tryAcquire("192.168.1.50", RateLimitTier.AUTH)).thenReturn(false);

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getContentAsString()).contains("Too Many Requests");
        assertThat(response.getContentAsString()).contains("Muitas requisições");
    }

    @Test
    @DisplayName("Deve extrair IP a partir do cabeçalho X-Forwarded-For")
    void shouldExtractIpFromXForwardedFor() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/flights/search");
        request.addHeader("X-Forwarded-For", "203.0.113.195, 70.41.3.18");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(rateLimitService.tryAcquire("203.0.113.195", RateLimitTier.EXTERNAL)).thenReturn(true);

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(rateLimitService).tryAcquire("203.0.113.195", RateLimitTier.EXTERNAL);
        verify(filterChain).doFilter(request, response);
    }
}
