package com.qianqi.mylook.utils;

import java.math.BigDecimal;

/**
 * Created by Administrator on 2017/1/16.
 */

public class MathUtils {

    public static float scaleFloat(float f) {
        BigDecimal b = new BigDecimal(f);
        float f1 = b.setScale(4, BigDecimal.ROUND_HALF_UP).floatValue();
        return f1;
    }
}
