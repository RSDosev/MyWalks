package com.radodosev.mywalks.walksjournal;

import com.radodosev.mywalks.data.model.Walk;

import java.util.List;

/**
 * Created by Rado on 7/8/2017.
 */

public interface WalksJournalViewStatePartialChanges {

    final class Loading implements WalksJournalViewStatePartialChanges {

        @Override
        public String toString() {
            return "Loading{}";
        }
    }

    final class Error implements WalksJournalViewStatePartialChanges{
        private final Throwable error;

        public Error(final Throwable error) {
            this.error = error;
        }

        public Throwable getError() {
            return error;
        }

        @Override
        public String toString() {
            return "Error{" +
                    "error=" + error +
                    '}';
        }
    }

    final class AllWalksLoaded implements WalksJournalViewStatePartialChanges{
        private final List<Walk> walks;

        public AllWalksLoaded(final List<Walk> walks) {
            this.walks = walks;
        }

        public List<Walk> getWalks() {
            return walks;
        }

        @Override
        public String toString() {
            return "AllWalksLoaded{" +
                    "allWalks=" + walks +
                    '}';
        }
    }

    final class SingleWalkShown implements WalksJournalViewStatePartialChanges{
        private final Walk walk;

        public SingleWalkShown(final Walk walk) {
            this.walk = walk;
        }

        public Walk getWalk() {
            return walk;
        }

        @Override
        public String toString() {
            return "SingleWalkShown{" +
                    "walk=" + walk +
                    '}';
        }
    }
}
