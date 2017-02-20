package com.qianqi.mylook.learning;

import com.qianqi.mylook.utils.MathUtils;

/**
 * Created by Administrator on 2017/1/16.
 */

public class VisibleInfo {
    private float timePercentage;
    private long showTime = 0;
    private long hideTime = 0;
    private boolean visible = false;

    public VisibleInfo(){

    }

    public void start(boolean visible){
        this.visible = visible;
        timePercentage = 0;
        showTime = 0;
        hideTime = 0;
    }

    public void stop(long delta){
        if(visible){
            showTime += delta;
        }
        else{
            hideTime += delta;
        }
        float totalTime = showTime + hideTime;
        if(totalTime == 0){
            timePercentage = 0;
        }
        else{
            timePercentage = MathUtils.scaleFloat(showTime/totalTime);
        }
    }

    public void forward(long delta) {
        if(visible){
            showTime += delta;
        }
        else{
            hideTime += delta;
        }
    }

    public float getTimePercentage() {
        return timePercentage;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
