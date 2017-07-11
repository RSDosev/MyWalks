package com.radodosev.mywalks.walksjournal.single_walk;

import android.support.annotation.IntDef;

import com.radodosev.mywalks.data.model.Walk;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import static com.radodosev.mywalks.walksjournal.single_walk.SingleWalkViewState.State.ERROR;
import static com.radodosev.mywalks.walksjournal.single_walk.SingleWalkViewState.State.LOADING;
import static com.radodosev.mywalks.walksjournal.single_walk.SingleWalkViewState.State.WALK_LOADED;

/**
 * Created by Rado on 7/8/2017.
 */

class SingleWalkViewState {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LOADING, WALK_LOADED, ERROR})
    @interface State {
        int LOADING = 1;
        int WALK_LOADED = 2;
        int ERROR = 3;
    }

    private final
    @State
    int type;
    private final Throwable error;
    private final Walk walk;

    SingleWalkViewState(int type, Throwable error, Walk walk) {
        this.type = type;
        this.error = error;
        this.walk = walk;
    }

    public Throwable getError() {
        return error;
    }

    public
    @State
    int getType() {
        return type;
    }

    public Walk getWalk() {
        return walk;
    }

    public static SingleWalkViewState ERROR(Throwable error) {
        return new SingleWalkViewState(ERROR, error, null);
    }

    public static SingleWalkViewState WALK_LOADED(Walk walk) {
        return new SingleWalkViewState(WALK_LOADED, null, walk);
    }

    public static SingleWalkViewState LOADING() {
        return new SingleWalkViewState(LOADING, null, null);
    }
}
