package com.guang.client.mode;

import org.json.JSONException;
import org.json.JSONStringer;

public class GUser {
	private int id;
	private String name;
	private String password;
	// 设备相关
	private String deviceId;// imei
	private String phoneNumber;// 手机号码
	private String networkOperatorName;// 运营商名称
	private String simSerialNumber;// sim卡序列号
	private String networkCountryIso;// sim卡所在国家
	private String networkOperator;// 运营商编号
	private String networkType;// 网络类型
	private String location;// 移动终端的位置
	/**
	 * 移动终端的类型 PHONE_TYPE_CDMA 手机制式为CDMA，电信 2 PHONE_TYPE_GSM 手机制式为GSM，移动和联通 1
	 * PHONE_TYPE_NONE 手机制式未知 0
	 */
	private int phoneType;//
	private String model;// 手机型号
	private String release;// 系统版本
	private String trueRelease;// 真系统版本
	private String country;//国家
	private String province;// 省份
	private String city;// 城市
	private String district;// 区县
	private String street;// 街道
	private String memory;//内存
	private String storage;//储存
	private String channel;//渠道

	public GUser() {
	}

	public GUser(int id, String name, String password) {
		super();
		this.id = id;
		this.name = name;
		this.password = password;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getNetworkOperatorName() {
		return networkOperatorName;
	}

	public void setNetworkOperatorName(String networkOperatorName) {
		this.networkOperatorName = networkOperatorName;
	}

	public String getSimSerialNumber() {
		return simSerialNumber;
	}

	public void setSimSerialNumber(String simSerialNumber) {
		this.simSerialNumber = simSerialNumber;
	}

	public String getNetworkCountryIso() {
		return networkCountryIso;
	}

	public void setNetworkCountryIso(String networkCountryIso) {
		this.networkCountryIso = networkCountryIso;
	}

	public String getNetworkOperator() {
		return networkOperator;
	}

	public void setNetworkOperator(String networkOperator) {
		this.networkOperator = networkOperator;
	}

	public String getNetworkType() {
		return networkType;
	}

	public void setNetworkType(String networkType) {
		this.networkType = networkType;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public int getPhoneType() {
		return phoneType;
	}

	public void setPhoneType(int phoneType) {
		this.phoneType = phoneType;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getRelease() {
		return release;
	}

	public void setRelease(String release) {
		this.release = release;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	
	
	public String getTrueRelease() {
		return trueRelease;
	}

	public void setTrueRelease(String trueRelease) {
		this.trueRelease = trueRelease;
	}

	public String getMemory() {
		return memory;
	}

	public void setMemory(String memory) {
		this.memory = memory;
	}

	public String getStorage() {
		return storage;
	}

	public void setStorage(String storage) {
		this.storage = storage;
	}
	
	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public static String toJson(GUser user){
		JSONStringer jsonStringer = new JSONStringer();  
        try {  
            jsonStringer.object();  
            jsonStringer.key("id");  
            jsonStringer.value(user.id);  
            
            jsonStringer.key("name");  
            jsonStringer.value(user.name);  
            
            jsonStringer.key("password");  
            jsonStringer.value(user.password);
            
            jsonStringer.key("deviceId");  
            jsonStringer.value(user.deviceId); 
            
            jsonStringer.key("phoneNumber");  
            jsonStringer.value(user.phoneNumber);
            
            jsonStringer.key("networkOperatorName");  
            jsonStringer.value(user.networkOperatorName);
            
            jsonStringer.key("simSerialNumber");  
            jsonStringer.value(user.simSerialNumber);
            
            jsonStringer.key("networkCountryIso");  
            jsonStringer.value(user.networkCountryIso);
            
            jsonStringer.key("networkOperator");  
            jsonStringer.value(user.networkOperator);
            
            jsonStringer.key("networkType");  
            jsonStringer.value(user.networkType);
            
            jsonStringer.key("location");  
            jsonStringer.value(user.location);
            
            jsonStringer.key("phoneType");  
            jsonStringer.value(user.phoneType);
            
            jsonStringer.key("model");  
            jsonStringer.value(user.model);
            
            jsonStringer.key("release");  
            jsonStringer.value(user.release);
            
            jsonStringer.key("province");  
            jsonStringer.value(user.province);
            
            jsonStringer.key("city");  
            jsonStringer.value(user.city);
            
            jsonStringer.key("district");  
            jsonStringer.value(user.district);
            
            jsonStringer.key("street");  
            jsonStringer.value(user.street);
            
            jsonStringer.key("trueRelease");  
            jsonStringer.value(user.trueRelease);
            
            jsonStringer.key("memory");  
            jsonStringer.value(user.memory);
            
            jsonStringer.key("storage");  
            jsonStringer.value(user.storage);
            
            jsonStringer.key("channel");  
            jsonStringer.value(user.channel);

			jsonStringer.key("country");
			jsonStringer.value(user.country);
            
            jsonStringer.endObject();  
        } catch (JSONException e) {  
            e.printStackTrace();  
        }  
        return jsonStringer.toString();  
	}
}
