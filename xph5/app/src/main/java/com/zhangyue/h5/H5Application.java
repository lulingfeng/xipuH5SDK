package com.zhangyue.h5;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.bun.miitmdid.core.JLibrary;
import com.bytedance.applog.AppLog;
import com.bytedance.applog.InitConfig;
import com.bytedance.applog.util.UriConfig;
import com.qq.gdt.action.GDTAction;
import com.startobj.util.common.SOCommonUtil;
import com.startobj.util.toast.SOToastUtil;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.smtt.sdk.QbSdk;
import com.zhangyue.h5.util.GDTUtils;
import com.zhangyue.h5.util.H5Utils;
import com.zhangyue.h5.util.OaidHelper;
import com.zhangyue.h5.util.ParamUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class H5Application extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        SOToastUtil.init(this);

        if (!BuildConfig.DEBUG) {
            Log.d(H5Utils.TAG, "Bugly初始化");
            CrashReport.initCrashReport(getApplicationContext(), "592580b3cc", false);
        }

        if (!H5Utils.isVirtualMachine(getApplicationContext())) {
            OaidHelper oaidHelper = new OaidHelper();
            oaidHelper.getDeviceOaid(getApplicationContext());
        }

        ParamUtil.loadConfig(getApplicationContext());
        Log.d(H5Utils.TAG, "onCreate: "+H5Utils.getOaid());

        GDTAction.init(this,
                GDTUtils.getUserActionSetID(this),
                GDTUtils.getAppSecretKey(this));

        if (ParamUtil.isUseJrtt()) {
            Log.d(H5Utils.TAG, "头条初始化");
            initRangersAppLog();
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        JLibrary.InitEntry(this);
    }

    /**
     * 获取进程号对应的进程名
     *
     * @param pid 进程号
     * @return 进程名
     */
    private static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 初始化
     * 今日头条
     * 行为收集日志模块
     * UriConfig:  域名默认国内: DEFAULT     新加坡:SINGAPORE    美东:AMERICA
     */
    private void initRangersAppLog() {
        try {
            final InitConfig config = new InitConfig(String.valueOf(ParamUtil.getJrttAid()), ParamUtil.getJrttChannel());
            config.setUriConfig(UriConfig.DEFAULT);
            AppLog.setEnableLog(false);  // 是否在控制台输出日志上报情况
            config.setEnablePlay(true);  // 时长统计  每隔一分钟上报心跳日志
            AppLog.init(this, config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
