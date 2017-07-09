package com.radodosev.mywalks.data.model;

import com.google.android.gms.maps.model.LatLng;
import com.radodosev.mywalks.data.db.RoutePointsTable;
import com.radodosev.mywalks.data.db.WalksTable;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by Rado on 7/8/2017.
 */

public final class ModelMapper {
    private ModelMapper(){}

    public static WalksTable fromWalksToWalkTable(final Walk walk){
        return new WalksTable(walk.getStartTime(), walk.getEndTime(), fromRoutePointsToRoutePointsTable(walk.getRoutePoints()));
    }

    public static List<RoutePointsTable> fromRoutePointsToRoutePointsTable(final List<Walk.RoutePoint> routePoints){
        return Observable
                .fromIterable(routePoints)
                .map(routePoint -> new RoutePointsTable(routePoint.getLatitude(), routePoint.getLongitude()))
                .toList()
                .blockingGet();
    }

    public static Walk fromWalksTableToWalk(final WalksTable walksTable){
        return Walk.createNew(walksTable.getStartTime(), walksTable.getEndTime(), fromRoutePointsTableToRoutePoints(walksTable.getRoutePoints()));
    }

    public static List<Walk.RoutePoint> fromRoutePointsTableToRoutePoints(final List<RoutePointsTable> routePointsTables){
        return Observable
                .fromIterable(routePointsTables)
                .map(routePointsTable -> new Walk.RoutePoint(routePointsTable.getLatitude(), routePointsTable.getLongitude()))
                .toList()
                .blockingGet();
    }

    public static List<LatLng> fromRoutePointsToLatLng(final List<Walk.RoutePoint> routePoints){
        return Observable
                .fromIterable(routePoints)
                .map(ModelMapper::fromRoutePointToLatLng)
                .toList()
                .blockingGet();
    }

    public static LatLng fromRoutePointToLatLng(final Walk.RoutePoint routePoint){
        return new LatLng(routePoint.getLatitude(), routePoint.getLongitude());
    }
}
