package com.network;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;

public class NetworkUtils {

    private static final int TWO_G = 2;
    private static final int THREE_G = 3;
    private static final int FOUR_G = 4;
    private static final int WI_FI = 1;
    private static final int OTHER = -1;

    public static boolean isConnectedToNetwork(Context context) {
        return true;
    }

    public static int getNetworkClass(Context context) {
        if (context==null) {
            return OTHER;
        }
        ConnectivityManager mConnectivity =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = mConnectivity.getActiveNetworkInfo();
        int networkType = OTHER;
        if (info != null && info.isConnected()) {
            int type = info.getType();
            if (type == ConnectivityManager.TYPE_WIFI) {
                networkType = WI_FI;
            } else if (type == ConnectivityManager.TYPE_MOBILE) {
                int networkSubType = info.getSubtype();
                switch (networkSubType) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        networkType = TWO_G;
                        break;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                        networkType = THREE_G;
                        break;
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        networkType = FOUR_G;
                        break;
                    default:
                        networkType = OTHER;
                }
            }
        }
        return networkType;
    }

    public static String getDeviceIdOrAndroidId(Context context) {
        String id = getDeviceId(context);
        return TextUtils.isEmpty(id) ? getAndroidId(context) : id;
    }

    public static String getDeviceId(Context context) {
        if (canReadPhoneState(context)) {
            try {
                TelephonyManager tm = (TelephonyManager) context
                        .getSystemService(Context.TELEPHONY_SERVICE);
                return tm.getDeviceId();
            } catch (Exception ex) {
                Log.e("TelephonyManager:", "Fetching deviceId:" + ex.getMessage());
            }
        }
        return null;
    }

    public static String getAndroidId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    public static String getDeviceIdBySlotId(Context context,String predictedMethodName,int slotID){
        String imei = null;

        if (canReadPhoneState(context)) {

            TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            try{
                Class<?> telephonyClass = Class.forName(telephony.getClass().getName());

                Class<?>[] parameter = new Class[1];
                parameter[0] = int.class;
                Method getSimID = telephonyClass.getMethod(predictedMethodName, parameter);

                Object[] obParameter = new Object[1];
                obParameter[0] = slotID;
                Object ob_phone = getSimID.invoke(telephony, obParameter);

                if(ob_phone != null){
                    imei = ob_phone.toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return imei;
    }

    public static boolean hasSelfPermission(Context context, String permission) {
        if (context == null) {
            return false;
        }
        try {
            return ContextCompat.checkSelfPermission(context, permission) ==
                    PackageManager.PERMISSION_GRANTED;
        }
        catch (RuntimeException e){
            return false ;
        }
    }

    public static boolean canReadPhoneState(Context context) {
        return hasSelfPermission(context, Manifest.permission.READ_PHONE_STATE);
    }

}