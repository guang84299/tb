package com.qianqi.mylook.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.chad.library.adapter.base.BaseSectionQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.qianqi.mylook.R;
import com.qianqi.mylook.activity.SmartModeConfigActivity;
import com.qianqi.mylook.bean.EnhancePackageInfo;
import com.qianqi.mylook.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/1/7.
 */

public class SmartConfigAdapter extends BaseSectionQuickAdapter<SmartConfigSection, BaseViewHolder> {

    private SmartModeConfigActivity activity;

    /**
     * dynamic object to start
     */
    public SmartConfigAdapter(SmartModeConfigActivity activity) {
        super(SmartConfigSection.item_layout, SmartConfigSection.head_layout, null);
        this.activity = activity;
    }

    public void setData(List<EnhancePackageInfo> list){
        String curHead = "";
        String head = "";
        String noLimit = activity.getString(R.string.not_limit);
        String smart = activity.getString(R.string.smart_power_saving);
        ArrayList<SmartConfigSection> sections = new ArrayList<>();
        for(int i = 0;i < list.size();i++){
            SmartConfigSection section = null;
            EnhancePackageInfo p = list.get(i);
            if(p.isInSmartList()){
                head = noLimit;
            }
            else{
                head = smart;
            }
            if(!curHead.equals(head)){
                SmartConfigSection headSection = new SmartConfigSection(true,head);
                sections.add(headSection);
                curHead = head;
            }
            if(i+1 < list.size()){
                String nextHead = "";
                EnhancePackageInfo next = list.get(i+1);
                if(next.isInSmartList()){
                    nextHead = noLimit;
                }
                else{
                    nextHead = smart;
                }
                if(!nextHead.equals(head)){
                    section = new SmartConfigSection(p,true);
                }
                else{
                    section = new SmartConfigSection(p,false);
                }
            }
            else {
                section = new SmartConfigSection(p,true);
            }
            sections.add(section);
        }
        super.setNewData(sections);
    }

    @Override
    protected void convertHead(BaseViewHolder helper, final SmartConfigSection item) {
        helper.setText(R.id.title, item.header);
    }

    @Override
    protected void convert(BaseViewHolder helper, SmartConfigSection item) {
        EnhancePackageInfo enhancePackageInfo = item.t;
        helper.setText(R.id.title,enhancePackageInfo.getLabel());
//        if(enhancePackageInfo.isRunning){
//            helper.setTextColor(R.id.title,Color.RED);
//        }
//        else{
//            helper.setTextColor(R.id.title,Color.BLACK);
//        }
        helper.setImageDrawable(R.id.icon,enhancePackageInfo.icon);
        if(item.isLast){
            ((LinearLayout.LayoutParams)helper.getView(R.id.divider).getLayoutParams()).setMargins(0,0,0,0);

        }
        else{
            ((LinearLayout.LayoutParams)helper.getView(R.id.divider).getLayoutParams()).setMargins(CommonUtils.dp2px(25),0,0,0);
        }
        helper.addOnClickListener(R.id.item);
    }

}
