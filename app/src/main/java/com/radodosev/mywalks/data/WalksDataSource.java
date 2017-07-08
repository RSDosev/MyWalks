package com.radodosev.mywalks.data;

import com.radodosev.mywalks.data.model.Walk;

import java.util.List;

import io.reactivex.Single;

/**
 * Created by Rado on 7/8/2017.
 */

public interface WalksDataSource {

    Single<Long> addWalk(Walk walk);

    Single<List<Walk>> getAllWalks();
}
