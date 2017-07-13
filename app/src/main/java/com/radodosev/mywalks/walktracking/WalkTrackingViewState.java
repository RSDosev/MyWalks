package com.radodosev.mywalks.walktracking;

import android.support.annotation.IntDef;

import com.google.android.gms.common.api.Status;
import com.radodosev.mywalks.data.model.Walk;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.radodosev.mywalks.walktracking.WalkTrackingViewState.State.ERROR;
import static com.radodosev.mywalks.walktracking.WalkTrackingViewState.State.GPS_NOT_AVAILABLE;
import static com.radodosev.mywalks.walktracking.WalkTrackingViewState.State.GPS_OFF;
import static com.radodosev.mywalks.walktracking.WalkTrackingViewState.State.GPS_ON;
import static com.radodosev.mywalks.walktracking.WalkTrackingViewState.State.LOCATION_PERMISSION_NOT_GRANTED;
import static com.radodosev.mywalks.walktracking.WalkTrackingViewState.State.WALK_FINISHED;
import static com.radodosev.mywalks.walktracking.WalkTrackingViewState.State.WALK_IN_PROGRESS;
import static com.radodosev.mywalks.walktracking.WalkTrackingViewState.State.WALK_STARTED;

/**
 * Created by Rado on 7/8/2017.
 */

class WalkTrackingViewState {
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LOCATION_PERMISSION_NOT_GRANTED, GPS_ON, GPS_OFF,
            GPS_NOT_AVAILABLE, WALK_IN_PROGRESS, WALK_STARTED, WALK_FINISHED, ERROR})
    public @interface State {
        int LOCATION_PERMISSION_NOT_GRANTED = 1;
        int GPS_ON = 2;
        int GPS_OFF = 3;
        int GPS_NOT_AVAILABLE = 4;
        int WALK_IN_PROGRESS = 5;
        int WALK_STARTED = 6;
        int WALK_FINISHED = 7;
        int ERROR = 8;
    }

    private final
    @State
    int type;

    private final Throwable error;
    private final Walk currentWalk;
    private final Status locationSettingsStatus;

    WalkTrackingViewState(int type, Throwable error, Walk currentWalk, Status locationSettingsStatus) {
        this.type = type;
        this.error = error;
        this.currentWalk = currentWalk;
        this.locationSettingsStatus = locationSettingsStatus;
    }

    public Throwable getError() {
        return error;
    }

    public boolean areLocationRequirementsMet() {
        return type == GPS_ON;
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

    public static WalkTrackingViewState ERROR(Throwable error) {
        return new WalkTrackingViewState(ERROR, error, null, null);
    }

    public static WalkTrackingViewState LOCATION_PERMISSION_NOT_GRANTED(boolean shouldShowRequestPermissionRationale) {
        return new WalkTrackingViewState(LOCATION_PERMISSION_NOT_GRANTED,
                shouldShowRequestPermissionRationale ? new Exception() : null, null, null);
    }

    public static WalkTrackingViewState GPS_ON() {
        return new WalkTrackingViewState(GPS_ON, null, null, null);
    }

    public static WalkTrackingViewState GPS_OFF(Status settingsStatus) {
        return new WalkTrackingViewState(GPS_OFF, null, null, settingsStatus);
    }

    public static WalkTrackingViewState GPS_NOT_AVAILABLE() {
        return new WalkTrackingViewState(GPS_NOT_AVAILABLE, null, null, null);
    }

    public static WalkTrackingViewState WALK_STARTED(Walk walk) {
        return new WalkTrackingViewState(WALK_STARTED, null, walk, null);
    }

    public static WalkTrackingViewState WALK_IN_PROGRESS(Walk walk) {
        return new WalkTrackingViewState(WALK_IN_PROGRESS, null, walk, null);
    }

    public static WalkTrackingViewState WALK_FINISHED(Walk walk) {
        return new WalkTrackingViewState(WALK_FINISHED, null, walk, null);
    }
}
