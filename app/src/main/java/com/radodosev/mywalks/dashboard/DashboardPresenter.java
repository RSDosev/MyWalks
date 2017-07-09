package com.radodosev.mywalks.dashboard;

import android.content.Context;

import com.hannesdorfmann.mosby3.mvi.MviBasePresenter;
import com.radodosev.mywalks.MyWalksApplication;
import com.radodosev.mywalks.domain.LocationFetcher;
import com.radodosev.mywalks.domain.WalkTrackerService;
import com.radodosev.mywalks.domain.WalksTracker;

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

    LocationFetcher locationFetcher;

    DashboardPresenter(WalksTracker walksTracker, LocationFetcher locationFetcher) {
        this.locationFetcher = locationFetcher;
    }

    @Override
    protected void bindIntents() {
        Context context = MyWalksApplication.get();

        final Observable<DashboardViewState> gpsTurnedOnCheck = intent(DashboardView::checkGPSTurnedOn)
                .doOnNext(ignore -> Timber.d("intent: DashboardView::checkGPSTurnedOn"))
                .flatMap(ignore -> locationFetcher.getLocationSettings(MyWalksApplication.get())
                        .map(locationSettingsStatus -> {
                            if (locationSettingsStatus.areAllSettingsEnabled()) {
                                return DashboardViewState.GPS_ON();
                            } else if (locationSettingsStatus.isProblemRecoverable()) {
                                return DashboardViewState.GPS_OFF(locationSettingsStatus.getStatus());
                            }
                            return DashboardViewState.GPS_NOT_AVAILABLE();
                        })
                        .onErrorReturn(DashboardViewState::ERROR));

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
//                intent(DashboardView::checkGPSTurnedOn)
//                        .doOnNext(ignore -> Timber.d("intent: location tracking"))
//                        .flatMap(toStart -> locationFetcher.getLocationUpdates(MyWalksApplication.get())
//                                .map(DashboardViewState::LOCATION_UPDATE)
//                                .onErrorReturn(DashboardViewState::ERROR));

        intent(DashboardView::startStopTracking)
                .doOnNext(ignore -> Timber.d("intent: start stop tracking"))
                .doOnNext(ignore -> {
                    if (WalkTrackerService.isRunning(context))
                        WalkTrackerService.stop(context);
                    else
                        WalkTrackerService.start(context);
                })
                .subscribe();

        final Observable<DashboardViewState> allIntentsObservablesMerged =
                Observable.merge(gpsTurnedOnCheck, walkTracking)
                        .observeOn(AndroidSchedulers.mainThread());

        subscribeViewState(allIntentsObservablesMerged, DashboardView::render);
    }
}
