package com.radodosev.mywalks.walksjournal;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.transition.TransitionManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.hannesdorfmann.mosby3.mvi.MviActivity;
import com.radodosev.mywalks.R;
import com.radodosev.mywalks.data.model.Walk;
import com.radodosev.mywalks.domain.DI;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import permissions.dispatcher.RuntimePermissions;

import static com.radodosev.mywalks.walksjournal.WalksJournalViewState.State.ERROR;
import static com.radodosev.mywalks.walksjournal.WalksJournalViewState.State.LOADING;
import static com.radodosev.mywalks.walksjournal.WalksJournalViewState.State.WALKS_LOADED;

@RuntimePermissions
public class WalksJournalActivity extends MviActivity<WalksJournalView, WalksJournalPresenter>
        implements WalksJournalView {


    @BindView(R.id.recycle_view_walks_journal)
    RecyclerView walksView;
    @BindView(R.id.view_loading)
    View loadingView;
    @BindView(R.id.layout_root)
    ViewGroup rootView;

    private WalksJournalAdapter walksAdapter;
    private Unbinder viewUnbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walks_journal);
        viewUnbinder = ButterKnife.bind(this);

        initWalksVIew();
    }

    private void initWalksVIew() {
        walksAdapter = new WalksJournalAdapter(this);
        walksView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        walksView.setAdapter(walksAdapter);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewUnbinder.unbind();
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
            case ERROR:
                showError(viewState.getError());
                break;
            case LOADING:
                showLoading();
                break;
        }
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
}
