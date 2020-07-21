package com.xipu.h5.osdk.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.xipu.h5.osdk.config.OSDKConfig;

public class SharedPreferencesUtil {

    private static SharedPreferences instance;

    public static SharedPreferences getInstance(Context context) {
        if (instance == null) {
            synchronized (SharedPreferencesUtil.class) {
                if (instance == null) {
                    instance = context.getSharedPreferences(OSDKConfig.SDK_SP_NAME, Context.MODE_PRIVATE);
                }
            }
        }
        return instance;
    }
}
