package com.qianqi.mylook.adapter;

import android.graphics.Color;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.entity.SectionEntity;
import com.qianqi.mylook.MainApplication;
import com.qianqi.mylook.R;
import com.qianqi.mylook.bean.EnhancePackageInfo;
import com.qianqi.mylook.utils.CommonUtils;

/**
 * Created by hesk on 16/2/16.
 * this is the example holder for the simple adapter
 */
public class SmartConfigSection extends SectionEntity<EnhancePackageInfo> {
    static final int item_layout = R.layout.item_smart_config;
    static final int head_layout = R.layout.head_smart_config;
    boolean isLast;

    SmartConfigSection(boolean isHeader, String header) {
        super(isHeader, header);
    }

    SmartConfigSection(EnhancePackageInfo p,boolean isLast) {
        super(p);
        this.isLast = isLast;
    }
}
