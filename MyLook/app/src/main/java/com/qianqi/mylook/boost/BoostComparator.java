package com.qianqi.mylook.boost;

import com.qianqi.mylook.MainApplication;
import com.qianqi.mylook.bean.EnhancePackageInfo;
import com.qianqi.mylook.model.PackageModel;

import java.util.Comparator;

/**
 * Created by Administrator on 2017/1/23.
 */

public class BoostComparator implements Comparator<EnhancePackageInfo> {

    private int mode = -1;

    public void setMode(int mode){
        this.mode = mode;
    }

    @Override
    public int compare(EnhancePackageInfo a, EnhancePackageInfo b) {
        if(mode == PackageModel.POWER_MODE_GAME){
            boolean aInGame = PackageModel.getInstance(MainApplication.getInstance()).inGameMode(a.packageName);
            boolean bInGame = PackageModel.getInstance(MainApplication.getInstance()).inGameMode(b.packageName);
            if(aInGame && !bInGame){
                return -1;
            }
            else if(!aInGame && bInGame){
                return 1;
            }
        }
        else if(mode == PackageModel.POWER_MODE_SMART){
            boolean aInGame = PackageModel.getInstance(MainApplication.getInstance()).inSmartMode(a.packageName);
            boolean bInGame = PackageModel.getInstance(MainApplication.getInstance()).inSmartMode(b.packageName);
            if(aInGame && !bInGame){
                return -1;
            }
            else if(!aInGame && bInGame){
                return 1;
            }
        }
        if(a.getUsagePrediction() > b.getUsagePrediction()){
            return -1;
        }
        else if(a.getUsagePrediction() < b.getUsagePrediction()){
            return 1;
        }
        else{
            if(a.getUsageQuickPrediction() > b.getUsageQuickPrediction()){
                return -1;
            }
            else if(a.getUsageQuickPrediction() < b.getUsageQuickPrediction()){
                return 1;
            }
            else{
                return 0;
            }
        }
    }
}
