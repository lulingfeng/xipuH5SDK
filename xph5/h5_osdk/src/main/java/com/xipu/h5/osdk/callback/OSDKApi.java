package com.xipu.h5.osdk.callback;

import android.app.Activity;
import android.content.Intent;

import java.util.HashMap;

public interface OSDKApi {

    // api
    OLoginApi getLoginApi();

    OShareApi getShareApi();

    OBindAccountApi getBindAccountApi();

    OPayApi getPayApi();

    // 接口
    void onLogin();

    void onCheckThirdLogin();

    void onLogOut();

    void onLoginSuccess(String value);

    void onThirdLoginSuccess(HashMap<String, String> loginMaps);

    void onBindThirdAccount(HashMap<String, String> loginMaps);

    void onThirdLoginFailure(String loginType, String msg);

    void onPaySuccess(HashMap<String, String> payMaps, boolean isQueryOrder);

    void onPayCancel();

    void onPayFailure(String msg);

    void onShareSuccess(String platform);

    void onShareCancel(String platform);

    void onShareFailure(String platform, String msg);

    // 生命周期
    void onCreate(Activity activity, Intent intent);

    void onStart();

    void onRestart(Activity activity);

    void onResume(Activity activity);

    void onPause(Activity activity);

    void onStop(Activity activity);

    void onDestroy(Activity activity);

    void onNewIntent(Intent intent);

    void onActivityResult(int requestCode, int resultCode, Intent data);

}
