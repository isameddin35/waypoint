package com.waypoint.service;

import com.waypoint.dto.ReviewResponse;
import com.waypoint.mapper.EntityMapper;
import com.waypoint.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final ReviewRepository reviewRepository;
    private final EntityMapper entityMapper;

    public List<ReviewResponse> getUserReviews(Long userId) {
        return reviewRepository.findByUserId(userId).stream()
                .map(entityMapper::toReviewResponse)
                .toList();
    }
}
