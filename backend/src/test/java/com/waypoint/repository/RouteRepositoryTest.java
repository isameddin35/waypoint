package com.waypoint.repository;

import com.waypoint.entity.Difficulty;
import com.waypoint.entity.Role;
import com.waypoint.entity.Route;
import com.waypoint.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RouteRepositoryTest {

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .username("routeowner").email("owner@test.com")
                .password("encoded").role(Role.USER).build());
    }

    @Test
    void findByNameContainingIgnoreCase_returnsMatchingRoutes() {
        routeRepository.save(createRoute("Alpine Trail", Difficulty.HARD, 20.0));
        routeRepository.save(createRoute("Forest Walk", Difficulty.EASY, 3.0));

        Page<Route> result = routeRepository.findByNameContainingIgnoreCase("alpine", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getName()).isEqualTo("Alpine Trail");
    }

    @Test
    void findByDifficulty_returnsFilteredRoutes() {
        routeRepository.save(createRoute("Hard Route", Difficulty.HARD, 25.0));
        routeRepository.save(createRoute("Easy Route", Difficulty.EASY, 2.0));
        routeRepository.save(createRoute("Medium Route", Difficulty.MEDIUM, 10.0));

        Page<Route> result = routeRepository.findByDifficulty(Difficulty.EASY, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getName()).isEqualTo("Easy Route");
    }

    @Test
    void findByNameContainingIgnoreCaseAndDifficulty_returnsFilteredRoutes() {
        routeRepository.save(createRoute("Forest Trail", Difficulty.MEDIUM, 8.0));
        routeRepository.save(createRoute("Forest Walk", Difficulty.EASY, 3.0));

        Page<Route> result = routeRepository.findByNameContainingIgnoreCaseAndDifficulty(
                "forest", Difficulty.EASY, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getName()).isEqualTo("Forest Walk");
    }

    @Test
    void findTopRoutes_ordersByCreatedAtDesc() {
        Route r1 = routeRepository.save(createRoute("First", Difficulty.EASY, 1.0));
        Route r2 = routeRepository.save(createRoute("Second", Difficulty.MEDIUM, 5.0));
        Route r3 = routeRepository.save(createRoute("Third", Difficulty.HARD, 10.0));

        List<Route> result = routeRepository.findTopRoutes(PageRequest.of(0, 2));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Third");
        assertThat(result.get(1).getName()).isEqualTo("Second");
    }

    @Test
    void findByCreatedById_returnsUserRoutes() {
        routeRepository.save(createRoute("My Route", Difficulty.EASY, 5.0));

        List<Route> result = routeRepository.findByCreatedById(user.getId());

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("My Route");
    }

    private Route createRoute(String name, Difficulty difficulty, double distanceKm) {
        return Route.builder()
                .name(name)
                .difficulty(difficulty)
                .distanceKm(distanceKm)
                .createdBy(user)
                .build();
    }
}
