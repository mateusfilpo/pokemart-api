package br.com.filpo.pokemart.infrastructure.adapters.in.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.BucketProxy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.distributed.proxy.RemoteBucketBuilder;
import jakarta.servlet.FilterChain;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    @InjectMocks
    private RateLimitFilter rateLimitFilter;

    @Mock
    private ProxyManager<byte[]> proxyManager;

    @Mock
    private FilterChain filterChain;

    @Mock
    private RemoteBucketBuilder<byte[]> remoteBucketBuilder;

    @Mock
    private BucketProxy bucket;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        ReflectionTestUtils.setField(rateLimitFilter, "globalCapacity", 50L);
        ReflectionTestUtils.setField(rateLimitFilter, "globalMinutes", 1L);
        ReflectionTestUtils.setField(rateLimitFilter, "loginCapacity", 10L);
        ReflectionTestUtils.setField(rateLimitFilter, "loginMinutes", 1L);
    }

    @Test
    @DisplayName("Deve permitir a requisição quando o bucket tem tokens disponíveis")
    void shouldAllowRequestWhenBucketHasTokens() throws Exception {
        // Arrange
        request.setRequestURI("/api/items");
        request.setMethod("GET");
        request.setRemoteAddr("192.168.1.1");

        ConsumptionProbe probe = mock(ConsumptionProbe.class);
        when(probe.isConsumed()).thenReturn(true);
        when(probe.getRemainingTokens()).thenReturn(49L);

        when(proxyManager.builder()).thenReturn(remoteBucketBuilder);
        when(remoteBucketBuilder.build(any(byte[].class), any(BucketConfiguration.class))).thenReturn(bucket);
        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(probe);

        // Act
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        assertEquals("49", response.getHeader("X-Rate-Limit-Remaining"));
    }

    @Test
    @DisplayName("Deve retornar 429 quando o bucket estiver esgotado")
    void shouldReturn429WhenBucketIsExhausted() throws Exception {
        // Arrange
        request.setRequestURI("/api/items");
        request.setMethod("GET");
        request.setRemoteAddr("192.168.1.1");

        ConsumptionProbe probe = mock(ConsumptionProbe.class);
        when(probe.isConsumed()).thenReturn(false);
        when(probe.getNanosToWaitForRefill()).thenReturn(30_000_000_000L); // 30 segundos

        when(proxyManager.builder()).thenReturn(remoteBucketBuilder);
        when(remoteBucketBuilder.build(any(byte[].class), any(BucketConfiguration.class))).thenReturn(bucket);
        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(probe);

        // Act
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, never()).doFilter(request, response);
        assertEquals(429, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertEquals("30", response.getHeader("X-Rate-Limit-Retry-After-Seconds"));

        String body = response.getContentAsString();
        assertTrue(body.contains("Rate limit exceeded"));
        assertTrue(body.contains("30 seconds"));
        assertTrue(body.contains("/api/items"));
    }

    @Test
    @DisplayName("Deve usar chave de login para endpoint POST /api/auth/login")
    void shouldUseLoginBucketForLoginEndpoint() throws Exception {
        // Arrange
        request.setRequestURI("/api/auth/login");
        request.setMethod("POST");
        request.setRemoteAddr("10.0.0.1");

        ConsumptionProbe probe = mock(ConsumptionProbe.class);
        when(probe.isConsumed()).thenReturn(true);
        when(probe.getRemainingTokens()).thenReturn(9L);

        when(proxyManager.builder()).thenReturn(remoteBucketBuilder);
        when(remoteBucketBuilder.build(any(byte[].class), any(BucketConfiguration.class))).thenReturn(bucket);
        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(probe);

        // Act
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        assertEquals("9", response.getHeader("X-Rate-Limit-Remaining"));

        // Verifica que a chave usada começa com "rate_limit:login:"
        verify(remoteBucketBuilder).build(
                eq("rate_limit:login:10.0.0.1".getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                any(BucketConfiguration.class));
    }

    @Test
    @DisplayName("Deve usar chave global para GET em /api/auth/login (não é login POST)")
    void shouldUseGlobalBucketForNonPostLoginEndpoint() throws Exception {
        // Arrange
        request.setRequestURI("/api/auth/login");
        request.setMethod("GET");
        request.setRemoteAddr("10.0.0.1");

        ConsumptionProbe probe = mock(ConsumptionProbe.class);
        when(probe.isConsumed()).thenReturn(true);
        when(probe.getRemainingTokens()).thenReturn(49L);

        when(proxyManager.builder()).thenReturn(remoteBucketBuilder);
        when(remoteBucketBuilder.build(any(byte[].class), any(BucketConfiguration.class))).thenReturn(bucket);
        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(probe);

        // Act
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(remoteBucketBuilder).build(
                eq("rate_limit:global:10.0.0.1".getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                any(BucketConfiguration.class));
    }

    @Test
    @DisplayName("Deve adicionar header X-Rate-Limit-Remaining na resposta bem-sucedida")
    void shouldAddRateLimitHeaderOnSuccess() throws Exception {
        // Arrange
        request.setRequestURI("/api/categories");
        request.setMethod("GET");
        request.setRemoteAddr("192.168.1.100");

        ConsumptionProbe probe = mock(ConsumptionProbe.class);
        when(probe.isConsumed()).thenReturn(true);
        when(probe.getRemainingTokens()).thenReturn(25L);

        when(proxyManager.builder()).thenReturn(remoteBucketBuilder);
        when(remoteBucketBuilder.build(any(byte[].class), any(BucketConfiguration.class))).thenReturn(bucket);
        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(probe);

        // Act
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertEquals("25", response.getHeader("X-Rate-Limit-Remaining"));
    }

    @Test
    @DisplayName("Deve ignorar rate limit para endpoints do Swagger")
    void shouldSkipRateLimitForSwaggerEndpoints() throws Exception {
        // Arrange - Swagger UI
        request.setRequestURI("/swagger-ui/index.html");
        request.setMethod("GET");

        // Act
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        verify(proxyManager, never()).builder();
    }

    @Test
    @DisplayName("Deve ignorar rate limit para endpoints da API docs")
    void shouldSkipRateLimitForApiDocsEndpoints() throws Exception {
        // Arrange - API Docs
        request.setRequestURI("/v3/api-docs/swagger-config");
        request.setMethod("GET");

        // Act
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        verify(proxyManager, never()).builder();
    }

    @Test
    @DisplayName("Deve usar X-Forwarded-For como IP do cliente quando disponível")
    void shouldUseXForwardedForHeaderAsClientIP() throws Exception {
        // Arrange
        request.setRequestURI("/api/items");
        request.setMethod("GET");
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("X-Forwarded-For", "203.0.113.50, 70.41.3.18");

        ConsumptionProbe probe = mock(ConsumptionProbe.class);
        when(probe.isConsumed()).thenReturn(true);
        when(probe.getRemainingTokens()).thenReturn(49L);

        when(proxyManager.builder()).thenReturn(remoteBucketBuilder);
        when(remoteBucketBuilder.build(any(byte[].class), any(BucketConfiguration.class))).thenReturn(bucket);
        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(probe);

        // Act
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert - Deve usar o primeiro IP do X-Forwarded-For (203.0.113.50), não o
        // remoteAddr
        verify(remoteBucketBuilder).build(
                eq("rate_limit:global:203.0.113.50".getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                any(BucketConfiguration.class));
    }

    @Test
    @DisplayName("Deve usar remoteAddr quando X-Forwarded-For não estiver presente")
    void shouldUseRemoteAddrWhenXForwardedForIsAbsent() throws Exception {
        // Arrange
        request.setRequestURI("/api/items");
        request.setMethod("GET");
        request.setRemoteAddr("192.168.0.42");

        ConsumptionProbe probe = mock(ConsumptionProbe.class);
        when(probe.isConsumed()).thenReturn(true);
        when(probe.getRemainingTokens()).thenReturn(49L);

        when(proxyManager.builder()).thenReturn(remoteBucketBuilder);
        when(remoteBucketBuilder.build(any(byte[].class), any(BucketConfiguration.class))).thenReturn(bucket);
        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(probe);

        // Act
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(remoteBucketBuilder).build(
                eq("rate_limit:global:192.168.0.42".getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                any(BucketConfiguration.class));
    }
}
