package com.qianqi.mylook;

/**
 * Created by Administrator on 2017/1/5.
 */

public class BusTag {
    public static final String TAG_COMPONENT_CHANGE_START = "component_change_start";
    public static final String TAG_COMPONENT_CHANGE_FINISH = "component_change_finish";
    public static final String TAG_COMPONENT_CHANGE_FAILED = "component_change_failed";
    public static final String TAG_REQUEST_PROCESS_UPDATE = "process_update";
    public static final String TAG_PACKAGE_UPDATE = "package_update";
    public static final String TAG_PACKAGE_PROCESS_UPDATE = "package_process_update";
    public static final String TAG_PACKAGE_AUTOSTART_UPDATE = "package_autostart_update";
    public static final String TAG_PACKAGE_SMART_UPDATE = "package_smart_update";
    public static final String TAG_POWER_MODE_UPDATE = "power_mode_update";
    public static final String TAG_WINDOW_CHANGED = "window_changed";
    public static final String TAG_TOP_TASK_CHANGED = "top_task_changed";
    public static final String TAG_NETWORK_CHANGED = "network_changed";
    public static final String TAG_BATTERY_CHANGED = "battery_changed";
    public static final String TAG_FLUSH_LEARNING_DATA = "flush_learning_data";
    public static final String TAG_CURRENT = "current";
    public static final String TAG_MASTER_CONNECTED = "master_connected";
    public static final String TAG_MASTER_DISCONNECTED = "master_disconnected";
    public static final String TAG_GRAY_APPS_UPDATE = "gray_apps_update";
    public static final String TAG_GRAY_APPS_EXIST = "gray_apps_exist";
    public static final String TAG_GRAY_APPS_NOT_EXIST = "gray_apps_not_exist";
    public static final String TAG_SCREEN_OFF = "screen_off";
    public static final String TAG_SCREEN_ON = "screen_on";
    public static final String TAG_USER_PRESENT = "user_present";

    public String tag = "";
    public Object data = null;

    public BusTag(String tag){
        this.tag = tag;
    }

    public BusTag(String tag,Object data){
        this.tag = tag;
        this.data = data;
    }
}
