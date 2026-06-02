package com.waypoint.service;

import com.waypoint.dto.WildlifeReportRequest;
import com.waypoint.dto.WildlifeReportResponse;
import com.waypoint.entity.Route;
import com.waypoint.entity.User;
import com.waypoint.entity.WildlifeReport;
import com.waypoint.exception.ResourceNotFoundException;
import com.waypoint.mapper.EntityMapper;
import com.waypoint.repository.RouteRepository;
import com.waypoint.repository.WildlifeReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WildlifeReportService {

    private final WildlifeReportRepository wildlifeReportRepository;
    private final RouteRepository routeRepository;
    private final EntityMapper entityMapper;

    @Transactional
    public WildlifeReportResponse createReport(WildlifeReportRequest request, Long userId) {
        Route route = routeRepository.findById(request.getRouteId())
                .orElseThrow(() -> new ResourceNotFoundException("Route not found"));

        WildlifeReport report = WildlifeReport.builder()
                .route(route)
                .user(User.builder().id(userId).build())
                .species(request.getSpecies())
                .description(request.getDescription())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();

        report = wildlifeReportRepository.save(report);
        return entityMapper.toWildlifeReportResponse(report);
    }

    public List<WildlifeReportResponse> getReportsByRoute(Long routeId) {
        return wildlifeReportRepository.findByRouteIdOrderByCreatedAtDesc(routeId).stream()
                .map(entityMapper::toWildlifeReportResponse)
                .toList();
    }

    public List<WildlifeReportResponse> getAllReports() {
        return wildlifeReportRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(entityMapper::toWildlifeReportResponse)
                .toList();
    }
}
