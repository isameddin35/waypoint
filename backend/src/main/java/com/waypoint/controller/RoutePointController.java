package com.waypoint.controller;

import com.waypoint.dto.RoutePointRequest;
import com.waypoint.dto.RoutePointResponse;
import com.waypoint.service.ElevationService;
import com.waypoint.service.RoutePointService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes/{routeId}/points")
@RequiredArgsConstructor
public class RoutePointController {

    private final RoutePointService routePointService;
    private final ElevationService elevationService;

    @PostMapping
    public ResponseEntity<RoutePointResponse> addPoint(
            @PathVariable Long routeId,
            @Valid @RequestBody RoutePointRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(routePointService.addPoint(routeId, request));
    }

    @PostMapping("/batch")
    public ResponseEntity<List<RoutePointResponse>> addPoints(
            @PathVariable Long routeId,
            @Valid @RequestBody List<RoutePointRequest> requests) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(routePointService.addPoints(routeId, requests));
    }

    @GetMapping
    public ResponseEntity<List<RoutePointResponse>> getPoints(@PathVariable Long routeId) {
        return ResponseEntity.ok(routePointService.getPointsByRouteId(routeId));
    }

    @PostMapping("/elevation/fetch")
    public ResponseEntity<List<RoutePointResponse>> fetchElevation(@PathVariable Long routeId) {
        return ResponseEntity.ok(elevationService.fetchAndStoreElevation(routeId));
    }
}
