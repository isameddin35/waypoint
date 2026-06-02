package com.waypoint.repository;

import com.waypoint.entity.FavoriteRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FavoriteRouteRepository extends JpaRepository<FavoriteRoute, Long> {
    List<FavoriteRoute> findByUserId(Long userId);
    Optional<FavoriteRoute> findByUserIdAndRouteId(Long userId, Long routeId);
    boolean existsByUserIdAndRouteId(Long userId, Long routeId);
    void deleteByUserIdAndRouteId(Long userId, Long routeId);
}
