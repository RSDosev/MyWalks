package com.radodosev.mywalks.utils;

import android.content.Context;
import android.content.res.Resources;

import com.radodosev.mywalks.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by blue on 9.7.2017 г..
 */

public final class CommonUtils {
    private CommonUtils() {
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }


    /**
     * Formats date in format "dd/MM/yyyy \nhh:mm:ss a" - 23/02/2017 \n05:32:12 PM
     * @param date
     * @return the formatted date
     */
    public static String formatDateInMyLocale(Date date) {
        final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy \nhh:mm:ss a");
        dateFormatter.setTimeZone(TimeZone.getDefault());

        return dateFormatter.format(date);
    }

    /**
     * Formats distance in meters or kilometers and meters if distance is above 1 kilometer.
     * For example: 3123 -> 3km 123m , 321 -> 321m
     * @param context current context
     * @param distanceInMeters the distance
     * @return the formatted distance
     */
    public static String formatDistance(final Context context, final float distanceInMeters) {
        final int distance = Math.round(distanceInMeters);
        final String metersPostfix = context.getString(R.string.distance_postfix_meters);
        if (distance < 1000) {
            return distance + metersPostfix;
        } else {
            final String kilometersPostfix = context.getString(R.string.distance_postfix_kilometers);
            int kilometers = (int) (distance / 1000);
            int meters = (int) (distance % 1000);
            return kilometers + kilometersPostfix + " " + meters + metersPostfix;
        }
    }

    /**
     * Formats speed in km/h
     * For example: 123 -> 123km/h
     * @param context current context
     * @param speed the speed
     * @return the formatted speed
     */
    public static String formatSpeed(final Context context, final float speed) {
        final String speedFormat = context.getString(R.string.speed_format);
        return String.format(speedFormat, speed);
    }
}
