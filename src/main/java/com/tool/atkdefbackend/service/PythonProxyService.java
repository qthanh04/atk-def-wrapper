package com.tool.atkdefbackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class PythonProxyService {

    private static final Logger logger = LoggerFactory.getLogger(PythonProxyService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${python.server-url:http://localhost:8000}")
    private String pythonServerUrl;

    private final RestTemplate restTemplate;

    public PythonProxyService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Generic proxy POST request - handles Python backend errors gracefully
     */
    @SuppressWarnings("unchecked")
    public <T> T proxyPost(String endpoint, Object body, Class<T> responseType) {
        String url = pythonServerUrl + endpoint;
        logger.info("Proxying POST request to: {}", url);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<Object> entity = new HttpEntity<>(body == null ? Collections.emptyMap() : body, headers);

            ResponseEntity<T> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    responseType);

            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // Handle 4xx/5xx from Python backend - return error as Map
            logger.warn("Python backend returned {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            try {
                // Try to parse the error response JSON
                Map<String, Object> errorResponse = objectMapper.readValue(e.getResponseBodyAsString(), Map.class);
                errorResponse.put("status", e.getStatusCode().value());
                errorResponse.put("success", false);
                return (T) errorResponse;
            } catch (Exception parseError) {
                // Return raw error if JSON parsing fails
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", e.getResponseBodyAsString());
                errorResponse.put("status", e.getStatusCode().value());
                errorResponse.put("success", false);
                return (T) errorResponse;
            }
        } catch (RestClientException e) {
            logger.error("Failed to proxy POST to {}: {}", endpoint, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to connect to game server: " + e.getMessage());
            errorResponse.put("success", false);
            return (T) errorResponse;
        }
    }

    /**
     * Generic proxy GET request
     */
    public <T> T proxyGet(String endpoint, Class<T> responseType) {
        try {
            String url = pythonServerUrl + endpoint;
            logger.info("Proxying GET request to: {}", url);

            ResponseEntity<T> response = restTemplate.getForEntity(url, responseType);
            return response.getBody();
        } catch (RestClientException e) {
            logger.error("Failed to proxy GET to {}: {}", endpoint, e.getMessage());
            throw e;
        }
    }

    /**
     * Find the latest game (by created_at) to interact with.
     */
    public Map<String, Object> getLatestGame() {
        try {
            Map response = proxyGet("/games?limit=100", Map.class);
            if (response == null || !response.containsKey("games")) {
                return null;
            }

            List<Map<String, Object>> games = (List<Map<String, Object>>) response.get("games");
            if (games.isEmpty()) {
                return null;
            }

            // Sort by created_at descending
            games.sort((g1, g2) -> {
                String t1 = (String) g1.get("created_at");
                String t2 = (String) g2.get("created_at");
                // Handle nulls safely
                if (t1 == null)
                    return 1;
                if (t2 == null)
                    return -1;
                return t2.compareTo(t1);
            });

            return games.get(0);
        } catch (Exception e) {
            logger.error("Error finding latest game: {}", e.getMessage());
            return null;
        }
    }

    // === Game Control Methods ===

    public Map<String, Object> startGame() {
        Map<String, Object> game = getLatestGame();
        if (game == null) {
            throw new RuntimeException("No active game found to start.");
        }
        String gameId = (String) game.get("id");
        return proxyPost("/games/" + gameId + "/start", null, Map.class);
    }

    public Map<String, Object> stopGame() {
        Map<String, Object> game = getLatestGame();
        if (game == null) {
            throw new RuntimeException("No active game found to stop.");
        }
        String gameId = (String) game.get("id");
        return proxyPost("/games/" + gameId + "/stop", null, Map.class);
    }

    public Object getGameStatus() {
        return getLatestGame();
    }

    // === Scoreboard Method ===

    public Object getScoreboard() {
        Map<String, Object> game = getLatestGame();
        if (game == null) {
            logger.warn("No game found for scoreboard");
            return Map.of("error", "No active game");
        }
        String gameId = (String) game.get("id");
        return proxyGet("/scoreboard/" + gameId, Map.class);
    }
}
