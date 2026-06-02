package com.waypoint.mapper;

import com.waypoint.dto.*;
import com.waypoint.entity.*;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EntityMapper {

    public RouteResponse toRouteResponse(Route route) {
        List<RoutePointResponse> points = route.getRoutePoints() != null
                ? route.getRoutePoints().stream().map(this::toRoutePointResponse).toList()
                : Collections.emptyList();

        List<String> photoUrls = route.getPhotos() != null
                ? route.getPhotos().stream().map(Photo::getFilePath).toList()
                : Collections.emptyList();

        return RouteResponse.builder()
                .id(route.getId())
                .name(route.getName())
                .description(route.getDescription())
                .difficulty(route.getDifficulty().name())
                .distanceKm(route.getDistanceKm())
                .elevationGain(route.getElevationGain())
                .startLatitude(route.getStartLatitude())
                .startLongitude(route.getStartLongitude())
                .endLatitude(route.getEndLatitude())
                .endLongitude(route.getEndLongitude())
                .averageRating(route.getAverageRating())
                .reviewCount(route.getReviewCount())
                .createdAt(route.getCreatedAt())
                .createdByUsername(route.getCreatedBy().getUsername())
                .createdById(route.getCreatedBy().getId())
                .routePoints(points)
                .photoUrls(photoUrls)
                .build();
    }

    public RoutePointResponse toRoutePointResponse(RoutePoint point) {
        return RoutePointResponse.builder()
                .id(point.getId())
                .latitude(point.getLatitude())
                .longitude(point.getLongitude())
                .sequenceNumber(point.getSequenceNumber())
                .elevation(point.getElevation())
                .build();
    }

    public ReviewResponse toReviewResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .username(review.getUser().getUsername())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }

    public WildlifeReportResponse toWildlifeReportResponse(WildlifeReport report) {
        return WildlifeReportResponse.builder()
                .id(report.getId())
                .routeId(report.getRoute().getId())
                .routeName(report.getRoute().getName())
                .userId(report.getUser().getId())
                .username(report.getUser().getUsername())
                .species(report.getSpecies())
                .description(report.getDescription())
                .latitude(report.getLatitude())
                .longitude(report.getLongitude())
                .createdAt(report.getCreatedAt())
                .build();
    }

    public PhotoResponse toPhotoResponse(Photo photo) {
        return PhotoResponse.builder()
                .id(photo.getId())
                .routeId(photo.getRoute().getId())
                .filePath(photo.getFilePath())
                .fileUrl("/api/files/" + photo.getFilePath())
                .uploadedByUsername(photo.getUploadedBy().getUsername())
                .uploadedAt(photo.getUploadedAt())
                .build();
    }

    public UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
