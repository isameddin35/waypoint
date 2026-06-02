package com.waypoint.service;

import com.waypoint.dto.*;
import com.waypoint.entity.*;
import com.waypoint.exception.ResourceNotFoundException;
import com.waypoint.exception.UnauthorizedException;
import com.waypoint.mapper.EntityMapper;
import com.waypoint.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;
    private final EntityMapper entityMapper;

    public PagedResponse<RouteResponse> getAllRoutes(int page, int size, String sortBy,
                                                     String sortDir, String search, String difficulty) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Difficulty diff = null;
        if (difficulty != null && !difficulty.isEmpty()) {
            diff = Difficulty.valueOf(difficulty.toUpperCase());
        }

        Page<Route> routePage;
        if ((search == null || search.isEmpty()) && diff == null) {
            routePage = routeRepository.findAll(pageable);
        } else if (diff == null) {
            routePage = routeRepository.findByNameContainingIgnoreCase(search, pageable);
        } else if (search == null || search.isEmpty()) {
            routePage = routeRepository.findByDifficulty(diff, pageable);
        } else {
            routePage = routeRepository.findByNameContainingIgnoreCaseAndDifficulty(search, diff, pageable);
        }

        List<RouteResponse> routes = routePage.getContent().stream()
                .map(entityMapper::toRouteResponse)
                .toList();

        return PagedResponse.<RouteResponse>builder()
                .content(routes)
                .page(routePage.getNumber())
                .size(routePage.getSize())
                .totalElements(routePage.getTotalElements())
                .totalPages(routePage.getTotalPages())
                .last(routePage.isLast())
                .build();
    }

    public RouteResponse getRouteById(Long id) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with id: " + id));
        return entityMapper.toRouteResponse(route);
    }

    @Transactional
    public RouteResponse createRoute(RouteRequest request, Long userId) {
        Route route = Route.builder()
                .name(request.getName())
                .description(request.getDescription())
                .difficulty(Difficulty.valueOf(request.getDifficulty().toUpperCase()))
                .distanceKm(request.getDistanceKm())
                .elevationGain(request.getElevationGain())
                .startLatitude(request.getStartLatitude())
                .startLongitude(request.getStartLongitude())
                .endLatitude(request.getEndLatitude())
                .endLongitude(request.getEndLongitude())
                .createdBy(User.builder().id(userId).build())
                .build();

        route = routeRepository.save(route);
        return entityMapper.toRouteResponse(route);
    }

    @Transactional
    public RouteResponse updateRoute(Long id, RouteRequest request, Long userId, String role) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with id: " + id));

        if (!route.getCreatedBy().getId().equals(userId) && !role.equals("ADMIN")) {
            throw new UnauthorizedException("You are not authorized to update this route");
        }

        route.setName(request.getName());
        route.setDescription(request.getDescription());
        route.setDifficulty(Difficulty.valueOf(request.getDifficulty().toUpperCase()));
        route.setDistanceKm(request.getDistanceKm());
        route.setElevationGain(request.getElevationGain());
        route.setStartLatitude(request.getStartLatitude());
        route.setStartLongitude(request.getStartLongitude());
        route.setEndLatitude(request.getEndLatitude());
        route.setEndLongitude(request.getEndLongitude());

        route = routeRepository.save(route);
        return entityMapper.toRouteResponse(route);
    }

    @Transactional
    public void deleteRoute(Long id, Long userId, String role) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with id: " + id));

        if (!route.getCreatedBy().getId().equals(userId) && !role.equals("ADMIN")) {
            throw new UnauthorizedException("You are not authorized to delete this route");
        }

        routeRepository.delete(route);
    }

    public List<RouteResponse> getRoutesByUser(Long userId) {
        return routeRepository.findByCreatedById(userId).stream()
                .map(entityMapper::toRouteResponse)
                .toList();
    }

    public List<RouteResponse> getFeaturedRoutes() {
        return routeRepository.findTopRoutes(PageRequest.of(0, 6)).stream()
                .map(entityMapper::toRouteResponse)
                .toList();
    }

    public long getRouteCount() {
        return routeRepository.count();
    }
}
