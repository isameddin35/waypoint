package com.waypoint.repository;

import com.waypoint.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
    List<Photo> findByRouteId(Long routeId);
}
