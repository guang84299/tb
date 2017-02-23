package com.qianqi.mylook.client;

import com.android.system.manager.server.MS;
import com.qianqi.mylook.bean.ComponentInfo;
import com.qianqi.mylook.utils.L;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Created by Administrator on 2017/1/4.
 */

public class ComponentHelper {

    private MS masterServer;
    protected HashMap<ComponentInfo, Boolean> mStates;
    protected LinkedBlockingQueue<ComponentInfo> mQueue;
    private ComponentInfo mItemBeingProcessed = null;
    private boolean mItemBeingProcessedDesiredState;

    public ComponentHelper() {
        mStates = new LinkedHashMap<ComponentInfo, Boolean>();
        mQueue = new LinkedBlockingQueue<ComponentInfo>();
    }

    public void setMasterServer(MS server){
        this.masterServer = server;
    }

    public boolean getQueuedState(ComponentInfo component, boolean defaultValue) {
        if (component.equals(mItemBeingProcessed))
            return mItemBeingProcessedDesiredState;

        Boolean desiredState = mStates.get(component);
        if (desiredState == null)
            return defaultValue;

        return desiredState;
    }

    public boolean has(ComponentInfo component) {
        return (component.equals(mItemBeingProcessed) || mStates.containsKey(component));
    }

    public void toggleComponent(ComponentInfo component, boolean newState) {
        if (mStates.put(component,  newState) == null);
        mQueue.offer(component);

        L.d("Added "+component+" to service queue, now size: "+mStates.size());
//        RxBus.get().post(BusTag.TAG_COMPONENT_CHANGE_START, null);

        if(mItemBeingProcessed == null){
            subscribeToggleComponent();
        }
    }

    private void subscribeToggleComponent(){
//        Observable<ComponentInfo> observable = Observable.create(new Observable.OnSubscribe<ComponentInfo>() {
//            @Override
//            public void call(Subscriber<? super ComponentInfo> subscriber) {
//                // Use queue to determine next item to process
//                while(true){
//                    final ComponentInfo component = mQueue.poll();
//                    if (component == null) {
//                        L.d("ToggleService mQueue empty, shutting down");
//                        RxBus.get().post(BusTag.TAG_COMPONENT_CHANGE_START, null);
//                        break;
//                    }
//                    // See whether we should enable or disable
//                    final boolean desiredState = mStates.remove(component);
//                    L.d("Processing "+component+", remaining items in queue: "+mStates.size());
//                    RxBus.get().post(BusTag.TAG_COMPONENT_CHANGE_START, component);
//
//                    mItemBeingProcessed = component;
//                    mItemBeingProcessedDesiredState = desiredState;
//                    ComponentName c = new ComponentName(component.enhancePackageInfo.packageName, component.componentName);
////                    final int state = masterServer.setComponentEnabledSetting(c,desiredState);
////                    if(state != -1)component.currentEnabledState = state;
////                    final boolean success = (component.isCurrentlyEnabled() == desiredState);
//                    final boolean success = masterServer.forceStopPackage(component.enhancePackageInfo.packageName);
//                    mItemBeingProcessed = null;
//                    if (!success) {
//
//                        RxBus.get().post(BusTag.TAG_COMPONENT_CHANGE_FAILED, component);
//                        continue;
//                    }
//                    L.d("Processing "+component+" done");
//                    subscriber.onNext(component);
//                }
//                subscriber.onCompleted();
//            }
//        });
//        componentToggleSubscription = observable.subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<ComponentInfo>() {
//                    @Override
//                    public void onStart() {
//                    }
//
//                    @Override
//                    public void onNext(ComponentInfo info) {
//                        RxBus.get().post(BusTag.TAG_COMPONENT_CHANGE_FINISH, info);
//                    }
//
//                    @Override
//                    public void onCompleted() {
//                        unsubscribe();
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        unsubscribe();
//                    }
//                });
    }

    private void unsubscribeToggleComponent(){
//        if(componentToggleSubscription != null){
//            componentToggleSubscription.unsubscribe();
//            componentToggleSubscription = null;
//        }
    }
}
