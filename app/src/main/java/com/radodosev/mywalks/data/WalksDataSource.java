package com.radodosev.mywalks.data;

import com.radodosev.mywalks.data.db.WalksTable;
import com.radodosev.mywalks.data.model.Walk;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created by Rado on 7/8/2017.
 */

public interface WalksDataSource {

    void addWalk(Walk walk);

    Observable<Walk> getWalk(long walkId);

    Observable<List<Walk>> getAllWalks();
}
