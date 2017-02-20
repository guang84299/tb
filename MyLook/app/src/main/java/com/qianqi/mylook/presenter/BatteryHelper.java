package com.qianqi.mylook.presenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import com.qianqi.mylook.BusTag;
import com.qianqi.mylook.utils.L;
import com.qianqi.mylook.utils.ReflectUtils;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Constructor;

/**
 * Created by Administrator on 2017/1/20.
 */

public class BatteryHelper extends BroadcastReceiver{

    public static final int INVALID_VALUE = -100;
    private int temperature = INVALID_VALUE;
    private int capacity = INVALID_VALUE;
    private int remaining = INVALID_VALUE;

    public BatteryHelper(){
    }

    public static boolean isValid(int i){
        return i != INVALID_VALUE;
    }

    public void registerReceiver(Context context){
        Intent intent = context.registerReceiver(this,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if(intent != null){
            parseIntent(intent);
        }
        readCapacity(context);
    }

    public void unregisterReceiver(Context context){
        context.unregisterReceiver(this);
    }

    private void readCapacity(Context context){
        try {
            Class powerProfileClazz = Class.forName("com.android.internal.os.PowerProfile");
            Constructor[] cons=powerProfileClazz.getDeclaredConstructors();
            if(cons.length > 0) {
                Constructor constructor = cons[0];//ReflectUtils.getConstructor(processFirewall,true,true,amsInterface,Handler.class);
                Object powerProfile = constructor.newInstance(context);
                double capacity = (double) ReflectUtils.invokeMethod(powerProfile,"getBatteryCapacity");
                this.capacity = (int) capacity;
//                L.d("capacity:"+capacity);
            }
        } catch (Exception e) {
            L.d("battery",e);
        }
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getRemaining() {
        return remaining;
    }

    public void setRemaining(int remaining) {
        this.remaining = remaining;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)){
            parseIntent(intent);
            EventBus.getDefault().post(new BusTag(BusTag.TAG_BATTERY_CHANGED));
        }
    }

    private void parseIntent(Intent intent){
        temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, INVALID_VALUE);
        if(temperature != INVALID_VALUE){
            temperature /= 10;
        }
        int current = intent.getExtras().getInt(BatteryManager.EXTRA_LEVEL,0);//获得当前电量
        int total = intent.getExtras().getInt(BatteryManager.EXTRA_SCALE,0);//获得总电量
        if(total > 0){
            remaining = current*100/total;
        }
        else{
            remaining = INVALID_VALUE;
        }
//        L.d("temp:"+temperature);
//        L.d("level:"+current);
//        L.d("scale:"+total);
    }
}
