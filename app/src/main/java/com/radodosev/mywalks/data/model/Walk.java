package com.radodosev.mywalks.data.model;

import java.util.Date;
import java.util.List;

/**
 * Created by Rado on 7/8/2017.
 */

public final class Walk {
    private final Date startTime;
    private final Date endTime;
    private final List<RoutePoint> routePoints;

    public Walk(Date startTime, Date endTime, List<RoutePoint> routePoints) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.routePoints = routePoints;
    }

    public static Walk createNew(Date startTime, Date endTime, List<RoutePoint> routePoints) {
        return new Walk(startTime, endTime, routePoints);
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public List<RoutePoint> getRoutePoints() {
        return routePoints;
    }

    public static class RoutePoint{
        private final double latitude;
        private final double longitude;

        public RoutePoint(double latitude, double longitude){
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public double getLatitude() {
            return latitude;
        }
    }
}
