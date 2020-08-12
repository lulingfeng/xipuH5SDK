package com.xipu.h5.sdk.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.startobj.util.http.SOCallBack;
import com.startobj.util.http.SOHttpConnection;
import com.startobj.util.http.SOJsonMapper;
import com.startobj.util.http.SORequestParams;
import com.startobj.util.toast.SOToastUtil;
import com.xipu.h5.sdk.config.H5Config;

import java.util.HashMap;
import java.util.Map;

public class AppConfigUtils {

    private static AppConfigUtils instance;

    public static AppConfigUtils getInstance() {
        if (instance == null) {
            synchronized (AppConfigUtils.class) {
                if (instance == null) {
                    instance = new AppConfigUtils();
                }
            }
        }
        return instance;
    }

    public void loadConfig(final Context context, final Handler handler) {
        try {
            Map<String, String> map = new HashMap<>();
            map.put("app_id", context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA)
                    .metaData.get("xp_appid").toString());
            SORequestParams params = new SORequestParams(H5Config.APPCONFIG_URL, map);
            SOHttpConnection.get(params, new SOCallBack.SOCommonCallBack<String>() {
                @Override
                public void onSuccess(String result) {
                    Log.d(H5Utils.TAG, "loadConfig() onSuccess: " + result);
                    try {
                        String[] datas = SOJsonMapper.fromJson(result);
                        if (datas.length != 0) {
                            H5Utils.setIsAppConfigInit(true);
                            ZYJSONObject jsonObject = new ZYJSONObject(datas[1]);
                            String switch_third = jsonObject.getStringDef("switch_third");
                            if (!TextUtils.isEmpty(switch_third)
                                    && "0".equals(switch_third)) {
                                H5Utils.setIsUseThirdSDK(false);
                            }
                            H5Utils.setIsUseThirdSDK(true);
                        }
                    } catch (Exception e) {
                        Log.e(H5Utils.TAG + new Throwable().getStackTrace()[0]
                                .getLineNumber(), e.toString());
                        SOToastUtil.showShort(e.getMessage());
                    }
                }

                @Override
                public void onHttpError(Throwable e, boolean isOnCallback) {
                    Log.e(H5Utils.TAG + +new Throwable().getStackTrace()[0]
                            .getLineNumber(), e.toString());
                }

                @Override
                public void onCodeError(CodeErrorException e) {
                    Log.e(H5Utils.TAG + +new Throwable().getStackTrace()[0]
                            .getLineNumber(), e.toString());
                }

                @Override
                public void onFinished() {
                    if(handler != null){
                        handler.sendEmptyMessage(0);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
