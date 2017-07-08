package com.radodosev.mywalks.domain;

import android.content.Context;
import android.support.annotation.IntDef;

import com.radodosev.mywalks.data.model.Walk;
import com.radodosev.mywalks.data.WalksLocalDataSource;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

import static com.radodosev.mywalks.domain.WalksTracker.WalkTrackState.Status.FINISHED;
import static com.radodosev.mywalks.domain.WalksTracker.WalkTrackState.Status.JUST_STARTED;
import static com.radodosev.mywalks.domain.WalksTracker.WalkTrackState.Status.RUNNING;

/**
 * Created by Rado on 7/8/2017.
 */

public final class WalksTracker {

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

    public Observable<WalkTrackState> startTracking(Context context) {
        currentState.clearState();
        currentState.setStartTime(getCurrentTime());
        currentState.setStatus(JUST_STARTED);

        locationsDisposable = locationFetcher
                .getLocationUpdates(context)
                .share()
                .subscribe(
                        location -> {
                            currentState.setStatus(RUNNING);
                            currentState.addRoutePoint(location.getLatitude(),
                                    location.getLongitude());
                            emitTheTrackState();
                        },
                        throwable -> {

                        });

        return subscribeForWalkState()
                .startWith(currentState);
    }

    public Observable<WalkTrackState> subscribeForWalkState(){
        return stateEmitter;
    }

    public void stopTracking() {
        locationsDisposable.dispose();

        currentState.setStatus(FINISHED);
        emitTheTrackState();
        walksLocalDataSource.addWalk(currentState.toWalk());
    }
    private void emitTheTrackState() {
        stateEmitter.onNext(currentState);
    }

    private Date getCurrentTime(){
        return new Date();
    }


    public static class WalkTrackState{
        @Retention(RetentionPolicy.SOURCE)
        @IntDef({JUST_STARTED, RUNNING, FINISHED})
        @interface Status{
            int JUST_STARTED = 1;
            int RUNNING = 2;
            int FINISHED = 3;
        }

        private @Status int status;
        private Date startTime;
        private Date endTime;
        private List<Walk.RoutePoint> routePoints;

        private WalkTrackState(){
            routePoints = new LinkedList<>();
        }

        private void setStatus(@Status int status) {
            this.status = status;
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

        private void clearState(){
            startTime = null;
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

        public @Status int getStatus() {
            return status;
        }

        public Walk toWalk(){
            return new Walk(startTime, endTime, routePoints);
        }
    }
}
