package com.tool.atkdefbackend.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
@Data
public class JwtReponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private Set<String> roles;

    public JwtReponse(String token, Long id, String username, String email, List<String> roles) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = new HashSet<>(roles);
    }

    public JwtReponse(String token, String type, Long id, String username, String email, Set<String> roles) {
        this.token = token;
        this.type = type;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }
}
