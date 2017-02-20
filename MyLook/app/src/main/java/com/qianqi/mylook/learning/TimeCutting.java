package com.qianqi.mylook.learning;

import com.qianqi.mylook.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2017/1/16.
 */

public class TimeCutting {
    public static final long MAX_VALID_INTERVAL = 1*60*60*1000;
    public static final int UNIT_MINUTE = 10;
    private String date = "";
    private int unit;//0-143
    private long curTime;
    private long beginTime;
    private long endTime;
    private long delta;

    public TimeCutting(){

    }

    public void start(long time){
        refresh(time);
    }

    public static int getUnit(long time){
        String timeString = DateUtils.getTime(time);
        int hour = Integer.parseInt(timeString.substring(0,2));
        int minute = Integer.parseInt(timeString.substring(3,5));
        minute = (int)Math.floor(minute*1.0f/UNIT_MINUTE);
        return hour*6+minute;
    }

    private void refresh(long newTime){
        curTime = newTime;
        date = DateUtils.getDay(curTime);
        String dateS = DateUtils.getDay(curTime);
        String time = DateUtils.getTime(curTime);
        int hour = Integer.parseInt(time.substring(0,2));
        int minute = Integer.parseInt(time.substring(3,5));
        minute = (int)Math.floor(minute*1.0f/UNIT_MINUTE);
        unit = hour*6+minute;
        String hourS = hour+"";
        if(hourS.length() < 2)
            hourS = "0"+hourS;
        String minuteS = minute*UNIT_MINUTE+"";
        if(minuteS.length() < 2)
            minuteS = "0"+minuteS;
        String s = dateS + " " + hourS + ":" + minuteS;
        Date date = null;
        try {
            String format = "yyyy-MM-dd HH:mm";
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            date = sdf.parse(s);
            beginTime = date.getTime();
            endTime = beginTime + UNIT_MINUTE*60*1000 - 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean canStep(long time){
        if(time > endTime){
            return true;
        }
        return false;
    }

    public void step(){
        long nextTime = endTime + 1;
        delta = nextTime - curTime;
        refresh(nextTime);
    }

    public boolean forward(long time) {
        if(time < curTime)
            return false;
        delta = time - curTime;
        refresh(time);
        return true;
    }

    public long getDelta(){
        return delta;
    }

    public int getUnit() {
        return unit;
    }

    public void setUnit(int unit) {
        this.unit = unit;
    }

    public long getCurTime() {
        return curTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }

    public boolean isValid(long timestamp) {
        if(timestamp < getCurTime() || timestamp >= getCurTime() + MAX_VALID_INTERVAL){
            return false;
        }
        return true;
    }

    public String getDate() {
        return date;
    }
}
