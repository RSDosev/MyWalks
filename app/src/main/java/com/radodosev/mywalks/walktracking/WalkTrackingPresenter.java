package com.radodosev.mywalks.walktracking;

import android.Manifest;
import android.content.Context;

import com.hannesdorfmann.mosby3.mvi.MviBasePresenter;
import com.radodosev.mywalks.MyWalksApplication;
import com.radodosev.mywalks.domain.LocationFetcher;
import com.radodosev.mywalks.domain.WalkTrackerService;
import com.radodosev.mywalks.domain.WalksTracker;
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

import static com.radodosev.mywalks.domain.WalksTracker.WalkTrackState.Status.ERROR;
import static com.radodosev.mywalks.domain.WalksTracker.WalkTrackState.Status.FINISHED;
import static com.radodosev.mywalks.domain.WalksTracker.WalkTrackState.Status.JUST_STARTED;
import static com.radodosev.mywalks.domain.WalksTracker.WalkTrackState.Status.RUNNING;

/**
 * Created by Rado on 7/8/2017.
 */

public class WalkTrackingPresenter extends MviBasePresenter<WalkTrackingView, WalkTrackingViewState> {

    private final RxPermissions permissionsChecker;

    public WalkTrackingPresenter(RxPermissions permissionsChecker) {
        this.permissionsChecker = permissionsChecker;
    }

    @Override
    protected void bindIntents() {
        final Context context = MyWalksApplication.get();

        // On sending initialLocationRequirementCheck intent from the view this code block checks the
        // location settings and permissions and then sends the response to the view to be rendered
        final Observable<WalkTrackingViewState> initialLocationRequirementCheck = intent(WalkTrackingView::checkLocationRequirements)
                .doOnNext(ignore -> Timber.d("intent: WalkTrackingView::initialLocationRequirementCheck"))
                .flatMap(ignore -> locationRequirementCheck(LocationFetcher.newInstance()));

        // On sending walkTracking intent from the view this code block subscribes for new track data
        // and then sends the response to the view to be rendered
        final Observable<WalkTrackingViewState> walkTracking =
                intent(WalkTrackingView::trackAWalk)
                        .doOnNext(ignore -> Timber.d("intent: WalkTrackingView::walkTracking"))
                        .flatMap(toStart -> WalkTrackerService.subscribe()
                                .map(walkTrackState -> {
                                    switch (walkTrackState.getStatus()) {
                                        case FINISHED:
                                            return WalkTrackingViewState.WALK_FINISHED(walkTrackState.toWalk());
                                        case JUST_STARTED:
                                            return WalkTrackingViewState.WALK_STARTED(walkTrackState.toWalk());
                                        case RUNNING:
                                            return WalkTrackingViewState.WALK_IN_PROGRESS(walkTrackState.toWalk());
                                        case ERROR:
                                            return WalkTrackingViewState.ERROR(walkTrackState.getError());
                                    }
                                    return null;
                                })
                                .onErrorReturn(WalkTrackingViewState::ERROR));
        // On sending startStopTracking intent from the view this code block first checks for location
        // settings and permissions to be enabled and then stops or starts the walk tracker. It send
        // the location check response to the view to be rendered
        final Observable<WalkTrackingViewState> startStopTracking = intent(WalkTrackingView::startStopTracking)
                .doOnNext(ignore -> Timber.d("intent: WalkTrackingView::startStopTracking"))
                .flatMap(ignore -> locationRequirementCheck(LocationFetcher.newInstance())
                        .doOnNext(viewState -> {
                            if (WalkTrackerService.isRunning(context))
                                WalkTrackerService.stop(context);
                            else if (viewState.areLocationRequirementsMet())
                                WalkTrackerService.start(context);
                        }));

        // Merges all intents from the view to be as single source
        final Observable<WalkTrackingViewState> allIntentsObservablesMerged =
                Observable.merge(initialLocationRequirementCheck, walkTracking, startStopTracking)
                        .observeOn(AndroidSchedulers.mainThread());

        subscribeViewState(allIntentsObservablesMerged, WalkTrackingView::render);
    }

    /**
     * Create observable which emits location settings and permissions status
     * @param locationFetcher
     * @return the observable
     */
    private Observable<WalkTrackingViewState> locationRequirementCheck(final LocationFetcher locationFetcher) {
        return Observable.zip(
                permissionsChecker.requestEach(Manifest.permission.ACCESS_FINE_LOCATION),
                locationFetcher.getLocationSettings(MyWalksApplication.get())
                , (locationPermissionStatus, locationSettingsStatus) -> {
                    if (!locationPermissionStatus.granted)
                        return WalkTrackingViewState.LOCATION_PERMISSION_NOT_GRANTED(
                                locationPermissionStatus.shouldShowRequestPermissionRationale);

                    if (locationSettingsStatus.areAllSettingsEnabled()) {
                        return WalkTrackingViewState.GPS_ON();
                    } else if (locationSettingsStatus.isProblemRecoverable()) {
                        return WalkTrackingViewState.GPS_OFF(locationSettingsStatus.getStatus());
                    }
                    return WalkTrackingViewState.GPS_NOT_AVAILABLE();
                })
                .onErrorReturn(WalkTrackingViewState::ERROR);
    }
}
