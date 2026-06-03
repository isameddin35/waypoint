package com.waypoint.service;

import com.waypoint.dto.*;
import com.waypoint.entity.*;
import com.waypoint.exception.ResourceNotFoundException;
import com.waypoint.exception.UnauthorizedException;
import com.waypoint.mapper.EntityMapper;
import com.waypoint.repository.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteServiceTest {

    @Mock private RouteRepository routeRepository;
    @Mock private EntityMapper entityMapper;

    private RouteService routeService;

    private User creator;
    private Route route;
    private RouteResponse routeResponse;

    @BeforeEach
    void setUp() {
        routeService = new RouteService(routeRepository, entityMapper);

        creator = User.builder().id(1L).username("creator").build();
        route = Route.builder()
                .id(10L).name("Test Route").description("A test route")
                .difficulty(Difficulty.MEDIUM).distanceKm(12.5)
                .createdBy(creator)
                .build();
        routeResponse = RouteResponse.builder()
                .id(10L).name("Test Route").difficulty("MEDIUM")
                .distanceKm(12.5).createdById(1L).createdByUsername("creator")
                .build();
    }

    @Test
    void getRouteById_returnsRoute() {
        when(routeRepository.findById(10L)).thenReturn(Optional.of(route));
        when(entityMapper.toRouteResponse(route)).thenReturn(routeResponse);

        RouteResponse result = routeService.getRouteById(10L);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("Test Route");
    }

    @Test
    void getRouteById_throwsWhenNotFound() {
        when(routeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> routeService.getRouteById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Route not found with id: 99");
    }

    @Test
    void createRoute_savesAndReturnsRoute() {
        RouteRequest request = new RouteRequest();
        request.setName("New Route");
        request.setDifficulty("EASY");
        request.setDistanceKm(5.0);

        when(routeRepository.save(any(Route.class))).thenReturn(route);
        when(entityMapper.toRouteResponse(route)).thenReturn(routeResponse);

        RouteResponse result = routeService.createRoute(request, 1L);

        assertThat(result.getName()).isEqualTo("Test Route");
        verify(routeRepository).save(any(Route.class));
    }

    @Test
    void deleteRoute_deletesOwnRoute() {
        when(routeRepository.findById(10L)).thenReturn(Optional.of(route));

        routeService.deleteRoute(10L, 1L, "USER");

        verify(routeRepository).delete(route);
    }

    @Test
    void deleteRoute_throwsWhenNotOwner() {
        when(routeRepository.findById(10L)).thenReturn(Optional.of(route));

        assertThatThrownBy(() -> routeService.deleteRoute(10L, 2L, "USER"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("You are not authorized to delete this route");
    }

    @Test
    void deleteRoute_allowsAdmin() {
        when(routeRepository.findById(10L)).thenReturn(Optional.of(route));

        routeService.deleteRoute(10L, 2L, "ADMIN");

        verify(routeRepository).delete(route);
    }

    @Test
    void getFeaturedRoutes_returnsTop6() {
        when(routeRepository.findTopRoutes(PageRequest.of(0, 6))).thenReturn(List.of(route));
        when(entityMapper.toRouteResponse(route)).thenReturn(routeResponse);

        List<RouteResponse> result = routeService.getFeaturedRoutes();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Test Route");
    }

    @Test
    void getRouteCount_returnsCount() {
        when(routeRepository.count()).thenReturn(42L);

        assertThat(routeService.getRouteCount()).isEqualTo(42L);
    }

    @Test
    void getRoutesByUser_returnsUserRoutes() {
        when(routeRepository.findByCreatedById(1L)).thenReturn(List.of(route));
        when(entityMapper.toRouteResponse(route)).thenReturn(routeResponse);

        List<RouteResponse> result = routeService.getRoutesByUser(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void getAllRoutes_withoutSearchOrFilter() {
        Page<Route> page = new PageImpl<>(List.of(route));
        when(routeRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(entityMapper.toRouteResponse(route)).thenReturn(routeResponse);

        PagedResponse<RouteResponse> result = routeService.getAllRoutes(0, 10, "createdAt", "desc", null, null);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getAllRoutes_withSearch() {
        Page<Route> page = new PageImpl<>(List.of(route));
        when(routeRepository.findByNameContainingIgnoreCase(eq("test"), any(Pageable.class))).thenReturn(page);
        when(entityMapper.toRouteResponse(route)).thenReturn(routeResponse);

        PagedResponse<RouteResponse> result = routeService.getAllRoutes(0, 10, "createdAt", "desc", "test", null);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getAllRoutes_withDifficulty() {
        Page<Route> page = new PageImpl<>(List.of(route));
        when(routeRepository.findByDifficulty(eq(Difficulty.MEDIUM), any(Pageable.class))).thenReturn(page);
        when(entityMapper.toRouteResponse(route)).thenReturn(routeResponse);

        PagedResponse<RouteResponse> result = routeService.getAllRoutes(0, 10, "createdAt", "desc", null, "medium");

        assertThat(result.getContent()).hasSize(1);
    }
}
