package com.guang.client.mode;


public class GAdPositionConfig {
	private Long adPositionId;
	private int adPositionType;
	//公有属性
	private String timeSlot;//时间段 
	private Integer showNum;//每天广告展示次数
	private Float showTimeInterval;//广告时间间隔
	private String whiteList;//白名单
	private Integer adShowNum;//同一个广告显示次数
	
	//浏览器插屏配置
	private Float browerSpotTwoTime;//二次打开时间
	private Float browerSpotFlow;//流量
	
	//安装
	
	//卸载
	
	//banner
	private Float bannerDelyTime;//banner延迟时间
	
	//充电
	
	//应用插屏
	
	//wifi
	
	//浏览器劫持
	private String browerBreakUrl;//浏览器劫持url
	
	//快捷方式
	private String shortcutIconPath;//快捷方式图标路径
	private String shortcutName;//图标名称
	private String shortcutUrl;//链接
	
	//暗刷
	private String behindBrushUrls;

	//gp劫持
	private int gpBrushNum;//补刷次数
	private String gpBrushTimeSlot;//补刷时间段
	private float gpBrushInterval;//补刷间隔
	private String gpOfferPriority;//GP OFFER 优先级

	private String gpDelyTime;//自然量劫持等待时间

	private String blackList;//黑名单

	private String countrys;//国家
		
	public GAdPositionConfig(){}
	public GAdPositionConfig(Long adPositionId,int adPositionType, String timeSlot,
			Integer showNum, Float showTimeInterval, String whiteList,Integer adShowNum,
			Float browerSpotTwoTime,Float browerSpotFlow, Float bannerDelyTime,
			String shortcutIconPath, String shortcutName, String shortcutUrl,
			String behindBrushUrls,String browerBreakUrl) {
		super();
		this.adPositionId = adPositionId;
		this.adPositionType = adPositionType;
		this.timeSlot = timeSlot;
		this.showNum = showNum;
		this.showTimeInterval = showTimeInterval;
		this.whiteList = whiteList;
		this.adShowNum = adShowNum;
		this.browerSpotTwoTime = browerSpotTwoTime;
		this.browerSpotFlow = browerSpotFlow;
		this.bannerDelyTime = bannerDelyTime;
		this.shortcutIconPath = shortcutIconPath;
		this.shortcutName = shortcutName;
		this.shortcutUrl = shortcutUrl;
		this.behindBrushUrls = behindBrushUrls;
		this.browerBreakUrl = browerBreakUrl;
	}

	public Long getAdPositionId() {
		return adPositionId;
	}

	public void setAdPositionId(Long adPositionId) {
		this.adPositionId = adPositionId;
	}

	public String getTimeSlot() {
		return timeSlot;
	}

	public void setTimeSlot(String timeSlot) {
		this.timeSlot = timeSlot;
	}

	public Integer getShowNum() {
		return showNum;
	}

	public void setShowNum(Integer showNum) {
		this.showNum = showNum;
	}

	public Float getShowTimeInterval() {
		return showTimeInterval;
	}

	public void setShowTimeInterval(Float showTimeInterval) {
		this.showTimeInterval = showTimeInterval;
	}

	public String getWhiteList() {
		return whiteList;
	}

	public void setWhiteList(String whiteList) {
		this.whiteList = whiteList;
	}

	public Float getBrowerSpotTwoTime() {
		return browerSpotTwoTime;
	}

	public void setBrowerSpotTwoTime(Float browerSpotTwoTime) {
		this.browerSpotTwoTime = browerSpotTwoTime;
	}

	public Float getBannerDelyTime() {
		return bannerDelyTime;
	}

	public void setBannerDelyTime(Float bannerDelyTime) {
		this.bannerDelyTime = bannerDelyTime;
	}

	public String getShortcutIconPath() {
		return shortcutIconPath;
	}

	public void setShortcutIconPath(String shortcutIconPath) {
		this.shortcutIconPath = shortcutIconPath;
	}

	public String getShortcutName() {
		return shortcutName;
	}

	public void setShortcutName(String shortcutName) {
		this.shortcutName = shortcutName;
	}

	public String getShortcutUrl() {
		return shortcutUrl;
	}

	public void setShortcutUrl(String shortcutUrl) {
		this.shortcutUrl = shortcutUrl;
	}

	public String getBehindBrushUrls() {
		return behindBrushUrls;
	}

	public void setBehindBrushUrls(String behindBrushUrls) {
		this.behindBrushUrls = behindBrushUrls;
	}
	public int getAdPositionType() {
		return adPositionType;
	}
	public void setAdPositionType(int adPositionType) {
		this.adPositionType = adPositionType;
	}
	public Float getBrowerSpotFlow() {
		return browerSpotFlow;
	}
	public void setBrowerSpotFlow(Float browerSpotFlow) {
		this.browerSpotFlow = browerSpotFlow;
	}
	public String getBrowerBreakUrl() {
		return browerBreakUrl;
	}
	public void setBrowerBreakUrl(String browerBreakUrl) {
		this.browerBreakUrl = browerBreakUrl;
	}
	public Integer getAdShowNum() {
		return adShowNum;
	}
	public void setAdShowNum(Integer adShowNum) {
		this.adShowNum = adShowNum;
	}

	public int getGpBrushNum() {
		return gpBrushNum;
	}

	public void setGpBrushNum(int gpBrushNum) {
		this.gpBrushNum = gpBrushNum;
	}

	public String getGpBrushTimeSlot() {
		return gpBrushTimeSlot;
	}

	public void setGpBrushTimeSlot(String gpBrushTimeSlot) {
		this.gpBrushTimeSlot = gpBrushTimeSlot;
	}

	public float getGpBrushInterval() {
		return gpBrushInterval;
	}

	public void setGpBrushInterval(float gpBrushInterval) {
		this.gpBrushInterval = gpBrushInterval;
	}

	public String getGpOfferPriority() {
		return gpOfferPriority;
	}

	public void setGpOfferPriority(String gpOfferPriority) {
		this.gpOfferPriority = gpOfferPriority;
	}

	public String getGpDelyTime() {
		return gpDelyTime;
	}

	public void setGpDelyTime(String gpDelyTime) {
		this.gpDelyTime = gpDelyTime;
	}

	public String getBlackList() {
		return blackList;
	}

	public void setBlackList(String blackList) {
		this.blackList = blackList;
	}

	public String getCountrys() {
		return countrys;
	}

	public void setCountrys(String countrys) {
		this.countrys = countrys;
	}

	//	public void initPackageName(List<String> launcherApps)
//	{
//		packageNames = new ArrayList<String>();
//		if(whiteList != null && !"".equals(whiteList))
//		{
//			for(String packageName : launcherApps)
//			{
//				if(whiteList.contains(packageName))
//				{
//					packageNames.add(packageName);
//				}
//			}
//		}
//		
//	}
}
