package com.radodosev.mywalks.utils;

import android.content.Intent;
import android.content.res.Resources;
import android.provider.Settings;

/**
 * Created by blue on 9.7.2017 г..
 */

public final class CommonUtils {
    private CommonUtils() {
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }
}
