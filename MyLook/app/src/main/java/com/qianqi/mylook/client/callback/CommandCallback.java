package com.qianqi.mylook.client.callback;

import xiaofei.library.hermes.annotation.ClassId;
import xiaofei.library.hermes.annotation.MethodId;

/**
 * Created by Administrator on 2017/1/3.
 */

@ClassId("CommandCallback")
public interface CommandCallback {

    @MethodId("onSuccess")
    void onSuccess();

    @MethodId("onFail")
    void onFail();
}
