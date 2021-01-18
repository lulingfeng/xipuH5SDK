package com.xipu.h5.h5sdk;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.bytedance.applog.AppLog;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.startobj.util.device.SODensityUtil;
import com.xipu.h5.h5sdk.callback.H5SDKApi;
import com.xipu.h5.h5sdk.manager.GDTManager;
import com.xipu.h5.h5sdk.manager.JrttManager;
import com.xipu.h5.h5sdk.manager.TTAdManagerHolder;
import com.xipu.h5.h5sdk.manager.TuiaManager;
import com.xipu.h5.sdk.BSDK;
import com.xipu.h5.sdk.model.PayReportModel;
import com.xipu.h5.sdk.util.H5Utils;
import com.xipu.h5.sdk.util.ParamUtil;
import com.xipu.h5.sdk.util.ReportTypeUtils;
import com.xipu.h5.sdk.util.ZYJSONObject;

import org.json.JSONException;

import java.util.HashMap;

public class H5SDK extends BSDK implements H5SDKApi {

    private static Activity mActivity;

    private static TTAdNative mTTAdNative;

    @Override
    public void onLoginSuccess(HashMap<String, Object> map) {

    }

    @Override
    public void onLoginFailure() {

    }

    @Override
    public void thirdSaveUserInfo(Activity activity) {

    }

    @Override
    public void onThirdSDKPay(Activity activity, HashMap<String, String> map) {

    }

    @Override
    public void onPaySuccess(PayReportModel payReportModel1) {

    }

    @Override
    public void onPayCancel() {

    }

    @Override
    public void onPayFailure(String msg) {

    }

    @Override
    public void onCreate(Activity activity, Intent intent) {
        Log.d(H5Utils.TAG, "onCreate()");
        super.onCreate(activity, intent);
        this.mActivity = activity;
        TuiaManager.getInstance().tuiaActivate(activity);
    }

    @Override
    public void onStart() {
        Log.d(H5Utils.TAG, "onStart()");
        super.onStart();
    }

    @Override
    public void onRestart(Activity activity) {
        Log.d(H5Utils.TAG, "onRestart()");
        super.onRestart(activity);
    }

    @Override
    public void onResume(Activity activity) {
        Log.d(H5Utils.TAG, "onResume()");
        if (ParamUtil.isUseJrtt()){
            AppLog.onResume(activity);
        }
        super.onResume(activity);
    }

    @Override
    public void onPause(Activity activity) {
        Log.d(H5Utils.TAG, "onPause()");
        if (ParamUtil.isUseJrtt()){
            AppLog.onPause(activity);
        }
        super.onPause(activity);
    }

    @Override
    public void onStop(Activity activity) {
        Log.d(H5Utils.TAG, "onStop()");
        super.onStop(activity);
    }

