package com.xipu.h5.sdk.callback;


public interface OPayApi {

    void onPaySuccess();

    void onPayCancel();

    void onPayFailure(String msg);
}
