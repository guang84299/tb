package com.android.system.manager.fw;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

import com.android.system.manager.server.MasterConstant;

/**
 * Created by Administrator on 2017/1/12.
 */

public class AutostartObserver extends ContentObserver {
    public static final String URI = MasterConstant.URI_AUTOSTART;
    private Context context;
    private FWManager manager;

    private static final int PERSON_UPDATE = 0;


    public AutostartObserver(Handler handler) {
        super(handler);
        // TODO Auto-generated constructor stub
    }

    public AutostartObserver(Context context, Handler handler) {
        super(handler);
        this.context = context;
    }

    public void setCallback(FWManager manager){
        this.manager = manager;
    }

    //当监听的Uri发生变化，便会执行这个方法
    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        if(this.manager == null){
            return;
        }
        this.manager.loadAutoStartDB();
    }
}
