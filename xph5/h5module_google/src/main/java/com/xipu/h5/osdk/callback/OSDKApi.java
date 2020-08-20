package com.xipu.h5.osdk.callback;


import com.xipu.h5.sdk.callback.OLoginApi;
import com.xipu.h5.sdk.callback.OPayApi;

import java.util.HashMap;

public interface OSDKApi {

    // api
    OLoginApi getLoginApi();

    OShareApi getShareApi();


    OPayApi getPayApi();

    void onCheckThirdLogin();

    void showOLoginDialog();

    void onLogOut();

    void onBindThirdAccount(HashMap<String, String> loginMaps);

    void onShareSuccess(String platform);

    void onShareCancel(String platform);

    void onShareFailure(String platform, String msg);

}
