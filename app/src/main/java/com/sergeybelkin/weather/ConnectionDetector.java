package com.sergeybelkin.weather;

import android.app.Service;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;

public class ConnectionDetector {

    public static boolean isConnected(Context context){
        ConnectivityManager connectivity = (ConnectivityManager)
                context.getSystemService(Service.CONNECTIVITY_SERVICE);
        if (connectivity != null){
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null){
                return info.isConnected();
            }
        }
        return false;
    }

    public static boolean isConnectedViaWifi(Context context){
        ConnectivityManager connectivity = (ConnectivityManager)
                context.getSystemService(Service.CONNECTIVITY_SERVICE);
        if (connectivity != null){
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()){
                return info.getType() == ConnectivityManager.TYPE_WIFI;
            }
        }
        return false;
    }
}
