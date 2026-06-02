package com.waypoint.repository;

import com.waypoint.entity.Difficulty;
import com.waypoint.entity.Route;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RouteRepository extends JpaRepository<Route, Long> {

    Page<Route> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Route> findByDifficulty(Difficulty difficulty, Pageable pageable);

    Page<Route> findByNameContainingIgnoreCaseAndDifficulty(
            String name, Difficulty difficulty, Pageable pageable);

    List<Route> findByCreatedById(Long userId);

    @Query("SELECT r FROM Route r ORDER BY r.createdAt DESC")
    List<Route> findTopRoutes(Pageable pageable);

    @Query("SELECT r FROM Route r WHERE " +
           "(:search IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:difficulty IS NULL OR r.difficulty = :difficulty)")
    Page<Route> searchRoutes(@Param("search") String search,
                             @Param("difficulty") Difficulty difficulty,
                             Pageable pageable);

    long count();
}
