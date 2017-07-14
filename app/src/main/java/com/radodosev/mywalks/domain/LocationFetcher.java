package com.radodosev.mywalks.domain;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.radodosev.mywalks.R;

import java.lang.annotation.Retention;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import timber.log.Timber;

import static com.radodosev.mywalks.domain.LocationFetcher.Mode.LOCATION;
import static com.radodosev.mywalks.domain.LocationFetcher.Mode.SETTINGS;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by Rado on 11/25/2016.
 * Rx based wrapper of the Google's FusedLocationApi for fetching the current location
 * with functionality to check locations's settings' status as well
 */
public class LocationFetcher implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    @Retention(SOURCE)
    @IntDef({SETTINGS, LOCATION})
    public @interface Mode {
        int SETTINGS = 1;
        int LOCATION = 2;
    }

    private
    @Mode
    int mode;
    private Context context;
    private GoogleApiClient googleApiClient;

    private ObservableEmitter<Location> locationEmitter;
    private ObservableEmitter<LocationSettingsStatus> locationSettingsEmitter;

    private LocationFetcher() {
    }

    public static LocationFetcher newInstance() {
        return new LocationFetcher();
    }

    /**
     * Checks if the settings for making location requests are on. GPS, Airplane mode and etc.
     *
     * @param context current Context
     * @return Observable which emits the location settings when they are checked
     */
    public Observable<LocationSettingsStatus> getLocationSettings(final Context context) {
        return Observable.create(emitter -> {
            locationSettingsEmitter = emitter;
            mode = SETTINGS;
            connectToLocationService(context.getApplicationContext());
        });
    }

    /**
     * Requests the current location on every 1 second or every 10 meters
     *
     * @param context current Context
     * @return Observable which emits the current location on every location change
     */
    public Observable<Location> getLocationUpdates(final Context context) {
        return Observable.create(emitter -> {
            locationEmitter = emitter;
            mode = LOCATION;
            connectToLocationService(context.getApplicationContext());
        });
    }

    private void connectToLocationService(final Context context) {
        this.context = context;

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        googleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        switch (mode) {
            case SETTINGS:
                requestCurrentLocationSettings();
                break;
            case LOCATION:
                requestLocation();
                break;
        }
    }

    private void requestCurrentLocationSettings() {
        if (locationSettingsEmitter.isDisposed())
            return;
        final PendingResult<LocationSettingsResult> locationSettingsResult =
                LocationServices.SettingsApi.checkLocationSettings(
                        googleApiClient,
                        new LocationSettingsRequest.Builder()
                                .addLocationRequest(createLocationRequest())
                                .setAlwaysShow(true)
                                .build());

        locationSettingsResult.setResultCallback(result -> {
            if (locationSettingsEmitter.isDisposed())
                return;
            switch (result.getStatus().getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    // All location settings are satisfied. The client can
                    // initialize location requests here.
                    locationSettingsEmitter.onNext(new LocationSettingsStatus());
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    locationSettingsEmitter.onNext(new LocationSettingsStatus(result.getStatus()));
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    // Location settings are not satisfied. However, we have no way
                    // to fix the settings so we won't show the dialog.
                    locationSettingsEmitter.onNext(new LocationSettingsStatus(result.getStatus(), false, false));
                    break;
            }
            locationSettingsEmitter.onComplete();
        });
    }

    private void requestLocation() {
        if (!isLocationSubscriberHere())
            return;
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            locationEmitter.onError(new LocationPermissionNotGrantedException(context.getString(R.string.location_permissions_not_granted)));
            disconnectFromLocationService();
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, createLocationRequest(), this);
    }

    private LocationRequest createLocationRequest() {
        final LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        locationRequest.setSmallestDisplacement(1);
        return locationRequest;
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (isLocationSubscriberHere()) {
            locationEmitter.onError(new IllegalStateException(context.getString(R.string.play_services_stopped_error)));
            disconnectFromLocationService();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (isLocationSubscriberHere()) {
            locationEmitter.onError(new IllegalStateException(context.getString(R.string.play_services_not_available_error)));
            disconnectFromLocationService();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (isLocationSubscriberHere())
            locationEmitter.onNext(location);
        else
            disconnectFromLocationService();
    }

    private boolean isLocationSubscriberHere() {
        return !locationEmitter.isDisposed();
    }

    private void disconnectFromLocationService() {
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
            googleApiClient = null;
        }
    }

    public static class LocationSettingsStatus {
        private final Status status;
        private final boolean allSettingsEnabled;
        private final boolean problemRecoverable;

        public LocationSettingsStatus(Status status) {
            this(status, false, true);
        }

        public LocationSettingsStatus() {
            this(null, true, true);
        }

        public LocationSettingsStatus(Status status, boolean allSettingsEnabled, boolean problemIsRecoverable) {
            this.status = status;
            this.allSettingsEnabled = allSettingsEnabled;
            this.problemRecoverable = problemIsRecoverable;
        }

        public Status getStatus() {
            return status;
        }

        public boolean areAllSettingsEnabled() {
            return allSettingsEnabled;
        }

        public boolean isProblemRecoverable() {
            return problemRecoverable;
        }
    }

    public static class LocationPermissionNotGrantedException extends IllegalStateException {
        public LocationPermissionNotGrantedException(String message) {
            super(message);
        }
    }
}
