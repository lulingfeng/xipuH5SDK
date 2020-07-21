package com.xipu.h5.osdk.callback;

import java.util.HashMap;

public interface OBindAccountApi {

    void onSuccess(HashMap<String, String> maps);

    void onFailure(String msg);
}
