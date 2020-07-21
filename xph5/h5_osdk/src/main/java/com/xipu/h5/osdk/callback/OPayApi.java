package com.xipu.h5.osdk.callback;

import java.util.HashMap;

public interface OPayApi {

    void onPaySuccess(HashMap<String,String> payMaps,boolean isQueryOrder);

    void onPayCancel();

    void onPayFailure(String msg);
}
