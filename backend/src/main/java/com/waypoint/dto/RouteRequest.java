package com.waypoint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RouteRequest {
    @NotBlank
    private String name;

    private String description;

    @NotNull
    private String difficulty;

    @NotNull @Positive
    private Double distanceKm;

    private Double elevationGain;
    private Double startLatitude;
    private Double startLongitude;
    private Double endLatitude;
    private Double endLongitude;
}
