package com.tool.atkdefbackend.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating team information
 * All fields are optional for partial updates
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateTeamRequest {

    @Size(min = 2, max = 100, message = "Team name must be between 2 and 100 characters")
    private String name;

    @Size(max = 50, message = "Country name cannot exceed 50 characters")
    private String country;

    @Size(max = 200, message = "Affiliation cannot exceed 200 characters")
    private String affiliation;

    @JsonProperty("ip_address")
    @Pattern(
        regexp = "^$|^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$",
        message = "Invalid IP address format. Must be a valid IPv4 address or empty"
    )
    private String ipAddress;
}
