package com.qianqi.mylook.event;

/**
 * Created by Administrator on 2017/4/28.
 */

public class WindowEvent{
    public static final String TAG_WINDOW_CHANGED = "window_changed";

    public String tag = "";
    public Object data = null;

    public WindowEvent(String tag,Object data){
        this.tag = tag;
        this.data = data;
    }
}
