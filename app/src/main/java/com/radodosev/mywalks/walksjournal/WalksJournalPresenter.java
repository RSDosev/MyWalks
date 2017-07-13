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
        // On sending loadAllWalks intent from the view this code block loads all walks from the local DB
        // and then sends them to the view to be rendered
        final Observable<WalksJournalViewStatePartialChanges> loadAllWalks =
                intent(WalksJournalView::loadWalksIntent)
                        .doOnNext(ignore -> Timber.d("intent: WalksJournalView::loadAllWalks"))
                        .flatMap(ignore -> dataSource.getAllWalks()
                                .map(walks -> (WalksJournalViewStatePartialChanges) new WalksJournalViewStatePartialChanges.AllWalksLoaded(walks))
                                .startWith(new WalksJournalViewStatePartialChanges.Loading())
                                .onErrorReturn(WalksJournalViewStatePartialChanges.Error::new));

        // On sending showSingleWalk intent from the view this code block
        // tels the view to rendered the single walk
        final Observable<WalksJournalViewStatePartialChanges> showSingleWalk =
                intent(WalksJournalView::viewSingleWalkIntent)
                        .doOnNext(ignore -> Timber.d("intent: WalksJournalView::showSingleWalk"))
                        .map(walk -> (WalksJournalViewStatePartialChanges) new WalksJournalViewStatePartialChanges.SingleWalkShown(walk))
                        .onErrorReturn(WalksJournalViewStatePartialChanges.Error::new);

        // Merges all intents from the view to be as single source
        final Observable<WalksJournalViewStatePartialChanges> allIntentsObservablesMerged =
                Observable.merge(loadAllWalks, showSingleWalk)
                        .observeOn(AndroidSchedulers.mainThread());

        final WalksJournalViewState initialState = new WalksJournalViewState.Builder().loading(true).build();

        subscribeViewState(allIntentsObservablesMerged.scan(initialState, this::viewStateReducer).distinctUntilChanged()
                , WalksJournalView::render);
    }

    /**
     * Creates {@link WalksJournalViewState} from {@WalksJournalViewStatePartialChanges}.
     * The main purpose is to not loose state data. For example when all data is loaded and then
     * new loading is presented, the new loading state will loose the previously loaded data.
     * This method fixes that as recreates new view states from previous ones.
     * @param previousState
     * @param partialChanges
     * @return {@link WalksJournalViewState} containing the changes from the partial changes
     */
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
