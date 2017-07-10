package com.radodosev.mywalks.domain;

import com.radodosev.mywalks.dashboard.DashboardPresenter;
import com.radodosev.mywalks.data.WalksLocalDataSource;
import com.radodosev.mywalks.walksjournal.WalksJournalPresenter;

/**
 * Created by blue on 9.7.2017 г..
 */

public class DI {
    public static LocationFetcher provideLocationFetcher() {
        return LocationFetcher.get();
    }

    public static WalksLocalDataSource provideLocalDataSource() {
        return WalksLocalDataSource.get();
    }

    public static WalksTracker provideWalksTracker() {
        return WalksTracker.get();
    }

    public static DashboardPresenter provideDashboardPresenter() {
        return new DashboardPresenter(provideWalksTracker(), provideLocationFetcher());
    }

    public static WalksJournalPresenter provideWalksJournalPresenter() {
        return new WalksJournalPresenter(provideLocalDataSource());
    }
}
