package com.qianqi.mylook.learning;

import com.qianqi.mylook.BusTag;
import com.qianqi.mylook.MainApplication;
import com.qianqi.mylook.bean.EnhancePackageInfo;
import com.qianqi.mylook.utils.CommonUtils;
import com.qianqi.mylook.utils.L;
import com.qianqi.mylook.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Administrator on 2017/1/23.
 */

public class UsagePredictor {
    private static UsagePredictor instance;
//    private HashMap<String,float[]> lastInput = new HashMap<>();
//    private HashMap<String,Float> cacheResult = new HashMap<>();

    public static UsagePredictor getInstance(){
        if(instance == null){
            synchronized (UsagePredictor.class){
                if(instance == null){
                    instance = new UsagePredictor();
                }
            }
        }
        return instance;
    }

    public void updateFit(List<EnhancePackageInfo> packageList){
        if(packageList == null) {
            L.d("package is null,return");
            return;
        }
        for (EnhancePackageInfo p : packageList) {
            ArrayList<RecordItem> recordItems = UsageCache.read(MainApplication.getInstance(), p.packageName);
            if (recordItems == null) {
                continue;
            }
            List<float[]> inputList = new ArrayList<>();
            List<Float> outputList = new ArrayList<>();
            for (RecordItem item : recordItems) {
                float[] input = item.getInput();
                float output = item.getOutput();
                if (input != null && output >= 0) {
                    inputList.add(input);
                    outputList.add(output);
                }
            }
            float[][] X = inputList.toArray(new float[0][0]);
            float[] y = new float[outputList.size()];
            for (int i = 0; i < y.length; i++) {
                y[i] = outputList.get(i);
            }
            if(Cooker.getInstance().fit(p.packageName, X, y))
                L.d("fit:"+p.getLabel());
        }
    }

    /*
    * 预测分为两步
    * 1.已建模的包列表使用机器学习算法预测
    * 2.预测值最低的包和还没预测的包使用快速预测算法进行预测
    * */
    public void predict(List<EnhancePackageInfo> packageList){
        if(packageList == null)
            return;
        List<EnhancePackageInfo> predictFail = new ArrayList<>();
        List<EnhancePackageInfo> predictLow = new ArrayList<>();
        float lowestPrediction = Float.MAX_VALUE;
        for (EnhancePackageInfo p : packageList) {
            float[] input = getInput(p.packageName);
//            float[] lastPackageInput = lastInput.get(p.packageName);
//            boolean changed = !CommonUtils.isSameArray(lastPackageInput,getInput(p.packageName));
//            if(!changed){
//                Float y = cacheResult.get(p.packageName);
//                if(y != null){
//                    p.setUsagePrediction(y);
//                    continue;
//                }
//            }
            float y = predict(p.packageName, input);
            if (y == -1) {
                p.setUsagePrediction(-1);
                predictFail.add(p);
                continue;
            } else {
                p.setUsagePrediction(y);
            }
            if (y == lowestPrediction) {
                predictLow.add(p);
            } else if (y < lowestPrediction) {
                lowestPrediction = y;
                predictLow.clear();
                predictLow.add(p);
            }
//            lastInput.put(p.packageName,input);
//            cacheResult.put(p.packageName,y);
        }
        predictLow.addAll(predictFail);
        if(predictLow.size() > 1){
            for(EnhancePackageInfo p:predictLow){
                p.setUsagePrediction(-1);
                float[] input = getInput(p.packageName);
                float y = quickPredict(p.packageName,input);
                p.setUsageQuickPrediction(y);
            }
        }
    }

    public static float predict(String packageName,float[] input){
        float[][] X = new float[1][input.length];
        X[0] = input;
        float[] y = Cooker.getInstance().predict(packageName,X);
        if(y != null && y.length > 0){
            return y[0];
        }
        return -1;
    }

    public static float quickPredict(String packageName,float[] input){
        List<RecordItem> recordItems = UsageCache.read(MainApplication.getInstance(),packageName);
        if(recordItems == null)
            return 0;
        int count = recordItems.size();
        if(count == 0)
            return 0;
        return QuickCooker.predict(recordItems,input);
    }

    public static float[] getInput(String packageName){
        long time = System.currentTimeMillis();
        TimeInfo timeInfo = new TimeInfo();
        timeInfo.setTime(time);
        int networkType = NetworkUtils.getConnectedType(MainApplication.getInstance());
        NetworkInfo networkInfo = new NetworkInfo();
        networkInfo.setState(networkType);
        AppStat app = new AppStat(packageName);
        return app.getInput(timeInfo,networkInfo);
    }

}
