package com.tool.atkdefbackend.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamSignUpRequest {

    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;

    @NotBlank
    @Size(min = 2, max = 100)
    private String teamName;

    @Size(max = 200)
    private String affiliation;

    @Size(max = 50)
    private String country;

    @Size(max = 20)
    private String role;
}
