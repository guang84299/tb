package com.qianqi.mylook.learning;

import android.content.Context;

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

    public static final int MAX_ITEM = 1000;

    public static void write(Context context, RecordItem item){
        File dir = context.getFilesDir();
        File packageDir = new File(dir,item.getPackageName());
        packageDir.mkdirs();
        clearDir(packageDir);
        File dateFile = new File(packageDir,item.getDate());
        String io = item.encode();
        writeFile(dateFile,io+"\n");
    }

    public static ArrayList<RecordItem> read(Context context,String packageName){
        ArrayList<RecordItem> records = null;
        File dir = context.getFilesDir();
        File packageDir = new File(dir,packageName);
        File[] files = packageDir.listFiles();
        if(files != null && files.length > 0){
            records = new ArrayList<>();
            for(File dateFile:files){
                String date = dateFile.getName();
                ArrayList<String> lines = readFile(dateFile);
                for(String s:lines){
                    RecordItem item = new RecordItem(packageName,date);
                    item.decode(s);
                    records.add(item);
                }
            }
        }
        return records;
    }

    private static void clearDir(File dir){

    }

    private static void writeFile(File file,String value){
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(file,true), 1024);
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

    private static ArrayList<String> readFile(File file){
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
}
