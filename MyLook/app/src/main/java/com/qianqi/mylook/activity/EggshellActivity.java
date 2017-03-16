package com.qianqi.mylook.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.qianqi.mylook.BusTag;
import com.qianqi.mylook.MainApplication;
import com.qianqi.mylook.R;
import com.qianqi.mylook.bean.EnhancePackageInfo;
import com.qianqi.mylook.boost.BoostComparator;
import com.qianqi.mylook.learning.Cooker;
import com.qianqi.mylook.learning.RecordItem;
import com.qianqi.mylook.learning.UsageCache;
import com.qianqi.mylook.learning.UsagePredictor;
import com.qianqi.mylook.model.PackageFilter;
import com.qianqi.mylook.model.PackageModel;
import com.qianqi.mylook.presenter.AppConfigPresenter;
import com.qianqi.mylook.utils.L;
import com.qianqi.mylook.view.TopTitleBar;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import qiu.niorgai.StatusBarCompat;

public class EggshellActivity extends BaseActivity {

    @BindView(R.id.status)
    TextView statusView;
    @BindView(R.id.app_1)
    TextView app1View;
    @BindView(R.id.app_1_p)
    TextView app1PView;
    @BindView(R.id.app_2)
    TextView app2View;
    @BindView(R.id.app_2_p)
    TextView app2PView;
    @BindView(R.id.app_3)
    TextView app3View;
    @BindView(R.id.app_3_p)
    TextView app3PView;
    @BindView(R.id.app_4)
    TextView app4View;
    @BindView(R.id.app_4_p)
    TextView app4PView;
    @BindView(R.id.app_5)
    TextView app5View;
    @BindView(R.id.app_5_p)
    TextView app5PView;
    @BindView(R.id.kill_app)
    TextView killAppView;
    @BindView(R.id.titleBar)
    TopTitleBar titleBar;
    private boolean running = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eggshell);
