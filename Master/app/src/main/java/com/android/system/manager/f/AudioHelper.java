package com.android.system.manager.f;

import android.app.IProcessObserver;
import android.content.Context;
import android.media.AudioFocusInfo;
import android.media.AudioManager;
import android.media.audiopolicy.IAudioPolicyCallback;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.android.system.manager.MainApplication;
import com.android.system.manager.utils.FileUtils;
import com.android.system.manager.utils.L;
import com.android.system.manager.utils.ReflectUtils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Administrator on 2017/2/28.
 */

public class AudioHelper {

    private IAudioPolicyCallback mPolicyCb;
    private String focus = "";

    public AudioHelper(){
        followAudioFocus();
    }

    private void followAudioFocus(){
//        L.d("audio:followFocus");
        try {
            Class serviceManagerClazz = Class.forName("android.os.ServiceManager");
            Method getServiceMethod = serviceManagerClazz.getMethod("getService", String.class);
            Object service = getServiceMethod.invoke(null, Context.AUDIO_SERVICE);
            L.d(service.getClass().getName());
            Object mediaFocusControl = ReflectUtils.getValue(service,"mMediaFocusControl");
            L.d(mediaFocusControl.getClass().getName());
            Method[] methods = mediaFocusControl.getClass().getDeclaredMethods();
            Method addFocusFollowerMethod = null;
            for(Method m:methods){
                L.d(m.getName());
                if(m.getName().equals("addFocusFollower")){
                    addFocusFollowerMethod = m;
                    addFocusFollowerMethod.setAccessible(true);
                    break;
                }
            }
            if(addFocusFollowerMethod == null){
//                L.d("audio:addFocusFollower=null");
            }
            else{
                //Method addFocusFollowerMethod = mediaFocusControl.getClass().getMethod("addFocusFollower", IAudioPolicyCallback.class);
                initCB();
                if(mPolicyCb != null){
                    addFocusFollowerMethod.invoke(mediaFocusControl,mPolicyCb);
//                    L.d("audio:886");
                }
                else{
//                    L.d("audio:cb=null");
                }
            }
        } catch (ClassNotFoundException e) {
            L.d("audio",e);
        } catch (NoSuchMethodException e) {
            L.d("audio",e);
        } catch (IllegalAccessException e) {
            L.d("audio",e);
        } catch (InvocationTargetException e) {
            L.d("audio",e);
        } catch (NoSuchFieldException e) {
            L.d("audio",e);
        }
    }

    private void initCB(){
        mPolicyCb = new IAudioPolicyCallback.Stub() {

            public void notifyAudioFocusGrant(AudioFocusInfo afi, int requestResult) {
                String curFocus = afi.getPackageName();
//                L.d("audio:focusGrant="+curFocus);
                if(curFocus != null){
                    if(curFocus.equals(focus))
                        return;
                    focus = curFocus;
                }
                if(curFocus == null){
                    if(focus.equals(""))
                        return;
                    focus = "";
                }
//                L.d("audio:writeFocus="+focus);
                File dir = MainApplication.getInstance().getFilesDir();
                File logFile = new File(dir,"af");
                FileUtils.writeFile(logFile,focus,false);
            }

            public void notifyAudioFocusLoss(AudioFocusInfo afi, boolean wasNotified) {
                String curFocus = afi.getPackageName();
//                L.d("audio:focusLoss="+curFocus);
            }

            public void notifyMixStateUpdate(String regId, int state) {

            }
        };
    }
}
