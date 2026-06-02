package com.waypoint.util;

import com.waypoint.entity.RoutePoint;

import java.util.List;

public class HaversineDistance {

    private static final double EARTH_RADIUS_KM = 6371.0;

    public static double calculate(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    public static double calculateRouteDistance(List<RoutePoint> points) {
        double total = 0;
        for (int i = 1; i < points.size(); i++) {
            RoutePoint p1 = points.get(i - 1);
            RoutePoint p2 = points.get(i);
            total += calculate(p1.getLatitude(), p1.getLongitude(),
                               p2.getLatitude(), p2.getLongitude());
        }
        return Math.round(total * 100.0) / 100.0;
    }
}
