package com.guang.client.mode;

import com.guang.client.tools.GTools;

import org.json.JSONException;
import org.json.JSONStringer;


public class GOffer {
	private String id = "";
	private String packageName = "";
	private String appName = "";
	private String appDesc = "";
	private String size = "";
	private String iconUrl = "";
	private String imageUrl = "";
	private int type = 1;
	private long time;
	private String urlApp;

	private int picNum;

	private String offerType;

	String gpUrl;
	String trackUrl;


	public GOffer() {
	}

	;

	public GOffer(String id, String packageName, String appName,
				  String appDesc, String size, String iconUrl, String imageUrl, String urlApp) {
		super();
		this.id = id;
		this.packageName = packageName;
		this.appName = appName;
		this.appDesc = appDesc;
		this.size = size;
		this.iconUrl = iconUrl;
		this.imageUrl = imageUrl;
		this.urlApp = urlApp;
		this.picNum = 0;
		this.time = GTools.getCurrTime();
	}

	public GOffer(String id, String packageName, String appName, String gpUrl, String trackUrl) {
		this.id = id;
		this.packageName = packageName;
		this.appName = appName;
		this.gpUrl = gpUrl;
		this.trackUrl = trackUrl;
	}


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getAppDesc() {
		return appDesc;
	}

	public void setAppDesc(String appDesc) {
		this.appDesc = appDesc;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getUrlApp() {
		return urlApp;
	}

	public void setUrlApp(String urlApp) {
		this.urlApp = urlApp;
	}

	public boolean isTimeOut() {
		return GTools.getCurrTime() - time < 60 * 60 * 1000;
	}

	public int getPicNum() {
		return picNum;
	}

	public void setPicNum(int picNum) {
		this.picNum = picNum;
	}

	public String getOfferType() {
		return offerType;
	}

	public void setOfferType(String offerType) {
		this.offerType = offerType;
	}

	public String getGpUrl() {
		return gpUrl;
	}

	public void setGpUrl(String gpUrl) {
		this.gpUrl = gpUrl;
	}

	public String getTrackUrl() {
		return trackUrl;
	}

	public void setTrackUrl(String trackUrl) {
		this.trackUrl = trackUrl;
	}

	public static String toJson(GOffer offer) {
		JSONStringer jsonStringer = new JSONStringer();
		try {
			jsonStringer.object();
			jsonStringer.key("id");
			jsonStringer.value(offer.id);

			jsonStringer.key("packageName");
			jsonStringer.value(offer.packageName);

			jsonStringer.key("urlApp");
			jsonStringer.value(offer.urlApp);

			jsonStringer.key("offerType");
			jsonStringer.value(offer.offerType);

			jsonStringer.endObject();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonStringer.toString();
	}
}