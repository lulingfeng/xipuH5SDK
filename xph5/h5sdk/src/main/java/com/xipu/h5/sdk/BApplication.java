package com.xipu.h5.sdk;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import com.bun.miitmdid.core.JLibrary;
import com.startobj.andserver.AndServer;
import com.startobj.andserver.Server;
import com.startobj.andserver.website.AssetsWebsite;
import com.startobj.andserver.website.WebSite;
import com.startobj.util.toast.SOToastUtil;
import com.xipu.h5.sdk.util.H5Utils;
import com.xipu.h5.sdk.util.OaidHelper;
import com.xipu.h5.sdk.util.ParamUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class BApplication extends Application {

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
        Log.d(H5Utils.TAG, "onCreate: " + H5Utils.getOaid());
        initOaid();
        initAndServer();
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

    private void initAndServer() {
        WebSite webSite = new AssetsWebsite(getAssets(), "");
        AndServer andServer = new AndServer.Build()
                .timeout(10)
                .port(8888)
                .listener(new Server.Listener() {
                    @Override
                    public void onStarted() {
                        Log.d(H5Utils.TAG, "server onStarted");
                    }

                    @Override
                    public void onStopped() {
                        Log.d(H5Utils.TAG, "server onStopped");
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(H5Utils.TAG, "server onError"+e.getMessage());
                    }
                })
                .website(webSite)
                .build();
        Server server = andServer.createServer();
        server.start();
    }
}
