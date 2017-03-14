package com.android.system.manager.plugin.master;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import com.android.system.manager.plugin.utils.L;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2017/1/17.
 */

public class SettingHelper {
    static final String ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR = ":";
    static final ArrayList<String> ensureAccessibilityList = new ArrayList<>();
    static AccessibilityObserver accessibilityObserver = null;
    static Timer accessibilityTimer;
    static TimerTask accessibilityTask;

    /**
     * @return the set of enabled accessibility services. If there are not services
     * it returned the unmodifiable {@link Collections#emptySet()}.
     */
    static Set<ComponentName> getEnabledServicesFromSettings(Context context) {
        final String enabledServicesSetting = Settings.Secure.getString(
                context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (enabledServicesSetting == null) {
            return Collections.emptySet();
        }
        final Set<ComponentName> enabledServices = new HashSet<ComponentName>();
        String[] split = enabledServicesSetting.split(ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR);
        for(String s:split) {
            final String componentNameString = s;
            final ComponentName enabledService = ComponentName.unflattenFromString(
                    componentNameString);
            if (enabledService != null) {
                enabledServices.add(enabledService);
                L.d("enabled:"+enabledService);
            }
        }
        return enabledServices;
    }

    public static void onDestroy(){
        if(accessibilityObserver != null){
            accessibilityObserver.unregister(MasterProcess.ins().getContext().getContentResolver());
        }
    }

    private static void ensureAccessibility(String s){
        L.d("ensureAccessibility:"+s);
        if(!ensureAccessibilityList.contains(s))
            ensureAccessibilityList.add(s);
        if(accessibilityObserver == null){
            accessibilityObserver = new AccessibilityObserver(new Handler(Looper.getMainLooper()));
            accessibilityObserver.register(MasterProcess.ins().getContext().getContentResolver());
        }
    }

    public static void checkAccessibility(){
        L.d("checkAccessibility");
        if(accessibilityTimer != null){
            accessibilityTimer.cancel();
        }
        if(accessibilityTask != null){
            accessibilityTask.cancel();
        }
        accessibilityTimer = new Timer();
        accessibilityTask = new TimerTask() {
            @Override
            public void run() {
                for(String s:ensureAccessibilityList){
                    enableAccessibilityService(MasterProcess.ins().getContext(),s,false);
                }
            }
        };
        accessibilityTimer.schedule(accessibilityTask,10000);
    }

    public static void enableAccessibilityService(Context context,String s, boolean force) {
        L.d("enable accessibility : "+s);
        ensureAccessibility(s);
        Set<ComponentName> enabledServices = getEnabledServicesFromSettings(context);

        if (enabledServices == (Set<?>) Collections.emptySet()) {
            enabledServices = new HashSet<ComponentName>();
        }

        // Determine enabled services and accessibility state.
        ComponentName toggledService = ComponentName.unflattenFromString(s);
        PackageInfo pm = null;
        try {
            pm = context.getPackageManager().getPackageInfo(toggledService.getPackageName(),0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if(pm == null){
            L.d("has uninstall,return:"+s);
            return;
        }
        boolean found = false;
        for(ComponentName com:enabledServices){
            if(com.getPackageName().equals(toggledService.getPackageName()) && com.getClassName().equals(toggledService.getClassName())){
                L.d("has enable,return:"+s);
                found = true;
                if(force)
                    break;
                else
                    return;
            }
        }
        if(!found)enabledServices.add(toggledService);
        // Enabling at least one service enables accessibility.
        boolean accessibilityEnabled = true;

        // Update the enabled services setting.
        StringBuilder enabledServicesBuilder = new StringBuilder();
        // Keep the enabled services even if they are not installed since we
        // have no way to know whether the application restore process has
        // completed. In general the system should be responsible for the
        // clean up not settings.
        for (ComponentName enabledService : enabledServices) {
            enabledServicesBuilder.append(enabledService.flattenToString());
            enabledServicesBuilder.append(ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR);
        }
        final int enabledServicesBuilderLength = enabledServicesBuilder.length();
        if (enabledServicesBuilderLength > 0) {
            enabledServicesBuilder.deleteCharAt(enabledServicesBuilderLength - 1);
        }
        try {
            Settings.Secure.putString(context.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                    enabledServicesBuilder.toString());

            // Update accessibility enabled.
            Settings.Secure.putInt(context.getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED, accessibilityEnabled ? 1 : 0);
            L.d("enable success:"+s);
        } catch (Exception e) {
            L.d("enableAccessibilityService",e);
        }
    }
}
