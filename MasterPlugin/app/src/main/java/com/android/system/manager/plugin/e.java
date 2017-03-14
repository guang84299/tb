package com.android.system.manager.plugin;

import android.content.Context;

import com.android.system.manager.plugin.master.MasterProcess;

/**
 * Created by Administrator on 2017/3/8.
 */

public class e {
    public e(Context context,Class serviceManager){
        MasterProcess.ins().init(context,serviceManager);
    }

    public void d(){
        MasterProcess.ins().onDestroy();
    }
}
