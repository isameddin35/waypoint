package com.waypoint.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RoutePointResponse {
    private Long id;
    private Double latitude;
    private Double longitude;
    private Integer sequenceNumber;
    private Double elevation;
}
