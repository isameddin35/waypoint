package com.waypoint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class WildlifeReportRequest {
    @NotNull
    private Long routeId;

    @NotBlank
    private String species;

    private String description;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;
}
