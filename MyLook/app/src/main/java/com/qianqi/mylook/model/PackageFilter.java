package com.qianqi.mylook.model;

import android.app.ActivityManager;

import com.qianqi.mylook.MainApplication;
import com.qianqi.mylook.bean.EnhancePackageInfo;
import com.qianqi.mylook.client.MasterClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/2/8.
 */

public class PackageFilter {
    private static final int IGNORE = -1;
    private static final int NEGATIVE = 0;
    private static final int POSITIVE = 1;
    private int persistent = IGNORE;
    private int qianqi = IGNORE;
    private int running = IGNORE;
    private int top = IGNORE;
    private int hasActivity = IGNORE;
    private int system = IGNORE;

    public void setPersistent(int persistent) {
        this.persistent = persistent;
    }

    public void setQianqi(int qianqi) {
        this.qianqi = qianqi;
    }

    public void setRunning(int running) {
        this.running = running;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public void setHasActivity(int hasActivity) {
        this.hasActivity = hasActivity;
    }

    public void setSystem(int system) {
        this.system = system;
    }

    public List<EnhancePackageInfo> filterPackageList(List<EnhancePackageInfo> packageInfoList){
        if(packageInfoList == null)
            return null;
        List<EnhancePackageInfo> res = new ArrayList<>();
        String topPackage = PackageModel.getInstance(MainApplication.getInstance()).getTopPackageName();
        for(EnhancePackageInfo p:packageInfoList){
            if(persistent == NEGATIVE && p.isPersistent){
                continue;
            }
            if(persistent == POSITIVE && !p.isPersistent){
                continue;
            }
            if(qianqi == NEGATIVE && p.isQianqi){
                continue;
            }
            if(qianqi == POSITIVE && !p.isQianqi){
                continue;
            }
            if(running == NEGATIVE && p.isRunning){
                continue;
            }
            if(running == POSITIVE && !p.isRunning){
                continue;
            }
            if(top == NEGATIVE && p.packageName.equals(topPackage)){
                continue;
            }
            if(top == POSITIVE && !p.packageName.equals(topPackage)){
                continue;
            }
            if(hasActivity == NEGATIVE && p.hasActivity){
                continue;
            }
            if(hasActivity == POSITIVE && !p.hasActivity){
                continue;
            }
            if(system == NEGATIVE && p.isSystem){
                continue;
            }
            if(system == POSITIVE && !p.isSystem){
                continue;
            }
            res.add(p);
        }
        return res;
    }

    public static class Builder{

        private PackageFilter filter;

        public Builder(){
            PackageFilter filter = new PackageFilter();
            this.filter = filter;
        }

        public PackageFilter build(){
            return filter;
        }

        public Builder persistent(boolean bool){
            if(bool){
                filter.setPersistent(POSITIVE);
            }
            else{
                filter.setPersistent(NEGATIVE);
            }
            return this;
        }

        public Builder qianqi(boolean bool){
            if(bool){
                filter.setQianqi(POSITIVE);
            }
            else{
                filter.setQianqi(NEGATIVE);
            }
            return this;
        }

        public Builder running(boolean bool){
            if(bool){
                filter.setRunning(POSITIVE);
            }
            else{
                filter.setRunning(NEGATIVE);
            }
            return this;
        }

        public Builder top(boolean bool){
            if(bool){
                filter.setTop(POSITIVE);
            }
            else{
                filter.setTop(NEGATIVE);
            }
            return this;
        }

        public Builder hasActivity(boolean bool){
            if(bool){
                filter.setHasActivity(POSITIVE);
            }
            else{
                filter.setHasActivity(NEGATIVE);
            }
            return this;
        }

        public Builder system(boolean bool){
            if(bool){
                filter.setSystem(POSITIVE);
            }
            else{
                filter.setSystem(NEGATIVE);
            }
            return this;
        }
    }
}
