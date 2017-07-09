package com.radodosev.mywalks.utils;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.PolylineOptions;
import com.radodosev.mywalks.R;
import com.radodosev.mywalks.data.model.Walk;

import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;

/**
 * Created by blue on 9.7.2017 г..
 */

public final class GoogleMapUtils {
    private GoogleMapUtils(){}

    public static void drawDottedRoute(final GoogleMap map,
                                       final List<LatLng> points) {
        if (points.isEmpty())
            return;

        final PolylineOptions routeOptions = new PolylineOptions();
        routeOptions.addAll(points);

        final List<PatternItem> routePattern = Arrays.asList(new Dot(), new Gap(CommonUtils.dpToPx(6)));
        routeOptions.pattern(routePattern);
        routeOptions.color(Color.RED);
        routeOptions.jointType(JointType.BEVEL);
        routeOptions.width(CommonUtils.dpToPx(6));
        map.addPolyline(routeOptions);
    }

    public static void drawMarker(final GoogleMap map,
                                  final LatLng point,
                                  final String label) {
        map.addMarker(new MarkerOptions()
                .position(point)
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location))
                .draggable(false)
                .visible(true)
                .title(label));
    }
}
