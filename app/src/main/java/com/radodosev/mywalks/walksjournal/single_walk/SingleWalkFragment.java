package com.radodosev.mywalks.walksjournal.single_walk;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.hannesdorfmann.mosby3.mvi.MviFragment;
import com.hannesdorfmann.mosby3.mvi.MviPresenter;
import com.radodosev.mywalks.R;
import com.radodosev.mywalks.data.model.ModelMapper;
import com.radodosev.mywalks.data.model.Walk;
import com.radodosev.mywalks.domain.DI;
import com.radodosev.mywalks.utils.CommonUtils;
import com.radodosev.mywalks.utils.GoogleMapUtils;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import static com.radodosev.mywalks.walksjournal.single_walk.SingleWalkViewState.State.ERROR;
import static com.radodosev.mywalks.walksjournal.single_walk.SingleWalkViewState.State.LOADING;
import static com.radodosev.mywalks.walksjournal.single_walk.SingleWalkViewState.State.WALK_LOADED;


/**
 * Created by Rado on 7/11/2017.
 */

public class SingleWalkFragment extends MviFragment implements SingleWalkView, OnMapReadyCallback {
    public static String TAG = SingleWalkFragment.class.getSimpleName();
    private static String EXTRA_WALK_ID = "EXTRA_WALK_ID";


    private GoogleMap mapView;
    private View loadingView;
    private long walkId;
    private Subject<Long> onMapLoadedIntent = PublishSubject.create();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        extractWalkId(savedInstanceState);

        final View contentView = inflater.inflate(R.layout.dialog_single_walk, null, false);
        loadingView = contentView.findViewById(R.id.view_loading);

        return contentView;
    }

    private void extractWalkId(Bundle savedInstanceState) {
        if (savedInstanceState != null)
            walkId = savedInstanceState.getLong(EXTRA_WALK_ID);
        else
            walkId = getArguments().getLong(EXTRA_WALK_ID);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Obtain the SupportMapFragment and newInstance notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map_view_single_walk_map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapView = googleMap;
        mapView.getUiSettings().setMapToolbarEnabled(false);
        onMapLoadedIntent.onNext(walkId);
    }

    @NonNull
    @Override
    public MviPresenter createPresenter() {
        return DI.provideSingleWalkPresenter();
    }

    @Override
    public Observable<Long> loadWalkIntent() {
        return onMapLoadedIntent;
    }

    @Override
    public void render(SingleWalkViewState viewState) {
        switch (viewState.getType()) {
            case WALK_LOADED:
                showTheWalk(viewState.getWalk());
                break;
            case ERROR:
                showError(viewState.getError());
                break;
            case LOADING:
                showLoading();
                break;
        }
    }

    private void showError(Throwable error) {
        Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_LONG).show();
    }

    private void showLoading() {
        loadingView.setVisibility(View.VISIBLE);
    }

    private void showTheWalk(Walk walk) {
        loadingView.setVisibility(View.INVISIBLE);

        if (mapView == null || walk.getRoutePoints().isEmpty())
            return;

        final List<LatLng> points =  ModelMapper.fromRoutePointsToLatLng(walk.getRoutePoints());

        final LatLngBounds.Builder latLngBounds = new LatLngBounds.Builder();
        for (LatLng point: points) {
            latLngBounds.include(point);
        }
        mapView.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds.build(), CommonUtils.dpToPx(50)));

        GoogleMapUtils.drawDottedRoute(mapView, points);
        GoogleMapUtils.drawMarker(mapView, points.get(0), "Here you started");
        GoogleMapUtils.drawMarker(mapView, points.get(points.size()-1), "Here you stopped");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong(EXTRA_WALK_ID, walkId);
        super.onSaveInstanceState(outState);
    }

    public static Fragment newInstance(long walkId) {
        final Bundle arguments = new Bundle();
        arguments.putLong(EXTRA_WALK_ID, walkId);
        final SingleWalkFragment singleWalkView = new SingleWalkFragment();
        singleWalkView.setArguments(arguments);
        return singleWalkView;
    }
}
