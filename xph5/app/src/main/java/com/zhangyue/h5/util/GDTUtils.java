package com.zhangyue.h5.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

public class GDTUtils {

    public static String getUserActionSetID(final Context context) {
        String mUserActionSetID = "";
        try {
            mUserActionSetID = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA)
                    .metaData.get("xp_gdt_useractionsetid").toString();
        } catch (Exception e) {
            Log.e(H5Utils.TAG, "获取user action id出错 " + e.getMessage());
        }
        if (TextUtils.isEmpty(mUserActionSetID))
            Log.e(H5Utils.TAG,"--------GDT id NULL---------");
        return mUserActionSetID;
    }

    public static String getAppSecretKey(final Context context) {
        String mAppSecretKey = "";
        try {
            mAppSecretKey = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA)
                    .metaData.get("xp_gdt_appsecretkey").toString();
        } catch (Exception e) {
            Log.e(H5Utils.TAG, "获取SecretKey出错 " + e.getMessage());
        }
        if (TextUtils.isEmpty(mAppSecretKey))
            Log.e(H5Utils.TAG,"--------GDT key NULL---------");
        return mAppSecretKey;
    }
}
