package com.radodosev.mywalks.data.model;

import java.util.Date;
import java.util.List;

/**
 * Created by Rado on 7/8/2017.
 */

public final class Walk {
    private final long id;
    private final Date startTime;
    private final Date endTime;
    private final List<RoutePoint> routePoints;

    public Walk(long id, Date startTime, Date endTime, List<RoutePoint> routePoints) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.routePoints = routePoints;
    }

    public static Walk createNew(long id, Date startTime, Date endTime, List<RoutePoint> routePoints) {
        return new Walk(id, startTime, endTime, routePoints);
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

    public long geId() {
        return id;
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
