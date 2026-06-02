package com.waypoint.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "route_points")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RoutePoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private Integer sequenceNumber;

    @Column
    private Double elevation;
}
