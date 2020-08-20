package com.xipu.h5.h5sdk.manager;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.qq.gdt.action.ActionType;
import com.qq.gdt.action.GDTAction;
import com.xipu.h5.sdk.util.H5Utils;
import com.xipu.h5.sdk.util.ParamUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class GDTManager {

    private static GDTManager instance;

    public static GDTManager getInstance() {
        if (instance == null) {
            synchronized (GDTManager.class) {
                if (instance == null) {
                    instance = new GDTManager();
                }
            }
        }
        return instance;
    }

    public String getUserActionSetID(final Context context) {
        String mUserActionSetID = "";
        try {
            mUserActionSetID = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA)
                    .metaData.get("xp_gdt_useractionsetid").toString();
        } catch (Exception e) {
            Log.e(H5Utils.TAG, "获取user action id出错 " + e.getMessage());
        }
        if (TextUtils.isEmpty(mUserActionSetID))
            Log.e(H5Utils.TAG, "--------GDT id NULL---------");
        return mUserActionSetID;
    }

    public String getAppSecretKey(final Context context) {
        String mAppSecretKey = "";
        try {
            mAppSecretKey = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA)
                    .metaData.get("xp_gdt_appsecretkey").toString();
        } catch (Exception e) {
            Log.e(H5Utils.TAG, "获取SecretKey出错 " + e.getMessage());
        }
        if (TextUtils.isEmpty(mAppSecretKey))
            Log.e(H5Utils.TAG, "--------GDT key NULL---------");
        return mAppSecretKey;
    }


    /**
     * 广点通注册
     */
    public void sendGDTRegister(Activity activity, boolean is_newuser, boolean is_ysdk_report, boolean ysdk_report, String openId) {
        if (is_newuser && is_ysdk_report && ysdk_report) {
            GDTAction.logAction(ActionType.START_APP);
            GDTAction.logAction(ActionType.REGISTER);
            HashMap<String, String> map = new HashMap<>();
            map.put("app_id", ParamUtil.getAppId());
            map.put("channel", H5Utils.getChannel());
            map.put("open_id", openId);
            map.put("imei", H5Utils.mDeviceEntity.getImei1());
            map.put("androidid", H5Utils.mDeviceEntity.getAndroidID());
            map.put("oaid", H5Utils.getOaid());
            H5Utils.reportGDT(activity, map);
            Log.d(H5Utils.TAG, "GDT REGISTER & START_APP");
        }
    }


    /**
     * 广点通 支付上报
     *
     * @param is_ysdk_report
     * @param ysdk_report
     * @param ysdk_report_amount
     */
    public void sendGDTPayInfo(boolean is_ysdk_report, boolean ysdk_report,
                               int ysdk_report_amount) {
        if (is_ysdk_report && ysdk_report) {
            try {
                JSONObject actionParam = new JSONObject();
                actionParam.put("value", ysdk_report_amount);
                GDTAction.logAction(ActionType.PURCHASE, actionParam);
                Log.e(H5Utils.TAG, "GDT PURCHASE " + ysdk_report_amount);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
