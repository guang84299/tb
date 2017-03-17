package com.qianqi.mylook.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Administrator on 2017/3/9.
 */

public class FileUtils {

    public static void deleteFile(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                File files[] = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    deleteFile(files[i]);
                }
                file.delete();
            }
        }
    }

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

    public static boolean copyAssetsFile(Context context, String assetsPath, String dstDirPath, String dstFileName){
        boolean res = true;
        InputStream is = null;
        FileOutputStream fos = null;
        File dstFile = null;
        File dstDir = new File(dstDirPath);
        try {
            is = context.getAssets().open(assetsPath);
            if (!dstDir.exists()) {
                dstDir.mkdirs();
            }
            dstFile = new File(dstDir, dstFileName);
            if(dstFile.exists())
                dstFile.delete();
            dstFile.createNewFile();
            fos = new FileOutputStream(dstFile);
            byte[] temp = new byte[1024];
            int i = 0;
            while ((i = is.read(temp)) > 0) {
                fos.write(temp, 0, i);
            }
        } catch (Exception e) {
            L.d("copy",e);
            res = false;
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {
            }
            return res;
        }
    }
}
