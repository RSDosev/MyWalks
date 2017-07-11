package com.radodosev.mywalks.walksjournal;

import android.support.annotation.IntDef;

import com.google.android.gms.common.api.Status;
import com.radodosev.mywalks.data.model.Walk;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;

import static com.radodosev.mywalks.walksjournal.WalksJournalViewState.State.SHOW_SINGLE_WALK;
import static com.radodosev.mywalks.walksjournal.WalksJournalViewState.State.WALKS_LOADED;
import static com.radodosev.mywalks.walksjournal.WalksJournalViewState.State.ERROR;
import static com.radodosev.mywalks.walksjournal.WalksJournalViewState.State.LOADING;

/**
 * Created by Rado on 7/8/2017.
 */

class WalksJournalViewState {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LOADING, WALKS_LOADED, SHOW_SINGLE_WALK, ERROR})
    @interface State {
        int LOADING = 1;
        int WALKS_LOADED = 2;
        int SHOW_SINGLE_WALK = 3;
        int ERROR = 4;
    }

    private final
    @State
    int type;
    private final Throwable error;
    private final List<Walk> walks;

    WalksJournalViewState(int type, Throwable error, List<Walk> walks) {
        this.type = type;
        this.error = error;
        this.walks = walks;
    }

    public Throwable getError() {
        return error;
    }

    public
    @State
    int getType() {
        return type;
    }

    public List<Walk> getWalks() {
        return walks;
    }

    public Walk getSingleWalk() {
        return walks.get(0);
    }

    public static WalksJournalViewState ERROR(Throwable error) {
        return new WalksJournalViewState(ERROR, error, null);
    }

    public static WalksJournalViewState WALKS_LOADED(List<Walk> walks) {
        return new WalksJournalViewState(WALKS_LOADED, null, walks);
    }

    public static WalksJournalViewState SHOW_SINGLE_WALK(Walk walk) {
        return new WalksJournalViewState(SHOW_SINGLE_WALK, null, Arrays.asList(walk));
    }

    public static WalksJournalViewState LOADING() {
        return new WalksJournalViewState(LOADING, null, null);
    }
}
