package com.android.system.manager.plugin.master;

/**
 * Created by Administrator on 2017/1/13.
 */

public class MasterConstant {
    public static final String[] CORE_PKGS = new String[]{
            "com.android.system.manager",
            "com.qianqi.mylook"
    };
    public static final String[] CORE_SERVICE = new String[]{
            "com.qianqi.mylook",
            "com.qianqi.mylook.core.CoreService"
    };
    public static final String[] SYSTEM_SERVICE = new String[]{
            "com.android.system.manager",
            "com.android.system.manager.SService",
    };
    public static final String URI_AUTOSTART = "content://com.qianqi.mylook.provider/autostart";
    public static final String URI_SETTING = "content://com.qianqi.mylook.provider/setting";
    public static final String CORE_ACTION = "qianqi.action.LAUNCH_MYLOOK";
}
