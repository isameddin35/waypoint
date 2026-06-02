package com.waypoint.service;

import com.waypoint.dto.ReviewRequest;
import com.waypoint.dto.ReviewResponse;
import com.waypoint.entity.Route;
import com.waypoint.entity.Review;
import com.waypoint.entity.User;
import com.waypoint.exception.ResourceNotFoundException;
import com.waypoint.mapper.EntityMapper;
import com.waypoint.repository.ReviewRepository;
import com.waypoint.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RouteRepository routeRepository;
    private final EntityMapper entityMapper;

    @Transactional
    public ReviewResponse createReview(Long routeId, ReviewRequest request, Long userId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found"));

        Review review = Review.builder()
                .route(route)
                .user(User.builder().id(userId).build())
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        review = reviewRepository.save(review);
        return entityMapper.toReviewResponse(review);
    }

    public List<ReviewResponse> getReviewsByRoute(Long routeId) {
        return reviewRepository.findByRouteIdOrderByCreatedAtDesc(routeId).stream()
                .map(entityMapper::toReviewResponse)
                .toList();
    }

    @Transactional
    public void deleteReview(Long reviewId, String role) {
        if (!role.equals("ADMIN")) {
            throw new RuntimeException("Only admins can delete reviews");
        }
        reviewRepository.deleteById(reviewId);
    }
}
