package com.waypoint.repository;

import com.waypoint.entity.WildlifeReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WildlifeReportRepository extends JpaRepository<WildlifeReport, Long> {
    List<WildlifeReport> findByRouteIdOrderByCreatedAtDesc(Long routeId);
    List<WildlifeReport> findAllByOrderByCreatedAtDesc();
}
