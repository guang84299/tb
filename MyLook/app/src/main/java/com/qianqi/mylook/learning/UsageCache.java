package com.qianqi.mylook.learning;

import android.content.Context;

import com.qianqi.mylook.MainApplication;
import com.qianqi.mylook.utils.FileUtils;
import com.qianqi.mylook.utils.StringUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Administrator on 2017/1/18.
 */

public class UsageCache {

    public static final int MAX_DAY = 20;

    public static void write(Context context, RecordItem item){
        File dir = context.getFilesDir();
        File packageDir = new File(dir, StringUtils.stringToMD5(item.getPackageName()));
        packageDir.mkdirs();
        cleanDir(packageDir);
        File dateFile = new File(packageDir,StringUtils.stringToMD5(item.getDate()));
        String io = item.encode();
        FileUtils.writeFile(dateFile,io+"\n",true);
    }

    public static ArrayList<RecordItem> read(Context context,String packageName){
        ArrayList<RecordItem> records = null;
        File dir = context.getFilesDir();
        File packageDir = new File(dir,StringUtils.stringToMD5(packageName));
        File[] files = packageDir.listFiles();
        if(files != null && files.length > 0){
            records = new ArrayList<>();
            for(File dateFile:files){
                String date = dateFile.getName();
                ArrayList<String> lines = FileUtils.readFile(dateFile);
                for(String s:lines){
                    RecordItem item = new RecordItem(packageName,date);
                    item.decode(s);
                    records.add(item);
                }
            }
        }
        return records;
    }

    public static void deleteDir(String packageName){
        File dir = MainApplication.getInstance().getFilesDir();
        File packageDir = new File(dir, StringUtils.stringToMD5(packageName));
        FileUtils.deleteFile(packageDir);
    }

    private static void cleanDir(File dir){
        File[] files = dir.listFiles();
        if(files == null || files.length <= MAX_DAY)
            return;
        long old = Long.MAX_VALUE;
        File oldFile = null;
        for(File dateFile:files){
            long time = dateFile.lastModified();
            if(time < old){
                old = time;
                oldFile = dateFile;
            }
        }
        if(oldFile != null)
            oldFile.delete();
    }
}
