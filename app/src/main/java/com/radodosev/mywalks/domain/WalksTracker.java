package com.radodosev.mywalks.domain;

import android.content.Context;
import android.support.annotation.IntDef;

import com.radodosev.mywalks.data.WalksLocalDataSource;
import com.radodosev.mywalks.data.model.Walk;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

import static com.radodosev.mywalks.domain.WalksTracker.WalkTrackState.Status.ERROR;
import static com.radodosev.mywalks.domain.WalksTracker.WalkTrackState.Status.FINISHED;
import static com.radodosev.mywalks.domain.WalksTracker.WalkTrackState.Status.JUST_STARTED;
import static com.radodosev.mywalks.domain.WalksTracker.WalkTrackState.Status.RUNNING;

/**
 * Created by Rado on 7/8/2017.
 */

public final class WalksTracker {
    private static WalksTracker INSTANCE;

    public static WalksTracker get() {
        if (INSTANCE == null) {
            synchronized (WalksTracker.class) {
                if (INSTANCE == null)
                    INSTANCE = new WalksTracker(DI.provideLocationFetcher(), DI.provideLocalDataSource());
            }
        }
        return INSTANCE;
    }

    private final LocationFetcher locationFetcher;
    private final WalksLocalDataSource walksLocalDataSource;

    private Subject<WalkTrackState> stateEmitter;
    private final WalkTrackState currentState;
    private Disposable locationsDisposable;

    public WalksTracker(LocationFetcher locationFetcher, WalksLocalDataSource walksLocalDataSource) {
        this.locationFetcher = locationFetcher;
        this.walksLocalDataSource = walksLocalDataSource;
        this.currentState = new WalkTrackState();
        this.stateEmitter = BehaviorSubject.create();
    }

    public void startTracking(Context context) {
        currentState.clearState();
        currentState.setStartTime(getCurrentTime());
        currentState.setStatus(JUST_STARTED);

        locationsDisposable = locationFetcher
                .getLocationUpdates(context)
                .subscribe(
                        location -> {
                            currentState.setStatus(RUNNING);
                            currentState.addRoutePoint(location.getLatitude(),
                                    location.getLongitude());

                            emitTheTrackState();
                        },
                        error -> {

                            currentState.setStatus(ERROR);
                            currentState.setError(error);
                            emitTheTrackState();
                        });

        emitTheTrackState();
    }

    public Observable<WalkTrackState> subscribeForWalkState() {
        if (stateEmitter.hasThrowable()) {
            stateEmitter = BehaviorSubject.create();
        }
        return stateEmitter;
    }

    public void stopTracking() {
        if (locationsDisposable == null)
            throw new IllegalStateException("Start the tracking first to be able to stop it!");

        locationsDisposable.dispose();
        locationsDisposable = null;

        currentState.setStatus(FINISHED);
        currentState.setEndTime(getCurrentTime());
        emitTheTrackState();

        if (!currentState.getRoutePoints().isEmpty())
            walksLocalDataSource.addWalk(currentState.toWalk()).subscribe();
    }

    public boolean isTracking() {
        return locationsDisposable != null;
    }

    private void emitTheTrackState() {
        stateEmitter.onNext(currentState);
    }

    private void emitError(Throwable error) {
        stateEmitter.onError(error);
    }

    private Date getCurrentTime() {
        return new Date();
    }


    public static class WalkTrackState {
        @Retention(RetentionPolicy.SOURCE)
        @IntDef({JUST_STARTED, RUNNING, FINISHED, ERROR})
        public @interface Status {
            int JUST_STARTED = 1;
            int RUNNING = 2;
            int FINISHED = 3;
            int ERROR = 4;
        }
        private
        @Status
        int status;

        private Throwable error;
        private Date startTime;
        private Date endTime;
        private List<Walk.RoutePoint> routePoints;
        private WalkTrackState() {
            routePoints = new LinkedList<>();
        }

        private void setStatus(@Status int status) {
            this.status = status;
        }

        private void setError(Throwable error) {
            this.error = error;
        }

        private void setStartTime(Date startedTime) {
            this.startTime = startedTime;
        }

        private void setEndTime(Date endTime) {
            this.endTime = endTime;
        }

        private void addRoutePoint(double latitude, double longitude) {
            this.routePoints.add(new Walk.RoutePoint(latitude, longitude));
        }

        private void clearState() {
            startTime = null;
            endTime = null;
            error = null;
            routePoints.clear();
        }

        public Date getStartedTime() {
            return startTime;
        }

        public Date getEndTime() {
            return endTime;
        }

        public List<Walk.RoutePoint> getRoutePoints() {
            return routePoints;
        }

        public
        @Status
        int getStatus() {
            return status;
        }

        public Throwable getError() {
            return error;
        }

        public Walk toWalk() {
            return new Walk(startTime, endTime, routePoints);
        }
    }
}
