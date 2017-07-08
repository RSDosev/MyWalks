package com.radodosev.mywalks.dashboard;

import java.util.Collections;
import java.util.List;

/**
 * Created by Rado on 7/8/2017.
 */

class DashboardViewState {
    private final boolean loading;
    private final Throwable error;
    private final List data;

    DashboardViewState(boolean loading, Throwable error, List data) {
        this.loading = loading;
        this.error = error;
        this.data = data;
    }

    public boolean isLoading() {
        return loading;
    }

    public List getData() {
        return data;
    }

    public Throwable getError() {
        return error;
    }

    public static final class Builder {
        private boolean loading;
        private Throwable error;
        private List data;

        public Builder() {
            data = Collections.emptyList();
        }

        public Builder loading(boolean loading) {
            this.loading = loading;
            return this;
        }

        public Builder data(List data) {
            this.data = data;
            return this;
        }

        public Builder error(Throwable error) {
            this.error = error;
            return this;
        }

        public DashboardViewState build() {
            return new DashboardViewState(loading, error, data);
        }
    }
}
