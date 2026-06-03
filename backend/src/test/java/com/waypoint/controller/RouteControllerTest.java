package com.waypoint.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waypoint.dto.*;
import com.waypoint.service.RouteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RouteController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.waypoint\\.security\\..*"))
@AutoConfigureMockMvc(addFilters = false)
class RouteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RouteService routeService;

    @Test
    void getAllRoutes_returnsPagedResponse() throws Exception {
        PagedResponse<RouteResponse> paged = PagedResponse.<RouteResponse>builder()
                .content(List.of())
                .page(0).size(10).totalElements(0).totalPages(0).last(true)
                .build();

        when(routeService.getAllRoutes(anyInt(), anyInt(), anyString(), anyString(), any(), any()))
                .thenReturn(paged);

        mockMvc.perform(get("/api/routes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getRouteById_returnsRoute() throws Exception {
        RouteResponse response = RouteResponse.builder()
                .id(1L).name("Test Route").difficulty("EASY").distanceKm(5.0).build();

        when(routeService.getRouteById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/routes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Route"));
    }

    @Test
    void getFeaturedRoutes_returnsList() throws Exception {
        when(routeService.getFeaturedRoutes()).thenReturn(List.of());

        mockMvc.perform(get("/api/routes/featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getRouteCount_returnsCount() throws Exception {
        when(routeService.getRouteCount()).thenReturn(42L);

        mockMvc.perform(get("/api/routes/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("42"));
    }
}
