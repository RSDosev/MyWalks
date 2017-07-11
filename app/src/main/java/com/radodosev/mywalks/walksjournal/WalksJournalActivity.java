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
import com.radodosev.mywalks.data.WalksLocalDataSource;
import com.radodosev.mywalks.data.db.RoutePointsTable;
import com.radodosev.mywalks.data.model.Walk;
import com.radodosev.mywalks.domain.DI;
import com.radodosev.mywalks.walksjournal.single_walk.SingleWalkFragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;

import static com.radodosev.mywalks.walksjournal.WalksJournalViewState.State.ERROR;
import static com.radodosev.mywalks.walksjournal.WalksJournalViewState.State.LOADING;
import static com.radodosev.mywalks.walksjournal.WalksJournalViewState.State.SHOW_SINGLE_WALK;
import static com.radodosev.mywalks.walksjournal.WalksJournalViewState.State.WALKS_LOADED;

public class WalksJournalActivity extends MviActivity<WalksJournalView, WalksJournalPresenter>
        implements WalksJournalView {

    @BindView(R.id.recycle_view_walks_journal)
    RecyclerView walksView;
    @BindView(R.id.view_loading)
    View loadingView;
    @BindView(R.id.layout_root)
    ViewGroup rootView;

    private BottomSheetBehavior singleWalkView;
    private WalksJournalAdapter walksAdapter;
    private Unbinder viewUnbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walks_journal);
        viewUnbinder = ButterKnife.bind(this);

        initWalksView();
//        List<Walk.RoutePoint> points = new ArrayList<>();
//        for (int i=0; i<5; i++){
//            points.add(new Walk.RoutePoint(23.4, 12.43323));
//        }
//        Walk walk = new Walk(new Date(), new Date(), points);
//        WalksLocalDataSource.get().addWalk(walk);
    }

    private void initWalksView() {
        walksAdapter = new WalksJournalAdapter(this);
        walksView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        walksView.setAdapter(walksAdapter);

        singleWalkView = BottomSheetBehavior.from(findViewById(R.id.layout_single_walk));
        singleWalkView.setHideable(true);
        singleWalkView.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @NonNull
    @Override
    public WalksJournalPresenter createPresenter() {
        return DI.provideWalksJournalPresenter();
    }

    @Override
    public Observable<Boolean> loadWalksIntent() {
        return Observable.just(true);
    }

    @Override
    public Observable<Walk> selectWalkIntent() {
        return walksAdapter.walkClickObservable();
    }

    @Override
    public Observable<Walk> removeWalkIntent() {
        return null;
    }

    @Override
    public void render(WalksJournalViewState viewState) {
        switch (viewState.getType()) {
            case WALKS_LOADED:
                showAllWalks(viewState.getWalks());
                break;
            case SHOW_SINGLE_WALK:
                showSingleWalk(viewState.getSingleWalk());
                break;
            case ERROR:
                showError(viewState.getError());
                break;
            case LOADING:
                showLoading();
                break;
        }
    }

    private void showSingleWalk(Walk walk) {
        singleWalkView.setState(BottomSheetBehavior.STATE_EXPANDED);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.layout_single_walk, SingleWalkFragment.newInstance(walk.geId()), SingleWalkFragment.TAG)
                .commit();
    }

    private void showError(Throwable error) {
        Snackbar.make(findViewById(R.id.layout_root), error.getMessage(), Snackbar.LENGTH_LONG).show();
    }

    private void showLoading() {
        TransitionManager.beginDelayedTransition(rootView);
        loadingView.setVisibility(View.VISIBLE);
        walksView.setVisibility(View.INVISIBLE);
    }

    private void showAllWalks(List<Walk> walks) {
        TransitionManager.beginDelayedTransition(rootView);
        loadingView.setVisibility(View.INVISIBLE);
        walksView.setVisibility(View.VISIBLE);
        walksAdapter.setWalks(walks);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewUnbinder.unbind();
    }

    public static void start(Context context){
        context.startActivity(new Intent(context, WalksJournalActivity.class));
    }
}
