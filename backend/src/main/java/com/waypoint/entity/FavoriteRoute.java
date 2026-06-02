package com.waypoint.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "favorite_routes",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "route_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FavoriteRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;
}
