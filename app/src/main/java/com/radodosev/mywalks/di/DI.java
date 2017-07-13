package com.radodosev.mywalks.di;

import android.app.Activity;

import com.hannesdorfmann.mosby3.mvi.MviPresenter;
import com.radodosev.mywalks.domain.LocationFetcher;
import com.radodosev.mywalks.domain.WalksTracker;
import com.radodosev.mywalks.walktracking.WalkTrackingPresenter;
import com.radodosev.mywalks.data.WalksLocalDataSource;
import com.radodosev.mywalks.walksjournal.WalksJournalPresenter;
import com.radodosev.mywalks.walksjournal.single_walk.SingleWalkPresenter;
import com.tbruyelle.rxpermissions2.RxPermissions;

/**
 * Created by blue on 9.7.2017 г.
 * Basic dependency injection
 */
public final class DI {
    private DI(){}

    public static LocationFetcher provideLocationFetcher() {
        return LocationFetcher.newInstance();
    }

    public static WalksLocalDataSource provideLocalDataSource() {
        return WalksLocalDataSource.get();
    }

    public static WalksTracker provideWalksTracker() {
        return WalksTracker.get();
    }

    public static WalkTrackingPresenter provideDashboardPresenter(Activity activity) {
        return new WalkTrackingPresenter(new RxPermissions(activity));
    }

    public static WalksJournalPresenter provideWalksJournalPresenter() {
        return new WalksJournalPresenter(provideLocalDataSource());
    }

    public static MviPresenter provideSingleWalkPresenter() {
        return new SingleWalkPresenter(provideLocalDataSource());
    }
}
