package com.zhangyue.h5.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.bytedance.sdk.openadsdk.TTAdConstant;

import org.json.JSONException;

public class TTAdUtils {


    public static String getTTAdAppId(final Context context) {
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

    public static String getTTAdAppName(final Context context) {
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

    /**
     * @param values
     * @return Banner广告参数
     */
    public static AdConfig getBannerAdParams(final String values) {
        AdConfig adConfig = new AdConfig();
        try {
            ZYJSONObject dataResult = new ZYJSONObject(values);
            adConfig.setAd_id(dataResult.getStringDef("ad_id"));
            adConfig.setCount(dataResult.getInt("count"));
            ZYJSONObject styleObject = new ZYJSONObject(dataResult.getStringDef("style"));
            adConfig.setTop(styleObject.getInt("top"));
            adConfig.setLeft(styleObject.getInt("left"));
            adConfig.setWidth(styleObject.getInt("width"));
            adConfig.setHeight(styleObject.getInt("height"));
            Log.d(H5Utils.TAG, "getBannerAdParams: " + adConfig.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(H5Utils.TAG, "getBannerAdParams: " + e.getMessage());
        }
        return adConfig;
    }

    /**
     * @param values
     * @return 插屏广告参数
     */
    public static AdConfig getInteractionAdParams(final String values) {
        AdConfig adConfig = new AdConfig();
        try {
            ZYJSONObject dataResult = new ZYJSONObject(values);
            adConfig.setAd_id(dataResult.getStringDef("ad_id"));
            adConfig.setCount(dataResult.getInt("count"));
            ZYJSONObject styleObject = new ZYJSONObject(dataResult.getStringDef("style"));
            adConfig.setTop(styleObject.getInt("top"));
            adConfig.setLeft(styleObject.getInt("left"));
            adConfig.setWidth(styleObject.getInt("width"));
            adConfig.setHeight(styleObject.getInt("height"));
            Log.d(H5Utils.TAG, "getInteractionAdParams: " + adConfig.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(H5Utils.TAG, "getInteractionAdParams: " + e.getMessage());
        }
        return adConfig;
    }

    public static AdConfig getRewardVideoAdParams(final String values) {
        AdConfig adConfig = new AdConfig();
        try {
            ZYJSONObject dataResult = new ZYJSONObject(values);
            adConfig.setAd_id(dataResult.getStringDef("ad_id"));
            if (dataResult.getStringDef("orientation").contains("vertical")) {
                adConfig.setOrientation(TTAdConstant.VERTICAL);
            } else if (dataResult.getStringDef("orientation").contains("horizontal")) {
                adConfig.setOrientation(TTAdConstant.HORIZONTAL);
            }
            adConfig.setReward_name(dataResult.getStringDef("reward_name"));
            adConfig.setReward_count(dataResult.getInt("reward_count"));
            adConfig.setUser_id(dataResult.getStringDef("user_id"));
            Log.d(H5Utils.TAG, "getFullScreenVideoAdParams: " + adConfig.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(H5Utils.TAG, "getFullScreenVideoAdParams: " + e.getMessage());
        }
        return adConfig;
    }

    /**
     * @param values
     * @return 全屏广告参数
     */
    public static AdConfig getFullScreenVideoAdParams(final String values) {
        AdConfig adConfig = new AdConfig();
        try {
            ZYJSONObject dataResult = new ZYJSONObject(values);
            adConfig.setAd_id(dataResult.getStringDef("ad_id"));
            if (dataResult.getStringDef("orientation").contains("vertical")) {
                adConfig.setOrientation(TTAdConstant.VERTICAL);
            } else if (dataResult.getStringDef("orientation").contains("horizontal")) {
                adConfig.setOrientation(TTAdConstant.HORIZONTAL);
            }
            Log.d(H5Utils.TAG, "getFullScreenVideoAdParams: " + adConfig.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(H5Utils.TAG, "getFullScreenVideoAdParams: " + e.getMessage());
        }
        return adConfig;
    }
}
