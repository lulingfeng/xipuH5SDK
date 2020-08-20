package com.xipu.h5.sdk.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;


import com.startobj.util.common.SOCommonUtil;
import com.xipu.h5.sdk.config.H5Config;

public class ParamUtil {

    private static String mAppid;
    // 今日头条统计SDK 是否启用
    private static boolean mIsUseJrtt;
    // 今日头条统计SDK appname
    private static String mJrttAppname;
    // 今日头条统计SDK channel
    private static String mJrttChannel;
    // 今日头条统计SDK aid
    private static int mJrttAid;
    // 推啊API 是否启用
    private static boolean mIsUseTuia;
    // 推啊API AdvertKey
    private static String mTuiaAdvertkey;
    // 是否任玩SDK
    private static String mSdkType;
    // 是否启用闪屏
    private static boolean mIsUseSplash;
    /**
     * 喜扑sdk
     */
    public static final String XI_PU_SDK = "xp";
    /**
     * 掌越sdk
     */
    public static final String REN_WAN_SDK = "rw";
    /**
     * 殷玩sdk
     */
    public static final String YIN_WAN_SDK = "yw";

    public static void loadConfig(Context context) {
        if (context == null)
            return;
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
            mAppid = info.metaData.getString("xp_appid");
            if (TextUtils.isEmpty(mAppid))
                Toast.makeText(context, "此信息未配置[游戏ID:appid]时提示", Toast.LENGTH_SHORT).show();

            // 今日参数获取
            String ues_jrtt = H5Utils.getChannelFromApk(context, "use_jrtt_");
            if (!TextUtils.isEmpty(ues_jrtt) && ues_jrtt.equals("true")) {
//                Log.d(H5Utils.TAG, "jrtt=getChannelFromApk=true");
                mIsUseJrtt = true;
            } else {
                mIsUseJrtt = info.metaData.getBoolean("xp_use_jrtt", false);
//                Log.d(H5Utils.TAG, "jrtt=metaData=" + mIsUseJrtt);
            }
            mJrttAppname = info.metaData.getString("xp_jrtt_appname");
            mJrttChannel = info.metaData.getString("xp_jrtt_channel");
            if (TextUtils.isEmpty(mJrttAppname) || TextUtils.isEmpty(mJrttChannel)) {
                mIsUseJrtt = false;
//                Log.d(H5Utils.TAG, "jrtt=params=" + mIsUseJrtt);
            }
            mJrttAid = info.metaData.getInt("xp_jrtt_aid");

            Log.d(H5Utils.TAG, "jrtt=" + (mIsUseJrtt ? "true" : "false"));

            // 推啊参数获取
            String use_tuia = H5Utils.getChannelFromApk(context, "use_tuia_");
            if (!TextUtils.isEmpty(use_tuia) && use_tuia.equals("true")) {
                mIsUseTuia = true;
            } else
                mIsUseTuia = info.metaData.getBoolean("xp_use_tuia", false);
            mTuiaAdvertkey = info.metaData.getString("xp_tuia_advertkey");
            if (TextUtils.isEmpty(mTuiaAdvertkey))
                mIsUseTuia = false;

            Log.d(H5Utils.TAG, "tuia=" + (mIsUseTuia ? "true" : "false"));

            // SDK类型获取
            String sdkType = H5Utils.getChannelFromApk(context, "sdk_type_");
            mSdkType = XI_PU_SDK;
            if (!TextUtils.isEmpty(sdkType)) {
                if (sdkType.equals(REN_WAN_SDK))
                    mSdkType = REN_WAN_SDK;
                else if (sdkType.equals(YIN_WAN_SDK))
                    mSdkType = YIN_WAN_SDK;
            }
            Log.d(H5Utils.TAG, "SdkType=" + mSdkType);

            // 是否使用闪屏获取
            String use_splash = H5Utils.getChannelFromApk(context, "use_splash_");
            if (TextUtils.isEmpty(use_splash) || !use_splash.equals("false"))
                mIsUseSplash = true;
            Log.d(H5Utils.TAG, "splash=" + (mIsUseSplash ? "true" : "false"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getSdkType() {
        return mSdkType;
    }

    public static boolean isUseSplash() {
        return mIsUseSplash;
    }

    public static String getAppId() {
        return mAppid;
    }

    public static boolean isUseJrtt() {
        return mIsUseJrtt;
    }

    public static String getJrttAppname() {
        return mJrttAppname;
    }

    public static String getJrttChannel() {
        return mJrttChannel;
    }

    public static int getJrttAid() {
        return mJrttAid;
    }

    public static boolean isIsUseTuia() {
        return mIsUseTuia;
    }

    public static String getTuiaAdvertkey() {
        return mTuiaAdvertkey;
    }


    /**
     * 获取推啊ID
     */
    public static String getTuiaID(Activity activity) {
        if (!SOCommonUtil.hasContext(activity))
            return "";
        // 读取当前用户数据
        SharedPreferences spActivate = activity.getSharedPreferences(H5Config.SDK_SP_NAME, Context.MODE_PRIVATE);
        return spActivate.getString(H5Config.SP_TUIAID, "");
    }

    /**
     * 设置推啊ID
     */
    public static void setTuiaID(Context context, String tuiaID) {
        if (!SOCommonUtil.hasContext(context) && !TextUtils.isEmpty(tuiaID))
            return;
        SharedPreferences spActivate = context.getSharedPreferences(H5Config.SDK_SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor userEditor = spActivate.edit();
        userEditor.putString(H5Config.SP_TUIAID, tuiaID);
        userEditor.commit();
    }
}
