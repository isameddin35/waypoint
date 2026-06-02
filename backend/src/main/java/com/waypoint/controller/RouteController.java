package com.waypoint.controller;

import com.waypoint.dto.*;
import com.waypoint.security.UserPrincipal;
import com.waypoint.service.RouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @GetMapping
    public ResponseEntity<PagedResponse<RouteResponse>> getAllRoutes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String difficulty) {
        return ResponseEntity.ok(
                routeService.getAllRoutes(page, size, sortBy, sortDir, search, difficulty));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RouteResponse> getRouteById(@PathVariable Long id) {
        return ResponseEntity.ok(routeService.getRouteById(id));
    }

    @PostMapping
    public ResponseEntity<RouteResponse> createRoute(
            @Valid @RequestBody RouteRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(routeService.createRoute(request, principal.getUserId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RouteResponse> updateRoute(
            @PathVariable Long id,
            @Valid @RequestBody RouteRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                routeService.updateRoute(id, request, principal.getUserId(), principal.getRole()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoute(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        routeService.deleteRoute(id, principal.getUserId(), principal.getRole());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/featured")
    public ResponseEntity<?> getFeaturedRoutes() {
        return ResponseEntity.ok(routeService.getFeaturedRoutes());
    }

    @GetMapping("/count")
    public ResponseEntity<?> getRouteCount() {
        return ResponseEntity.ok(routeService.getRouteCount());
    }
}
