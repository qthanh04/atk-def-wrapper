package com.tool.atkdefbackend.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * Core API Client - Base HTTP client for Python Core API communication
 * Uses Spring WebFlux's WebClient for non-blocking I/O
 * 
 * Based on NewTech.md Section 1: API Gateway / Proxy Pattern
 * Features:
 * - Non-blocking HTTP communication
 * - Configurable timeouts
 * - Type-safe response handling
 * - Multipart form data support for file uploads
 */
@Component
public class CoreApiClient {

    private static final Logger log = LoggerFactory.getLogger(CoreApiClient.class);
    private final WebClient webClient;
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration FILE_UPLOAD_TIMEOUT = Duration.ofMinutes(5);

    public CoreApiClient(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Generic GET request
     */
    public <T> T get(String endpoint, Class<T> responseType) {
        log.info("GET request to: {}", endpoint);
        return webClient.get()
                .uri(endpoint)
                .retrieve()
                .bodyToMono(responseType)
                .timeout(DEFAULT_TIMEOUT)
                .block();
    }

    /**
     * Generic GET request with ParameterizedTypeReference for complex types
     */
    public <T> T get(String endpoint, ParameterizedTypeReference<T> responseType) {
        log.info("GET request to: {}", endpoint);
        return webClient.get()
                .uri(endpoint)
                .retrieve()
                .bodyToMono(responseType)
                .timeout(DEFAULT_TIMEOUT)
                .block();
    }

    /**
     * Generic POST request with JSON body
     */
    public <T> T post(String endpoint, Object body, Class<T> responseType) {
        log.info("POST request to: {}", endpoint);
        return webClient.post()
                .uri(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body != null ? body : new Object())
                .retrieve()
                .bodyToMono(responseType)
                .timeout(DEFAULT_TIMEOUT)
                .block();
    }

    /**
     * POST request with multipart form data (for file uploads)
     * Uses longer timeout (5 minutes) for large file uploads
     */
    public <T> T postMultipart(String endpoint, MultiValueMap<String, ?> body, Class<T> responseType) {
        log.info("Multipart POST request to: {}", endpoint);
        return webClient.post()
                .uri(endpoint)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(body))
                .retrieve()
                .bodyToMono(responseType)
                .timeout(FILE_UPLOAD_TIMEOUT)
                .block();
    }

    /**
     * Generic PATCH request with JSON body
     */
    public <T> T patch(String endpoint, Object body, Class<T> responseType) {
        log.info("PATCH request to: {}", endpoint);
        return webClient.patch()
                .uri(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body != null ? body : new Object())
                .retrieve()
                .bodyToMono(responseType)
                .timeout(DEFAULT_TIMEOUT)
                .block();
    }

    /**
     * Generic DELETE request
     */
    public <T> T delete(String endpoint, Class<T> responseType) {
        log.info("DELETE request to: {}", endpoint);
        return webClient.delete()
                .uri(endpoint)
                .retrieve()
                .bodyToMono(responseType)
                .timeout(DEFAULT_TIMEOUT)
                .block();
    }

    /**
     * Generic POST request without body (for control endpoints)
     */
    public <T> T postEmpty(String endpoint, Class<T> responseType) {
        log.info("POST (empty) request to: {}", endpoint);
        return webClient.post()
                .uri(endpoint)
                .retrieve()
                .bodyToMono(responseType)
                .timeout(DEFAULT_TIMEOUT)
                .block();
    }
}
