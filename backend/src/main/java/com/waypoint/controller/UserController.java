package com.waypoint.controller;

import com.waypoint.dto.RouteResponse;
import com.waypoint.dto.ReviewResponse;
import com.waypoint.security.UserPrincipal;
import com.waypoint.service.RouteService;
import com.waypoint.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final RouteService routeService;
    private final UserService userService;

    @GetMapping("/routes")
    public ResponseEntity<List<RouteResponse>> getUserRoutes(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(routeService.getRoutesByUser(principal.getUserId()));
    }

    @GetMapping("/reviews")
    public ResponseEntity<List<ReviewResponse>> getUserReviews(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(userService.getUserReviews(principal.getUserId()));
    }
}
