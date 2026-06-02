package com.waypoint.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JwtResponse {
    private String token;
    private String username;
    private String email;
    private String role;
    private Long id;
}
