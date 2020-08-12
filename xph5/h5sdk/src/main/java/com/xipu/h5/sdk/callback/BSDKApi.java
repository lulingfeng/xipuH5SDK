package com.xipu.h5.sdk.callback;

import android.app.Activity;
import android.content.Intent;

import com.xipu.h5.sdk.model.ConfigModule;

import java.util.HashMap;

public interface BSDKApi {

    void onModuleInit(ConfigModule configModule);

    void onActivate(Activity activity);

    void onLogin(Activity activity);

    void onSwitchAccount();

    void onPay(final Activity activity, HashMap<String, String> paramsMap);

    void onSendPaySuccess(HashMap<String, String> payMaps,String send_pay_out_order_no,boolean isQueryOrder);

    void handlerPaySuccessOrder();

    void onCreateRole(Activity activity,String values);

    void onLoginRole(Activity activity,String values);

    void onUpdateRole(Activity activity,String values);

    void onThirdLoginSuccess(Activity activity,HashMap<String, String> loginMaps,int sdkType);

    void onThirdLoginFailure(String loginType, String msg);


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