//        StatusBarCompat.setStatusBarColor(this, Color.parseColor("#0eb3ca"));
        StatusBarCompat.translucentStatusBar(this);
        ButterKnife.bind(this);
        titleBar.setLeftVisible(true);
        titleBar.setImmersive(true, true, R.color.bar_bg);
        titleBar.setTitle("预测");

    }

    private void setText(final TextView tv, final String s) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv.setText(s);
            }
        });
    }

    private void predict() {
        if (running) {
            setText(statusView, "正在训练模型...请稍等");
            return;
        }
        running = true;
        PackageFilter filter = new PackageFilter.Builder().persistent(false).qianqi(false).build();
        List<EnhancePackageInfo> packageList = PackageModel.getInstance(MainApplication.getInstance()).getPackageList(filter);
        if (packageList == null) {
            setText(statusView, "获取应用列表失败!");
            running = false;
            return;
        }
        setText(statusView, "开始训练模型...");
        EventBus.getDefault().post(new BusTag(BusTag.TAG_FLUSH_LEARNING_DATA));
        Iterator<EnhancePackageInfo> ite = packageList.iterator();
        while (ite.hasNext()) {
            EnhancePackageInfo p = ite.next();
            ArrayList<RecordItem> recordItems = UsageCache.read(MainApplication.getInstance(), p.packageName);
            if (recordItems == null) {
                setText(statusView, "无数据");
                continue;
            }
            List<float[]> inputList = new ArrayList<float[]>();
            List<Float> outputList = new ArrayList<Float>();
            for (RecordItem item : recordItems) {
                float[] input = item.getInput();
                float output = item.getOutput();
                if (input != null && output >= 0) {
                    inputList.add(input);
                    outputList.add(output);
                }
            }
            float[][] X = inputList.toArray(new float[0][0]);
            float[] y = new float[outputList.size()];
            for (int i = 0; i < y.length; i++) {
                y[i] = outputList.get(i);
            }
            boolean res = Cooker.getInstance().fit(p.packageName, X, y);
            setText(statusView, "训练成功:\n" + p.getLabel() + "(" + X.length + ")");
        }
        setText(statusView, "开始预测...");
        ite = packageList.iterator();
        while (ite.hasNext()) {
            EnhancePackageInfo p = ite.next();
            float[] input = UsagePredictor.getInput(p.packageName);
            float[][] X = new float[1][input.length];
            X[0] = input;
            float[] y = Cooker.getInstance().predict(p.packageName, X);
            if (y != null && y.length > 0) {
                p.setUsagePrediction(y[0]);
            } else {
                p.setUsagePrediction(0);
                p.setUsageQuickPrediction(UsagePredictor.quickPredict(p.packageName, input));
            }
            L.d(p.getLabel() + ":" + p.getUsagePrediction());
        }
        setText(statusView, "完成预测!!!");
        BoostComparator comparator = new BoostComparator();
        Collections.sort(packageList, comparator);
        if (packageList.size() > 0) {
            EnhancePackageInfo p = packageList.get(0);
            setText(app1View, "1." + p.getLabel());
            if(p.getUsagePrediction() > 0)
                setText(app1PView, p.getUsagePrediction() + "");
            else
                setText(app1PView, p.getUsageQuickPrediction() + "");
        } else {
            setText(app1View, "1.--");
            setText(app1PView, "--");
        }
        if (packageList.size() > 1) {
            EnhancePackageInfo p = packageList.get(1);
            setText(app2View, "2." + p.getLabel());
            if(p.getUsagePrediction() > 0)
                setText(app2PView, p.getUsagePrediction() + "");
            else
                setText(app2PView, p.getUsageQuickPrediction() + "");
        } else {
            setText(app2View, "2.--");
            setText(app2PView, "--");
        }
        if (packageList.size() > 2) {
            EnhancePackageInfo p = packageList.get(2);
            setText(app3View, "3." + p.getLabel());
            if(p.getUsagePrediction() > 0)
                setText(app3PView, p.getUsagePrediction() + "");
            else
                setText(app3PView, p.getUsageQuickPrediction() + "");
        } else {
            setText(app3View, "3.--");
            setText(app3PView, "--");
        }
        if (packageList.size() > 3) {
            EnhancePackageInfo p = packageList.get(3);
            setText(app4View, "4." + p.getLabel());
            if(p.getUsagePrediction() > 0)
                setText(app4PView, p.getUsagePrediction() + "");
            else
                setText(app4PView, p.getUsageQuickPrediction() + "");
        } else {
            setText(app4View, "4.--");
            setText(app4PView, "--");
        }
        if (packageList.size() > 4) {
            EnhancePackageInfo p = packageList.get(4);
            setText(app5View, "5." + p.getLabel());
            if(p.getUsagePrediction() > 0)
                setText(app5PView, p.getUsagePrediction() + "");
            else
                setText(app5PView, p.getUsageQuickPrediction() + "");
        } else {
            setText(app5View, "5.--");
            setText(app5PView, "--");
        }
        String killApps = null;
        float usage = -1;
        for (int i = packageList.size() - 1; i >= 0; i--) {
            EnhancePackageInfo p = packageList.get(i);
            if (p.isRunning && !p.isSystem) {
                if (killApps == null) {
                    killApps = p.getLabel() + ",";
                    usage = p.getUsagePrediction();
                    break;
                }
//                else{
//                    if(p.getUsagePrediction() == usage){
//                        killApps += p.getLabel()+",";
//                    }
//                    else{
//                        break;
//                    }
//                }
            }
        }
        if (killApps != null && killApps.length() > 0) {
            setText(killAppView, killApps.substring(0, killApps.length() - 1));
        } else {
            setText(killAppView, "--");
        }
        running = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @OnClick(R.id.refresh)
    void refresh() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                predict();
            }
        }.start();
    }

    @OnClick(R.id.share)
    void share() {
        String dataPath = this.getFilesDir().getPath();
        String zipPath = this.getExternalCacheDir()+("/"+System.currentTimeMillis())+".zip";
        L.d("data:"+dataPath);
        L.d("zip:"+zipPath);
        try {
            zipFolder(dataPath,zipPath);
            Intent share = new Intent(Intent.ACTION_SEND);
            share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(zipPath)));
            share.setType("*/*");//此处可发送多种文件
            startActivity(Intent.createChooser(share, "分享应用点击数据"));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this,"",Toast.LENGTH_LONG).show();
        }
    }

    public static void zipFolder(String srcFilePath, String zipFilePath) throws Exception {

// 创建Zip包

        java.util.zip.ZipOutputStream outZip = new java.util.zip.ZipOutputStream(new java.io.FileOutputStream(

                zipFilePath));

// 打开要输出的文件

        java.io.File file = new java.io.File(srcFilePath);

// 压缩

        zipFiles(file.getParent() + java.io.File.separator, file.getName(), outZip);

// 完成,关闭

        outZip.finish();

        outZip.close();

    }

    private static void zipFiles(String folderPath, String filePath, java.util.zip.ZipOutputStream zipOut)

            throws Exception {

        if (zipOut == null) {

            return;

        }

        java.io.File file = new java.io.File(folderPath + filePath);


// 判断是不是文件

        if (file.isFile()) {

            java.util.zip.ZipEntry zipEntry = new java.util.zip.ZipEntry(filePath);

            java.io.FileInputStream inputStream = new java.io.FileInputStream(file);

            zipOut.putNextEntry(zipEntry);


            int len;

            byte[] buffer = new byte[100000];


            while ((len = inputStream.read(buffer)) != -1) {

                zipOut.write(buffer, 0, len);

            }

            inputStream.close();

            zipOut.closeEntry();

        } else {

// 文件夹的方式,获取文件夹下的子文件

            String fileList[] = file.list();

// 如果没有子文件, 则添加进去即可

            if (fileList.length <= 0) {

                java.util.zip.ZipEntry zipEntry = new java.util.zip.ZipEntry(filePath + java.io.File.separator);

                zipOut.putNextEntry(zipEntry);

                zipOut.closeEntry();

            }

// 如果有子文件, 遍历子文件

            for (int i = 0; i < fileList.length; i++) {

                zipFiles(folderPath, filePath + java.io.File.separator + fileList[i], zipOut);

            }

        }

    }

}
