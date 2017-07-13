package com.radodosev.mywalks.walktracking;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v13.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.hannesdorfmann.mosby3.mvi.MviActivity;
import com.jakewharton.rxbinding2.view.RxView;
import com.radodosev.mywalks.R;
import com.radodosev.mywalks.data.model.ModelMapper;
import com.radodosev.mywalks.data.model.Walk;
import com.radodosev.mywalks.di.DI;
import com.radodosev.mywalks.domain.LocationFetcher;
import com.radodosev.mywalks.utils.GoogleMapUtils;
import com.radodosev.mywalks.walksjournal.WalksJournalActivity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import static com.radodosev.mywalks.walktracking.WalkTrackingViewState.State.ERROR;
import static com.radodosev.mywalks.walktracking.WalkTrackingViewState.State.GPS_NOT_AVAILABLE;
import static com.radodosev.mywalks.walktracking.WalkTrackingViewState.State.GPS_OFF;
import static com.radodosev.mywalks.walktracking.WalkTrackingViewState.State.GPS_ON;
import static com.radodosev.mywalks.walktracking.WalkTrackingViewState.State.LOCATION_PERMISSION_NOT_GRANTED;
import static com.radodosev.mywalks.walktracking.WalkTrackingViewState.State.WALK_FINISHED;
import static com.radodosev.mywalks.walktracking.WalkTrackingViewState.State.WALK_IN_PROGRESS;
import static com.radodosev.mywalks.walktracking.WalkTrackingViewState.State.WALK_STARTED;

public class WalkTrackingActivity extends MviActivity<WalkTrackingView, WalkTrackingPresenter>
        implements WalkTrackingView, OnMapReadyCallback {
    private static final int REQUEST_CODE_CHECK_SETTINGS = 123;
    private static final int REQUEST_CODE_CHECK_LOCATION_PERMISSION = 321;

    // ----- Instance fields -----
    @BindView(R.id.button_start_stop_tracking)
    FloatingActionButton startStopTrackingButton;

    private GoogleMap mapView;
    private Unbinder viewUnbinder;
    private Subject<Boolean> trackWalkingIntent;
    private Subject<Boolean> checkLocationRequirementsIntent;

    // ----- Activity lifecycle logic -----
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewUnbinder = ButterKnife.bind(this);
        trackWalkingIntent = PublishSubject.create();
//        checkLocationRequirementsIntent = PublishSubject.create();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        checkLocationRequirementsIntent.onNext(true);
        trackWalkingIntent.onNext(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_show_all_walks:
                WalksJournalActivity.start(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewUnbinder.unbind();
    }

    // ----- Creating the presenter -----
    @NonNull
    @Override
    public WalkTrackingPresenter createPresenter() {
        return DI.provideDashboardPresenter(this);
    }

    // ----- Exposing the view intents-----
    @Override
    public Observable<Boolean> checkLocationRequirements() {
        return Observable.just(true);
    }

    @Override
    public Observable<Boolean> trackAWalk() {
        return trackWalkingIntent;
    }

    @Override
    public Observable<Boolean> startStopTracking() {
        return RxView.clicks(startStopTrackingButton).map(ignored -> true);
    }

    // ----- Rendering the view state -----
    @Override
    public void render(final WalkTrackingViewState viewState) {
        switch (viewState.getType()) {
            case LOCATION_PERMISSION_NOT_GRANTED:
                renderLocationPermissionNotGranted(viewState);
                break;
            case GPS_OFF:
                renderEnableLocationDialog(viewState);
                break;
            case GPS_NOT_AVAILABLE:
                renderGPSNoExistent();
                break;
            case GPS_ON:
                renderTheMap();
                break;
            case WALK_STARTED:
                renderWalkStarted();
                break;
            case WALK_IN_PROGRESS:
                renderWalkProgress(viewState);
                break;
            case WALK_FINISHED:
                renderWalkFinished(viewState);
                break;
            case ERROR:
                renderError(viewState);
                break;
        }
    }

    private void renderLocationPermissionNotGranted(final WalkTrackingViewState viewState) {
        renderMessage(getString(R.string.location_permissions_not_granted));
        if (viewState.getError() == null)
            return;
        // Render info dialog with rationale about the permission
        new AlertDialog.Builder(this)
                .setMessage(R.string.location_permissions_rationale)
                .setPositiveButton(getString(R.string.location_permission_rationale_positive_button), null)
                .create().show();
    }

    private void renderEnableLocationDialog(final WalkTrackingViewState viewState) {
        try {
            viewState.getLocationSettingsStatus().startResolutionForResult(
                    WalkTrackingActivity.this,
                    REQUEST_CODE_CHECK_SETTINGS);
        } catch (IntentSender.SendIntentException e) {
            // Ignore the error. It will not happen in the real world.
        }
    }

    void renderTheMap() {
        ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_view_main_map)).getMapAsync(this);
    }


    @SuppressWarnings({"MissingPermission"})
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapView = googleMap;
        mapView.setMyLocationEnabled(true);
        mapView.getUiSettings().setMapToolbarEnabled(false);
    }

    private void renderGPSNoExistent() {
        renderMessage(getString(R.string.notification_not_gps_available));
        startStopTrackingButton.setVisibility(View.GONE);
    }

    private void renderWalkStarted() {
        renderMessage(getString(R.string.notification_walk_start));
    }

    private void renderWalkProgress(WalkTrackingViewState viewState) {
        if (mapView == null)
            return;

        mapView.clear();
        startStopTrackingButton.setImageResource(R.drawable.ic_stop);

        final List<Walk.RoutePoint> currentPoints = viewState.getCurrentWalk().getRoutePoints();
        GoogleMapUtils.drawDottedRoute(mapView,
                ModelMapper.fromRoutePointsToLatLng(currentPoints));
        GoogleMapUtils.drawMarker(
                mapView,
                ModelMapper.fromRoutePointToLatLng(currentPoints.get(0)),
                getString(R.string.marker_label_walk_start));

        final Walk.RoutePoint currentPositions = currentPoints.get(currentPoints.size() - 1);
        mapView.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentPositions.getLatitude(),
                currentPositions.getLongitude()), 17f));
    }

    private void renderWalkFinished(WalkTrackingViewState viewState) {
        if (mapView == null)
            return;

        final List<Walk.RoutePoint> completedPoints = viewState.getCurrentWalk().getRoutePoints();
        if (!completedPoints.isEmpty())
            GoogleMapUtils.drawMarker(
                    mapView,
                    ModelMapper.fromRoutePointToLatLng(completedPoints.get(completedPoints.size() - 1)),
                    getString(R.string.marker_label_walk_end));

        renderMessage(getString(R.string.notification_walk_end));
        startStopTrackingButton.setImageResource(R.drawable.ic_directions_walk);
    }

    private void renderError(WalkTrackingViewState viewState) {
        if (viewState.getError() instanceof LocationFetcher.LocationPermissionNotGrantedException)
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_CHECK_LOCATION_PERMISSION);

        startStopTrackingButton.setImageResource(R.drawable.ic_directions_walk);
        renderMessage(viewState.getError().getMessage());
    }

    private void renderMessage(String message) {
        Snackbar.make(findViewById(R.id.layout_root), message, Snackbar.LENGTH_LONG).show();
    }

    // ----- Static helper method-----
    public static Intent getIntent(Context context) {
        final Intent startIntent = new Intent(context, WalkTrackingActivity.class);
        startIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return startIntent;
    }
}
