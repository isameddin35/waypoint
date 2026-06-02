package com.waypoint.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RoutePointRequest {
    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    @NotNull
    private Integer sequenceNumber;

    private Double elevation;
}
