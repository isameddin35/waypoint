package com.waypoint.service;

import com.waypoint.dto.RoutePointResponse;
import com.waypoint.entity.RoutePoint;
import com.waypoint.exception.ResourceNotFoundException;
import com.waypoint.mapper.EntityMapper;
import com.waypoint.repository.RoutePointRepository;
import com.waypoint.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ElevationService {

    private final RoutePointRepository routePointRepository;
    private final RouteRepository routeRepository;
    private final EntityMapper entityMapper;
    private final RestTemplate restTemplate;

    @Transactional
    public List<RoutePointResponse> fetchAndStoreElevation(Long routeId) {
        if (!routeRepository.existsById(routeId)) {
            throw new ResourceNotFoundException("Route not found");
        }

        List<RoutePoint> points = routePointRepository.findByRouteIdOrderBySequenceNumberAsc(routeId);

        if (points.isEmpty()) {
            return List.of();
        }

        int batchSize = 50;
        for (int i = 0; i < points.size(); i += batchSize) {
            int end = Math.min(i + batchSize, points.size());
            List<RoutePoint> batch = points.subList(i, end);

            String latitudes = batch.stream()
                    .map(p -> String.format("%.6f", p.getLatitude()))
                    .collect(java.util.stream.Collectors.joining(","));
            String longitudes = batch.stream()
                    .map(p -> String.format("%.6f", p.getLongitude()))
                    .collect(java.util.stream.Collectors.joining(","));

            String url = String.format(
                    "https://api.open-meteo.com/v1/elevation?latitude=%s&longitude=%s",
                    latitudes, longitudes
            );

            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                if (response != null && response.containsKey("elevation")) {
                    @SuppressWarnings("unchecked")
                    List<Object> elevations = (List<Object>) response.get("elevation");
                    for (int j = 0; j < elevations.size() && j < batch.size(); j++) {
                        Number elev = (Number) elevations.get(j);
                        batch.get(j).setElevation(elev != null ? elev.doubleValue() : 0.0);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to fetch elevation data: " + e.getMessage(), e);
            }
        }

        routePointRepository.saveAll(points);
        return points.stream()
                .map(entityMapper::toRoutePointResponse)
                .toList();
    }
}
