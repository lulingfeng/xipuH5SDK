package com.startobj.util.device;

import java.util.UUID;

import android.annotation.SuppressLint;
import android.content.Context;
import android.telephony.TelephonyManager;

public class SODeviceIdFactory {

	public static final String TAG = "DeviceIdFactory";

	@SuppressLint("NewApi")
	public static String getDeviceId(Context context) {

		final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

		final String tmDevice, tmSerial, androidId;
		tmDevice = "" + tm.getDeviceId();
		tmSerial = "" + tm.getSimSerialNumber();
		androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(),
				android.provider.Settings.Secure.ANDROID_ID);

		String formatStr = String.format("deviceId=%s, serialNumber=%s, androidId=%s", tmDevice, tmSerial, androidId);

		UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
		String uniqueId = deviceUuid.toString();

		return uniqueId;
	}

	public static String getUniqueDeviceId(Context context) {
		return UUID.randomUUID().toString();
	}
}