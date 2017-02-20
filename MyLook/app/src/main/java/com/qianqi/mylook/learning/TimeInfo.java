package com.qianqi.mylook.learning;

import com.qianqi.mylook.utils.DateUtils;

/**
 * Created by Administrator on 2017/2/7.
 */

public class TimeInfo {
    public static final int UNIT_MINUTE = 20;
    private String date = "";
    private int unit;
    private boolean isSaturday;
    private boolean isSunday;
    private long time;

    public TimeInfo(){

    }

    public void setTime(long time){
        this.time = time;
        this.date = DateUtils.getDay(time);
        this.unit = getUnit(time);
        this.isSaturday = DateUtils.isSaturday();
        this.isSunday = DateUtils.isSunday();
    }

    private int getUnit(long time){
        String timeString = DateUtils.getTime(time);
        int hour = Integer.parseInt(timeString.substring(0,2));
        int minute = Integer.parseInt(timeString.substring(3,5));
        minute = (int)Math.floor(minute*1.0f/UNIT_MINUTE);
        return hour*6+minute;
    }

    public int getUnit() {
        return unit;
    }

    public boolean isSaturday() {
        return isSaturday;
    }

    public boolean isSunday() {
        return isSunday;
    }

    public long getTime() {
        return time;
    }

    public String getOutput(){
        return unit+" "+(isSaturday?1:0)+" "+(isSunday?1:0);
    }

    public String getDate() {
        return date;
    }

    public void fill(float[] array, int index){
        array[index] = unit;
        array[index+1] = (isSaturday?1:0);
        array[index+2] = (isSunday?1:0);
    }
}
