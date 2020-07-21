package com.xipu.h5.osdk.callback;

import java.util.HashMap;

public interface OLoginApi {

    void onSuccess(HashMap<String, String> maps);

    void onFailure(String loginType,String msg);

}
