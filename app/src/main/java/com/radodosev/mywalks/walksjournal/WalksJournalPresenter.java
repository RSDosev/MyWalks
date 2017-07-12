package com.radodosev.mywalks.walksjournal;

import com.hannesdorfmann.mosby3.mvi.MviBasePresenter;
import com.radodosev.mywalks.data.WalksDataSource;
import com.radodosev.mywalks.data.WalksLocalDataSource;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

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
        final Observable<WalksJournalViewStatePartialChanges> loadAllWalks =
                intent(WalksJournalView::loadWalksIntent)
                        .doOnNext(ignore -> Timber.d("intent: loadAllWalks"))
                        .flatMap(ignore -> dataSource.getAllWalks()
                                .map(walks -> (WalksJournalViewStatePartialChanges) new WalksJournalViewStatePartialChanges.AllWalksLoaded(walks))
                                .startWith(new WalksJournalViewStatePartialChanges.Loading())
                                .onErrorReturn(WalksJournalViewStatePartialChanges.Error::new));

        final Observable<WalksJournalViewStatePartialChanges> showSingleWalk =
                intent(WalksJournalView::selectWalkIntent)
                        .doOnNext(ignore -> Timber.d("intent: SHOW_SINGLE_WALK"))
                        .map(walk -> (WalksJournalViewStatePartialChanges) new WalksJournalViewStatePartialChanges.SingleWalkShown(walk))
                        .onErrorReturn(WalksJournalViewStatePartialChanges.Error::new);

        final Observable<WalksJournalViewStatePartialChanges> allIntentsObservablesMerged =
                Observable.merge(loadAllWalks, showSingleWalk)
                        .observeOn(AndroidSchedulers.mainThread());

        final WalksJournalViewState initialState = new WalksJournalViewState.Builder().loading(true).build();

        subscribeViewState(allIntentsObservablesMerged.scan(initialState, this::viewStateReducer).distinctUntilChanged()
                , WalksJournalView::render);
    }

    private WalksJournalViewState viewStateReducer(WalksJournalViewState previousState,
                                                   WalksJournalViewStatePartialChanges partialChanges) {
        if (partialChanges instanceof WalksJournalViewStatePartialChanges.Loading) {
            return previousState.builder()
                    .loading(true)
                    .error(null)
                    .build();
        }

        if (partialChanges instanceof WalksJournalViewStatePartialChanges.Error) {
            return previousState.builder()
                    .loading(false)
                    .error(((WalksJournalViewStatePartialChanges.Error) partialChanges).getError())
                    .build();
        }

        if (partialChanges instanceof WalksJournalViewStatePartialChanges.AllWalksLoaded) {
            return previousState.builder()
                    .loading(false)
                    .error(null)
                    .allWalks(((WalksJournalViewStatePartialChanges.AllWalksLoaded) partialChanges).getWalks())
                    .build();
        }

        if (partialChanges instanceof WalksJournalViewStatePartialChanges.SingleWalkShown) {
            return previousState.builder()
                    .loading(false)
                    .error(null)
                    .currentWalkToShow(((WalksJournalViewStatePartialChanges.SingleWalkShown) partialChanges).getWalk())
                    .build();
        }
        throw new IllegalStateException("Don't know how to reduce the partial state " + partialChanges);
    }
}
