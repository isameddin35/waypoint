package com.waypoint.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RouteResponse {
    private Long id;
    private String name;
    private String description;
    private String difficulty;
    private Double distanceKm;
    private Double elevationGain;
    private Double startLatitude;
    private Double startLongitude;
    private Double endLatitude;
    private Double endLongitude;
    private Double averageRating;
    private Integer reviewCount;
    private LocalDateTime createdAt;
    private String createdByUsername;
    private Long createdById;
    private List<RoutePointResponse> routePoints;
    private List<String> photoUrls;
}
