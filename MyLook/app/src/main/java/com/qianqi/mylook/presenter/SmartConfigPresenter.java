package com.qianqi.mylook.presenter;

import com.qianqi.mylook.BusTag;
import com.qianqi.mylook.activity.PowerModeSelectActivity;
import com.qianqi.mylook.activity.SmartModeConfigActivity;
import com.qianqi.mylook.bean.EnhancePackageInfo;
import com.qianqi.mylook.model.PackageFilter;
import com.qianqi.mylook.model.PackageModel;
import com.qianqi.mylook.utils.CollectionUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Administrator on 2017/1/21.
 */

public class SmartConfigPresenter {
    public static final int TAB_INSTALL = 0;
    public static final int TAB_SYSTEM = 1;
    private SmartModeConfigActivity activity;
    private int curTab = TAB_INSTALL;
    private List<EnhancePackageInfo> data;

    public SmartConfigPresenter(SmartModeConfigActivity activity){
        this.activity = activity;
        EventBus.getDefault().register(this);
    }

    public void load(){
        if(curTab == TAB_INSTALL){
            PackageFilter filter = new PackageFilter.Builder().system(false).build();
            data = PackageModel.getInstance(activity).getPackageList(filter);
        }
        else if(curTab == TAB_SYSTEM){
            PackageFilter filter = new PackageFilter.Builder().system(true).persistent(false).build();
            data = PackageModel.getInstance(activity).getPackageList(filter);
        }
        if(data == null){
            data = new ArrayList<>(0);
        }
        else{
            Collections.sort(data,new SmartComparator());
        }
        this.activity.updateData(data);
    }

    @Subscribe(
            threadMode = ThreadMode.MAIN
    )
    public void onPackageChanged(BusTag event){
        if(event.tag.equals(BusTag.TAG_PACKAGE_UPDATE) || event.tag.equals(BusTag.TAG_PACKAGE_SMART_UPDATE)){
            load();
        }
    }

    public void onDestroy() {
        EventBus.getDefault().unregister(this);
    }

    public void selectInstall() {
        if(curTab == TAB_INSTALL)
            return;
        curTab = TAB_INSTALL;
        load();
    }

    public void selectSystem(){
        if(curTab == TAB_SYSTEM)
            return;
        curTab = TAB_SYSTEM;
        load();
    }

    public static class SmartComparator implements Comparator<EnhancePackageInfo>{

        @Override
        public int compare(EnhancePackageInfo o1, EnhancePackageInfo o2) {
            if(o1.isInSmartList() && !o2.isInSmartList()){
                return -1;
            }
            else if(!o1.isInSmartList() && o2.isInSmartList()){
                return 1;
            }
            return o1.getLabel().compareToIgnoreCase(o2.getLabel());
        }
    }
}
