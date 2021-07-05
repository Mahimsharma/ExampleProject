package com.example.exampleproject.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;

public class NetworkManager {
    private static ConnectivityManager cm;
    public static boolean isOnline(Context context) {
        cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    public static int getUploadSpeed(Context context){
        boolean isOnline = isOnline(context);
        NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
        if(! isOnline) return  0;
        int upSpeed = nc.getLinkUpstreamBandwidthKbps();
        return upSpeed;
    }

}
