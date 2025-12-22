package com.tool.atkdefbackend.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeamResponse {
    private Integer id;
    private String name;
    private String country;
    private String affiliation;
    private String ipAddress;
}
