package com.radodosev.mywalks.dashboard;

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

public class DashboardPresenter extends MviBasePresenter<DashboardView, DashboardViewState> {

    private final RxPermissions permissionsChecker;
    LocationFetcher locationFetcher;

    public DashboardPresenter(RxPermissions permissionsChecker, WalksTracker walksTracker, LocationFetcher locationFetcher) {
        this.locationFetcher = locationFetcher;
        this.permissionsChecker = permissionsChecker;
    }

    @Override
    protected void bindIntents() {
        Context context = MyWalksApplication.get();

        final Observable<DashboardViewState> initialLocationRequirementCheck = intent(DashboardView::checkLocationRequirements)
                .doOnNext(ignore -> Timber.d("intent: SingleWalkView::checkLocationRequirements"))
                .flatMap(ignore -> locationRequirementCheck(LocationFetcher.newInstance()));

        final Observable<DashboardViewState> walkTracking =
                intent(DashboardView::trackAWalk)
                        .doOnNext(ignore -> Timber.d("intent: walk tracking"))
                        .flatMap(toStart -> WalkTrackerService.subscribe()
                                .map(walkTrackState -> {
                                    switch (walkTrackState.getStatus()) {
                                        case FINISHED:
                                            return DashboardViewState.WALK_FINISHED(walkTrackState.toWalk());
                                        case JUST_STARTED:
                                            return DashboardViewState.WALK_STARTED(walkTrackState.toWalk());
                                        case RUNNING:
                                            return DashboardViewState.WALK_IN_PROGRESS(walkTrackState.toWalk());
                                        case ERROR:
                                            return DashboardViewState.ERROR(walkTrackState.getError());
                                    }
                                    return null;
                                })
                                .onErrorReturn(DashboardViewState::ERROR));


//        final Observable<DashboardViewState> currentLocationTracking =
//                intent(SingleWalkView::checkLocationRequirements)
//                        .doOnNext(ignore -> Timber.d("intent: location tracking"))
//                        .flatMap(toStart -> locationFetcher.getLocationUpdates(MyWalksApplication.newInstance())
//                                .map(DashboardViewState::LOCATION_UPDATE)
//                                .onErrorReturn(DashboardViewState::ERROR));

        final Observable<DashboardViewState> startStopTracking = intent(DashboardView::startStopTracking)
                .flatMap(ignore -> locationRequirementCheck(LocationFetcher.newInstance())
                        .doOnNext(viewState -> {
                            if (WalkTrackerService.isRunning(context))
                                WalkTrackerService.stop(context);
                            else
//                            else if (viewState.areLocationRequirementsMet())
                                WalkTrackerService.start(context);
                        }));

        final Observable<DashboardViewState> allIntentsObservablesMerged =
                Observable.merge(initialLocationRequirementCheck, walkTracking, startStopTracking)
                        .observeOn(AndroidSchedulers.mainThread());

        subscribeViewState(allIntentsObservablesMerged, DashboardView::render);
    }

    private Observable<DashboardViewState> locationRequirementCheck(LocationFetcher locationFetcher) {
        return Observable.zip(
                permissionsChecker.request(Manifest.permission.ACCESS_FINE_LOCATION),
                locationFetcher.getLocationSettings(MyWalksApplication.get())
                , (locationPermissionGranted, locationSettingsStatus) -> {
                    if (!locationPermissionGranted)
                        return DashboardViewState.LOCATION_PERMISSION_NOT_GRANTED();

                    if (locationSettingsStatus.areAllSettingsEnabled()) {
                        return DashboardViewState.GPS_ON();
                    } else if (locationSettingsStatus.isProblemRecoverable()) {
                        return DashboardViewState.GPS_OFF(locationSettingsStatus.getStatus());
                    }
                    return DashboardViewState.GPS_NOT_AVAILABLE();
                })
                .onErrorReturn(DashboardViewState::ERROR);
    }
}
