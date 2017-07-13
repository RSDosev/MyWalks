package com.radodosev.mywalks.walksjournal;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.transition.TransitionManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.hannesdorfmann.mosby3.mvi.MviActivity;
import com.radodosev.mywalks.R;
import com.radodosev.mywalks.data.model.Walk;
import com.radodosev.mywalks.di.DI;
import com.radodosev.mywalks.walksjournal.single_walk.SingleWalkFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;

public class WalksJournalActivity extends MviActivity<WalksJournalView, WalksJournalPresenter>
        implements WalksJournalView {

    // ----- Instance fields -----
    @BindView(R.id.recycle_view_walks_journal)
    RecyclerView walksView;
    @BindView(R.id.view_loading)
    View loadingView;
    @BindView(R.id.layout_root)
    ViewGroup rootView;

    private BottomSheetBehavior singleWalkView;
    private WalksJournalAdapter walksAdapter;
    private Unbinder viewUnbinder;

    // ----- Activity lifecycle logic -----
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walks_journal);
        viewUnbinder = ButterKnife.bind(this);

        initWalksView();
    }

    private void initWalksView() {
        walksAdapter = new WalksJournalAdapter(this);
        walksView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        walksView.setAdapter(walksAdapter);

        singleWalkView = BottomSheetBehavior.from(findViewById(R.id.layout_single_walk));
        singleWalkView.setHideable(true);
        singleWalkView.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @Override
    public void onBackPressed() {
        if (singleWalkView.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            singleWalkView.setState(BottomSheetBehavior.STATE_HIDDEN);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewUnbinder.unbind();
    }

    // ----- Creating the presenter -----
    @NonNull
    @Override
    public WalksJournalPresenter createPresenter() {
        return DI.provideWalksJournalPresenter();
    }

    // ----- Exposing the view intents-----
    @Override
    public Observable<Boolean> loadWalksIntent() {
        return Observable.just(true);
    }

    @Override
    public Observable<Walk> viewSingleWalkIntent() {
        return walksAdapter.walkClickObservable();
    }

    // ----- Rendering the view state -----
    @Override
    public void render(WalksJournalViewState viewState) {
        TransitionManager.beginDelayedTransition(rootView);
        renderLoading(viewState.isLoading());
        if (viewState.getError() != null)
            renderError(viewState.getError());

        if (viewState.getCurrentWalkToShow() != null)
            renderSingleWalk(viewState.getCurrentWalkToShow());

        if (!viewState.getAllWalks().isEmpty())
            renderWalksLoaded(viewState.getAllWalks());
    }

    private void renderLoading(final boolean toShow) {
        loadingView.setVisibility(toShow ? View.VISIBLE : View.GONE);
    }

    private void renderError(final Throwable error) {
        Snackbar.make(findViewById(R.id.layout_root), error.getMessage(), Snackbar.LENGTH_LONG).show();
    }

    private void renderSingleWalk(final Walk walk) {
        singleWalkView.setState(BottomSheetBehavior.STATE_EXPANDED);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.layout_single_walk, SingleWalkFragment.newInstance(walk.geId()), SingleWalkFragment.TAG)
                .commit();
    }

    private void renderWalksLoaded(final List<Walk> walks) {
        walksView.setVisibility(View.VISIBLE);
        walksAdapter.setWalks(walks);
    }

    // ----- Static helper method-----
    public static void start(Context context) {
        context.startActivity(new Intent(context, WalksJournalActivity.class));
    }
}
