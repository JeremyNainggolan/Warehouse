package com.example.warehouse.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class CheckInternet {

    public static String getNetworkInfo(Context context) {
        String status;
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null) {
            status = "connected";
            return status;
        } else {
            status = "disconnected";
            return status;
        }
    }
}
