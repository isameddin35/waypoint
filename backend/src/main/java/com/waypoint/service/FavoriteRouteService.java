package com.waypoint.service;

import com.waypoint.dto.RouteResponse;
import com.waypoint.entity.FavoriteRoute;
import com.waypoint.entity.Route;
import com.waypoint.entity.User;
import com.waypoint.exception.ResourceNotFoundException;
import com.waypoint.mapper.EntityMapper;
import com.waypoint.repository.FavoriteRouteRepository;
import com.waypoint.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteRouteService {

    private final FavoriteRouteRepository favoriteRouteRepository;
    private final RouteRepository routeRepository;
    private final EntityMapper entityMapper;

    @Transactional
    public void toggleFavorite(Long routeId, Long userId) {
        if (favoriteRouteRepository.existsByUserIdAndRouteId(userId, routeId)) {
            favoriteRouteRepository.deleteByUserIdAndRouteId(userId, routeId);
        } else {
            Route route = routeRepository.findById(routeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Route not found"));

            FavoriteRoute favorite = FavoriteRoute.builder()
                    .user(User.builder().id(userId).build())
                    .route(route)
                    .build();
            favoriteRouteRepository.save(favorite);
        }
    }

    public boolean isFavorite(Long routeId, Long userId) {
        return favoriteRouteRepository.existsByUserIdAndRouteId(userId, routeId);
    }

    public List<RouteResponse> getUserFavorites(Long userId) {
        return favoriteRouteRepository.findByUserId(userId).stream()
                .map(fav -> entityMapper.toRouteResponse(fav.getRoute()))
                .toList();
    }
}
