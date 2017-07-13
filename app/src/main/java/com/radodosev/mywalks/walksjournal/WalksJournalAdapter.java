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
import android.content.Context;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.radodosev.mywalks.R;
import com.radodosev.mywalks.data.model.Walk;
import com.radodosev.mywalks.utils.CommonUtils;
import com.radodosev.mywalks.utils.GoogleMapUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
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
        return WalksJournalAdapter.ViewHolder.create(parent, activity.getLayoutInflater());
    }

    @Override
    public void onBindViewHolder(WalksJournalAdapter.ViewHolder holder, int position) {
        holder.bind(walks.get(position), walk -> {
            clickedWalks.onNext(walk);
        });
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View rootView;
        @BindView(R.id.text_view_start_time)
        TextView startTime;
        @BindView(R.id.button_navigate_to_start)
        View navigateToStartButton;
        @BindView(R.id.text_view_end_time)
        TextView endTime;
        @BindView(R.id.button_navigate_to_finish)
        View navigateToEndButton;
        @BindView(R.id.text_view_distance)
        TextView distanceView;
        @BindView(R.id.text_view_max_speed)
        TextView maxSpeedView;
        @BindView(R.id.text_view_average_speed)
        TextView avgSpeedView;

        public static ViewHolder create(ViewGroup parent, LayoutInflater inflater) {
            return new ViewHolder(inflater.inflate(R.layout.list_item_walk, parent, false));
        }

        private ViewHolder(View rootView) {
            super(rootView);
            this.rootView = rootView;
            ButterKnife.bind(this, rootView);
            distanceView.setIncludeFontPadding(false);
            avgSpeedView.setIncludeFontPadding(false);
        }

        public void bind(Walk walk, OnWalkSelectedListener onWalkSelectedListener) {
            Context context = rootView.getContext();
            rootView.setOnClickListener(view -> onWalkSelectedListener.onWalkSelected(walk));
            startTime.setText(CommonUtils.formatDateInMyLocale(walk.getStartTime()));
            endTime.setText(CommonUtils.formatDateInMyLocale(walk.getEndTime()));
            navigateToStartButton.setOnClickListener(view -> {
                openLocationInMapsShowError(context, walk.getRoutePoints().get(0));
            });
            navigateToEndButton.setOnClickListener(view -> {
                final Walk.RoutePoint lastLocation = walk.getRoutePoints().get(walk.getRoutePoints().size() - 1);
                openLocationInMapsShowError(context, lastLocation);
            });
            distanceView.setText(CommonUtils.formatDistance(context, walk.getDistanceInMeters()));
            maxSpeedView.setText(CommonUtils.formatSpeed(context, walk.getMaxSpeed()));
            avgSpeedView.setText(CommonUtils.formatSpeed(context, walk.getAverageSpeed()));
        }

        private void openLocationInMapsShowError(Context context, Walk.RoutePoint firstLocation) {
            if (!GoogleMapUtils.openLocationIfPossible(context, firstLocation.getLatitude(),
                    firstLocation.getLongitude()))
                Toast.makeText(context, R.string.navigate_to_location_error, Toast.LENGTH_SHORT).show();
        }

        public interface OnWalkSelectedListener {
            void onWalkSelected(Walk walk);
        }
    }
}
