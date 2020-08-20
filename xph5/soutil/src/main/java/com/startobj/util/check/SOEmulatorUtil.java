package com.startobj.util.check;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.telephony.TelephonyManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static android.content.Context.SENSOR_SERVICE;

public class SOEmulatorUtil {

    /**
     * 判断是否为模拟器
     * @param context
     * @return true 为模拟器
     */
    public static boolean isEmulator(Context context) {
        if (notHasMotionSensorManager(context) || isFeatures(context) || checkIsNotRealPhone()) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否存在运动触发传感器来判断是否为模拟器
     *
     * @return true 为模拟器
     */
    public static Boolean notHasMotionSensorManager(Context context) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        Sensor sensor8 = sensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION); //运动触发传感器
        if (null == sensor8) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 根据部分特征参数设备信息来判断是否为模拟器
     *
     * @return true 为模拟器
     */
    public static boolean isFeatures(Context context) {
        String url = "tel:" + "123456";
        Intent intent = new Intent();
        intent.setData(Uri.parse(url));
        intent.setAction(Intent.ACTION_DIAL);
        // 是否可以处理跳转到拨号的 Intent
        boolean canResolveIntent = intent.resolveActivity(context.getPackageManager()) != null;
      /*  Log.d("Bruce", "-----1---------------" + Build.FINGERPRINT);
        Log.d("Bruce", "-----1---------------" + Build.FINGERPRINT.startsWith("generic"));
        Log.d("Bruce", "-----2---------------" + Build.FINGERPRINT.toLowerCase());
        Log.d("Bruce", "-----2---------------" + Build.FINGERPRINT.toLowerCase().contains("vbox"));
        Log.d("Bruce", "-----3---------------" + Build.MODEL);
        Log.d("Bruce", "-----3---------------" + Build.MODEL.contains("google_sdk"));
        Log.d("Bruce", "-----4---------------" + Build.MODEL);
        Log.d("Bruce", "-----4---------------" + Build.MODEL.contains("Emulator"));
        //    Log.d("Bruce","-----5---------------"+Build.SERIAL.equalsIgnoreCase("unknown"));
        Log.d("Bruce", "------6--------------" + Build.SERIAL);
        Log.d("Bruce", "------6--------------" + Build.SERIAL.equalsIgnoreCase("android"));
        Log.d("Bruce", "-----7---------------" + Build.MODEL);
        Log.d("Bruce", "-----7---------------" + Build.MODEL.contains("Android SDK built for x86"));
        Log.d("Bruce", "-----8---------------" + Build.MANUFACTURER);
        Log.d("Bruce", "-----8---------------" + Build.MANUFACTURER.contains("Genymotion"));
        Log.d("Bruce", "-----9---------------" + Build.BRAND + "-----" + Build.DEVICE);
        Log.d("Bruce", "-----9---------------" + (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")));
        Log.d("Bruce", "-----10---------------" + Build.PRODUCT);
        Log.d("Bruce", "-----10---------------" + "google_sdk".equals(Build.PRODUCT));
        Log.d("Bruce", "-----11---------------" + ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getNetworkOperatorName().toLowerCase());
        Log.d("Bruce", "-----11---------------" + ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getNetworkOperatorName().toLowerCase().equals("android"));
        Log.d("Bruce", "------12--------------" + !canResolveIntent);*/

        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.toLowerCase().contains("vbox")
                || Build.FINGERPRINT.toLowerCase().contains("test-keys")
        /*        || Build.MODEL.contains("google_sdk")*/
                || Build.MODEL.contains("Emulator")
                || Build.SERIAL.equalsIgnoreCase("android")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT)
                || ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE))
                .getNetworkOperatorName().toLowerCase().equals("android")
                || !canResolveIntent;
    }

    /**
     * 判断cpu是否为电脑来判断 模拟器
     *
     * @return true 为模拟器
     */
    public static boolean checkIsNotRealPhone() {
        String cpuInfo = readCpuInfo();
        if ((cpuInfo.contains("intel") || cpuInfo.contains("amd"))) {
            return true;
        }
        return false;
    }

    /**
     * @return 获取cpu信息
     */
    private static String readCpuInfo() {
        String result = "";
        try {
            String[] args = {"/system/bin/cat", "/proc/cpuinfo"};
            ProcessBuilder cmd = new ProcessBuilder(args);

            Process process = cmd.start();
            StringBuffer sb = new StringBuffer();
            String readLine = "";
            BufferedReader responseReader = new BufferedReader(new InputStreamReader(process.getInputStream(), "utf-8"));
            while ((readLine = responseReader.readLine()) != null) {
                sb.append(readLine);
            }
            responseReader.close();
            result = sb.toString().toLowerCase();
        } catch (IOException ex) {

        } finally {
            return result;
        }
    }

}
