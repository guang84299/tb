package com.qianqi.mylook.utils;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Created by Administrator on 2017/1/4.
 */

public class CollectionUtils {
    public static int getHashMapIndex(LinkedHashMap<?, ?> map, Object search) {
        Set<?> keys = map.keySet();
        Iterator<?> i = keys.iterator();
        Object curr;
        int count = -1;
        do {
            curr = i.next();
            count++;
            if (curr.equals(search))
                return count;
        }
        while (i.hasNext());
        return -1;
    }
}
