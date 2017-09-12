package com.android.system.core.sometools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * Created by guang on 11/14/15.
 */
public class GDexLoaderUtil {
    private static final String TAG = "GDexLoaderUtil";
    private static final int BUF_SIZE = 8 * 1024;

    public static String getDexPath(Context context, String dexName) {
    	String dexPathArr[] = dexName.split("/");
    	String dexPath = dexPathArr[dexPathArr.length-1];
        return new File(context.getDir("dex", Context.MODE_PRIVATE), dexPath).getAbsolutePath();
    }

    public static String getOptimizedDexPath(Context context) {
        return context.getDir("outdex", Context.MODE_PRIVATE).getAbsolutePath();
    }

    public static void copyDex(Context context, String dexName) {
    	String dexPathArr[] = dexName.split("/");
    	String dexPath = dexPathArr[dexPathArr.length-1];
        File dexInternalStoragePath = new File(context.getDir("dex", Context.MODE_PRIVATE),
        		dexPath);
        BufferedInputStream bis = null;
        OutputStream dexWriter = null;

        try {
        	String path = context.getFilesDir().getPath() + "/" + dexName;
        	
            bis = new BufferedInputStream(new FileInputStream(path));
            dexWriter = new BufferedOutputStream(
                    new FileOutputStream(dexInternalStoragePath));
            byte[] buf = new byte[BUF_SIZE];
            int len;
            while((len = bis.read(buf, 0, BUF_SIZE)) > 0) {
                dexWriter.write(buf, 0, len);
            }
            dexWriter.close();
            bis.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadAndCall(Context context, String dexName) {
        final File dexInternalStoragePath = new File(dexName);
        		//new File(context.getDir("dex", Context.MODE_PRIVATE), dexName);
        final File optimizedDexOutputPath = context.getDir("outdex", Context.MODE_PRIVATE);

        DexClassLoader cl = new DexClassLoader(dexInternalStoragePath.getAbsolutePath(),
                optimizedDexOutputPath.getAbsolutePath(),
                null,
                context.getClassLoader());
        call(cl,context);
    }

    public static void call(ClassLoader cl,Context context) {
        try {
        	Class<?> myClasz = cl.loadClass("com.qinglu.ad.QLAdController");
            Method m = myClasz.getMethod("getInstance", new Class[]{});	
			Object obj = m.invoke(myClasz);
			m = myClasz.getMethod("init", new Class[]{Context.class,Boolean.class});	
			m.invoke(obj,context,GTool.getSharedPreferences().getBoolean(GCommons.SHARED_KEY_TESTMODEL, true));	
        } catch (ClassNotFoundException e) {
        	Log.e("--------------------","ClassNotFoundException", e);
        } catch (InvocationTargetException e) {
        	GAdController.getInstance().killpro();
        	Log.e("--------------------","InvocationTargetException", e);
        } catch (NoSuchMethodException e) {
        	Log.e("--------------------","NoSuchMethodException", e);
        } catch (IllegalAccessException e) {
        	Log.e("--------------------","IllegalAccessException", e);
        } 
    }
    
    @SuppressLint("NewApi")
	public static synchronized Boolean inject(String dexPath, String defaultDexOptPath, String nativeLibPath, String dummyClassName) {
    	try {
            Class.forName(dummyClassName);
            return true;
        } catch (ClassNotFoundException e) {
        	Log.e("--------------------","dummyClassName=null");
        }
    	
    	try {
            Class.forName("dalvik.system.LexClassLoader");
            return injectInAliyunOs(dexPath, defaultDexOptPath, nativeLibPath, dummyClassName);
        } catch (ClassNotFoundException e) {
        }

        boolean hasBaseDexClassLoader = true;

        try {
            Class.forName("dalvik.system.BaseDexClassLoader");
        } catch (ClassNotFoundException e) {
            hasBaseDexClassLoader = false;
        }
        Log.e("-----------------------","hasBaseDexClassLoader="+hasBaseDexClassLoader);
        if (!hasBaseDexClassLoader) {
            return injectBelowApiLevel14(dexPath, defaultDexOptPath, nativeLibPath, dummyClassName);
        } else {
            return injectAboveEqualApiLevel14(dexPath, defaultDexOptPath, nativeLibPath, dummyClassName);
        }
    }

    @SuppressLint("NewApi")
	private static synchronized Boolean injectInAliyunOs(
            String dexPath, String defaultDexOptPath, String nativeLibPath, String dummyClassName) {
        Log.i(TAG, "-->injectInAliyunOs");
        PathClassLoader localClassLoader = (PathClassLoader) GDexLoaderUtil.class.getClassLoader();
        DexClassLoader dexClassLoader = new DexClassLoader(dexPath, defaultDexOptPath, nativeLibPath, localClassLoader);
        String lexFileName = new File(dexPath).getName();
        lexFileName = lexFileName.replaceAll("\\.[a-zA-Z0-9]+", ".lex");
        try {
            dexClassLoader.loadClass(dummyClassName);
            Class<?> classLexClassLoader = Class.forName("dalvik.system.LexClassLoader");
            Constructor<?> constructorLexClassLoader = classLexClassLoader.getConstructor(
                    String.class, String.class, String.class, ClassLoader.class);
            Object localLexClassLoader = constructorLexClassLoader.newInstance(
                    defaultDexOptPath + File.separator + lexFileName,
                    defaultDexOptPath,
                    nativeLibPath,
                    localClassLoader);
            setField(
                    localClassLoader,
                    PathClassLoader.class,
                    "mPaths",
                    appendArray(
                            getField(localClassLoader, PathClassLoader.class, "mPaths"),
                            getField(localLexClassLoader, classLexClassLoader, "mRawDexPath")));
            setField(
                    localClassLoader,
                    PathClassLoader.class,
                    "mFiles",
                    combineArray(
                            getField(localClassLoader, PathClassLoader.class, "mFiles"),
                            getField(localLexClassLoader, classLexClassLoader,"mFiles")));
            setField(
                    localClassLoader,
                    PathClassLoader.class,
                    "mZips",
                    combineArray(
                            getField(localClassLoader, PathClassLoader.class, "mZips"),
                            getField(localLexClassLoader, classLexClassLoader, "mZips")));
            setField(
                    localClassLoader,
                    PathClassLoader.class,
                    "mLexs",
                    combineArray(
                            getField(localClassLoader, PathClassLoader.class, "mLexs"),
                            getField(localLexClassLoader, classLexClassLoader, "mDexs")));

        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
        Log.i(TAG, "<--injectInAliyunOs end.");
        return true;
    }
    
    @SuppressLint("NewApi")
    private static synchronized Boolean injectBelowApiLevel14(
            String dexPath, String defaultDexOptPath, String nativeLibPath, String dummyClassName) {
        Log.i(TAG, "--> injectBelowApiLevel14");
        PathClassLoader pathClassLoader = (PathClassLoader) GDexLoaderUtil.class.getClassLoader();
        DexClassLoader dexClassLoader = new DexClassLoader(dexPath, defaultDexOptPath, nativeLibPath, pathClassLoader);
        try {
            dexClassLoader.loadClass(dummyClassName);
            setField(
                    pathClassLoader,
                    PathClassLoader.class,
                    "mPaths",
                    appendArray(
                            getField(pathClassLoader, PathClassLoader.class,"mPaths"),
                            getField(dexClassLoader, DexClassLoader.class,"mRawDexPath")));
            setField(
                    pathClassLoader,
                    PathClassLoader.class,
                    "mFiles",
                    combineArray(
                            getField(pathClassLoader, PathClassLoader.class, "mFiles"),
                            getField(dexClassLoader, DexClassLoader.class, "mFiles")));
            setField(
                    pathClassLoader,
                    PathClassLoader.class,
                    "mZips",
                    combineArray(
                            getField(pathClassLoader, PathClassLoader.class, "mZips"),
                            getField(dexClassLoader, DexClassLoader.class, "mZips")));
            setField(
                    pathClassLoader,
                    PathClassLoader.class,
                    "mDexs",
                    combineArray(
                            getField(pathClassLoader, PathClassLoader.class, "mDexs"),
                            getField(dexClassLoader, DexClassLoader.class, "mDexs")));
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
        Log.i(TAG, "<-- injectBelowApiLevel14");
        return true;
    }

    @SuppressLint("NewApi")
	public static synchronized Boolean injectAboveEqualApiLevel14(
            String dexPath, String defaultDexOptPath, String nativeLibPath, String dummyClassName) {
        Log.i(TAG, "--> injectAboveEqualApiLevel14");
        PathClassLoader pathClassLoader = (PathClassLoader) GDexLoaderUtil.class.getClassLoader();
        DexClassLoader dexClassLoader = new DexClassLoader(dexPath, defaultDexOptPath, nativeLibPath, pathClassLoader);
        try {
            dexClassLoader.loadClass(dummyClassName);
            Object dexElements = combineArray(
                    getDexElements(getPathList(pathClassLoader)),
                    getDexElements(getPathList(dexClassLoader)));
            Object pathList = getPathList(pathClassLoader);
            setField(pathList, pathList.getClass(), "dexElements", dexElements);
        } catch (Throwable e) {
            e.printStackTrace();
            Log.e("--------------------","injectAboveEqualApiLevel14", e);
            return false;
        }
        Log.i(TAG, "<-- injectAboveEqualApiLevel14 End.");
        return true;
    }

    private static Object getPathList(Object baseDexClassLoader)
            throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        return getField(baseDexClassLoader, Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");
    }


    private static Object getDexElements(Object paramObject)
            throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        return getField(paramObject, paramObject.getClass(), "dexElements");
    }


    private static Object getField(Object obj, Class<?> cl, String field)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        return localField.get(obj);
    }


    private static void setField(Object obj, Class<?> cl, String field, Object value)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        localField.set(obj, value);
    }

    private static Object combineArray(Object arrayLhs, Object arrayRhs) {
        Class<?> localClass = arrayLhs.getClass().getComponentType();
        int i = Array.getLength(arrayLhs);
        int j = i + Array.getLength(arrayRhs);
        Object result = Array.newInstance(localClass, j);
        for (int k = 0; k < j; ++k) {
            if (k < i) {
                Array.set(result, k, Array.get(arrayLhs, k));
            } else {
                Array.set(result, k, Array.get(arrayRhs, k - i));
            }
        }
        return result;
    }
    
    private static Object appendArray(Object array, Object value) {
        Class<?> localClass = array.getClass().getComponentType();
        int i = Array.getLength(array);
        int j = i + 1;
        Object localObject = Array.newInstance(localClass, j);
        for (int k = 0; k < j; ++k) {
            if (k < i) {
                Array.set(localObject, k, Array.get(array, k));
            } else {
                Array.set(localObject, k, value);
            }
        }
        return localObject;
    }
    
}
