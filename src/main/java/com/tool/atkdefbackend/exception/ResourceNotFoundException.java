package com.tool.atkdefbackend.exception;

/**
 * Custom exception for resource not found errors
 * Thrown when a requested resource (team, user, etc.) does not exist
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
    }
}
