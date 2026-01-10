package com.tool.atkdefbackend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.tool.atkdefbackend.exception.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global Exception Handler
 * Centralized error handling with consistent JSON responses
 * 
 * Based on NewTech.md Section 6: Global Exception Handling
 * 
 * Error Response Format:
 * {
 * "success": false,
 * "error": "Human-readable error message",
 * "status": 400,
 * "timestamp": "2026-01-09T03:30:00",
 * "details": { ... }
 * }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Handle validation errors from @Valid annotations
     * HTTP 400 Bad Request
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        Map<String, Object> errorResponse = createErrorResponse(
                "Validation failed",
                HttpStatus.BAD_REQUEST.value(),
                fieldErrors);

        log.warn("Validation error: {}", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle business logic validation errors
     * HTTP 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> errorResponse = createErrorResponse(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                null);

        log.warn("Illegal argument: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle conflict errors (e.g., duplicate username)
     * HTTP 409 Conflict
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        Map<String, Object> errorResponse = createErrorResponse(
                ex.getMessage(),
                HttpStatus.CONFLICT.value(),
                null);

        log.warn("Illegal state: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Handle custom validation errors
     * HTTP 400 Bad Request
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(ValidationException ex) {
        Map<String, Object> errorResponse = createErrorResponse(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                null);

        log.warn("Validation exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle resource not found errors
     * HTTP 404 Not Found
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        Map<String, Object> errorResponse = createErrorResponse(
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                null);

        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handle duplicate resource errors
     * HTTP 409 Conflict
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateResource(DuplicateResourceException ex) {
        Map<String, Object> errorResponse = createErrorResponse(
                ex.getMessage(),
                HttpStatus.CONFLICT.value(),
                null);

        log.warn("Duplicate resource: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Handle custom unauthorized errors
     * HTTP 401 Unauthorized
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(UnauthorizedException ex) {
        Map<String, Object> errorResponse = createErrorResponse(
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED.value(),
                null);

        log.warn("Unauthorized: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handle custom forbidden errors
     * HTTP 403 Forbidden
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, Object>> handleForbidden(ForbiddenException ex) {
        Map<String, Object> errorResponse = createErrorResponse(
                ex.getMessage(),
                HttpStatus.FORBIDDEN.value(),
                null);

        log.warn("Forbidden: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Handle constraint violation errors (e.g., from @Validated on method
     * parameters)
     * HTTP 400 Bad Request
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> violations = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (existing, replacement) -> existing)); // Handle duplicate keys

        Map<String, Object> errorResponse = createErrorResponse(
                "Constraint violation",
                HttpStatus.BAD_REQUEST.value(),
                violations);

        log.warn("Constraint violation: {}", violations);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle JSON parsing errors
     * HTTP 400 Bad Request
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String errorMessage = "Invalid JSON format";

        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) cause;
            errorMessage = String.format("Invalid value '%s' for field '%s'",
                    ife.getValue(),
                    ife.getPath().get(0).getFieldName());
        } else if (cause instanceof MismatchedInputException) {
            MismatchedInputException mie = (MismatchedInputException) cause;
            if (!mie.getPath().isEmpty()) {
                errorMessage = String.format("Invalid input for field '%s'",
                        mie.getPath().get(0).getFieldName());
            }
        }

        Map<String, Object> errorResponse = createErrorResponse(
                errorMessage,
                HttpStatus.BAD_REQUEST.value(),
                null);

        log.warn("JSON parsing error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle missing request parameters
     * HTTP 400 Bad Request
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParams(MissingServletRequestParameterException ex) {
        Map<String, Object> errorResponse = createErrorResponse(
                String.format("Missing required parameter: '%s'", ex.getParameterName()),
                HttpStatus.BAD_REQUEST.value(),
                null);

        log.warn("Missing parameter: {}", ex.getParameterName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle type mismatch errors
     * HTTP 400 Bad Request
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String errorMessage = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
                ex.getValue(),
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

        Map<String, Object> errorResponse = createErrorResponse(
                errorMessage,
                HttpStatus.BAD_REQUEST.value(),
                null);

        log.warn("Type mismatch: {}", errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle login failures
     * HTTP 401 Unauthorized
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        Map<String, Object> errorResponse = createErrorResponse(
                "Invalid username or password",
                HttpStatus.UNAUTHORIZED.value(),
                null);

        log.warn("Authentication failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handle authorization failures
     * HTTP 403 Forbidden
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        Map<String, Object> errorResponse = createErrorResponse(
                "Access denied: " + ex.getMessage(),
                HttpStatus.FORBIDDEN.value(),
                null);

        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Handle WebClient errors from Python Core API
     * Maps HTTP status from the upstream service
     */
    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<Map<String, Object>> handleWebClientError(WebClientResponseException ex) {
        Map<String, Object> errorResponse;

        try {
            // Try to parse error response from Python Core
            @SuppressWarnings("unchecked")
            Map<String, Object> parsedError = objectMapper.readValue(
                    ex.getResponseBodyAsString(),
                    Map.class);
            parsedError.put("success", false);
            parsedError.put("status", ex.getStatusCode().value());
            parsedError.put("timestamp", LocalDateTime.now().toString());
            errorResponse = parsedError;
        } catch (Exception e) {
            // If parsing fails, create a generic error response
            errorResponse = createErrorResponse(
                    "Python Core API error: " + ex.getMessage(),
                    ex.getStatusCode().value(),
                    null);
        }

        log.error("WebClient error: {} - {}", ex.getStatusCode(), ex.getMessage());
        return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
    }

    /**
     * Handle connection errors to Python Core API
     * HTTP 503 Service Unavailable
     */
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<Map<String, Object>> handleResourceAccess(ResourceAccessException ex) {
        Map<String, Object> errorResponse = createErrorResponse(
                "Failed to connect to game server: " + ex.getMessage(),
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                null);

        log.error("Connection error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    /**
     * Handle all other unexpected errors
     * HTTP 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericError(Exception ex) {
        Map<String, Object> errorResponse = createErrorResponse(
                "Internal server error: " + ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                null);

        log.error("Unexpected error: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Create standardized error response
     */
    private Map<String, Object> createErrorResponse(String message, int status, Object details) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", message);
        errorResponse.put("status", status);
        errorResponse.put("timestamp", LocalDateTime.now().toString());

        if (details != null) {
            errorResponse.put("details", details);
        }

        return errorResponse;
    }
}
