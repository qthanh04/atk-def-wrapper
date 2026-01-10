package com.tool.atkdefbackend.validator;

import com.tool.atkdefbackend.exception.ValidationException;
import com.tool.atkdefbackend.model.request.SignUpRequest;
import com.tool.atkdefbackend.model.request.TeamSignUpRequest;
import org.springframework.stereotype.Component;

/**
 * Custom validator for request DTOs
 * Performs business logic validation beyond basic field constraints
 */
@Component
public class RequestValidator {

    /**
     * Validate SignUpRequest for password matching
     */
    public void validateSignUpRequest(SignUpRequest request) {
        if (request.getPassword() == null || request.getConfirmPassword() == null) {
            throw new ValidationException("Password and confirmation password are required");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new ValidationException("Password and confirmation password do not match");
        }
    }

    /**
     * Validate TeamSignUpRequest
     */
    public void validateTeamSignUpRequest(TeamSignUpRequest request) {
        // Password validation is already handled by annotations
        // Add any additional business logic validation here

        if (request.getRole() != null && !isValidRole(request.getRole())) {
            throw new ValidationException("Invalid role. Must be one of: STUDENT, TEACHER, TEAM, ADMIN");
        }
    }

    /**
     * Validate role value
     */
    private boolean isValidRole(String role) {
        return role.matches("^(STUDENT|TEACHER|TEAM|ADMIN)$");
    }

    /**
     * Validate IP address format (additional validation beyond regex)
     */
    public void validateIpAddress(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            return; // IP address is optional
        }

        String[] parts = ipAddress.split("\\.");
        if (parts.length != 4) {
            throw new ValidationException("IP address must have 4 octets");
        }

        for (String part : parts) {
            try {
                int value = Integer.parseInt(part);
                if (value < 0 || value > 255) {
                    throw new ValidationException("Each octet in IP address must be between 0 and 255");
                }
            } catch (NumberFormatException e) {
                throw new ValidationException("IP address octets must be numeric");
            }
        }
    }

    /**
     * Validate team name for forbidden characters or patterns
     */
    public void validateTeamName(String teamName) {
        if (teamName == null || teamName.trim().isEmpty()) {
            throw new ValidationException("Team name cannot be empty");
        }

        // Check for forbidden patterns
        if (teamName.toLowerCase().contains("admin") ||
                teamName.toLowerCase().contains("system")) {
            throw new ValidationException("Team name cannot contain reserved words like 'admin' or 'system'");
        }

        // Check for SQL injection patterns
        if (teamName.matches(".*[';\"\\-\\-].*")) {
            throw new ValidationException("Team name contains invalid characters");
        }
    }

    /**
     * Validate username for security
     */
    public void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Username cannot be empty");
        }

        // Check for reserved usernames
        String lowerUsername = username.toLowerCase();
        if (lowerUsername.equals("admin") ||
                lowerUsername.equals("root") ||
                lowerUsername.equals("system") ||
                lowerUsername.equals("administrator")) {
            throw new ValidationException("This username is reserved and cannot be used");
        }
    }
}
