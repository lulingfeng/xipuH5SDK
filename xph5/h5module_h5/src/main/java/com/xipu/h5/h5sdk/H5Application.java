package com.xipu.h5.h5sdk;

import android.util.Log;

import com.bytedance.applog.AppLog;
import com.bytedance.applog.ILogger;
import com.bytedance.applog.InitConfig;
import com.bytedance.applog.util.UriConstants;
import com.qq.gdt.action.GDTAction;
import com.tencent.bugly.crashreport.CrashReport;
import com.xipu.h5.sdk.BApplication;
import com.xipu.h5.h5sdk.manager.GDTManager;
import com.xipu.h5.sdk.util.H5Utils;
import com.xipu.h5.sdk.util.ParamUtil;

public class H5Application extends BApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        if (!BuildConfig.DEBUG) {
            Log.d(H5Utils.TAG, "Bugly始化");
            CrashReport.initCrashReport(getApplicationContext(), "592580b3cc", false);
        }
        GDTAction.init(this,
                GDTManager.getInstance().getUserActionSetID(this),
                GDTManager.getInstance().getAppSecretKey(this));
        if (ParamUtil.isUseJrtt()) {
            Log.d(H5Utils.TAG, "头条初始化");
            initRangersAppLog();
        }
    }

    /*
     * 初始化
     * 今日头条
     * 行为收集日志模块
     * UriConfig:  域名默认国内: DEFAULT     新加坡:SINGAPORE    美东:AMERICA
     */
    private void initRangersAppLog() {
        try {
            final InitConfig config = new InitConfig(String.valueOf(ParamUtil.getJrttAid()), ParamUtil.getJrttChannel());
            config.setUriConfig(UriConstants.DEFAULT);
            config.setLogger(new ILogger() {  // 是否在控制台输出日志上报情况
                @Override
                public void log(String s, Throwable throwable) {
                    Log.d("JJTT", s);
                }
            });
            config.setEnablePlay(true);  // 时长统计  每隔一分钟上报心跳日志
            AppLog.init(this, config);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(H5Utils.TAG, "今日头条 initialization failed");
        }
    }
}
