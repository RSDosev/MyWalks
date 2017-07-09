package com.radodosev.mywalks.domain;

import com.radodosev.mywalks.data.WalksLocalDataSource;

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
}