    @Override
    public void onDestroy(Activity activity) {
        Log.d(H5Utils.TAG, "onDestroy()");
        super.onDestroy(activity);
        TTAdManagerHolder.getInstance().destroy();
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.d(H5Utils.TAG, "onNewIntent()");
        super.onNewIntent(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(H5Utils.TAG, "onActivityResult()");
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onReportJRTT(Activity activity, String reportType, String values) {
        Log.d(H5Utils.TAG,"onReportJRTT: "+reportType);
        try {
            ZYJSONObject dataResult = new ZYJSONObject(values);
            boolean is_report = dataResult.getIntDef("is_report") == 1 ? true : false; // 今日头条标识
            boolean is_newuser = dataResult.getIntDef("is_newuser") == 1 ? true : false; // 新用户标识
            if(reportType.equals(ReportTypeUtils.LOGIN)){
                H5Utils.setOpenID(dataResult.getStringDef("open_id"));
                JrttManager.getInstance().sendJrttUserInfo(activity, is_report, is_newuser,H5Utils.getOpenID());
            }else if(reportType.equals(ReportTypeUtils.PAY)){
                int amount = dataResult.getIntDef("report_amount");
                String out_trade_no = dataResult.getStringDef("out_trade_no");
                JrttManager.getInstance().sendJrttPayInfo(activity, is_report, amount, out_trade_no,H5Utils.getOpenID());
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(H5Utils.TAG, "onReportJRTT() Exception: " + e.getMessage());
        }
    }

    @Override
    public void onReportGDT(Activity activity, String reportType, String values) {
        Log.d(H5Utils.TAG,"onReportGDT: "+reportType);
        try {
            ZYJSONObject dataResult = new ZYJSONObject(values);
            boolean is_ysdk_report = dataResult.getIntDef("is_ysdk_report") == 1 ? true : false; // 广点通标识
            boolean ysdk_report = dataResult.getIntDef("ysdk_report") == 1 ? true : false; // 广点通全局标识
            if (reportType.equals(ReportTypeUtils.LOGIN)) {
                H5Utils.setOpenID(dataResult.getStringDef("open_id"));
                boolean is_newuser = dataResult.getIntDef("is_newuser") == 1 ? true : false; // 新用户标识
                GDTManager.getInstance().sendGDTRegister(activity,is_newuser,is_ysdk_report,ysdk_report,H5Utils.getOpenID());
            } else if (reportType.equals(ReportTypeUtils.PAY)) {
                int ysdk_report_amount = dataResult.getIntDef("ysdk_report_amount");
                GDTManager.getInstance().sendGDTPayInfo(is_ysdk_report, ysdk_report, ysdk_report_amount);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(H5Utils.TAG, "onReportGDT() " + reportType + " Exception: " + e.getMessage());
        }
    }

    @Override
    public void onReportTuia(Activity activity) {
        TuiaManager.getInstance().sendTuiaPayInfo(activity);
    }

    @Override
    public void onTTAdInit(Activity activity, WebView mWebView, FrameLayout mExpressContainer, String values) {
        try {
            Log.d(H5Utils.TAG, "TTAdInit: " + values);
            ZYJSONObject dataResult = new ZYJSONObject(values);
            TTAdManagerHolder.getInstance().init(activity.getApplicationContext(), dataResult.getStringDef("app_id"));
            com.bytedance.sdk.openadsdk.TTAdManager ttAdManager = TTAdManagerHolder.getInstance().get();
            TTAdManagerHolder.getInstance().get().requestPermissionIfNecessary(activity);
            TTAdManagerHolder.getInstance().setWebView(mWebView);
            TTAdManagerHolder.getInstance().setContainer(mExpressContainer);
            mTTAdNative = ttAdManager.createAdNative(activity);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(H5Utils.TAG, "TTAdInit:初始化失败 " + e.getMessage());
        }
    }

    @Override
    public void onOpenTTBannerAd(Activity activity, String values) {
        try {
            TTAdManagerHolder.getInstance().loadBannerAd(activity, mTTAdNative, TTAdManagerHolder.getInstance().getAdParams(values));
        }catch (Exception e){
            e.printStackTrace();
            Log.e(H5Utils.TAG,"onOpenTTBannerAd() Exception: "+e.getMessage());
        }
    }

    @Override
    public void onCloseTTBannerAd(String values) {
        try {
            TTAdManagerHolder.getInstance().closeBanner();
        }catch (Exception e){
            e.printStackTrace();
            Log.e(H5Utils.TAG,"onCloseTTBannerAd() Exception: "+e.getMessage());
        }
    }

    @Override
    public void onOpenTTInteractionAd(Activity activity, String values) {
        try {
            TTAdManagerHolder.getInstance().loadInteractionAd(activity, mTTAdNative, TTAdManagerHolder.getInstance().getAdParams(values));
        }catch (Exception e){
            e.printStackTrace();
            Log.e(H5Utils.TAG,"onOpenTTInteractionAd() Exception: "+e.getMessage());
        }
    }

    @Override
    public void onOpenTTRewardVideoAd(Activity activity, String values) {
        try {
            TTAdManagerHolder.getInstance().loadRewardAd(activity, mTTAdNative, TTAdManagerHolder.getInstance().getAdParams(values));
        }catch (Exception e){
            e.printStackTrace();
            Log.e(H5Utils.TAG,"onOpenTTRewardVideoAd() Exception: "+e.getMessage());
        }
    }

    @Override
    public void onOpenTTFullScreenVideoAd(Activity activity, String values) {
        try {
            TTAdManagerHolder.getInstance().loadFullScreenVideoAd(activity, mTTAdNative, TTAdManagerHolder.getInstance().getAdParams(values));
        }catch (Exception e){
            e.printStackTrace();
            Log.e(H5Utils.TAG,"onOpenTTFullScreenVideoAd() Exception: "+e.getMessage());
        }
    }

    @Override
    public void onGetScreenSize(Activity activity, String values) {
        try {
            TTAdManagerHolder.getInstance().screenSizeCallback(activity, TTAdManagerHolder.getInstance().setTTCallBackParams(SODensityUtil.getScreenWidth(activity), SODensityUtil.getScreenHeight(activity)));
        }catch (Exception e){
            e.printStackTrace();
            Log.e(H5Utils.TAG,"onGetScreenSize() Exception: "+e.getMessage());
        }
    }
}
