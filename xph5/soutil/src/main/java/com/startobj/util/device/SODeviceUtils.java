package com.startobj.util.device;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;


import com.startobj.util.log.SOLogUtil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.json.JSONObject;

/**
 * 设备信息
 *
 * @Explain
 * @Version 1.0
 * @CreateDate 2016/05/27 下午2:33
 * @Author Eagle Email:lizhengpei@gmail.com
 */
@SuppressLint("NewApi")
public class SODeviceUtils {
	private static String TAG = SODeviceUtils.class.getName();
	private static SODeviceUtils instance;
	/**
	 * 单例
	 * @return
	 */
	public static SODeviceUtils getInstance() {
		if (instance == null) {
			synchronized (SODeviceUtils.class){
				if(instance == null){
					instance = new SODeviceUtils();
				}
			}
		}
		return instance;
	}

	/**
	 * 获取设备信息
	 */
	public SODeviceEntity acquireDeviceInfo(Context context) {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		SODeviceEntity deviceEntity = new SODeviceEntity();
		SOTelephoneInfo telephoneInfo = SOTelephoneInfo.getInstance(context);
		telephoneInfo.setTelephoneInfo();
//		String macAddress = getLocalMacAddress(context);
//		if (!"02:00:00:00:00:00".equals(macAddress) && !TextUtils.isEmpty(macAddress)) {
//			deviceEntity.setMacAddress(macAddress);
//		} else {
//			try {
//				deviceEntity.setMacAddress(macAddress());
//			} catch (SocketException e) {
//			}
//		}
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		deviceEntity.setMacAddress(recupAdresseMAC(wifi));
		deviceEntity.setManufacturer(Build.MANUFACTURER);
		deviceEntity.setModel(Build.MODEL);
		deviceEntity.setSerial(Build.SERIAL);
		deviceEntity.setOSVersionCode(String.valueOf(Build.VERSION.SDK_INT));
		deviceEntity.setOSVersionName(Build.VERSION.RELEASE);
		deviceEntity.setScreen(SODensityUtil.getScreenWidth(context) + "x" + SODensityUtil.getScreenHeight(context));
		deviceEntity.setPhone(tm.getLine1Number());
		deviceEntity.setImei1(telephoneInfo.getImeiSIM1());
		deviceEntity.setImei2(telephoneInfo.getImeiSIM2());
		deviceEntity.setMeid1(telephoneInfo.getMeidSIM1());
		deviceEntity.setMeid2(telephoneInfo.getMeidSIM2());
		deviceEntity.setNetwork(getNetWorkState(context));
		deviceEntity.setAndroidID(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
		deviceEntity.setKid(SODeviceIdFactory.getDeviceId(context));
		deviceEntity.setVersionCode(getAPPVersionCode(context));
		deviceEntity.setVersionName(getAPPVersionName(context));
		deviceEntity.setPackageName(context.getPackageName());
//		deviceEntity.setLocation(getLocation(context));
		deviceEntity.setIp(getIp());
		return deviceEntity;
	}

	public static String getIp() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			SOLogUtil.e(TAG, ex.toString(), false);
		}
		return null;
	}

	public static String getLocation(Context context) {
		Location location = null;
		try {
			// You do not instantiate this class directly;
			// instead, retrieve it through:
			// Context.getSystemService(Context.LOCATION_SERVICE).
			LocationManager locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
			// 获取GPS支持
			location = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
			if (location == null) {
				// 获取NETWORK支持
				location = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
			}
			return location == null ? "" : location.getLongitude() + "," + location.getLatitude();
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * mac地址
	 *
	 * @param context
	 * @return
	 */
	public static String getLocalMacAddress(Context context) {
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		return info.getMacAddress();
	}

	public static String macAddress() throws SocketException {
		String address = null;
		// 把当前机器上的访问网络接口的存入 Enumeration集合中
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		while (interfaces.hasMoreElements()) {
			NetworkInterface netWork = interfaces.nextElement();
			// 如果存在硬件地址并可以使用给定的当前权限访问，则返回该硬件地址（通常是 MAC）。
			byte[] by = netWork.getHardwareAddress();
			if (by == null || by.length == 0) {
				continue;
			}
			StringBuilder builder = new StringBuilder();
			for (byte b : by) {
				builder.append(String.format("%02X:", b));
			}
			if (builder.length() > 0) {
				builder.deleteCharAt(builder.length() - 1);
			}
			String mac = builder.toString();
			// 从路由器上在线设备的MAC地址列表，可以印证设备Wifi的 name 是 wlan0
			if (netWork.getName().equals("wlan0")) {
				address = mac;
			}
		}
		return address;
	}

	/**
	 * app版本号
	 *
	 * @param context
	 * @return
	 */
	public static int getAPPVersionCode(Context context) {
		int currentVersionCode = 0;
		PackageManager manager = context.getPackageManager();
		try {
			PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
			currentVersionCode = info.versionCode;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return currentVersionCode;
	}

	/**
	 * app版本名称
	 *
	 * @param context
	 * @return
	 */
	public static String getAPPVersionName(Context context) {
		String appVersionName = "";
		PackageManager manager = context.getPackageManager();
		try {
			PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
			appVersionName = info.versionName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return appVersionName;
	}

	/**
	 * 获取网络状态
	 *
	 * @param context
	 * @return
	 */
	public static String getNetWorkState(Context context) {
		String strNetworkType = "";

		ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
				strNetworkType = "WIFI";
			} else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
				String _strSubTypeName = networkInfo.getSubtypeName();
				// TD-SCDMA networkType is 17
				int networkType = networkInfo.getSubtype();
				switch (networkType) {
					case TelephonyManager.NETWORK_TYPE_GPRS:
					case TelephonyManager.NETWORK_TYPE_EDGE:
					case TelephonyManager.NETWORK_TYPE_CDMA:
					case TelephonyManager.NETWORK_TYPE_1xRTT:
					case TelephonyManager.NETWORK_TYPE_IDEN: // api<8 : replace by
						strNetworkType = "2G";
						break;
					case TelephonyManager.NETWORK_TYPE_UMTS:
					case TelephonyManager.NETWORK_TYPE_EVDO_0:
					case TelephonyManager.NETWORK_TYPE_EVDO_A:
					case TelephonyManager.NETWORK_TYPE_HSDPA:
					case TelephonyManager.NETWORK_TYPE_HSUPA:
					case TelephonyManager.NETWORK_TYPE_HSPA:
					case TelephonyManager.NETWORK_TYPE_EVDO_B: // api<9 : replace by
						// 14
					case TelephonyManager.NETWORK_TYPE_EHRPD: // api<11 : replace by
						// 12
					case TelephonyManager.NETWORK_TYPE_HSPAP: // api<13 : replace by
						// 15
						strNetworkType = "3G";
						break;
					case TelephonyManager.NETWORK_TYPE_LTE: // api<11 : replace by
						// 13
						strNetworkType = "4G";
						break;
					default:
						if (_strSubTypeName.equalsIgnoreCase("TD-SCDMA") || _strSubTypeName.equalsIgnoreCase("WCDMA")
								|| _strSubTypeName.equalsIgnoreCase("CDMA2000")) {
							strNetworkType = "3G";
						} else {
							strNetworkType = _strSubTypeName;
						}
						break;
				}
			}
		}
		return strNetworkType;
	}

	private static final String marshmallowMacAddress = "02:00:00:00:00:00";
	private static final String fileAddressMac = "/sys/class/net/wlan0/address";

	public static String recupAdresseMAC(WifiManager wifiMan) {
		WifiInfo wifiInf = wifiMan.getConnectionInfo();

		if (wifiInf == null || wifiInf.getMacAddress() == null) {
			return marshmallowMacAddress;
		}
		if (wifiInf.getMacAddress().equals(marshmallowMacAddress)) {
			String ret = null;
			try {
				ret = getAdressMacByInterface();
				if (ret != null) {
					return ret;
				} else {
					ret = getAddressMacByFile(wifiMan);
					return ret;
				}
			} catch (IOException e) {
				Log.e("MobileAccess", "Erreur lecture propriete Adresse MAC");
			} catch (Exception e) {
				Log.e("MobileAcces", "Erreur lecture propriete Adresse MAC ");
			}
		} else {
			return wifiInf.getMacAddress();
		}
		return marshmallowMacAddress;
	}

	private static String getAdressMacByInterface() {
		try {
			List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface nif : all) {
				if (nif.getName().equalsIgnoreCase("wlan0")) {
					byte[] macBytes = nif.getHardwareAddress();
					if (macBytes == null) {
						return "";
					}

					StringBuilder res1 = new StringBuilder();
					for (byte b : macBytes) {
						res1.append(String.format("%02X:", b));
					}

					if (res1.length() > 0) {
						res1.deleteCharAt(res1.length() - 1);
					}
					return res1.toString();
				}
			}

		} catch (Exception e) {
			Log.e("MobileAcces", "Erreur lecture propriete Adresse MAC ");
		}
		return null;
	}

	private static String getAddressMacByFile(WifiManager wifiMan) throws Exception {
		String ret;
		int wifiState = wifiMan.getWifiState();

		wifiMan.setWifiEnabled(true);
		File fl = new File(fileAddressMac);
		FileInputStream fin = new FileInputStream(fl);
		ret = crunchifyGetStringFromStream(fin);
		fin.close();

		boolean enabled = WifiManager.WIFI_STATE_ENABLED == wifiState;
		wifiMan.setWifiEnabled(enabled);
		return ret;
	}

	// ConvertStreamToString() Utility - we name it as
	// crunchifyGetStringFromStream()
	private static String crunchifyGetStringFromStream(InputStream crunchifyStream) throws IOException {
		if (crunchifyStream != null) {
			Writer crunchifyWriter = new StringWriter();

			char[] crunchifyBuffer = new char[2048];
			try {
				Reader crunchifyReader = new BufferedReader(new InputStreamReader(crunchifyStream, "UTF-8"));
				int counter;
				while ((counter = crunchifyReader.read(crunchifyBuffer)) != -1) {
					crunchifyWriter.write(crunchifyBuffer, 0, counter);
				}
			} finally {
				crunchifyStream.close();
			}
			return crunchifyWriter.toString();
		} else {
			return "No Contents";
		}
	}

	public static String getIp2(Context context) {
		String ip = "0.0.0.0";
		ConnectivityManager conMann = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mobileNetworkInfo = conMann.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		NetworkInfo wifiNetworkInfo = conMann.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (mobileNetworkInfo.isConnected()) {
			ip = getLocalIpAddress();
		} else if (wifiNetworkInfo.isConnected()) {
			WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			int ipAddress = wifiInfo.getIpAddress();
			ip = intToIp(ipAddress);
		}
		return ip;
	}

	private static String getLocalIpAddress() {
		try {
			String ipv4;
			ArrayList<NetworkInterface> nilist = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface ni : nilist) {
				ArrayList<InetAddress> ialist = Collections.list(ni.getInetAddresses());
				for (InetAddress address : ialist) {
					if (!address.isLoopbackAddress()
							&& address instanceof Inet4Address) {
						ipv4 = address.getHostAddress();
						return ipv4;
					}
				}
			}

		} catch (SocketException ex) {
			Log.e("localip", ex.toString());
		}
		return null;
	}

	private static String intToIp(int ipInt) {
		StringBuilder sb = new StringBuilder();
		sb.append(ipInt & 0xFF).append(".");
		sb.append((ipInt >> 8) & 0xFF).append(".");
		sb.append((ipInt >> 16) & 0xFF).append(".");
		sb.append((ipInt >> 24) & 0xFF);
		return sb.toString();
	}

	/**
	 * 获取外网IP地址
	 * @return
	 */
	public static String getIp3(){
		String IP = "0.0.0.0";
		try {
			String address = "http://ip.taobao.com/service/getIpInfo2.php?ip=myip";
			URL url = new URL(address);

			//URLConnection htpurl=url.openConnection();

			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setUseCaches(false);
			connection.setRequestMethod("GET");
			connection.setRequestProperty("user-agent",
					"Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.7 Safari/537.36"); //设置浏览器ua 保证不出现503

			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				InputStream in = connection.getInputStream();

				// 将流转化为字符串
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(in));

				String tmpString = "";
				StringBuilder retJSON = new StringBuilder();
				while ((tmpString = reader.readLine()) != null) {
					retJSON.append(tmpString + "\n");
				}

				JSONObject jsonObject = new JSONObject(retJSON.toString());
				String code = jsonObject.getString("code");

				if (code.equals("0")) {
					JSONObject data = jsonObject.getJSONObject("data");
					IP = data.getString("ip");

					Log.e("提示", "您的IP地址是：" + IP);
				} else {
					IP = "0.0.0.0";
					Log.e("提示", "IP接口异常，无法获取IP地址！");
				}
			} else {
				IP = "0.0.0.0";
				Log.e("提示", "网络连接异常，无法获取IP地址！");
			}
		} catch (Exception e) {
			IP = "0.0.0.0";
			Log.e("提示", "获取IP地址时出现异常，异常信息是：" + e.toString());
		}
		return IP;
	}
}
