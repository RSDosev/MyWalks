package com.radodosev.mywalks.domain;

import android.app.Activity;

import com.radodosev.mywalks.dashboard.DashboardPresenter;
import com.radodosev.mywalks.data.WalksLocalDataSource;
import com.radodosev.mywalks.walksjournal.WalksJournalPresenter;
import com.tbruyelle.rxpermissions2.RxPermissions;

/**
 * Created by blue on 9.7.2017 г..
 */

public class DI {
    public static LocationFetcher provideLocationFetcher() {
        return LocationFetcher.newInstance();
    }

    public static WalksLocalDataSource provideLocalDataSource() {
        return WalksLocalDataSource.get();
    }

    public static WalksTracker provideWalksTracker() {
        return WalksTracker.get();
    }

    public static DashboardPresenter provideDashboardPresenter(Activity activity) {
        return new DashboardPresenter(new RxPermissions(activity), provideWalksTracker(), provideLocationFetcher());
    }

    public static WalksJournalPresenter provideWalksJournalPresenter() {
        return new WalksJournalPresenter(provideLocalDataSource());
    }
}
