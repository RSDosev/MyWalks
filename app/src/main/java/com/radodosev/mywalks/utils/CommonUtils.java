package com.radodosev.mywalks.utils;

import android.content.Intent;
import android.content.res.Resources;
import android.provider.Settings;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
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

    public static String formatDateInMyLocale(Date date) {
        SimpleDateFormat curFormater = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
        curFormater.setTimeZone(TimeZone.getDefault());

        return curFormater.format(date);
    }
}
