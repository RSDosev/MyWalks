package com.radodosev.mywalks.data;

import com.radodosev.mywalks.data.db.RoutePointsTable;
import com.radodosev.mywalks.data.db.WalksTable;
import com.radodosev.mywalks.data.db.WalksTable_Table;
import com.radodosev.mywalks.data.model.ModelMapper;
import com.radodosev.mywalks.data.model.Walk;
import com.raizlabs.android.dbflow.rx2.language.RXSQLite;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by Rado on 7/8/2017.
 */

public class WalksLocalDataSource implements WalksDataSource {
    private static class InstanceHolder {
        private static final WalksLocalDataSource INSTANCE = new WalksLocalDataSource();
    }

    public static WalksLocalDataSource get() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public void addWalk(Walk walk) {
        final WalksTable walksTable = ModelMapper.fromWalksToWalkTable(walk);
        walksTable.insert().subscribe();
        for (RoutePointsTable routePointsTable : walksTable.getRoutePoints()) {
            routePointsTable.setWalk(walksTable);
            routePointsTable.insert().subscribe();
        }
    }

    @Override
    public Observable<Walk> getWalk(long walkId) {
        return RXSQLite.rx(
                SQLite.select().from(WalksTable.class).where(WalksTable_Table.id.eq(walkId)))
                .querySingle()
                .map(ModelMapper::fromWalksTableToWalk)
                .toObservable();
    }

    @Override
    public Observable<List<Walk>> getAllWalks() {
        return RXSQLite.rx(
                SQLite.select().from(WalksTable.class))
                .queryList()
                .flatMapObservable(Observable::fromIterable)
                .map(ModelMapper::fromWalksTableToWalk)
                .toList()
                .toObservable();
    }
}
