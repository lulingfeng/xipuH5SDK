package com.xipu.h5.h5sdk.callback;

import android.app.Activity;
import android.webkit.WebView;
import android.widget.FrameLayout;


public interface H5SDKApi {

    void onReportJRTT(Activity activity,String reportType,String values);

    void onReportGDT(Activity activity,String reportType,String values);

    void onReportTuia(Activity activity);

    void onTTAdInit(Activity activity, WebView mWebView, FrameLayout mExpressContainer, String values);

    void onOpenTTBannerAd(Activity activity,String values);

    void onCloseTTBannerAd(String values);

    void onOpenTTInteractionAd(Activity activity,String values);

    void onOpenTTRewardVideoAd(Activity activity,String values);

    void onOpenTTFullScreenVideoAd(Activity activity,String values);

    void onGetScreenSize(Activity activity,String values);

}
