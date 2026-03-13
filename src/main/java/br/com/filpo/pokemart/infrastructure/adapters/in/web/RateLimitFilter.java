package br.com.filpo.pokemart.infrastructure.adapters.in.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import io.github.bucket4j.distributed.proxy.ProxyManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import br.com.filpo.pokemart.infrastructure.adapters.in.web.exceptions.CustomError;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final ProxyManager<byte[]> proxyManager;

    @Value("${rate-limit.capacity}")
    private long globalCapacity;

    @Value("${rate-limit.minutes}")
    private long globalMinutes;

    @Value("${rate-limit.login-capacity}")
    private long loginCapacity;

    @Value("${rate-limit.login-minutes}")
    private long loginMinutes;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        if (path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIP(request);
        boolean isLoginRequest = path.equals("/api/auth/login") && request.getMethod().equalsIgnoreCase("POST");
        String cacheKeyString = isLoginRequest ? "rate_limit:login:" + clientIp : "rate_limit:global:" + clientIp;
        
        byte[] cacheKey = cacheKeyString.getBytes(StandardCharsets.UTF_8);

        BucketConfiguration bucketConfig = BucketConfiguration.builder()
                .addLimit(createBandwidth(isLoginRequest))
                .build();

        Bucket bucket = proxyManager.builder().build(cacheKey, bucketConfig);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
        } else {
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
            writeRateLimitResponse(request, response, waitForRefill);
        }
    }

    private Bandwidth createBandwidth(boolean isLoginRequest) {
        long capacity = isLoginRequest ? loginCapacity : globalCapacity;
        long minutes = isLoginRequest ? loginMinutes : globalMinutes;
        return Bandwidth.classic(capacity, Refill.greedy(capacity, Duration.ofMinutes(minutes)));
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    private void writeRateLimitResponse(HttpServletRequest request, HttpServletResponse response, long waitForRefill) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");

        CustomError errorResponse = new CustomError(
            Instant.now(), 
            HttpStatus.TOO_MANY_REQUESTS.value(),
            "Rate limit exceeded. Try again in " + waitForRefill + " seconds.",
            request.getRequestURI() 
        );

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        response.getWriter().write(mapper.writeValueAsString(errorResponse));
    }
}