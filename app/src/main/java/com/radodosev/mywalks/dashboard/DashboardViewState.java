package com.radodosev.mywalks.dashboard;

import android.support.annotation.IntDef;

import com.google.android.gms.common.api.Status;
import com.radodosev.mywalks.data.model.Walk;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.radodosev.mywalks.dashboard.DashboardViewState.State.ERROR;
import static com.radodosev.mywalks.dashboard.DashboardViewState.State.GPS_NOT_AVAILABLE;
import static com.radodosev.mywalks.dashboard.DashboardViewState.State.GPS_OFF;
import static com.radodosev.mywalks.dashboard.DashboardViewState.State.GPS_ON;
import static com.radodosev.mywalks.dashboard.DashboardViewState.State.WALK_FINISHED;
import static com.radodosev.mywalks.dashboard.DashboardViewState.State.WALK_IN_PROGRESS;
import static com.radodosev.mywalks.dashboard.DashboardViewState.State.WALK_STARTED;

/**
 * Created by Rado on 7/8/2017.
 */

class DashboardViewState {
    @Retention(RetentionPolicy.SOURCE)
    @IntDef()
    @interface State {
        int GPS_ON = 1;
        int GPS_OFF = 2;
        int GPS_NOT_AVAILABLE = 3;
        int WALK_IN_PROGRESS = 4;
        int WALK_STARTED = 5;
        int WALK_FINISHED = 6;
        int ERROR = 7;
    }

    private final
    @State
    int type;

    private final Throwable error;
    private final Walk currentWalk;
    private final Status locationSettingsStatus;
    DashboardViewState(int type, Throwable error, Walk currentWalk, Status locationSettingsStatus) {
        this.type = type;
        this.error = error;
        this.currentWalk = currentWalk;
        this.locationSettingsStatus = locationSettingsStatus;
    }

    public Throwable getError() {
        return error;
    }

    public
    @State
    int getType() {
        return type;
    }

    public Walk getCurrentWalk() {
        return currentWalk;
    }

    public Status getLocationSettingsStatus() {
        return locationSettingsStatus;
    }

    public static DashboardViewState ERROR(Throwable error) {
        return new DashboardViewState(ERROR, error, null, null);
    }

    public static DashboardViewState GPS_ON() {
        return new DashboardViewState(GPS_ON, null, null, null);
    }

    public static DashboardViewState GPS_OFF(Status settingsStatus) {
        return new DashboardViewState(GPS_OFF, null, null, settingsStatus);
    }

    public static DashboardViewState GPS_NOT_AVAILABLE() {
        return new DashboardViewState(GPS_NOT_AVAILABLE, null, null, null);
    }

    public static DashboardViewState WALK_STARTED(Walk walk) {
        return new DashboardViewState(WALK_STARTED, null, walk, null);
    }

    public static DashboardViewState WALK_IN_PROGRESS(Walk walk) {
        return new DashboardViewState(WALK_IN_PROGRESS, null, walk, null);
    }

    public static DashboardViewState WALK_FINISHED(Walk walk) {
        return new DashboardViewState(WALK_FINISHED, null, walk, null);
    }
}
