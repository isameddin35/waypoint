package com.waypoint.service;

import com.waypoint.dto.RouteResponse;
import com.waypoint.dto.UserResponse;
import com.waypoint.dto.WildlifeReportResponse;
import com.waypoint.entity.Route;
import com.waypoint.mapper.EntityMapper;
import com.waypoint.repository.RouteRepository;
import com.waypoint.repository.UserRepository;
import com.waypoint.repository.WildlifeReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final RouteRepository routeRepository;
    private final WildlifeReportRepository wildlifeReportRepository;
    private final EntityMapper entityMapper;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(entityMapper::toUserResponse)
                .toList();
    }

    public List<RouteResponse> getAllRoutes() {
        return routeRepository.findAll().stream()
                .map(entityMapper::toRouteResponse)
                .toList();
    }

    public List<WildlifeReportResponse> getAllReports() {
        return wildlifeReportRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(entityMapper::toWildlifeReportResponse)
                .toList();
    }

    public long getUserCount() {
        return userRepository.count();
    }

    public long getRouteCount() {
        return routeRepository.count();
    }

    public long getReportCount() {
        return wildlifeReportRepository.count();
    }
}
