package com.waypoint.security;

import lombok.*;

@Getter @AllArgsConstructor
public class UserPrincipal {
    private String username;
    private Long userId;
    private String role;
}
