package com.locationservice;

import android.content.Context;
import android.preference.PreferenceManager;

public class AppSharedPreference {

    final static String LOCATION_REQUEST = "LocationRequest";

    public static void setLocationRequest(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(LOCATION_REQUEST, value)
                .apply();
    }

    public static boolean getLocationRequest(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(LOCATION_REQUEST, false);
    }
}
