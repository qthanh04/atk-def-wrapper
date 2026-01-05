package com.tool.atkdefbackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j // 1. Tự động sinh logger
@Service
@RequiredArgsConstructor // 2. Tự động sinh Constructor injection
public class PythonProxyService {

    // Lỗi này xảy ra vì Spring Boot không tự động tạo Bean RestTemplate mặc định.
    // Bạn cần phải tự khai báo nó.
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper; // 3. Dùng chung ObjectMapper của Spring

    @Value("${python.server-url:http://localhost:8000}")
    private String pythonServerUrl;

    /**
     * Proxy POST request.
     * LƯU Ý: Để an toàn, responseType nên là Map.class hoặc Object.class
     * nếu bạn muốn giữ logic "trả về Map lỗi khi backend fail".
     */
    public <T> T proxyPost(String endpoint, Object body, Class<T> responseType) {
        String url = pythonServerUrl + endpoint;
        log.info("Proxying POST request to: {}", url);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> entity = new HttpEntity<>(body == null ? Collections.emptyMap() : body, headers);

            return restTemplate.postForObject(url, entity, responseType);

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.warn("Python backend returned {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return handleBackendError(e.getResponseBodyAsString(), e.getStatusCode().value(), responseType);
        } catch (RestClientException e) {
            log.error("Failed to proxy POST to {}: {}", endpoint, e.getMessage());
            return createErrorResponse("Failed to connect to game server: " + e.getMessage(), 500, responseType);
        }
    }

    /**
     * Proxy POST request with Multipart/Form-Data (File Upload)
     */
    public <T> T proxyMultipartPost(String endpoint, org.springframework.util.MultiValueMap<String, Object> body,
            Class<T> responseType) {
        String url = pythonServerUrl + endpoint;
        log.info("Proxying Multipart POST request to: {}", url);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<Object> entity = new HttpEntity<>(body, headers);

            return restTemplate.postForObject(url, entity, responseType);

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.warn("Python backend returned {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return handleBackendError(e.getResponseBodyAsString(), e.getStatusCode().value(), responseType);
        } catch (RestClientException e) {
            log.error("Failed to proxy Multipart POST to {}: {}", endpoint, e.getMessage());
            return createErrorResponse("Failed to connect to game server: " + e.getMessage(), 500, responseType);
        }
    }

    /**
     * Generic proxy GET request
     */
    public <T> T proxyGet(String endpoint, Class<T> responseType) {
        String url = pythonServerUrl + endpoint;
        log.info("Proxying GET request to: {}", url);
        try {
            return restTemplate.getForObject(url, responseType);
        } catch (RestClientException e) {
            log.error("Failed to proxy GET to {}: {}", endpoint, e.getMessage());
            throw e; // Tốt nhất là ném lỗi ra để Controller xử lý
        }
    }

    /**
     * Generic proxy PATCH request
     */
    public <T> T proxyPatch(String endpoint, Object body, Class<T> responseType) {
        String url = pythonServerUrl + endpoint;
        log.info("Proxying PATCH request to: {}", url);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> entity = new HttpEntity<>(body == null ? Collections.emptyMap() : body, headers);

            ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.PATCH, entity, responseType);
            return response.getBody();

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.warn("Python backend returned {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return handleBackendError(e.getResponseBodyAsString(), e.getStatusCode().value(), responseType);
        } catch (RestClientException e) {
            log.error("Failed to proxy PATCH to {}: {}", endpoint, e.getMessage());
            return createErrorResponse("Failed to connect to game server: " + e.getMessage(), 500, responseType);
        }
    }

    /**
     * Generic proxy DELETE request
     */
    public <T> T proxyDelete(String endpoint, Class<T> responseType) {
        String url = pythonServerUrl + endpoint;
        log.info("Proxying DELETE request to: {}", url);

        try {
            ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.DELETE, null, responseType);
            return response.getBody();

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.warn("Python backend returned {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return handleBackendError(e.getResponseBodyAsString(), e.getStatusCode().value(), responseType);
        } catch (RestClientException e) {
            log.error("Failed to proxy DELETE to {}: {}", endpoint, e.getMessage());
            return createErrorResponse("Failed to connect to game server: " + e.getMessage(), 500, responseType);
        }
    }

    /**
     * Find the latest game using Java Streams
     */
    public Map<String, Object> getLatestGame() {
        try {
            // Dùng ParameterizedTypeReference để lấy đúng List<Map> thay vì Map chung chung
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    pythonServerUrl + "/games?limit=100",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    });

            Map<String, Object> body = response.getBody();
            if (body == null || !body.containsKey("games"))
                return null;

            List<Map<String, Object>> games = (List<Map<String, Object>>) body.get("games");

            // 4. Tối ưu logic sort bằng Stream
            return games.stream()
                    .filter(g -> g.get("created_at") != null) // Lọc game lỗi không có ngày
                    .max(Comparator.comparing(g -> (String) g.get("created_at"))) // Tìm cái mới nhất
                    .orElse(null);

        } catch (Exception e) {
            log.error("Error finding latest game: {}", e.getMessage());
            return null;
        }
    }

    // === Game Control Methods ===

    public Map<String, Object> startGame() {
        return controlGame("start");
    }

    public Map<String, Object> stopGame() {
        return controlGame("stop");
    }

    // Gộp logic start/stop để tránh lặp code
    private Map<String, Object> controlGame(String action) {
        Map<String, Object> game = getLatestGame();
        if (game == null) {
            throw new IllegalStateException("No active game found to " + action);
        }
        String gameId = (String) game.get("id");
        // Ép kiểu về Map.class an toàn vì proxyPost trả về Map khi lỗi
        return proxyPost("/games/" + gameId + "/" + action, null, Map.class);
    }

    public Object getGameStatus() {
        return getLatestGame();
    }

    public Object getScoreboard() {
        Map<String, Object> game = getLatestGame();
        if (game == null) {
            log.warn("No game found for scoreboard");
            return Map.of("error", "No active game", "success", false);
        }
        return proxyGet("/scoreboard/" + (String) game.get("id"), Map.class);
    }

    // === Helper Methods ===

    @SuppressWarnings("unchecked")
    private <T> T handleBackendError(String responseBody, int status, Class<T> responseType) {
        // Chỉ an toàn nếu T là Map hoặc Object
        if (!Map.class.isAssignableFrom(responseType) && !Object.class.equals(responseType)) {
            throw new RuntimeException("Backend Error: " + responseBody); // Không thể ép kiểu Map sang POJO được
        }

        try {
            Map<String, Object> errorMap = objectMapper.readValue(responseBody, Map.class);
            errorMap.put("status", status);
            errorMap.put("success", false);
            return (T) errorMap;
        } catch (Exception e) {
            return createErrorResponse(responseBody, status, responseType);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T createErrorResponse(String message, int status, Class<T> responseType) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        errorResponse.put("status", status);
        errorResponse.put("success", false);
        return (T) errorResponse;
    }
}