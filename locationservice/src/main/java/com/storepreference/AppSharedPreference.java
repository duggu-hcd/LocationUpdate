package com.storepreference;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.model.LatLngModel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

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

    public static void saveSharedPreferencesLogList(Context context, List<LatLngModel> location) {
        SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        Gson gson = new Gson();
        String json = gson.toJson(location);
        prefsEditor.putString("locationArray", json);
        prefsEditor.apply();
    }

    public static List<LatLngModel> loadSharedPreferencesLogList(Context context) {
        List<LatLngModel> locationList;
        Gson gson = new Gson();
        String json = PreferenceManager.getDefaultSharedPreferences(context).getString("locationArray", "");
        if (json.isEmpty()) {
            locationList = new ArrayList<>();
        } else {
            Type type = new TypeToken<List<LatLngModel>>() {
            }.getType();
            locationList = gson.fromJson(json, type);
        }
        return locationList;
    }
}
