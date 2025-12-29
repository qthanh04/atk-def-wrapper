package com.tool.atkdefbackend.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamInfoResponse {
    private Integer id;
    private String username;
    private String teamName;
    private String role;
}
