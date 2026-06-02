package com.waypoint.controller;

import com.waypoint.dto.WildlifeReportRequest;
import com.waypoint.dto.WildlifeReportResponse;
import com.waypoint.security.UserPrincipal;
import com.waypoint.service.WildlifeReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WildlifeController {

    private final WildlifeReportService wildlifeReportService;

    @PostMapping("/wildlife")
    public ResponseEntity<WildlifeReportResponse> createReport(
            @Valid @RequestBody WildlifeReportRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(wildlifeReportService.createReport(request, principal.getUserId()));
    }

    @GetMapping("/wildlife")
    public ResponseEntity<List<WildlifeReportResponse>> getAllReports() {
        return ResponseEntity.ok(wildlifeReportService.getAllReports());
    }

    @GetMapping("/routes/{routeId}/wildlife")
    public ResponseEntity<List<WildlifeReportResponse>> getReportsByRoute(
            @PathVariable Long routeId) {
        return ResponseEntity.ok(wildlifeReportService.getReportsByRoute(routeId));
    }
}
