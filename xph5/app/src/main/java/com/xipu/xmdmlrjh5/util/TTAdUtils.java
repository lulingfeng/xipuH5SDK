package com.xipu.xmdmlrjh5.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.bytedance.sdk.openadsdk.TTAdConstant;

import org.json.JSONException;

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




    public AdConfig getAdParams(final String values) {
        AdConfig adConfig = new AdConfig();
        if (!TextUtils.isEmpty(values)) {
            try {
                ZYJSONObject dataResult = new ZYJSONObject(values);
                adConfig.setAd_id(dataResult.getStringDef("ad_id"));
                adConfig.setCount(dataResult.optInt("count"));
                adConfig.setReward_name(dataResult.getStringDef("reward_name"));
                adConfig.setReward_count(dataResult.optInt("reward_count"));
                adConfig.setUser_id(dataResult.getStringDef("user_id"));
                adConfig.setExtra(dataResult.getStringDef("extra"));
                if (dataResult.getStringDef("orientation").contains("vertical")) {
                    adConfig.setOrientation(TTAdConstant.VERTICAL);
                } else if (dataResult.getStringDef("orientation").contains("horizontal")) {
                    adConfig.setOrientation(TTAdConstant.HORIZONTAL);
                }
                //获取坐标
                if (!TextUtils.isEmpty(dataResult.getStringDef("style"))) {
                    ZYJSONObject styleObject = new ZYJSONObject(dataResult.getStringDef("style"));
                    adConfig.setTop(styleObject.optInt("top"));
                    adConfig.setLeft(styleObject.optInt("left"));
                    adConfig.setWidth(styleObject.optInt("width"));
                    adConfig.setHeight(styleObject.optInt("height"));
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(H5Utils.TAG, "getBannerAdParams: " + e.getMessage());
            }
        }
        return adConfig;
    }
}
