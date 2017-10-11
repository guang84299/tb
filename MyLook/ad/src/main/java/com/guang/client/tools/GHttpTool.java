package com.guang.client.tools;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by guang on 2017/10/10.
 */

public class GHttpTool {
    private static GHttpTool _instance;
    private GHttpTool(){}

    public static GHttpTool getInstance()
    {
        if(_instance == null)
            _instance = new GHttpTool();
        return _instance;
    }


    public interface GHttpCallback
    {
        void result(boolean state, Object data);
    }

    class GHttpData
    {
        private boolean state;
        private Object response;

        public GHttpData()
        {
            state = false;
        }

        public boolean isState() {
            return state;
        }

        public void setState(boolean state) {
            this.state = state;
        }

        public Object getResponse() {
            return response;
        }

        public void setResponse(Object response) {
            this.response = response;
        }
    }

    public void httpGetRequest(final String dataUrl,final GHttpCallback callback) {
        final GHttpData httpData = new GHttpData();
        final Handler handler = new Handler(){
            @Override
            public void dispatchMessage(Message msg) {
                super.dispatchMessage(msg);
                if(msg.what == 0x01)
                {
                    if(callback != null)
                        callback.result(httpData.isState(),httpData.getResponse());
                }
            }
        };
        new Thread() {
            public void run() {
                // 第一步：创建HttpClient对象
                HttpClient httpCient = new DefaultHttpClient();
                httpCient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);
                httpCient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 60000);
                HttpGet httpGet = new HttpGet(dataUrl);
                HttpResponse httpResponse;
                boolean state = false;
                String response = null;
                try {
                    httpResponse = httpCient.execute(httpGet);
                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        HttpEntity entity = httpResponse.getEntity();
                        response = EntityUtils.toString(entity, "utf-8");// 将entity当中的数据转换为字符串
                        state = true;
                    } else {
                        Log.e("-------------", "httptools httpGetRequest 请求失败！"+dataUrl);
                    }
                } catch (Exception e) {
                    Log.e("--------------", "httptools httpGetRequest 请求失败！"+e.getLocalizedMessage());
                } finally {
                    httpData.setState(state);
                    httpData.setResponse(response);
                    handler.sendEmptyMessage(0x01);
                }
            };
        }.start();
    }

    public void downloadRes(final String dataUrl,final String filePath,final GHttpCallback callback) {
        final GHttpData httpData = new GHttpData();
        new Thread() {
            public void run() {
                boolean state = false;
                File response = null;
                try {
                    // 如果不存在判断文件夹是否存在，不存在则创建
                    File destDir = new File(filePath.substring(0, filePath.lastIndexOf("/")));
                    if (!destDir.exists()) {
                        destDir.mkdirs();
                    }
                    response = new File(filePath);
                    // 请求服务器广告图片
                    URLConnection openConnection = new URL(dataUrl)
                            .openConnection();
                    openConnection.setConnectTimeout(20*1000);
                    openConnection.setReadTimeout(1000*1000);
                    InputStream is = openConnection.getInputStream();
                    byte[] buff = new byte[1024];
                    int len;
                    // 然后是创建文件夹
                    FileOutputStream fos = new FileOutputStream(response);
                    if (null != is) {
                        while ((len = is.read(buff)) != -1) {
                            fos.write(buff, 0, len);
                        }
                    }
                    fos.close();
                    is.close();
                    state = true;

                } catch (Exception e) {
                    Log.e("--------------", "httptools downloadRes 请求失败！",e);
                } finally {
                    httpData.setState(state);
                    httpData.setResponse(response);
                    if(callback != null)
                        callback.result(httpData.isState(),httpData.getResponse());
                }
            };
        }.start();
    }
}
