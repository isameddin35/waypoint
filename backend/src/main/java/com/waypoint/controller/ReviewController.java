package com.waypoint.controller;

import com.waypoint.dto.ReviewRequest;
import com.waypoint.dto.ReviewResponse;
import com.waypoint.security.UserPrincipal;
import com.waypoint.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes/{routeId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable Long routeId,
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.createReview(routeId, request, principal.getUserId()));
    }

    @GetMapping
    public ResponseEntity<List<ReviewResponse>> getReviews(@PathVariable Long routeId) {
        return ResponseEntity.ok(reviewService.getReviewsByRoute(routeId));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserPrincipal principal) {
        reviewService.deleteReview(reviewId, principal.getRole());
        return ResponseEntity.noContent().build();
    }
}
