package com.xipu.h5.sdk.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.startobj.util.common.SOCommonUtil;
import com.xipu.h5.sdk.config.H5Config;

import java.util.HashMap;

public class H5OrderFileUtils {

    private static H5OrderFileUtils mInstance;

    public static H5OrderFileUtils getInstance() {
        if (mInstance == null) {
            synchronized (H5OrderFileUtils.class) {
                if (mInstance == null) {
                    mInstance = new H5OrderFileUtils();
                }
            }
        }
        return mInstance;
    }


    public void addSendPaySuccessOrderNo(Context context, String orderNo, HashMap<String, String> thirdPayMaps) {
        if (!SOCommonUtil.hasContext(context))
            return;

        HashMap<String, String> maps = getSendPaySuccessOrderNoList(context);
        maps.put(orderNo, JSON.toJSONString(thirdPayMaps));
        Log.d(H5Utils.TAG, "addSendPaySuccessOrderNo: " + maps.toString() + "，size: " + maps.size());
        saveSendPaySuccessOrderNo(context, maps);
    }

    public HashMap<String, String> getSendPaySuccessOrderNoList(Context context) {
        HashMap<String, String> maps = new HashMap<String, String>();
        if (!SOCommonUtil.hasContext(context))
            return maps;

        String json = SharedPreferencesUtil.getInstance(context).getString(H5Config.SP_PAYSUCCESS_DATA, "");
        if (TextUtils.isEmpty(json))
            return maps;
        try {
            maps.putAll(JSON.parseObject(json, new TypeReference<HashMap<String, String>>() {
            }));
            Log.d(H5Utils.TAG, "getSendPaySuccessOrderNoList: " + maps.toString() + "，size: " + maps.size());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return maps;
        }
    }

    public void removeSendPaySuccessOrderNo(Context context, String orderNo) {
        HashMap<String, String> maps = getSendPaySuccessOrderNoList(context);
        maps.remove(orderNo);
        saveSendPaySuccessOrderNo(context, maps);
    }

    private void saveSendPaySuccessOrderNo(Context context, HashMap<String, String> maps) {
        if (!SOCommonUtil.hasContext(context))
            return;
        Log.d(H5Utils.TAG, "saveSendPaySuccessOrderNo(): " + maps.toString() + "，size: " + maps.size());
        SharedPreferences.Editor editor = SharedPreferencesUtil.getInstance(context).edit();
        editor.putString(H5Config.SP_PAYSUCCESS_DATA,
                JSON.toJSONString(maps, false)).apply();
    }
}
