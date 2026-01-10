package com.tool.atkdefbackend.exception;

/**
 * Custom exception for forbidden access
 * Thrown when user is authenticated but lacks required permissions
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
