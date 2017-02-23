package com.android.system.manager.f;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

import com.android.system.manager.server.MasterConstant;

/**
 * Created by Administrator on 2017/1/12.
 */

public class SettingObserver extends ContentObserver {
    public static final String URI = MasterConstant.URI_SETTING;
    private Context context;
    private M manager;

    public SettingObserver(Handler handler) {
        super(handler);
        // TODO Auto-generated constructor stub
    }

    public SettingObserver(Context context, Handler handler) {
        super(handler);
        this.context = context;
    }

    public void setCallback(M manager){
        this.manager = manager;
    }

    //当监听的Uri发生变化，便会执行这个方法
    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        if(this.manager == null){
            return;
        }
        this.manager.ls();
    }
}
