package com.waypoint.controller;

import com.waypoint.dto.RouteResponse;
import com.waypoint.security.UserPrincipal;
import com.waypoint.service.FavoriteRouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteRouteService favoriteRouteService;

    @PostMapping("/routes/{routeId}/favorite")
    public ResponseEntity<Void> toggleFavorite(
            @PathVariable Long routeId,
            @AuthenticationPrincipal UserPrincipal principal) {
        favoriteRouteService.toggleFavorite(routeId, principal.getUserId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/routes/{routeId}/favorite")
    public ResponseEntity<Map<String, Boolean>> checkFavorite(
            @PathVariable Long routeId,
            @AuthenticationPrincipal UserPrincipal principal) {
        boolean isFav = favoriteRouteService.isFavorite(routeId, principal.getUserId());
        return ResponseEntity.ok(Map.of("favorite", isFav));
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<RouteResponse>> getFavorites(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(favoriteRouteService.getUserFavorites(principal.getUserId()));
    }
}
