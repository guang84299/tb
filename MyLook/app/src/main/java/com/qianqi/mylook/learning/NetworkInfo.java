package com.qianqi.mylook.learning;

import com.qianqi.mylook.MainApplication;
import com.qianqi.mylook.utils.MathUtils;
import com.qianqi.mylook.utils.NetworkUtils;

import java.math.BigDecimal;

/**
 * Created by Administrator on 2017/1/16.
 */

public class NetworkInfo {

    private float offlinePercentage;
    private float wifiPercentage;
    private float mobilePercentage;
    private long offlineTime = 0;
    private long wifiTime = 0;
    private long mobileTime = 0;
    private int lastState = -1;

    public void start() {
        offlinePercentage = 0;
        wifiPercentage = 0;
        mobilePercentage = 0;
        offlineTime = 0;
        wifiTime = 0;
        mobileTime = 0;
    }

    public void stop(long delta){
        log(delta);
        float totalTime = offlineTime + wifiTime + mobileTime;
        offlinePercentage = MathUtils.scaleFloat(offlineTime/totalTime);
        wifiPercentage = MathUtils.scaleFloat(wifiTime/totalTime);
        mobilePercentage = MathUtils.scaleFloat(mobileTime/totalTime);
    }

    public void forward(long delta) {
        log(delta);
    }

    public void setState(int state) {
        lastState = state;
    }

    private void log(long delta){
        switch (lastState){
            case NetworkUtils.NETWORK_OFFLINE:
                offlineTime += delta;
                break;
            case NetworkUtils.NETWORK_WIFI:
                wifiTime += delta;
                break;
            case NetworkUtils.NETWORK_MOBILE:
                mobileTime += delta;
                break;
        }
    }

    public void fill(float[] array,int index){
        array[index] = lastState == NetworkUtils.NETWORK_OFFLINE?1:0;
        array[index+1] = lastState == NetworkUtils.NETWORK_WIFI?1:0;
        array[index+2] = lastState == NetworkUtils.NETWORK_MOBILE?1:0;
    }

    public String getOutput(){
        String res = "1 0 0";
        switch (lastState){
            case NetworkUtils.NETWORK_OFFLINE:
                res = "1 0 0";
                break;
            case NetworkUtils.NETWORK_WIFI:
                res = "0 1 0";
                break;
            case NetworkUtils.NETWORK_MOBILE:
                res = "0 0 1";
                break;
        }
        return res;
    }

    public float getOfflinePercentage() {
        return offlinePercentage;
    }

    public void setOfflinePercentage(float offlinePercentage) {
        this.offlinePercentage = offlinePercentage;
    }

    public float getWifiPercentage() {
        return wifiPercentage;
    }

    public void setWifiPercentage(float wifiPercentage) {
        this.wifiPercentage = wifiPercentage;
    }

    public float getMobilePercentage() {
        return mobilePercentage;
    }

    public void setMobilePercentage(float mobilePercentage) {
        this.mobilePercentage = mobilePercentage;
    }

//    public String getOutput(){
//        return getOfflinePercentage()+" "+getWifiPercentage()+" "+ getMobilePercentage();
//    }
}
