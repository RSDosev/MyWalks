package com.radodosev.mywalks.dashboard;

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hannesdorfmann.mosby3.mvi.MviActivity;
import com.jakewharton.rxbinding2.view.RxView;
import com.radodosev.mywalks.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;

public class DashboardActivity extends MviActivity<DashboardView, DashboardPresenter>
    implements DashboardView, OnMapReadyCallback {

    @BindView(R.id.button_start_stop_tracking)
    View startStopTrackingButton;

    private GoogleMap mMap;
    private Unbinder viewUnbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewUnbinder = ButterKnife.bind(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_view_main_map);
        mapFragment.getMapAsync(this);
    }

    @NonNull
    @Override
    public DashboardPresenter createPresenter() {
        return new DashboardPresenter();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
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
                Snackbar.make(findViewById(R.id.layout_root), "Click on row to know more details", Snackbar.LENGTH_LONG)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        }).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Observable<Boolean> loadCurrentLocation() {
        return Observable.just(true);
    }

    @Override
    public Observable<Boolean> startStopTracking() {
        return RxView.clicks(startStopTrackingButton).map(ignored -> true);
    }

    @Override
    public void render(DashboardViewState viewState) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewUnbinder.unbind();
    }
}
