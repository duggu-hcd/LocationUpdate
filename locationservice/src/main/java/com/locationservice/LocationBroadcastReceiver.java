package com.locationservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;

import com.google.android.gms.location.LocationResult;

import java.util.List;

public class LocationBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_PROCESS_UPDATES = "com.locationservice.action" + ".PROCESS_UPDATES";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PROCESS_UPDATES.equals(action)) {
                LocationResult result = LocationResult.extractResult(intent);
                if (result != null) {
                    List<Location> locations = result.getLocations();
                    DatabaseHelper mInstance = DatabaseHelper.getInstance(context);
                    Cursor cursor = mInstance.getAllData();

                    if(cursor != null ) {
                        cursor.moveToLast();
                        String lat = cursor.getString(1);
                        String lng = cursor.getString(2);
                        if(lat.equalsIgnoreCase(locations.get(0).getLatitude()+"")
                                && lng.equalsIgnoreCase(locations.get(0).getLongitude()+"")) {
                            // nothing to do bcz old location and current location is same
                        } else {
                            mInstance.insertData(locations.get(0).getLatitude() + "", locations.get(0).getLongitude() + "");
                        }
                    } else {
                        mInstance.insertData(locations.get(0).getLatitude() + "", locations.get(0).getLongitude() + "");
                    }

                    if(cursor != null && cursor.getCount() >= 50) {
                        for (int i = 0; i < cursor.getCount(); i++) {

                        }
                    }
                }
            }
        }
    }
}