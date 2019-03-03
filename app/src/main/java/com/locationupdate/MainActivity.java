package com.locationupdate;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.locationservice.LocationUpdateService;
import com.storepreference.AppSharedPreference;
import com.locationservice.LocationService;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends FragmentActivity {//implements GoogleApiClient.ConnectionCallbacks,
//        GoogleApiClient.OnConnectionFailedListener{
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 2001;
    private static final String TAG = MainActivity.class.getSimpleName();
    @BindView(R.id.startLocation)
    TextView startLocation;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    private static final long UPDATE_INTERVAL = 7000;
    private static final long FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL / 2;
    private static final long MAX_WAIT_TIME = UPDATE_INTERVAL * 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        startLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if the user revoked runtime permissions.
                if(checkPlayServices()) {
                    if (!checkPermissions()) {
                        requestPermissions();
                    } else {
                        if (((TextView) view).getText().toString().equalsIgnoreCase(getString(R.string.stop_location))) {
                            removeLocationUpdates();
                        } else {
                            requestLocationUpdates();
                        }
                    }
                } else {
                    Toast.makeText(MainActivity.this,"You don't have google play service.",Toast.LENGTH_LONG).show();
                }
            }
        });
//        buildGoogleApiClient();
        updateButtonsState(AppSharedPreference.getLocationRequest(this));
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        if (shouldProvideRationale) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                buildGoogleApiClient();
                requestLocationUpdates();
            }
        }
    }

    public void requestLocationUpdates() {
        try {
            AppSharedPreference.setLocationRequest(this,true);
            Intent intent = new Intent(this, LocationService.class);
            startService(intent);

//            LocationServices.FusedLocationApi.requestLocationUpdates(
//                    mGoogleApiClient, mLocationRequest, getPendingIntent());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        updateButtonsState(AppSharedPreference.getLocationRequest(this));
    }

    public void removeLocationUpdates() {
        AppSharedPreference.setLocationRequest(this,false);

//        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,
//                getPendingIntent());
        stopService(new Intent(this, LocationService.class));
        updateButtonsState(AppSharedPreference.getLocationRequest(this));
    }


    private void updateButtonsState(boolean requestingLocationUpdates) {
        if (requestingLocationUpdates) {
            startLocation.setText(getString(R.string.stop_location));
        } else {
            startLocation.setText(getString(R.string.start_location));
        }
    }

    protected boolean checkPlayServices() {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS;
    }

//    @Override
//    public void onConnected(@Nullable Bundle bundle) {
//        Log.i(TAG, "GoogleApiClient connected");
//    }
//
//    private PendingIntent getPendingIntent() {
//        Intent intent = new Intent(this, LocationUpdateService.class);
//        intent.setAction(LocationUpdateService.ACTION_UPDATES);
//        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//    }
//
//    @Override
//    public void onConnectionSuspended(int i) {
//        final String text = "Connection suspended";
//        Log.w(TAG, text + ": Error code: " + i);
//    }
//
//    @Override
//    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//        final String text = "Exception while connecting to Google Play services";
//        Log.w(TAG, text + ": " + connectionResult.getErrorMessage());
//    }
//
//    private void buildGoogleApiClient() {
//        if (mGoogleApiClient != null) {
//            return;
//        }
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addConnectionCallbacks(this)
//                .enableAutoManage(this, this)
//                .addApi(LocationServices.API)
//                .build();
//        createLocationRequest();
//    }
//
//    private void createLocationRequest() {
//        mLocationRequest = new LocationRequest();
//
//        mLocationRequest.setInterval(UPDATE_INTERVAL);
//
//        // Sets the fastest rate for active location updates. This interval is exact, and your
//        // application will never receive updates faster than this value.
//        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//
//        // Sets the maximum time when batched location updates are delivered. Updates may be
//        // delivered sooner than this interval.
//        mLocationRequest.setMaxWaitTime(MAX_WAIT_TIME);
//    }
}
