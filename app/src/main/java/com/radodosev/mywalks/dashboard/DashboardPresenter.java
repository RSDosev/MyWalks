package com.radodosev.mywalks.dashboard;

import com.hannesdorfmann.mosby3.mvi.MviBasePresenter;
import com.radodosev.mywalks.MyWalksApplication;
import com.radodosev.mywalks.data.WalksLocalDataSource;
import com.radodosev.mywalks.domain.LocationFetcher;
import com.radodosev.mywalks.domain.WalksTracker;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by Rado on 7/8/2017.
 */

public class DashboardPresenter extends MviBasePresenter<DashboardView, DashboardViewState> {

    WalksTracker walksTracker;
    private LocationFetcher locationFetcher;

    @Override
    protected void bindIntents() {
        walksTracker = new WalksTracker(LocationFetcher.get(), WalksLocalDataSource.get());
        locationFetcher = LocationFetcher.get();


        Observable<PartialStateChanges> initialLocationFetch = intent(DashboardView::loadCurrentLocation)
                .doOnNext(ignored -> Timber.d("intent: pull to refresh"))
                .flatMap(ignored -> locationFetcher.getLocationUpdates(MyWalksApplication.get())
                        .subscribeOn(Schedulers.io())
                        .map(items -> (PartialStateChanges) new PartialStateChanges.PullToRefreshLoaded(items))
                        .startWith(new PartialStateChanges.PullToRefreshLoading())
                        .onErrorReturn(PartialStateChanges.PullToRefeshLoadingError::new));

        Observable<PartialStateChanges> loadMoreFromGroup =
                intent(HomeView::loadAllProductsFromCategoryIntent).doOnNext(
                        categoryName -> Timber.d("intent: load more from category %s", categoryName))
                        .flatMap(categoryName -> feedLoader.loadProductsOfCategory(categoryName)
                                .subscribeOn(Schedulers.io())
                                .map(
                                        products -> (PartialStateChanges) new PartialStateChanges.ProductsOfCategoryLoaded(
                                                categoryName, products))
                                .startWith(new PartialStateChanges.ProductsOfCategoryLoading(categoryName))
                                .onErrorReturn(
                                        error -> new PartialStateChanges.ProductsOfCategoryLoadingError(categoryName,
                                                error)));

        Observable<PartialStateChanges> allIntentsObservable =
                Observable.merge(loadFirstPage, nextPage, pullToRefresh, loadMoreFromGroup)
                        .observeOn(AndroidSchedulers.mainThread());

        HomeViewState initialState = new HomeViewState.Builder().firstPageLoading(true).build();

        subscribeViewState(
                allIntentsObservable.scan(initialState, this::viewStateReducer).distinctUntilChanged(),
                HomeView::render);

        subscribeViewState(loadDetails, ProductDetailsView::render);
    }
}
