package com.waypoint.controller;

import com.waypoint.dto.RouteResponse;
import com.waypoint.dto.UserResponse;
import com.waypoint.dto.WildlifeReportResponse;
import com.waypoint.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/routes")
    public ResponseEntity<List<RouteResponse>> getAllRoutes() {
        return ResponseEntity.ok(adminService.getAllRoutes());
    }

    @GetMapping("/reports")
    public ResponseEntity<List<WildlifeReportResponse>> getAllReports() {
        return ResponseEntity.ok(adminService.getAllReports());
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        return ResponseEntity.ok(Map.of(
                "users", adminService.getUserCount(),
                "routes", adminService.getRouteCount(),
                "reports", adminService.getReportCount()));
    }
}
