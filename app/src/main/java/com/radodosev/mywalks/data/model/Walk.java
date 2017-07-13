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
    private final float distanceInMeters;
    private final List<RoutePoint> routePoints;

    public Walk(long id, Date startTime, Date endTime, float distanceInMeters, List<RoutePoint> routePoints) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.distanceInMeters = distanceInMeters;
        this.routePoints = routePoints;
    }

    public long geId() {
        return id;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public float getDistanceInMeters() {
        return distanceInMeters;
    }

    public List<RoutePoint> getRoutePoints() {
        return routePoints;
    }

    public float getMaxSpeed() {
        if (routePoints == null || routePoints.isEmpty())
            return 0f;

        float maxSpeed = 0;
        for (RoutePoint routePoint : routePoints) {
            if (routePoint.getSpeed() > maxSpeed)
                maxSpeed = routePoint.getSpeed();
        }
        return maxSpeed;
    }

    public float getAverageSpeed() {
        if (routePoints == null || routePoints.isEmpty())
            return 0f;

        float speedsSum = 0;
        for (RoutePoint routePoint : routePoints) {
            speedsSum += routePoint.getSpeed();
        }
        return speedsSum / routePoints.size();
    }

    @Override
    public String toString() {
        return "Walk{" +
                "id=" + id +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", distanceInMeters=" + distanceInMeters +
                ", routePoints=" + routePoints +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Walk walk = (Walk) o;

        if (id != walk.id) return false;
        if (Float.compare(walk.distanceInMeters, distanceInMeters) != 0) return false;
        if (startTime != null ? !startTime.equals(walk.startTime) : walk.startTime != null)
            return false;
        if (endTime != null ? !endTime.equals(walk.endTime) : walk.endTime != null) return false;
        return routePoints != null ? routePoints.equals(walk.routePoints) : walk.routePoints == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
        result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
        result = 31 * result + (distanceInMeters != +0.0f ? Float.floatToIntBits(distanceInMeters) : 0);
        result = 31 * result + (routePoints != null ? routePoints.hashCode() : 0);
        return result;
    }

    public static class RoutePoint {
        private final double latitude;
        private final double longitude;
        private final float speed;

        public RoutePoint(double latitude, double longitude, float speed) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.speed = speed;
        }

        public double getLongitude() {
            return longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public float getSpeed() {
            return speed;
        }

        @Override
        public String toString() {
            return "RoutePoint{" +
                    "latitude=" + latitude +
                    ", longitude=" + longitude +
                    ", speed=" + speed +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            RoutePoint that = (RoutePoint) o;

            if (Double.compare(that.latitude, latitude) != 0) return false;
            if (Double.compare(that.longitude, longitude) != 0) return false;
            return Float.compare(that.speed, speed) == 0;

        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            temp = Double.doubleToLongBits(latitude);
            result = (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(longitude);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            result = 31 * result + (speed != +0.0f ? Float.floatToIntBits(speed) : 0);
            return result;
        }
    }
}
