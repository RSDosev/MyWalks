/*
 * Copyright 2017 Hannes Dorfmann.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.radodosev.mywalks.walksjournal;

import android.app.Activity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.radodosev.mywalks.R;
import com.radodosev.mywalks.data.model.Walk;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import timber.log.Timber;

/**
 * @author Hannes Dorfmann
 */

public class WalksJournalAdapter extends RecyclerView.Adapter<WalksJournalAdapter.ViewHolder> {
    private final Activity activity;
    private List<Walk> walks;
    private PublishSubject<Walk> clickedWalks = PublishSubject.create();

    public WalksJournalAdapter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public WalksJournalAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return WalksJournalAdapter.ViewHolder.create(activity.getLayoutInflater());
    }

    @Override
    public void onBindViewHolder(WalksJournalAdapter.ViewHolder holder, int position) {
        holder.bind(walks.get(position));
    }

    @Override
    public int getItemCount() {
        return walks == null ? 0 : walks.size();
    }

    public void setWalks(final List<Walk> walks) {
        final List<Walk> beforeWalks = this.walks;
        this.walks = walks;
        if (beforeWalks == null) {
            notifyDataSetChanged();
        } else {
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return beforeWalks.size();
                }

                @Override
                public int getNewListSize() {
                    return walks.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return beforeWalks.get(oldItemPosition).equals(walks.get(newItemPosition));
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    return beforeWalks.get(oldItemPosition).equals(walks.get(newItemPosition));
                }
            });
            diffResult.dispatchUpdatesTo(this);
        }
    }

    public Observable<Walk> walkClickObservable() {
        return clickedWalks.doOnNext(selected -> Timber.d("clicked %s ", selected));
    }

    public Walk getWalkAt(int position) {
        return walks.get(position);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        public static ViewHolder create(LayoutInflater inflater) {
            return new ViewHolder(inflater.inflate(R.layout.list_item_walk, null, false));
        }

        private ViewHolder(View rootView) {
            super(rootView);
        }

        public void bind(Walk item) {

        }
    }

}
