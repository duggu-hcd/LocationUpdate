package com.locationupdate;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.locationservice.AppSharedPreference;
import com.locationservice.LocationService;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends FragmentActivity {
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 2001;
    @BindView(R.id.startLocation)
    TextView startLocation;

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
                requestLocationUpdates();
            }
        }
    }

    public void requestLocationUpdates() {
        try {
            AppSharedPreference.setLocationRequest(this,true);
            Intent intent = new Intent(this, LocationService.class);
            startService(intent);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        updateButtonsState(AppSharedPreference.getLocationRequest(this));
    }

    public void removeLocationUpdates() {
        AppSharedPreference.setLocationRequest(this,false);
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

}
