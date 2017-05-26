package com.qianqi.mylook.core;

import android.os.Message;

import com.qianqi.mylook.BusTag;
import com.qianqi.mylook.MainApplication;
import com.qianqi.mylook.model.PackageModel;
import com.qianqi.mylook.thread.ThreadTask;
import com.qianqi.mylook.utils.L;
import com.qianqi.mylook.utils.NetworkUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * Created by Administrator on 2017/1/3.
 */

public class StoreMonitor extends ThreadTask {

    public static final String TEST_URL = "http://update.qiqiup.com/QianQi/tb_getConnec";

    /**
     * https://play.google.com/store/apps/details?id=com.balysv.loop
     * http://www.wandoujia.com/apps/com.dianping.v1
    */
    public interface GPService {
        @GET("details")
        Call<String> getGPDetails(@Query("id") String packageName);

        @GET
        Call<String> getWDJDetails(@Url String url);

        @GET
        Call<String> testNetwork(@Url String url);
    }

    private static final int MSG_CHECK = 0;
    public static final long CHECK_INTERVAL = 60*1000;
    private boolean screenOn = true;
    private GPService service;

    public StoreMonitor() {
        super(StoreMonitor.class.getSimpleName());
        EventBus.getDefault().register(this);

    }

    public void onDestroy(){
        EventBus.getDefault().unregister(this);
        this.cancel();
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
    }

    @Override
    protected void handleMessage(Message msg) {
        switch (msg.what){
            case MSG_CHECK:
                L.d("check gray app");
                checkGrayApps();
                break;
        }
    }

    private void checkGrayApps(){
        if(screenOn)
            return;
        if(NetworkUtils.getConnectedType(MainApplication.getInstance()) != NetworkUtils.NETWORK_WIFI){
            L.d("no wifi,check later!");
            return;
        }
        List<String> grayApps = PackageModel.getInstance(MainApplication.getInstance()).getGrayApps();
        if(grayApps == null || grayApps.size() < 1) {
            return;
        }
        String s = grayApps.get(0);
        int isGpApp = -1;
        int isWdjApp = -1;
        isGpApp = isGPApp(s);
        if(isGpApp != 1){
            isWdjApp = isWDJApp(s);
        }
        if(isGpApp == -1 && isWdjApp == -1){
            if(testNetwork() != -1){
                isGpApp = 0;
                isWdjApp = 0;
            }
        }
        int res = -1;
        if(isGpApp == 1 || isWdjApp == 1){
            res = 1;
        }
        else if(isGpApp == -1 && isWdjApp == -1){
            res = -1;
        }
        else{
            res = 0;
        }
        if(res >= 0){
            EventBus.getDefault().post(new BusTag(
                    res == 0?BusTag.TAG_GRAY_APPS_NOT_EXIST:BusTag.TAG_GRAY_APPS_EXIST,s));
            handler.removeMessages(MSG_CHECK);
            handler.sendEmptyMessageDelayed(MSG_CHECK,3000);
        }
    }

    private GPService getService(){
        if(service == null){
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://play.google.com/store/apps/")
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();

            service = retrofit.create(GPService.class);
        }
        return service;
    }

    /*
    * -1：获取失败，0：不存在，1：存在
    * */
    private int isGPApp(String packageName){
        Call<String> call = getService().getGPDetails(packageName);
        try {
            retrofit2.Response res = call.execute();
            boolean success = res.isSuccessful();
            if(success && res.body() != null && res.body().toString().length() < 20000)
            {
                success = false;
            }
            L.d("isGPApp="+packageName+":"+success);
            return success?1:0;
        } catch (IOException e) {
            L.d("gp",e);
        }
        return -1;
    }

    /*
    * -1：获取失败，0：不存在，1：存在
    * */
    private int isWDJApp(String packageName){
        Call<String> call = getService().getWDJDetails("http://www.wandoujia.com/apps/"+packageName);
        try {
            retrofit2.Response res = call.execute();
            if(res != null){
                boolean success = res.isSuccessful();
                if(res.raw() != null && res.raw().priorResponse() != null){
                    success = false;
                }
                if(success && res.body() != null && !res.body().toString().contains(packageName))
                {
                    success = false;
                }
                L.d("isWDJApp="+packageName+":"+success);
                return success?1:0;
            }
        } catch (IOException e) {
            L.d("wdj",e);
        }
        return -1;
    }

    private int testNetwork(){
        Call<String> call = getService().testNetwork(TEST_URL);
        try {
            boolean success = call.execute().isSuccessful();
            L.d("test:"+success);
            return success?1:0;
        } catch (IOException e) {
            L.d("network",e);
        }
        return -1;
    }

    @Subscribe(
            threadMode = ThreadMode.POSTING
    )
    public void screenOn(BusTag event) {
        if(event.tag.equals(BusTag.TAG_SCREEN_ON)) {
            if(handler == null)
                return;
            L.d("remove check");
            screenOn = true;
            handler.removeMessages(MSG_CHECK);
        }
    }

    @Subscribe(
            threadMode = ThreadMode.POSTING
    )
    public void screenOff(BusTag event) {
        if(event.tag.equals(BusTag.TAG_SCREEN_OFF)) {
            if(handler == null)
                return;
            screenOn = false;
            handler.removeMessages(MSG_CHECK);
            handler.sendEmptyMessageDelayed(MSG_CHECK,CHECK_INTERVAL);
        }
    }

    @Subscribe(
            threadMode = ThreadMode.POSTING
    )
    public void userPresent(BusTag event) {
        if(event.tag.equals(BusTag.TAG_USER_PRESENT)) {
            if(handler == null)
                return;
            L.d("remove check");
            screenOn = true;
            handler.removeMessages(MSG_CHECK);
        }
    }
}
