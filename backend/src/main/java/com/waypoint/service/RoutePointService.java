package com.waypoint.service;

import com.waypoint.dto.RoutePointRequest;
import com.waypoint.dto.RoutePointResponse;
import com.waypoint.entity.Route;
import com.waypoint.entity.RoutePoint;
import com.waypoint.exception.ResourceNotFoundException;
import com.waypoint.mapper.EntityMapper;
import com.waypoint.repository.RoutePointRepository;
import com.waypoint.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoutePointService {

    private final RoutePointRepository routePointRepository;
    private final RouteRepository routeRepository;
    private final EntityMapper entityMapper;

    @Transactional
    public RoutePointResponse addPoint(Long routeId, RoutePointRequest request) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found"));

        RoutePoint point = RoutePoint.builder()
                .route(route)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .sequenceNumber(request.getSequenceNumber())
                .elevation(request.getElevation())
                .build();

        point = routePointRepository.save(point);
        return entityMapper.toRoutePointResponse(point);
    }

    public List<RoutePointResponse> getPointsByRouteId(Long routeId) {
        return routePointRepository.findByRouteIdOrderBySequenceNumberAsc(routeId).stream()
                .map(entityMapper::toRoutePointResponse)
                .toList();
    }

    @Transactional
    public List<RoutePointResponse> addPoints(Long routeId, List<RoutePointRequest> requests) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found"));

        routePointRepository.deleteByRouteId(routeId);

        List<RoutePoint> points = requests.stream()
                .map(req -> RoutePoint.builder()
                        .route(route)
                        .latitude(req.getLatitude())
                        .longitude(req.getLongitude())
                        .sequenceNumber(req.getSequenceNumber())
                        .elevation(req.getElevation())
                        .build())
                .toList();

        points = routePointRepository.saveAll(points);
        return points.stream()
                .map(entityMapper::toRoutePointResponse)
                .toList();
    }
}
