package com.radodosev.mywalks.walksjournal.single_walk;

import com.hannesdorfmann.mosby3.mvi.MviBasePresenter;
import com.radodosev.mywalks.data.WalksDataSource;
import com.radodosev.mywalks.data.WalksLocalDataSource;

import io.reactivex.Observable;
import timber.log.Timber;

/**
 * Created by Rado on 7/8/2017.
 */

public class SingleWalkPresenter extends MviBasePresenter<SingleWalkView, SingleWalkViewState> {

    private WalksDataSource dataSource;

    public SingleWalkPresenter(WalksLocalDataSource walksLocalDataSource) {
        this.dataSource = walksLocalDataSource;
    }

    @Override
    protected void bindIntents() {
        //
//        final Observable<SingleWalkViewState> gpsTurnedOnCheck = intent(SingleWalkView::checkLocationRequirements)
//                .doOnNext(ignore -> Timber.d("intent: SingleWalkView::checkLocationRequirements"))
//                .flatMap(ignore -> locationFetcher.getLocationSettings(MyWalksApplication.newInstance())
//                        .map(locationSettingsStatus -> {
//                            if (locationSettingsStatus.areAllSettingsEnabled()) {
//                                return SingleWalkViewState.GPS_ON();
//                            } else if (locationSettingsStatus.isProblemRecoverable()) {
//                                return SingleWalkViewState.GPS_OFF(locationSettingsStatus.getStatus());
//                            }
//                            return SingleWalkViewState.GPS_NOT_AVAILABLE();
//                        })
//                        .onErrorReturn(SingleWalkViewState::ERROR));

        final Observable<SingleWalkViewState> loadAllWalks =
                intent(SingleWalkView::loadWalkIntent)
                        .doOnNext(walkId -> Timber.d("intent: load walk %d", walkId))
                        .flatMap(walkId -> dataSource.getWalk(walkId)
                                .map(SingleWalkViewState::WALK_LOADED)
                                .startWith(SingleWalkViewState.LOADING())
                                .onErrorReturn(SingleWalkViewState::ERROR));


//        final Observable<SingleWalkViewState> currentLocationTracking =
//                intent(SingleWalkView::checkLocationRequirements)
//                        .doOnNext(ignore -> Timber.d("intent: location tracking"))
//                        .flatMap(toStart -> locationFetcher.getLocationUpdates(MyWalksApplication.newInstance())
//                                .map(SingleWalkViewState::LOCATION_UPDATE)
//                                .onErrorReturn(SingleWalkViewState::ERROR));

//        intent(SingleWalkView::startStopTracking)
//                .doOnNext(ignore -> Timber.d("intent: start stop tracking"))
//                .doOnNext(ignore -> {
//                    if (WalkTrackerService.isRunning(context))
//                        WalkTrackerService.stop(context);
//                    else
//                        WalkTrackerService.start(context);
//                })
//                .subscribe();
//
//        final Observable<SingleWalkViewState> allIntentsObservablesMerged =
//                Observable.merge(gpsTurnedOnCheck, walkTracking)
//                        .observeOn(AndroidSchedulers.mainThread());

        subscribeViewState(loadAllWalks, new ViewStateConsumer<SingleWalkView, SingleWalkViewState>() {
            @Override
            public void accept(@android.support.annotation.NonNull SingleWalkView view, @android.support.annotation.NonNull SingleWalkViewState viewState) {
                view.render(viewState);
            }
        });
    }
}
