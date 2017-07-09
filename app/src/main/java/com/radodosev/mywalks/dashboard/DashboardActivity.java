package com.radodosev.mywalks.dashboard;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import com.radodosev.mywalks.domain.DI;
import com.radodosev.mywalks.utils.GoogleMapUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static com.radodosev.mywalks.dashboard.DashboardViewState.State.ERROR;
import static com.radodosev.mywalks.dashboard.DashboardViewState.State.GPS_NOT_AVAILABLE;
import static com.radodosev.mywalks.dashboard.DashboardViewState.State.GPS_OFF;
import static com.radodosev.mywalks.dashboard.DashboardViewState.State.GPS_ON;
import static com.radodosev.mywalks.dashboard.DashboardViewState.State.WALK_FINISHED;
import static com.radodosev.mywalks.dashboard.DashboardViewState.State.WALK_IN_PROGRESS;
import static com.radodosev.mywalks.dashboard.DashboardViewState.State.WALK_STARTED;

@RuntimePermissions
public class DashboardActivity extends MviActivity<DashboardView, DashboardPresenter>
        implements DashboardView, OnMapReadyCallback {
    private static final int REQUEST_CODE_CHECK_SETTINGS = 123;

    @BindView(R.id.button_start_stop_tracking)
    FloatingActionButton startStopTrackingButton;

    private GoogleMap mapView;
    private Unbinder viewUnbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewUnbinder = ButterKnife.bind(this);
    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    void onLocationPermissionsGranted() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_view_main_map);
        mapFragment.getMapAsync(this);
    }

    @SuppressWarnings({"MissingPermission"})
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapView = googleMap;
        mapView.setMyLocationEnabled(true);
        mapView.getUiSettings().setMapToolbarEnabled(false);
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

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @NonNull
    @Override
    public DashboardPresenter createPresenter() {
        return new DashboardPresenter(DI.provideWalksTracker(), DI.provideLocationFetcher());
    }

    @Override
    public Observable<Boolean> checkGPSTurnedOn() {
        return Observable.just(true);
    }

    @Override
    public Observable<Boolean> trackAWalk() {
        return Observable.just(true);
    }

    @Override
    public Observable<Boolean> startStopTracking() {
        return RxView.clicks(startStopTrackingButton).map(ignored -> true);
    }

    @Override
    public void render(final DashboardViewState viewState) {
        switch (viewState.getType()) {
            case GPS_OFF:
                showEnableLocationDialog(viewState);
                break;
            case GPS_NOT_AVAILABLE:
                showGPSNoExistent();
                break;
            case GPS_ON:
                DashboardActivityPermissionsDispatcher.onLocationPermissionsGrantedWithCheck(this);
                break;
            case WALK_STARTED:
                showWalkStarted();
                break;
            case WALK_IN_PROGRESS:
                showWalkProgress(viewState);
                break;
            case WALK_FINISHED:
                showWalkFinished(viewState);
                break;
            case ERROR:
                showError(viewState);
                break;
        }
    }

    private void showEnableLocationDialog(DashboardViewState viewState) {
        try {
            // Show the dialog by calling startResolutionForResult(),
            // and check the result in onActivityResult().
            viewState.getLocationSettingsStatus().startResolutionForResult(
                    DashboardActivity.this,
                    REQUEST_CODE_CHECK_SETTINGS);
        } catch (IntentSender.SendIntentException e) {
            // Ignore the error. It will not happen in the real world.
        }
    }

    private void showGPSNoExistent() {
        showMessage("No GPS module! Sorry, nothing to do about it!");
        startStopTrackingButton.setVisibility(View.GONE);
    }

    private void showWalkStarted() {
        showMessage("Walk started...");
        startStopTrackingButton.setImageResource(R.drawable.ic_stop);
    }

    private void showWalkProgress(DashboardViewState viewState) {
        if (mapView == null)
            return;

        mapView.clear();
        final List<Walk.RoutePoint> currentPoints = viewState.getCurrentWalk().getRoutePoints();
        GoogleMapUtils.drawDottedRoute(mapView,
                ModelMapper.fromRoutePointsToLatLng(currentPoints));
        GoogleMapUtils.drawMarker(
                mapView,
                ModelMapper.fromRoutePointToLatLng(currentPoints.get(0)),
                "Here you started");

        Walk.RoutePoint lastPoint = currentPoints.get(currentPoints.size() - 1);
        mapView.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastPoint.getLatitude(), lastPoint.getLongitude()), 17f));
    }

    private void showWalkFinished(DashboardViewState viewState) {
        if (mapView == null)
            return;

        final List<Walk.RoutePoint> completedPoints = viewState.getCurrentWalk().getRoutePoints();
        if (!completedPoints.isEmpty())
            GoogleMapUtils.drawMarker(
                    mapView,
                    ModelMapper.fromRoutePointToLatLng(completedPoints.get(completedPoints.size() - 1)),
                    "Here you stopped");

        showMessage("Walk finished...");
        startStopTrackingButton.setImageResource(R.drawable.ic_directions_walk);
    }

    private void showError(DashboardViewState viewState) {
        startStopTrackingButton.setImageResource(R.drawable.ic_directions_walk);
        showMessage(viewState.getError().getMessage());
    }

    private void showMessage(String message) {
        Snackbar.make(findViewById(R.id.layout_root), message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        switch (requestCode) {
            case REQUEST_CODE_CHECK_SETTINGS:
                switch (resultCode) {
                    case RESULT_OK:
                        // All required changes were successfully made
                        DashboardActivityPermissionsDispatcher.onLocationPermissionsGrantedWithCheck(this);
                        break;
                    case RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        finish();
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        DashboardActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewUnbinder.unbind();
    }
}
