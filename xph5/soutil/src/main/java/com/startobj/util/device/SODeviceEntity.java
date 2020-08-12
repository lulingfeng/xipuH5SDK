package com.startobj.util.device;

/**
 * @Explain
 * @Version 1.0
 * @CreateDate 2016/05/27 下午2:48
 * @Author Eagle Email:lizhengpei@gmail.com
 */
public class SODeviceEntity {
	// 包名
	private String packageName;
	// app版本号
	private int versionCode;
	// app版本名称
	private String versionName;
	// 品牌
	private String manufacturer;
	// 型号
	private String model;
	// 序列号
	private String serial;
	// Mac地址
	private String macAddress;
	// IMEI1
	private String imei1;
	// IMEI2
	private String imei2;
	// MEID
	private String meid1;
	// MEID
	private String meid2;
	// NETWORK
	private String network;
	// 安卓系统API
	private String OSVersionCode;
	// 安卓系统API
	private String OSVersionName;
	// 号码
	private String phone;
	// 分辨率
	private String screen;
	// Android_id
	private String androidID;
	// 自定义唯一ID
	private String kid;
	// 地理位置
	private String location;
	// IP
	private String ip;

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public String getImei1() {
		return imei1;
	}

	public void setImei1(String imei1) {
		this.imei1 = imei1;
	}

	public String getImei2() {
		return imei2;
	}

	public void setImei2(String imei2) {
		this.imei2 = imei2;
	}
	public String getNetwork() {
		return network;
	}

	public void setNetwork(String network) {
		this.network = network;
	}

	public String getOSVersionCode() {
		return OSVersionCode;
	}

	public void setOSVersionCode(String oSVersionCode) {
		OSVersionCode = oSVersionCode;
	}

	public String getOSVersionName() {
		return OSVersionName;
	}

	public void setOSVersionName(String oSVersionName) {
		OSVersionName = oSVersionName;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getScreen() {
		return screen;
	}

	public void setScreen(String screen) {
		this.screen = screen;
	}

	public String getAndroidID() {
		return androidID;
	}

	public void setAndroidID(String androidID) {
		this.androidID = androidID;
	}

	public String getKid() {
		return kid;
	}

	public void setKid(String kid) {
		this.kid = kid;
	}

	public int getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getMeid1() {
		return meid1;
	}

	public void setMeid1(String meid1) {
		this.meid1 = meid1;
	}

	public String getMeid2() {
		return meid2;
	}

	public void setMeid2(String meid2) {
		this.meid2 = meid2;
	}
}
