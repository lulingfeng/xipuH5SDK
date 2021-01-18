package com.xipu.h5.sdk.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;


import com.startobj.util.check.SOEmulatorUtil;
import com.startobj.util.common.SOCommonUtil;
import com.startobj.util.device.SODeviceEntity;
import com.startobj.util.device.SODeviceUtils;
import com.startobj.util.file.SOFileUtil;
import com.startobj.util.http.SOCallBack;
import com.startobj.util.http.SOHttpConnection;
import com.startobj.util.http.SORequestParams;
import com.startobj.util.lang.LocaleHelper;
import com.startobj.util.log.SOLogUtil;
import com.startobj.util.toast.SOToastUtil;
import com.xipu.h5.sdk.config.H5Config;
import com.xipu.h5.sdk.dialog.LoadingProgressDialogs;
import com.xipu.h5.sdk.model.UserModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class H5Utils {

    public static final String TAG = "H5SDK_TAG";
    private static Activity mActivity;
    // 自定义对话框
    public static LoadingProgressDialogs mProgressDialog;
    // 设备信息
    public static SODeviceEntity mDeviceEntity;
    private static String mChannel;
    // 用户token一次性
    private static String mIdentityToken;
    private static String mIP;
    private static String mUa;
    //oaid
    private static String oaid;
    //openID
    private static String mOpenID;
    // appConfig 是否 init
    private static boolean isAppConfigInit;
    //是否使第三方
    private static boolean isUseThirdSDK = true;
    // appId
    private static String mAppId;
    // sdk type
    private static int mSDKType;
    // token
    private static String mAccesstoken;
    // 当前登录用户
    private static UserModel mUserModel;
    // 保存的用户列表
    private static List<UserModel> mUserModelList = new ArrayList<UserModel>();
    // 身份认证状态
    private static String mIdentityStatus;

    /**
     * 获取channel
     */
    public static String getChannel() {
        return mChannel;
    }

    public static String getIdentityToken() {
        return mIdentityToken;
    }


    /**
     * 读取manifest配置
     */
    public static void loadConfig(Activity activity) {
        if (!SOCommonUtil.hasContext(activity))
            return;
        mActivity = activity;
        try {
            //////// 动态打包使用--开始////////
            mChannel = getChannel(activity);
            if (TextUtils.isEmpty(mChannel))
                mChannel = getChannelFromApk(activity, "channel_");
            if (TextUtils.isEmpty(mChannel)) {
                mChannel = "demo";
                SOToastUtil.showShort("此信息未配置[游戏渠道:mChannel]时提示，接入方请忽略");
            }
            Bundle metaData = activity.getPackageManager().getApplicationInfo(activity.getPackageName(),
                    PackageManager.GET_META_DATA).metaData;
            mAppId = metaData.getString("xp_appid");
            mSDKType = metaData.getInt("xp_sdk_type");
            //////// 动态打包使用--结束///////
            mIdentityToken = getChannelFromApk(activity, "itoken_");
            if (TextUtils.isEmpty(mIdentityToken))
                mIdentityToken = SOFileUtil.getAssetsFile(activity, "itoken.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 常用参数
    public static HashMap<String, String> getCommonParams(Activity context) {
        mDeviceEntity = SODeviceUtils.getInstance().acquireDeviceInfo(context);

        HashMap<String, String> paramsMap = new HashMap<String, String>();
        paramsMap.put("manufacturer", mDeviceEntity.getManufacturer());
        paramsMap.put("model", mDeviceEntity.getModel());
        paramsMap.put("serial", mDeviceEntity.getSerial());
        paramsMap.put("mac_address", mDeviceEntity.getMacAddress());
        paramsMap.put("imei", mDeviceEntity.getImei1());
        paramsMap.put("imei2", mDeviceEntity.getImei2());
        paramsMap.put("meid1", mDeviceEntity.getMeid1());
        paramsMap.put("meid2", mDeviceEntity.getMeid2());
        paramsMap.put("network", mDeviceEntity.getNetwork());
        paramsMap.put("os_version_name", mDeviceEntity.getOSVersionName());
        paramsMap.put("os_version", mDeviceEntity.getOSVersionCode());
        paramsMap.put("screen", mDeviceEntity.getScreen());
        paramsMap.put("android_id", mDeviceEntity.getAndroidID());
        paramsMap.put("phone", mDeviceEntity.getPhone());
        paramsMap.put("kid", TextUtils.isEmpty(getOaid()) ? getKid(context) : getOaid());
        paramsMap.put("channel", getChannel());
        paramsMap.put("identity_token", getIdentityToken());
        paramsMap.put("sdk_version", H5Config.SDK_VERSION);
        paramsMap.put("sdk_code", H5Config.SDK_CODE);
        paramsMap.put("app_version_code", mDeviceEntity.getVersionCode() + "");
        paramsMap.put("app_version_name", mDeviceEntity.getVersionName());
        paramsMap.put("package_name", mDeviceEntity.getPackageName());
        paramsMap.put("location", mDeviceEntity.getLocation());
        paramsMap.put("platform", getPlatform());
        paramsMap.put("oaid", H5Utils.getOaid());
        paramsMap.put("is_suit", isVirtualMachine(context) ? "1" : "0");
        paramsMap.put("lang", getLanguage());
        if (!TextUtils.isEmpty(getDevice_id(context))) {
            paramsMap.put("device_id", getDevice_id(context));
        }
        return paramsMap;
    }

    /*
     * 获取当前语言
     *
     * @return
     */
    public static String getLanguage() {
        Locale locale = LocaleHelper.getInstance().getLocale(mActivity);
        if (!TextUtils.isEmpty(locale.getCountry())) {
            return locale.getLanguage() + "-" + locale.getCountry();
        } else {
            return locale.getLanguage();
        }
    }

    /**
     * 获取唯一kid 先本地获取，获取不到生成
     */
    public static String getKid(Activity context) {
        String kid = mDeviceEntity.getKid();
        String sdPath = SOFileUtil.obtainFilePath(context, H5Config.SDK_FOLDER_LOCATION, H5Config.SDK_ID_FILENAME);
        if (!SOFileUtil.isFileExist(sdPath))
            try {
                SOFileUtil.writeFileSdcardFile(sdPath, kid, false);
            } catch (IOException e) {
                SOLogUtil.e(TAG, "kid写入错误", false);
            }
        else
            try {
                kid = TextUtils.isEmpty(SOFileUtil.readFileSdcardFile(sdPath)) ? kid
                        : SOFileUtil.readFileSdcardFile(sdPath);
            } catch (IOException e) {
                SOLogUtil.e(TAG, "kid读取错误", false);
            }
        return kid;
    }

    public static String getPlatform() {
        return "android";
    }

    public static void showProgress(Context context) {
        if (!SOCommonUtil.hasContext(context))
            return;
        showProgress(context, true, 0);
    }

    public static void showProgress(Context context, boolean canCancel, long delayMillis) {
        if (!SOCommonUtil.hasContext(context))
            return;
        cancelProgress();
        mProgressDialog = new LoadingProgressDialogs(context);
        mProgressDialog.setCancelable(canCancel);
        mProgressDialog.show();
        if (delayMillis > 0)
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    cancelProgress();
                }
            }, delayMillis);
    }

    public static void cancelProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.cancel();
        }
    }

    /**
     * 从apk中获取版本信息
     *
     * @param context
     * @param channelKey
     * @return
     */
    public static String getChannelFromApk(Context context, String channelKey) {
        // 从apk包中获取
        ApplicationInfo appinfo = context.getApplicationInfo();
        String sourceDir = appinfo.sourceDir;
        // 注意这里：默认放在meta-inf/里， 所以需要再拼接一下
        String key = "META-INF/" + channelKey;
        String ret = "";
        ZipFile zipfile = null;
        try {
            zipfile = new ZipFile(sourceDir);
            Enumeration<?> entries = zipfile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = ((ZipEntry) entries.nextElement());
                String entryName = entry.getName();
                if (entryName.startsWith(key)) {
                    ret = entryName;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (zipfile != null) {
                try {
                    zipfile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return ret.replace(key, "");
    }

    public static String getAccesstoken() {
        return getUserModel() != null ? getUserModel().getAccesstoken() : "";
    }

    public static void setAccesstoken(String mAccesstoken) {
        H5Utils.mAccesstoken = mAccesstoken;
    }

    public static String getOpenID() {
        return mOpenID;
    }

    public static void setOpenID(String mOpenID) {
        H5Utils.mOpenID = mOpenID;
    }

    public static String getIdentityStatus() {
        return TextUtils.isEmpty(mIdentityStatus) ? "0" : mIdentityStatus;
    }

    public static void setIdentityStatus(String identityStatus) {
        mIdentityStatus = identityStatus;
    }

    public static List<UserModel> getUserModelList() {
        return mUserModelList;
    }

    public static UserModel getUserModel() {
        return mUserModel;
    }

    public static void setUserModel(UserModel userModel) {
        mUserModel = userModel;
    }

    public static String getAppID() {
        return mAppId;
    }

    public static void setAppId(String mAppId) {
        H5Utils.mAppId = mAppId;
    }

    public static int getSDKType() {
        return mSDKType;
    }

    public static void setSDKType(int SDKType) {
        H5Utils.mSDKType = SDKType;
    }

    public static boolean isIsAppConfigInit() {
        return isAppConfigInit;
    }

    public static void setIsAppConfigInit(boolean isAppConfigInit) {
        H5Utils.isAppConfigInit = isAppConfigInit;
    }

    public static boolean isIsUseThirdSDK() {
        return isUseThirdSDK;
    }

    public static void setIsUseThirdSDK(boolean isUseThirdSDK) {
        H5Utils.isUseThirdSDK = isUseThirdSDK;
    }

    /**
     * 获取渠道号
     */
    public static String getChannel(Context context) {
        if (!SOCommonUtil.hasContext(context))
            return "";
        // 读取当前用户数据
        return SharedPreferencesUtil.getInstance(context).getString(H5Config.SP_CHANNEL, "");
    }

    /**
     * 设置渠道号
     */
    public static void setChannel(Context context, String channel) {
        if (!SOCommonUtil.hasContext(context) && !TextUtils.isEmpty(channel))
            return;
        mChannel = channel;
        SharedPreferences.Editor userEditor = SharedPreferencesUtil.getInstance(context).edit();
        userEditor.putString(H5Config.SP_CHANNEL, channel);
        userEditor.commit();
    }

    /*
     * 获取device_id
     */
    public static String getDevice_id(Context context) {
        if (!SOCommonUtil.hasContext(context))
            return "";
        return SharedPreferencesUtil.getInstance(context).getString(getAppID() + "_" + H5Config.SP_DEVICE_ID, "");
    }

    /*
     * 设置device_id
     */
    public static void setDevice_id(Context context, String device_id) {
        if (!SOCommonUtil.hasContext(context) && TextUtils.isEmpty(device_id))
            return;
        SharedPreferences.Editor userEditor = SharedPreferencesUtil.getInstance(context).edit();
        userEditor.putString(getAppID() + "_" + H5Config.SP_DEVICE_ID, device_id);
        userEditor.commit();
    }

    public static String joinUrl(String host) {
        return H5Config.API_URL + host;
    }

    /**
     * 隐藏虚拟按键，并且全屏
     */
    public static void hideBottomUIMenu(Activity activity) {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = activity.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = activity.getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }


    /**
     * 设置第一次使用APP
     */
    public static void setIsFirst(Context context) {
        if (!SOCommonUtil.hasContext(context))
            return;
        SharedPreferences.Editor editor = SharedPreferencesUtil.getInstance(context).edit();
        editor.putBoolean(H5Config.SP_ISFIRST, false);
        editor.commit();
    }

    /**
     * 获取是否第一次使用APP
     */
    public static boolean getIsFirst(Context context) {
        if (!SOCommonUtil.hasContext(context))
            return true;
        return SharedPreferencesUtil.getInstance(context).getBoolean(H5Config.SP_ISFIRST, true);
    }


    public static void tuiaApi(final Activity activity, String subType) {
        HashMap<String, String> paramsMap = new HashMap<String, String>();
        String tuiaID = ParamUtil.getTuiaID(activity);
        paramsMap.put("a_oId", tuiaID);
        paramsMap.put("advertKey", ParamUtil.getTuiaAdvertkey());
        paramsMap.put("subType", subType);

        if (TextUtils.isEmpty(tuiaID)) {
            paramsMap.put("ua", mUa);
            paramsMap.put("ip", mIP);
            Log.d(TAG, "ip=========" + mIP);
        }
        SORequestParams params = new SORequestParams(H5Config.THIRD_API_TUIA_URL, paramsMap);
        SOHttpConnection.post(activity, params, new SOCallBack.SOCommonCallBack<String>() {

            @Override
            public void onSuccess(String result) {
                try {
                    ZYJSONObject dataResult = new ZYJSONObject(result);
                    if (dataResult.has("record")) {
                        if ("0000000".equals(dataResult.getString("record"))) {
                            if (dataResult.has("a_oId")) {
                                ParamUtil.setTuiaID(activity, dataResult.getString("a_oId"));
                            }
                        }
                    }

                } catch (Exception e) {
                    SOLogUtil.d(TAG, "tuiaApi Exception : " + e.getMessage(), true);
                }
            }

            @Override
            public void onHttpError(Throwable ex, boolean isOnCallback) {
                SOLogUtil.d(TAG, "tuiaApi onHttpError : " + ex.getMessage(), true);
            }

            @Override
            public void onCodeError(CodeErrorException cex) {
                SOLogUtil.d(TAG, "tuiaApi onCodeError : " + cex.getMessage(), true);
            }

            @Override
            public void onFinished() {
                cancelProgress();
            }
        });
    }

    public static void getIP() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mIP = SODeviceUtils.getInstance().getIp3();
            }
        }).start();
    }

    public static void setWebViewUA(String ua) {
        mUa = ua;
    }

    public static void reportJrtt(final Activity activity, HashMap<String, String> paramsMap) {

        final String out_trade_no = paramsMap.get("out_trade_no");
        final String query_url = TextUtils.isEmpty(out_trade_no) ? H5Config.REPORT_JRTT_REGISTER_URL : H5Config.REPORT_JRTT_PAY_URL;
        SORequestParams params = new SORequestParams(query_url, paramsMap);
        SOHttpConnection.get(activity, params, new SOCallBack.SOCommonCallBack<String>() {

            @Override
            public void onSuccess(String result) {
                Log.d(H5Utils.TAG, "reportJrtt " + (TextUtils.isEmpty(out_trade_no) ? "register" : "pay") + " onSuccess");
            }

            @Override
            public void onHttpError(Throwable ex, boolean isOnCallback) {
                Log.e(H5Utils.TAG, "reportJrtt " + (TextUtils.isEmpty(out_trade_no) ? "register" : "pay") + " onHttpError");
            }

            @Override
            public void onCodeError(CodeErrorException cex) {
                Log.e(H5Utils.TAG, "reportJrtt " + (TextUtils.isEmpty(out_trade_no) ? "register" : "pay") + " onCodeError");
            }

            @Override
            public void onFinished() {
                H5Utils.cancelProgress();
            }
        });
        // pay      app_id, channel, open_id, jrtt, outtradeno

    }


    /**
     * 上报广点通注册行为
     *
     * @param activity
     * @param paramsMap
     */
    public static void reportGDT(final Activity activity, HashMap<String, String> paramsMap) {

        final String query_url = H5Config.REPORT_GDT_REGISTER_URL;
        SORequestParams params = new SORequestParams(query_url, paramsMap);
        SOHttpConnection.get(activity, params, new SOCallBack.SOCommonCallBack<String>() {

            @Override
            public void onSuccess(String result) {
                Log.d(H5Utils.TAG, "reportGDT " + "register onSuccess");
            }

            @Override
            public void onHttpError(Throwable ex, boolean isOnCallback) {
                Log.e(H5Utils.TAG, "reportGDT " + "register onHttpError");
            }

            @Override
            public void onCodeError(CodeErrorException cex) {
                Log.e(H5Utils.TAG, "reportGDT " + "register onCodeError");
            }

            @Override
            public void onFinished() {
                H5Utils.cancelProgress();
            }
        });

    }

    public static String getOaid() {
        return oaid;
    }

    public static void setOaid(String oaid) {
        H5Utils.oaid = oaid;
    }

    /**
     * 检测虚拟机
     *
     * @param context 上下文环境
     * @return true/false 是VM/不是VM
     */
    public static boolean isVirtualMachine(Context context) {
        return SOEmulatorUtil.isEmulator(context);
    }

    public static void clickSleep(final View view, long delayMillis) {
        view.setEnabled(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setEnabled(true);
            }
        }, delayMillis);
    }

}