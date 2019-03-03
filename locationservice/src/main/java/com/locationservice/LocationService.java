package com.locationservice;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.network.BaseModel;
import com.network.BaseVolley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import io.reactivex.subscribers.DisposableSubscriber;

public class LocationService extends Service{

    private static final String TAG = LocationService.class.getSimpleName();
    LocationRequest mLocationRequest = new LocationRequest();

    private static final long UPDATE_INTERVAL = 15000;
    private static final long MAX_WAIT_TIME = UPDATE_INTERVAL * 3;
    private static final long FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL / 2;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
        mLocationRequest.setMaxWaitTime(MAX_WAIT_TIME);

        int priority = LocationRequest.PRIORITY_HIGH_ACCURACY; //by default
        mLocationRequest.setPriority(priority);
        onConnected();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onConnected() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates( mLocationRequest,mLocationCallback, Looper.myLooper());
    }

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (!locationList.isEmpty()) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);
                if (location != null) {
                    Log.d(TAG, "== location != null");

                    DatabaseHelper mInstance = DatabaseHelper.getInstance(getApplicationContext());
                    Cursor cursor = mInstance.getAllData();

                    if(cursor.getCount() > 0) {
                        cursor.moveToLast();
                        String lat = cursor.getString(0);
                        String lng = cursor.getString(1);
                        if(lat.equalsIgnoreCase(location.getLatitude()+"")
                                && lng.equalsIgnoreCase(location.getLongitude()+"")) {
                            // nothing to do bcz old location and current location is same
                        } else {
                            mInstance.insertData(location.getLatitude() + "", location.getLongitude() + "");

                            if(cursor.getCount() >= 50) {
                                Cursor data = mInstance.getAllData();
                                try {
                                    JSONObject objectActual = new JSONObject();
                                    JSONArray jsonArray = new JSONArray();

                                    for (int i = 0; i < 50; i++) {
                                        JSONObject object = new JSONObject();
                                        object.put("LAT", data.getString(0));
                                        object.put("LNG", data.getString(1));
                                        jsonArray.put(object);
                                        data.moveToNext();
                                    }
                                    objectActual.put("location",jsonArray);
                                    getNetworkManger().startVolleyRequest("URL", objectActual, BaseModel.class, getSubscriber());
                                } catch (Exception e){

                                }
                            }
                        }
                    } else {
                        mInstance.insertData(location.getLatitude() + "", location.getLongitude() + "");
                    }

                }
            }
        }
    };

    private BaseVolley baseVolley;
    public BaseVolley getNetworkManger() {
        if(baseVolley == null) {
            baseVolley = new BaseVolley();
        }
        return baseVolley;
    }

    protected DisposableSubscriber<BaseModel> getSubscriber() {
        return new DisposableSubscriber<BaseModel>() {
            @Override
            public void onNext(BaseModel response) {
                if(response != null) {
                    if (!response.isError()) {
                        // deleting starting fifty data

                    } else {
                        // error
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                //when error comes
            }

            @Override
            public void onComplete() {
                // when complete api result
            }
        };
    }
}
