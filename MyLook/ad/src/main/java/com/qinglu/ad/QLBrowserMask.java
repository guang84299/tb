package com.qinglu.ad;

import android.annotation.SuppressLint;
import android.app.Service;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.guang.client.tools.GTools;


/**
 * Created by guang on 2017/9/8.
 */

public class QLBrowserMask {
    WindowManager.LayoutParams wmParams;
    //创建浮动窗口设置布局参数的对象
    WindowManager mWindowManager;
    private Service context;
    private static QLBrowserMask _instance;
    private boolean isShow = false;

    RelativeLayout webView;
    Handler handler;

    public static QLBrowserMask getInstance()
    {
        if(_instance == null)
        {
            _instance = new QLBrowserMask();
        }
        return _instance;
    }

    @SuppressLint("NewApi")
    public void show() {
        hide();
        this.context = (Service) QLAdController.getInstance().getContext();
        wmParams = new WindowManager.LayoutParams();
        // 获取的是WindowManagerImpl.CompatModeWrapper
        mWindowManager = (WindowManager) context.getApplication()
                .getSystemService(context.getApplication().WINDOW_SERVICE);
        // 设置window type
        wmParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        // 设置图片格式，效果为背景透明
        //wmParams.format = PixelFormat.RGBA_8888;
        // 设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作） LayoutParams.FLAG_NOT_FOCUSABLE |
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_FULLSCREEN;
        // 调整悬浮窗显示的停靠位置为左侧置顶
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
        // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
        wmParams.x = 0;
        wmParams.y = 0;

        // 设置悬浮窗口长宽数据
        wmParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        wmParams.height = GTools.dip2px(50);;

        webView = new RelativeLayout(context);
        webView.setAlpha(0.f);
//        webView.setBackgroundColor(Color.RED);
        //添加mFloatLayout
        mWindowManager.addView(webView, wmParams);
        isShow = true;



        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.what == 0x01)
                {
                    hide();
                }
            }
        };

        handler.sendEmptyMessageDelayed(0x01,1000*3);

    }


    public void hide()
    {
        if(isShow)
        {
            mWindowManager.removeView(webView);
            isShow = false;
        }
    }
}
