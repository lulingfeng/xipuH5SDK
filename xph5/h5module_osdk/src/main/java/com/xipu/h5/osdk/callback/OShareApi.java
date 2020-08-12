package com.xipu.h5.osdk.callback;

public interface OShareApi {

    void onSuccess(String platform);

    void onCancel(String platform);

    void onFailure(String platform,String msg);
}
