package com.qianqi.mylook.learning;

import com.qianqi.mylook.utils.DateUtils;

import java.util.List;

/**
 * Created by Administrator on 2017/2/9.
 */

public class QuickCooker {

    public static final long DAY = 24*60*60*1000;

    public static float predict(List<RecordItem> recordItems, float[] input) {
        float res = 0;
        float weight = 1;
        long curTime = System.currentTimeMillis();
        for(RecordItem item :recordItems){
            float output = item.getOutput();
            if(output != 1)
                continue;
            String date = item.getDate();
            long time = DateUtils.dayToTime(date);
            if(time < 0){
                weight = 0.1f;
            }
            else{
                int passedDay = (int) ((curTime-time)/DAY);
                if(passedDay < 1)
                    passedDay = 1;
                weight = 1/passedDay;
            }
            float[] recordInput = item.getInput();
            // unit
            if(recordInput[0] == input[0])
                output += 1;
            // saturday
            if(input[1] == 1 && recordInput[1] == 1)
                output += 1;
            // sunday
            if(input[2] == 1 && recordInput[2] == 1)
                output += 1;
            // wifi
            if(input[4] == 1 && recordInput[4] == 1)
                output += 1;
            // mobile
            if(input[5] == 1 && recordInput[5] == 1)
                output += 1;
            res += output*weight;
        }
        return res;
    }
}
