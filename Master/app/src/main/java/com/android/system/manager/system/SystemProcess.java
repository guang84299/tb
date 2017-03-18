package com.android.system.manager.system;

import android.content.Context;
import android.util.Log;

import com.android.system.manager.utils.L;


/**
 * Created by Administrator on 2017/3/8.
 */

public class SystemProcess {
    private static SystemProcess instance = null;
    private Context context;
    private M m;
    private AudioHelper audioHelper;

    public static SystemProcess ins(){
        if(instance == null){
            synchronized (SystemProcess.class){
                if(instance == null){
                    instance = new SystemProcess();
                }
            }
        }
        return instance;
    }

    public void init(Context context){
        L.d("SService onCreate");
        this.context = context;
        m = new M(context);
        audioHelper = new AudioHelper();
    }

    public void onStartCommand() {
        boolean res = m.i();
        Log.d("ss","f="+res);
        m.rm();
    }

    public void onDestroy() {
        m.d();
    }

    public Context getContext() {
        return context;
    }
}
