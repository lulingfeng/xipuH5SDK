package com.xipu.h5.h5sdk.manager;

import android.app.Activity;
import android.util.Log;

import com.bytedance.applog.GameReportHelper;
import com.xipu.h5.sdk.util.H5Utils;
import com.xipu.h5.sdk.util.ParamUtil;

import java.util.HashMap;

public class JrttManager {

    private static JrttManager instance;

    public static JrttManager getInstance() {
        if (instance == null) {
            synchronized (JrttManager.class) {
                if (instance == null) {
                    instance = new JrttManager();
                }
            }
        }
        return instance;
    }

    public void sendJrttUserInfo(Activity activity,boolean is_report, boolean is_newuser, String open_id) {
        if (ParamUtil.isUseJrtt() && is_report && is_newuser) {
            Log.d(H5Utils.TAG, "jrtt register");
            GameReportHelper.onEventRegister("mobile", true); // 注册行为上报
            HashMap<String, String> map = new HashMap<>();
            map.put("app_id", ParamUtil.getAppId());
            map.put("channel", H5Utils.getChannel());
            map.put("open_id", open_id);
            map.put("imei", H5Utils.mDeviceEntity.getImei1());
            map.put("jrtt_appname", ParamUtil.getJrttAppname());
            map.put("jrtt_channel", ParamUtil.getJrttChannel());
            map.put("jrtt_aid", ParamUtil.getJrttAid() + "");
            H5Utils.reportJrtt(activity, map);
        }
    }

    public void sendJrttPayInfo(Activity activity,boolean is_report, int amount, String out_trade_no, String openId) {
        if (ParamUtil.isUseJrtt() && is_report) {
            Log.e(H5Utils.TAG, "jrtt pay");
            GameReportHelper.onEventPurchase("type", "钻石", "1", 1, "xipudemo", "人民币", true, amount / 100);
            HashMap<String, String> map = new HashMap<>();
            map.put("app_id", ParamUtil.getAppId());
            map.put("channel", H5Utils.getChannel());
            map.put("open_id", openId);
            map.put("imei", H5Utils.mDeviceEntity.getImei1());
            map.put("jrtt_appname", ParamUtil.getJrttAppname());
            map.put("jrtt_channel", ParamUtil.getJrttChannel());
            map.put("jrtt_aid", ParamUtil.getJrttAid() + "");
            map.put("out_trade_no", out_trade_no);
            H5Utils.reportJrtt(activity, map);
        }
    }
}
