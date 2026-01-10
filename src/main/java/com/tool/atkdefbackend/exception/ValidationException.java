package com.tool.atkdefbackend.exception;

/**
 * Custom exception for business validation errors
 * Used when data passes basic validation but fails business rules
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
