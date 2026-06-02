package com.waypoint.repository;

import com.waypoint.entity.RoutePoint;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RoutePointRepository extends JpaRepository<RoutePoint, Long> {
    List<RoutePoint> findByRouteIdOrderBySequenceNumberAsc(Long routeId);
    void deleteByRouteId(Long routeId);
}
