package com.waypoint.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WildlifeReportResponse {
    private Long id;
    private Long routeId;
    private String routeName;
    private Long userId;
    private String username;
    private String species;
    private String description;
    private Double latitude;
    private Double longitude;
    private LocalDateTime createdAt;
}
