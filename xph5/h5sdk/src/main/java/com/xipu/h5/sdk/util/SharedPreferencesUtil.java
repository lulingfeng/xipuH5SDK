package com.xipu.h5.sdk.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.xipu.h5.sdk.config.H5Config;

public class SharedPreferencesUtil {

    private static SharedPreferences instance;

    public static SharedPreferences getInstance(Context context) {
        if (instance == null) {
            synchronized (SharedPreferencesUtil.class) {
                if (instance == null) {
                    instance = context.getSharedPreferences(H5Config.SDK_SP_NAME, Context.MODE_PRIVATE);
                }
            }
        }
        return instance;
    }
}
