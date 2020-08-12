package com.xipu.h5.osdk;

import android.text.TextUtils;
import android.util.Log;

import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.xipu.h5.osdk.utils.OSDKUtils;
import com.xipu.h5.sdk.BApplication;
import com.xipu.h5.sdk.util.AppConfigUtils;

import java.util.Map;

public class OSDKApplication extends BApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        OSDKUtils.getInstance().loadThirdConfig(this);
        AppConfigUtils.getInstance().loadConfig(this,null);
        initFacebookTransaction();
        initAppsFlyer();
    }

    /**
     * 初始化facebook 事务
     */
    private void initFacebookTransaction() {
        try {
            FacebookSdk.sdkInitialize(this);
            //记录应用启动
            AppEventsLogger.activateApp(this);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(OSDKUtils.TAG, "initFacebookTransaction Exception: " + e.getMessage());
        }
    }

    /*
     * 初始化 AppsFlyer
     */
    private void initAppsFlyer() {
        try {
            AppsFlyerConversionListener conversionListener = new AppsFlyerConversionListener() {


                @Override
                public void onConversionDataSuccess(Map<String, Object> conversionData) {

                    for (String attrName : conversionData.keySet()) {
                        Log.d(OSDKUtils.TAG, "AppsFlyer attribute: " + attrName + " = " + conversionData.get(attrName));
                    }
                }

                @Override
                public void onConversionDataFail(String errorMessage) {
                    Log.d(OSDKUtils.TAG, "AppsFlyer error getting conversion data: " + errorMessage);
                }

                @Override
                public void onAppOpenAttribution(Map<String, String> conversionData) {

                    for (String attrName : conversionData.keySet()) {
                        Log.d(OSDKUtils.TAG, "AppsFlyer attribute: " + attrName + " = " + conversionData.get(attrName));
                    }

                }

                @Override
                public void onAttributionFailure(String errorMessage) {
                    Log.d(OSDKUtils.TAG, "AppsFlyer error onAttributionFailure : " + errorMessage);
                }
            };

            AppsFlyerLib.getInstance().init(TextUtils.isEmpty(OSDKUtils.getInstance().getAppsFlyerDevKey()) ? "" : OSDKUtils.getInstance().getAppsFlyerDevKey(), conversionListener, this);
            AppsFlyerLib.getInstance().startTracking(this);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(OSDKUtils.TAG, "initAppsFlyer Exception: " + e.getMessage());
        }
    }
}
