package com.android.system.core.sometools;

import android.os.Build;

public class GCommons {
	public static String CHANNEL = "test";
	public static int VERSION_CODE = 1;
	public static String VERSION_NAME = "1.0";
	//SharedPreferences
	public static final String SHARED_PRE = "guangclient";
	public static final String SHARED_KEY_TESTMODEL = "testmodel";
	public static final String SHARED_KEY_SDK_VERSIONCODE = "sdk_versioncode";
	public static final String SHARED_KEY_DEX_NAME = "dex_name";
	
	
	public static final String SHARED_KEY_ACTION_TAG = "qew_action_tag";
	
	
	public static final String SHARED_KEY_SDK_VERSION = "sdk_version";
	public static int SDK_VERSION = Build.VERSION.SDK_INT;
	public static final String SHARED_KEY_COUNTRY = "country";
	public static final String SHARED_KEY_UPDATE_SDK_TIME = "update_sdk_time";

	
	public static String SERVER_ADDRESS = "http://api.qiqiup.com:8080/QiupAdServer/";

	//获取sdk配置
	public static  String URI_POST_GET_SDKCONFIG = "http://media.qiqiup.com/QiupAdServer/tb_getConfig";
	//获取最新sdk
	public static String URI_POST_NEW_SDK = SERVER_ADDRESS + "sdk_findNewSdk";
	public static String URI_POST_UPDATE_SDK_NUM = SERVER_ADDRESS + "sdk_updateNum";
	
	//获取地理位置用到
	public static final String IP_URL = "http://ip-api.com/json?lang=zh-CN";

}
