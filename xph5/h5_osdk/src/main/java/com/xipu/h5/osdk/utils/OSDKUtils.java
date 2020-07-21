package com.xipu.h5.osdk.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.startobj.util.common.SOCommonUtil;
import com.xipu.h5.osdk.config.OSDKConfig;

import java.util.HashMap;

public class OSDKUtils {

    private static OSDKUtils mInstance;
    public static String TAG = "H5_OSDK";

    private String mGoogleWebClientId;
    private String mLineChannelId;

    public static OSDKUtils getInstance() {
        if (mInstance == null) {
            synchronized (OSDKUtils.class) {
                if (mInstance == null) {
                    mInstance = new OSDKUtils();
                }
            }
        }
        return mInstance;
    }

    public String getGoogleWebClientId() {
        return mGoogleWebClientId;
    }

    public void setGoogleWebClientId(String mGoogleWebClientId) {
        this.mGoogleWebClientId = mGoogleWebClientId;
    }

    public String getLineChannelId() {
        return mLineChannelId;
    }

    public void setLineChannelId(String mLineChannelId) {
        this.mLineChannelId = mLineChannelId;
    }

    public void loadThirdConfig(Context context) {
        getGoogleClientId(context);
        getLineChannelId(context);
    }

    private String getGoogleClientId(Context context) {
        String mGoogleClientId = "";
        try {
            mGoogleClientId = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA)
                    .metaData.get("xp_google_web_client_id").toString();
            setGoogleWebClientId(mGoogleClientId);
        } catch (Exception e) {
            Log.e(TAG, "获取 google web client id 出错 " + e.getMessage());
        }
        if (TextUtils.isEmpty(mGoogleClientId))
            Toast.makeText(context, "google web client id NULL", Toast.LENGTH_SHORT).show();
        return mGoogleClientId;
    }

    private String getLineChannelId(Context context) {
        String mLineChannelId = "";
        try {
            mLineChannelId = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA)
                    .metaData.get("xp_line_channel_id").toString();
            setLineChannelId(mLineChannelId);
        } catch (Exception e) {
            Log.e(TAG, "获取 line channel id 出错 " + e.getMessage());
        }
        if (TextUtils.isEmpty(mLineChannelId))
            Toast.makeText(context, "line channel id NULL", Toast.LENGTH_SHORT).show();
        return mLineChannelId;
    }

    public boolean checkApkExist(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName))
            return false;
        try {
            ApplicationInfo info = context.getPackageManager()
                    .getApplicationInfo(packageName,
                            PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }


    public synchronized void addGoogleOrderInfo(Context context, String orderNo, HashMap<String, String> thirdPayMaps) {
        if (!SOCommonUtil.hasContext(context))
            return;
        HashMap<String, String> maps = getGoogleOrderInfoList(context);
        maps.put(orderNo, JSON.toJSONString(thirdPayMaps));
        Log.d(TAG, "addGoogleOrderInfo: " + maps.toString() + "，size: " + maps.size());
        saveGoogleOrderInfo(context, maps);
    }

    public synchronized HashMap<String, String> getGoogleOrderInfoList(Context context) {
        HashMap<String, String> maps = new HashMap<String, String>();
        if (!SOCommonUtil.hasContext(context))
            return maps;

        String json = SharedPreferencesUtil.getInstance(context).getString(OSDKConfig.GOOGLE_ORDER_INFO, "");
        if (TextUtils.isEmpty(json))
            return maps;
        try {
            maps.putAll(JSON.parseObject(json, new TypeReference<HashMap<String, String>>() {
            }));
            Log.d(TAG, "getGoogleOrderInfoList: " + maps.toString() + "，size: " + maps.size());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return maps;
        }
    }

    public synchronized void removeGoogleOrderInfo(Context context, String orderNo) {
        HashMap<String, String> maps = getGoogleOrderInfoList(context);
        if (maps.containsKey(orderNo)) {
            maps.remove(orderNo);
            saveGoogleOrderInfo(context, maps);
        }
    }

    private synchronized void saveGoogleOrderInfo(Context context, HashMap<String, String> maps) {
        if (!SOCommonUtil.hasContext(context))
            return;
        Log.d(TAG, "saveGoogleOrderInfo(): " + maps.toString() + "，size: " + maps.size());
        SharedPreferences.Editor editor = SharedPreferencesUtil.getInstance(context).edit();
        editor.putString(OSDKConfig.GOOGLE_ORDER_INFO,
                JSON.toJSONString(maps, false)).apply();
    }
}
