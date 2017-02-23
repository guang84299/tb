package com.android.support.servicemanager.local;

import com.android.support.servicemanager.util.ParamUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cailiming on 16/1/1.
 */
public abstract class ServiceFetcher {
    int mServiceId;
    String mGroupId;
    private Object mCachedInstance;
    private Map<String,Method> mCachedMethodMap;

    public final Object getService() {
        synchronized (ServiceFetcher.this) {
            Object service = mCachedInstance;
            if (service != null) {
                return service;
            }
            return mCachedInstance = createService(mServiceId);
        }
    }

    public Method getMethod(String methodName){
        synchronized (ServiceFetcher.this) {
            Map<String,Method> methodsMap = mCachedMethodMap;
            if (methodsMap != null) {
                return methodsMap.get(methodName);
            }
            Object service = getService();
            if (service != null && !Proxy.isProxyClass(service.getClass())) {
                Method[] methods = service.getClass().getInterfaces()[0].getDeclaredMethods();
                if (methods != null) {
                    methodsMap = new HashMap<>(methods.length);
                    for (Method m : methods) {
                        methodsMap.put(m.toGenericString(),m);
                    }
                }
            }
            mCachedMethodMap = methodsMap;
            if (methodsMap != null) {
                return methodsMap.get(methodName);
            }
            return null;
        }
    }

    public abstract Object createService(int serviceId);

}
