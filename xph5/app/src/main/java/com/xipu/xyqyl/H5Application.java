package com.xipu.xyqyl;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.bun.miitmdid.core.JLibrary;
import com.bytedance.applog.AppLog;
import com.bytedance.applog.ILogger;
import com.bytedance.applog.InitConfig;
import com.bytedance.applog.util.UriConstants;
import com.xipu.xyqyl.BuildConfig;
import com.qq.gdt.action.GDTAction;
import com.startobj.util.toast.SOToastUtil;
import com.tencent.bugly.crashreport.CrashReport;
import com.xipu.xyqyl.util.GDTUtils;
import com.xipu.xyqyl.util.H5Utils;
import com.xipu.xyqyl.util.OaidHelper;
import com.xipu.xyqyl.util.ParamUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class H5Application extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SOToastUtil.init(this);
        ParamUtil.loadConfig(this);

        try {
            JLibrary.InitEntry(this);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(H5Utils.TAG, "oaid JLibrary初始化失败" + e.getMessage());
        }
        SOToastUtil.init(this);
        if (!BuildConfig.DEBUG) {
            Log.d(H5Utils.TAG, "Bugly始化");
            CrashReport.initCrashReport(getApplicationContext(), "592580b3cc", false);
        }

        Log.d(H5Utils.TAG, "onCreate: " + H5Utils.getOaid());
        initOaid();
        GDTAction.init(this,
                GDTUtils.getUserActionSetID(this),
                GDTUtils.getAppSecretKey(this));

        if (ParamUtil.isUseJrtt()) {
            Log.d(H5Utils.TAG, "头条初始化");
            initRangersAppLog(this);
        }

      //  TTAdManagerHolder.init(this);
    }


    /*
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

    /*
     * 初始化Oaid
     */
    private void initOaid() {
        try {
            OaidHelper oaidHelper = new OaidHelper();
            oaidHelper.getDeviceOaid(getApplicationContext());  // 获取Oaid
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * 初始化
     * 今日头条
     * 行为收集日志模块
     * UriConfig:  域名默认国内: DEFAULT     新加坡:SINGAPORE    美东:AMERICA
     */
    private void initRangersAppLog(final Context context) {
        Log.e(H5Utils.TAG, "initRangersAppLog()");
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
            AppLog.init(context, config);
            Log.e(H5Utils.TAG, "今日头条 initialization success");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(H5Utils.TAG, "今日头条 initialization failed" + e.getMessage());
        }
    }
}
