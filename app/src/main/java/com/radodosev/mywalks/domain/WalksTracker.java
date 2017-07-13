package com.radodosev.mywalks.domain;

import android.content.Context;
import android.location.Location;
import android.support.annotation.IntDef;

import com.radodosev.mywalks.data.WalksLocalDataSource;
import com.radodosev.mywalks.data.model.Walk;
import com.radodosev.mywalks.di.DI;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import timber.log.Timber;

import static com.radodosev.mywalks.domain.WalksTracker.WalkTrackState.Status.ERROR;
import static com.radodosev.mywalks.domain.WalksTracker.WalkTrackState.Status.FINISHED;
import static com.radodosev.mywalks.domain.WalksTracker.WalkTrackState.Status.JUST_STARTED;
import static com.radodosev.mywalks.domain.WalksTracker.WalkTrackState.Status.RUNNING;

/**
 * Created by Rado on 7/8/2017.
 * Responsible tracking single walk. Singleton
 */
public final class WalksTracker {
    public static String TAG = WalksTracker.class.getSimpleName();
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
    private Location lastTrackedLocation;

    public WalksTracker(LocationFetcher locationFetcher, WalksLocalDataSource walksLocalDataSource) {
        this.locationFetcher = locationFetcher;
        this.walksLocalDataSource = walksLocalDataSource;
        this.currentState = new WalkTrackState();
        this.stateEmitter = BehaviorSubject.create();
    }

    /**
     * Start the tracking. Subscribes for current location changes,
     * clears the old state and saves the starting time
     * @param context
     */
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
                                    location.getLongitude(), location.getSpeed());
                            currentState.accumulateTheDistance(calcDistance(lastTrackedLocation, location));

                            emitTheTrackState();
                            lastTrackedLocation = location;
                        },
                        error -> {
                            currentState.setStatus(ERROR);
                            currentState.setError(error);
                            emitTheTrackState();
                        });

        emitTheTrackState();
    }

    private float calcDistance(Location location1, Location location2) {
        if (location1 == null || location2 == null)
            return 0f;
        return location1.distanceTo(location2);
    }

    /**
     * Subscribes for receiving {@link WalkTrackState} when the states changes
     * @return Observable emitting the {@link WalkTrackState} state
     */
    public Observable<WalkTrackState> subscribeForWalkState() {
        if (stateEmitter.hasThrowable()) {
            stateEmitter = BehaviorSubject.create();
        }
        return stateEmitter
                .doOnNext(walkTrackState -> Timber.d(TAG + ": walkTrackState %s", walkTrackState));
    }

    /**
     * Stops the walk tracking. Sets the walk end time and saves the walk locally in the DB
     */
    public void stopTracking() {
        if (locationsDisposable == null)
            throw new IllegalStateException("Start the tracking first to be able to stop it!");

        locationsDisposable.dispose();
        locationsDisposable = null;
        lastTrackedLocation = null;

        currentState.setStatus(FINISHED);
        currentState.setEndTime(getCurrentTime());
        emitTheTrackState();

        if (!currentState.getRoutePoints().isEmpty())
            walksLocalDataSource.addWalk(currentState.toWalk());
    }

    private void emitTheTrackState() {
        stateEmitter.onNext(currentState);
    }

    private Date getCurrentTime() {
        return new Date();
    }

    /**
     * Represents the walk tracking state
     */
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
        private float distanceInMeters;
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

        private void accumulateTheDistance(float newDistance) {
            this.distanceInMeters += newDistance;
        }

        private void addRoutePoint(double latitude, double longitude, float speed) {
            this.routePoints.add(new Walk.RoutePoint(latitude, longitude, speed));
        }

        private void clearState() {
            startTime = null;
            endTime = null;
            error = null;
            distanceInMeters = 0;
            routePoints.clear();
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
            return new Walk(0, startTime, endTime, distanceInMeters, routePoints);
        }

        @Override
        public String toString() {
            return "WalkTrackState{" +
                    "status=" + status +
                    ", error=" + error +
                    ", startTime=" + startTime +
                    ", endTime=" + endTime +
                    ", distanceInMeters=" + distanceInMeters +
                    ", routePoints=" + routePoints +
                    '}';
        }
    }
}
