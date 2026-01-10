package com.tool.atkdefbackend.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Rate Limiting Filter using Token Bucket Algorithm
 * Limits requests per IP address
 * 
 * Based on NewTech.md Section 5: Rate Limiting
 * 
 * Key Features:
 * - Configurable requests per minute (default: 60)
 * - Sliding window implementation
 * - Automatic bucket cleanup every 5 minutes
 * - Skips rate limiting for Swagger/actuator endpoints
 * - Adds X-RateLimit-* headers to responses
 */
@Component
public class RateLimitingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);
    private static final long BUCKET_WINDOW_MS = TimeUnit.MINUTES.toMillis(1); // 1 minute window
    private static final long CLEANUP_INTERVAL_MS = TimeUnit.MINUTES.toMillis(5); // 5 minute cleanup

    @Value("${rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    private final ConcurrentHashMap<String, RateLimitBucket> buckets = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void init(FilterConfig filterConfig) {
        // Schedule periodic cleanup of old buckets
        cleanupExecutor.scheduleAtFixedRate(
                this::cleanupOldBuckets,
                CLEANUP_INTERVAL_MS,
                CLEANUP_INTERVAL_MS,
                TimeUnit.MILLISECONDS
        );
        log.info("Rate limiting filter initialized with {} requests per minute", requestsPerMinute);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();
        
        // Skip rate limiting for Swagger, actuator, and auth endpoints
        if (shouldSkipRateLimiting(path)) {
            chain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(httpRequest);
        RateLimitBucket bucket = buckets.computeIfAbsent(clientIp, k -> new RateLimitBucket());

        if (bucket.tryConsume()) {
            // Request allowed
            addRateLimitHeaders(httpResponse, bucket);
            chain.doFilter(request, response);
        } else {
            // Rate limit exceeded
            log.warn("Rate limit exceeded for IP: {}", clientIp);
            addRateLimitHeaders(httpResponse, bucket);
            httpResponse.setStatus(429); // Too Many Requests
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write(String.format(
                    "{\"success\":false,\"error\":\"Rate limit exceeded\",\"status\":429,\"limit\":%d}",
                    requestsPerMinute
            ));
        }
    }

    @Override
    public void destroy() {
        cleanupExecutor.shutdown();
        try {
            if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("Rate limiting filter destroyed");
    }

    /**
     * Check if path should skip rate limiting
     * Only skip Swagger and API documentation endpoints
     * Security: Login/register endpoints are now rate-limited to prevent brute-force attacks
     */
    private boolean shouldSkipRateLimiting(String path) {
        return path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/actuator");
    }

    /**
     * Extract client IP from request
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Add rate limit information to response headers
     */
    private void addRateLimitHeaders(HttpServletResponse response, RateLimitBucket bucket) {
        response.setHeader("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(bucket.getRemaining(requestsPerMinute)));
        response.setHeader("X-RateLimit-Reset", String.valueOf(bucket.getResetTime()));
    }

    /**
     * Cleanup old buckets that haven't been used recently
     */
    private void cleanupOldBuckets() {
        long now = System.currentTimeMillis();
        buckets.entrySet().removeIf(entry -> {
            long lastAccess = entry.getValue().getWindowStart();
            return (now - lastAccess) > CLEANUP_INTERVAL_MS;
        });
        log.debug("Cleaned up old rate limit buckets. Current bucket count: {}", buckets.size());
    }

    /**
     * Token Bucket for tracking request counts per client
     * Thread-safe implementation using atomic operations
     */
    private class RateLimitBucket {
        private final AtomicInteger count = new AtomicInteger(0);
        private final AtomicLong windowStart = new AtomicLong(System.currentTimeMillis());

        /**
         * Try to consume a token from the bucket
         * Uses atomic operations to prevent race conditions
         */
        public boolean tryConsume() {
            long now = System.currentTimeMillis();
            long currentWindowStart = windowStart.get();
            
            // Check if window expired and try to reset atomically
            if (now - currentWindowStart > BUCKET_WINDOW_MS) {
                // Use compareAndSet to ensure atomic window reset
                if (windowStart.compareAndSet(currentWindowStart, now)) {
                    count.set(0);
                }
                // If CAS failed, another thread already reset the window
                // Continue with the new window
            }

            // Try to increment count if under limit
            // Use loop with CAS to ensure atomic increment
            int currentCount;
            do {
                currentCount = count.get();
                if (currentCount >= requestsPerMinute) {
                    return false; // Limit exceeded
                }
            } while (!count.compareAndSet(currentCount, currentCount + 1));
            
            return true; // Token consumed successfully
        }

        public int getRemaining(int limit) {
            return Math.max(0, limit - count.get());
        }

        public long getResetTime() {
            return windowStart.get() + BUCKET_WINDOW_MS;
        }

        public long getWindowStart() {
            return windowStart.get();
        }
    }
}
