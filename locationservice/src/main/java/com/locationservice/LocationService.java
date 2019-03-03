package com.locationservice;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.model.LatLngModel;
import com.network.BaseModel;
import com.network.BaseVolley;
import com.storepreference.AppSharedPreference;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.reactivex.subscribers.DisposableSubscriber;

public class LocationService extends Service{

    private static final String TAG = LocationService.class.getSimpleName();
    LocationRequest mLocationRequest = new LocationRequest();

    private static final long UPDATE_INTERVAL = 7000;
    private static final long MAX_WAIT_TIME = UPDATE_INTERVAL;
    private static final long FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL / 3;

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
        BaseVolley.init(getApplicationContext());
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
                    List<LatLngModel> latLngModelList = AppSharedPreference.loadSharedPreferencesLogList(getApplicationContext());

                    if(latLngModelList.isEmpty()) {
                        ArrayList<LatLngModel> lngModels = new ArrayList<>();
                        lngModels.add(new LatLngModel(location.getLatitude()+"", location.getLongitude() + ""));
                        AppSharedPreference.saveSharedPreferencesLogList(getApplicationContext() ,lngModels );
                    } else {
                        String lat = latLngModelList.get(latLngModelList.size()-1).getLat();
                        String lng = latLngModelList.get(latLngModelList.size()-1).getLng();
                        if(!(lat.equalsIgnoreCase(location.getLatitude()+"")
                                && lng.equalsIgnoreCase(location.getLongitude()+""))) {
                            latLngModelList.add(new LatLngModel(location.getLatitude()+"", location.getLongitude() + ""));
                            AppSharedPreference.saveSharedPreferencesLogList(getApplicationContext() ,latLngModelList );
                            if(latLngModelList.size() >= 50) {
                                try {
                                    JSONObject objectActual = new JSONObject();
                                    JSONArray jsonArray = new JSONArray();

                                    for (int i = 0; i < 50; i++) {
                                        LatLngModel latLngModel = latLngModelList.get(i);
                                        if(latLngModel !=  null) {
                                            JSONObject object = new JSONObject();
                                            object.put("LAT", latLngModel.getLat());
                                            object.put("LNG", latLngModel.getLng());
                                            jsonArray.put(object);
                                        }
                                    }
                                    objectActual.put("location",jsonArray);
                                    getNetworkManger().startVolleyRequest("URL", objectActual, BaseModel.class, getSubscriber());
                                } catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }
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
                        List<LatLngModel> latLngModelList = AppSharedPreference.loadSharedPreferencesLogList(getApplicationContext());
                        Iterator itr = latLngModelList.iterator();
                        for (int i = 0; i < 50; i++) {
                            itr.remove();
                            itr.next();
                        }
                        AppSharedPreference.saveSharedPreferencesLogList(getApplicationContext(),latLngModelList);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}