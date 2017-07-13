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
        // On sending loadWalk intent from the view this code block loads the walk from the local DB
        // and then sends it to the view to be rendered
        final Observable<SingleWalkViewState> loadWalk =
                intent(SingleWalkView::loadWalkIntent)
                        .doOnNext(walkId -> Timber.d("intent: SingleWalkView::loadWalk"))
                        .flatMap(walkId -> dataSource.getWalk(walkId)
                                .map(SingleWalkViewState::WALK_LOADED)
                                .startWith(SingleWalkViewState.LOADING())
                                .onErrorReturn(SingleWalkViewState::ERROR));

        subscribeViewState(loadWalk, SingleWalkView::render);
    }
}
