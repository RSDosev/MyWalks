package com.radodosev.mywalks.data;

import com.radodosev.mywalks.data.model.Walk;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by Rado on 7/8/2017.
 * General interface for accessing Walks data.
 * The so called Android's implementation of Repository design pattern.
 */

public interface WalksDataSource {

    void addWalk(Walk walk);

    Observable<Walk> getWalk(long walkId);

    Observable<List<Walk>> getAllWalks();
}
