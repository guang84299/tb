package com.android.system.manager.plugin.utils;

import android.content.Context;
import android.os.storage.StorageManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by Administrator on 2017/1/18.
 */

public class FileUtils {

    public static void writeFile(File file,String value,boolean append){
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(file,append), 1024);
            out.write(value);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static ArrayList<String> readFile(File file){
        ArrayList<String> lines = new ArrayList<>();
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(file));
            String currentLine;
            while ((currentLine = in.readLine()) != null) {
                lines.add(currentLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return lines;
        }
    }

    public static File getStorageFile(Context context, String dir){
        File f = getStoragePath(context,false);
        if(f == null){
            f = getStoragePath(context,true);
        }
        if(f == null){
            return null;
        }
        return new File(f,"Android/data/"+context.getPackageName()+"/files/"+dir);
    }

    private static File getStoragePath(Context mContext, boolean removable) {
        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                boolean bool = (Boolean) isRemovable.invoke(storageVolumeElement);
//                L.d(path+","+removable);
                if (removable == bool) {
                    File f = new File(path);
                    if(f.exists()) {
                        return f;
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
