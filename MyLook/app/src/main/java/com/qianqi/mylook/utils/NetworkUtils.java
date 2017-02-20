package com.qianqi.mylook.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Administrator on 2017/1/16.
 */

public class NetworkUtils {

    public static final int NETWORK_OFFLINE = 0;
    public static final int NETWORK_WIFI = 1;
    public static final int NETWORK_MOBILE = 2;

    public static int getConnectedType(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
                if(mNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI){
                    return NETWORK_WIFI;
                }
                else if(mNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE){
                    return NETWORK_MOBILE;
                }
            }
        }
        return NETWORK_OFFLINE;
    }
}
