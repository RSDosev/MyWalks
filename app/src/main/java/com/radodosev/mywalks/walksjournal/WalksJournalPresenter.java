package com.radodosev.mywalks.walksjournal;

import android.content.Context;

import com.hannesdorfmann.mosby3.mvi.MviBasePresenter;
import com.radodosev.mywalks.MyWalksApplication;
import com.radodosev.mywalks.data.WalksDataSource;
import com.radodosev.mywalks.data.WalksLocalDataSource;
import com.radodosev.mywalks.domain.LocationFetcher;
import com.radodosev.mywalks.domain.WalkTrackerService;
import com.radodosev.mywalks.domain.WalksTracker;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import timber.log.Timber;

import static com.radodosev.mywalks.domain.WalksTracker.WalkTrackState.Status.ERROR;
import static com.radodosev.mywalks.domain.WalksTracker.WalkTrackState.Status.FINISHED;
import static com.radodosev.mywalks.domain.WalksTracker.WalkTrackState.Status.JUST_STARTED;
import static com.radodosev.mywalks.domain.WalksTracker.WalkTrackState.Status.RUNNING;

/**
 * Created by Rado on 7/8/2017.
 */

public class WalksJournalPresenter extends MviBasePresenter<WalksJournalView, WalksJournalViewState> {

    private WalksDataSource dataSource;

    public WalksJournalPresenter(WalksLocalDataSource walksLocalDataSource) {
        this.dataSource = walksLocalDataSource;
    }

    @Override
    protected void bindIntents() {
        //
//        final Observable<WalksJournalViewState> gpsTurnedOnCheck = intent(WalksJournalView::checkGPSTurnedOn)
//                .doOnNext(ignore -> Timber.d("intent: WalksJournalView::checkGPSTurnedOn"))
//                .flatMap(ignore -> locationFetcher.getLocationSettings(MyWalksApplication.get())
//                        .map(locationSettingsStatus -> {
//                            if (locationSettingsStatus.areAllSettingsEnabled()) {
//                                return WalksJournalViewState.GPS_ON();
//                            } else if (locationSettingsStatus.isProblemRecoverable()) {
//                                return WalksJournalViewState.GPS_OFF(locationSettingsStatus.getStatus());
//                            }
//                            return WalksJournalViewState.GPS_NOT_AVAILABLE();
//                        })
//                        .onErrorReturn(WalksJournalViewState::ERROR));

        final Observable<WalksJournalViewState> loadAllWalks =
                intent(WalksJournalView::loadWalksIntent)
                        .doOnNext(ignore -> Timber.d("intent: loadAllWalks"))
                        .flatMap(ignore -> dataSource.getAllWalks()
                                .map(WalksJournalViewState::WALKS_LOADED)
                                .startWith(ignored -> WalksJournalViewState.LOADING())
                                .doOnError(WalksJournalViewState::ERROR));


//        final Observable<WalksJournalViewState> currentLocationTracking =
//                intent(WalksJournalView::checkGPSTurnedOn)
//                        .doOnNext(ignore -> Timber.d("intent: location tracking"))
//                        .flatMap(toStart -> locationFetcher.getLocationUpdates(MyWalksApplication.get())
//                                .map(WalksJournalViewState::LOCATION_UPDATE)
//                                .onErrorReturn(WalksJournalViewState::ERROR));

//        intent(WalksJournalView::startStopTracking)
//                .doOnNext(ignore -> Timber.d("intent: start stop tracking"))
//                .doOnNext(ignore -> {
//                    if (WalkTrackerService.isRunning(context))
//                        WalkTrackerService.stop(context);
//                    else
//                        WalkTrackerService.start(context);
//                })
//                .subscribe();
//
//        final Observable<WalksJournalViewState> allIntentsObservablesMerged =
//                Observable.merge(gpsTurnedOnCheck, walkTracking)
//                        .observeOn(AndroidSchedulers.mainThread());

        subscribeViewState(loadAllWalks, WalksJournalView::render);
    }
}
