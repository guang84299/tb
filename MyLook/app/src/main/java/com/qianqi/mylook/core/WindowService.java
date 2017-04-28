package com.qianqi.mylook.core;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.qianqi.mylook.BusTag;
import com.qianqi.mylook.event.WindowEvent;
import com.qianqi.mylook.utils.L;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Created by Administrator on 2017/1/14.
 */

public class WindowService extends AccessibilityService {

    @Override
    public void onCreate() {
        super.onCreate();
        L.d("WindowService create");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
//                List<CharSequence> texts = event.getText();
//                if (!texts.isEmpty()) {
//                    for (CharSequence text : texts) {
//                        String content = text.toString();
//                        L.i("text:"+content);
//                        if (content.contains("[微信红包]")) {
//                            //模拟打开通知栏消息
//                            if (event.getParcelableData() != null
//                                    &&
//                                    event.getParcelableData() instanceof Notification) {
//                                Notification notification = (Notification) event.getParcelableData();
//                                PendingIntent pendingIntent = notification.contentIntent;
//                                try {
//                                    pendingIntent.send();
//                                } catch (PendingIntent.CanceledException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }
//                    }
//                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
//                String className = event.getClassName().toString();
//                if (className.equals("com.tencent.mm.ui.LauncherUI")) {
//                    //开始抢红包
//                    getPacket();
//                } else if (className.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI")) {
//                    //开始打开红包
//                    openPacket();
//                }
//                L.d("post window changed:"+android.os.Process.myTid());
                EventBus.getDefault().post(new WindowEvent(WindowEvent.TAG_WINDOW_CHANGED,event.getPackageName()));
//                L.d("TYPE_WINDOW_STATE_CHANGED");
                break;
        }
    }

//    /**
//     * 查找到
//     */
//    private void openPacket() {
//        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
//        if (nodeInfo != null) {
//            List<AccessibilityNodeInfo> list = nodeInfo
//                    .findAccessibilityNodeInfosByText("抢红包");
//            for (AccessibilityNodeInfo n : list) {
//                n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//            }
//        }
//
//    }

//    private void getPacket() {
//        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
//        recycle(rootNode);
//    }
//
//    /**
//     * 打印一个节点的结构
//     * @param info
//     */
//    public void recycle(AccessibilityNodeInfo info) {
//        if (info.getChildCount() == 0) {
//            if(info.getText() != null){
//                if("领取红包".equals(info.getText().toString())){
//                    //这里有一个问题需要注意，就是需要找到一个可以点击的View
//                    L.i("Click"+",isClick:"+info.isClickable());
//                    info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                    AccessibilityNodeInfo parent = info.getParent();
//                    while(parent != null){
//                        L.i("parent isClick:"+parent.isClickable());
//                        if(parent.isClickable()){
//                            parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                            break;
//                        }
//                        parent = parent.getParent();
//                    }
//
//                }
//            }
//        } else {
//            for (int i = 0; i < info.getChildCount(); i++) {
//                if(info.getChild(i)!=null){
//                    recycle(info.getChild(i));
//                }
//            }
//        }
//    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        L.d("WindowService destroy");
    }
}
