package com.radodosev.mywalks.walksjournal;

import com.radodosev.mywalks.data.model.Walk;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rado on 7/8/2017.
 */

class WalksJournalViewState {
    private final boolean loading;
    private final Throwable error;
    private final List<Walk> allWalks;
    private final Walk currentWalkToShow;

    WalksJournalViewState(final boolean loading, final Throwable error,
                          final List<Walk> walks, final Walk walk) {
        this.loading = loading;
        this.error = error;
        this.allWalks = walks;
        this.currentWalkToShow = walk;
    }

    public boolean isLoading() {
        return loading;
    }

    public Throwable getError() {
        return error;
    }

    public List<Walk> getAllWalks() {
        return allWalks;
    }

    public Walk getCurrentWalkToShow() {
        return currentWalkToShow;
    }

    public Builder builder() {
        return new Builder(this);
    }

    public static class Builder {
        private boolean loading;
        private Throwable error;
        private List<Walk> walks;
        private  Walk walk;

        public Builder() {
            this.walks = new ArrayList<>();
        }

        public Builder(final WalksJournalViewState oldState) {
            this.error = oldState.error;
            this.walk = oldState.currentWalkToShow;
            this.walks = oldState.allWalks;
        }

        public Builder loading(final boolean loading) {
            this.loading = loading;
            return this;
        }

        public Builder error(final Throwable error) {
            this.error = error;
            return this;
        }

        public Builder allWalks(final List<Walk> walks) {
            this.walks = walks;
            return this;
        }

        public Builder currentWalkToShow(final Walk walk) {
            this.walk = walk;
            return this;
        }

        public WalksJournalViewState build() {
            return new WalksJournalViewState(loading, error, walks, walk);
        }
    }

}
