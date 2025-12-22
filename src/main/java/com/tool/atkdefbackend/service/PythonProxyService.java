package com.tool.atkdefbackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PythonProxyService {

    private static final Logger logger = LoggerFactory.getLogger(PythonProxyService.class);

    @Value("${python.server-url:http://localhost:8000}")
    private String pythonServerUrl;

    private final RestTemplate restTemplate;

    public PythonProxyService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Proxy POST request to Python game server
     */
    public Map proxyPost(String endpoint) {
        try {
            String url = pythonServerUrl + endpoint;
            logger.info("Proxying POST request to: {}", url);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<String> entity = new HttpEntity<>("{}", headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Map.class);

            response.getBody();
            return response.getBody();
        } catch (RestClientException e) {
            logger.error("Failed to proxy POST to {}: {}", endpoint, e.getMessage());
            return createMockResponse(endpoint, "POST");
        }
    }

    /**
     * Proxy GET request to Python game server
     */
    public Object proxyGet(String endpoint) {
        try {
            String url = pythonServerUrl + endpoint;
            logger.info("Proxying GET request to: {}", url);

            ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
            return response.getBody();
        } catch (RestClientException e) {
            logger.error("Failed to proxy GET to {}: {}", endpoint, e.getMessage());
            return createMockResponse(endpoint, "GET");
        }
    }

    /**
     * Create mock response when Python server is not available
     */
    private Map<String, Object> createMockResponse(String endpoint, String method) {
        Map<String, Object> mock = new HashMap<>();
        mock.put("mock", true);
        mock.put("message", "Python server not available, returning mock data");

        switch (endpoint) {
            case "/internal/game/start":
                mock.put("status", "STARTED");
                mock.put("tick", 1);
                break;
            case "/internal/game/stop":
                mock.put("status", "STOPPED");
                break;
            case "/internal/game/status":
                mock.put("running", false);
                mock.put("current_tick", 0);
                break;
            case "/api/scoreboard":
                return Map.of(
                        "mock", true,
                        "scoreboard", List.of(
                                Map.of("rank", 1, "team", "Team Demo", "score", 0)));
            default:
                mock.put("endpoint", endpoint);
        }

        return mock;
    }

    // === Game Control Methods ===

    public Map<String, Object> startGame() {
        return proxyPost("/internal/game/start");
    }

    public Map<String, Object> stopGame() {
        return proxyPost("/internal/game/stop");
    }

    public Object getGameStatus() {
        return proxyGet("/internal/game/status");
    }

    // === Scoreboard Method ===

    public Object getScoreboard() {
        return proxyGet("/api/scoreboard");
    }
}
