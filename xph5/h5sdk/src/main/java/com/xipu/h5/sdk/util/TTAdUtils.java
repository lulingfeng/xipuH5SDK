package com.xipu.h5.sdk.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

public class TTAdUtils {

    private static TTAdUtils instance;

    public static TTAdUtils getInstance() {
        if (instance == null) {
            synchronized (TTAdUtils.class) {
                if (instance == null) {
                    instance = new TTAdUtils();
                }
            }
        }
        return instance;
    }


    public String getTTAdAppId(final Context context) {
        String mTTAdAppId = "";
        try {
            mTTAdAppId = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA)
                    .metaData.get("xp_ttad_appid").toString();
        } catch (Exception e) {
            Log.e(H5Utils.TAG, "获取 TTAd appId id 出错 " + e.getMessage());
        }
        if (TextUtils.isEmpty(mTTAdAppId))
            Log.e(H5Utils.TAG, "--------TTAd appId NULL---------");
        return mTTAdAppId;
    }

    public String getTTAdAppName(final Context context) {
        String mTTAdAppName = "";
        try {
            mTTAdAppName = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA)
                    .metaData.get("xp_ttad_appname").toString();
        } catch (Exception e) {
            Log.e(H5Utils.TAG, "获取 TTAd appName 出错 " + e.getMessage());
        }
        if (TextUtils.isEmpty(mTTAdAppName))
            Log.e(H5Utils.TAG, "--------TTAd appName NULL---------");
        return mTTAdAppName;
    }

}
