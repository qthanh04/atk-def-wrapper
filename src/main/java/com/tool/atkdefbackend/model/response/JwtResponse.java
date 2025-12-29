package com.tool.atkdefbackend.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Integer id;
    private String username;
    private String teamName;
    private List<String> roles;

    public JwtResponse(String token, Integer id, String username, String teamName, List<String> roles) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.teamName = teamName;
        this.roles = roles;
    }
}
