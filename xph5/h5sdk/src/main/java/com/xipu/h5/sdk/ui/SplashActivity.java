package com.xipu.h5.sdk.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.startobj.util.common.SOCommonUtil;
import com.startobj.util.http.SOCallBack.SOCommonCallBack;
import com.startobj.util.http.SOHttpConnection;
import com.startobj.util.http.SOJsonMapper;
import com.startobj.util.http.SORequestParams;
import com.startobj.util.http.SOServertReturnErrorException;
import com.startobj.util.network.SONetworkUtil;

import com.startobj.util.toast.SOToastUtil;
import com.tencent.smtt.sdk.QbSdk;
import com.xipu.h5_sdk.R;
import com.xipu.h5.sdk.config.H5Config;
import com.xipu.h5.sdk.util.H5Utils;
import com.xipu.h5.sdk.util.ParamUtil;
import com.xipu.h5.sdk.util.PermissionsChecker;

import org.json.JSONObject;

import java.util.HashMap;

public class SplashActivity extends Activity {
    private static final int REQUEST_CODE = 0; // 请求码
    // 所需的全部权限
    private static String[] PERMISSIONS = new String[]{Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    // 权限检测器
    private PermissionsChecker mPermissionsChecker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash2);
        H5Utils.hideBottomUIMenu(this);
        init();
    }

    private void init() {
        mPermissionsChecker = new PermissionsChecker(this);// 缺少权限时, 进入权限配置页面
        if (mPermissionsChecker.lacksPermissions(PERMISSIONS)) {
            startPermissionsActivity(this);
        } else {
            startLoading();
            QbSdk.initX5Environment(this, new QbSdk.PreInitCallback() {
                @Override
                public void onCoreInitFinished() {

                }

                @Override
                public void onViewInitFinished(boolean b) {
                    Log.d(H5Utils.TAG, "onViewInitFinished()" + b);
                    // 获取数据
                    obtainData();
                    // loading结束
                }
            });
        }
    }

    /**
     * 开始loading
     */
    private void startLoading() {
        H5Utils.showProgress(this,false,0);
    }

    /**
     * 结束loading
     */
    private void stopLoading() {
        H5Utils.cancelProgress();
    }

    /**
     * 在这里去请求init方法 获取服务器数据 获取本地数据
     */
    private void obtainData() {
        H5Utils.loadConfig(this);
        networkObtain();
    }

    /**
     * 获取服务器数据
     */
    private void networkObtain() {
        if (!SONetworkUtil.isNetworkAvailable(this)) {
            SOToastUtil.showShort(SOCommonUtil.getRes4LocaleStr(SplashActivity.this, "h5_tip_network"));
            return;
        }
        HashMap<String, String> paramsMap = H5Utils.getCommonParams(this);
        paramsMap.put("app_id", ParamUtil.getAppId());
        SORequestParams params = new SORequestParams(H5Config.QUERY_CHANNEL, paramsMap);
        SOHttpConnection.post(this, params, new SOCommonCallBack<String>() {

            @Override
            public void onSuccess(String result) {
                try {
                    stopLoading();
                    String[] datas = SOJsonMapper.fromJson(result);
                    JSONObject dataResult = new JSONObject(datas[1]);
                    H5Utils.setChannel(SplashActivity.this, dataResult.optString("channel"));
                    H5Config.GAME_BASE_URL = dataResult.optString("h5base");
                    H5Config.GAME_URL = H5Config.GAME_BASE_URL + "/play.php";
                    Log.d(H5Utils.TAG, "实际请求地址: " + H5Config.GAME_URL);
                } catch (Exception e) {
                    if (e instanceof SOServertReturnErrorException)
                        SOToastUtil.showShort(e.getMessage());
                    else
                        SOToastUtil.showShort(SOCommonUtil.getRes4LocaleStr(SplashActivity.this, "h5_tip_server_error"));
                    e.printStackTrace();
                }
            }

            @Override
            public void onHttpError(Throwable ex, boolean isOnCallback) {
                SOToastUtil.showShort("networkObtain() onHttpError: "+ex.getMessage());
            }

            @Override
            public void onCodeError(CodeErrorException cex) {
                SOToastUtil.showShort("networkObtain() onCodeError: "+cex.getMessage());
            }

            @Override
            public void onFinished() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent(SplashActivity.this, BrowserActivity.class);
                        startActivity(i);
                        SplashActivity.this.finish();
                    }
                }, 300);
            }
        });


        H5Utils.getIP();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 拒绝时, 关闭页面, 缺少主要权限, 无法运行
        if (requestCode == REQUEST_CODE && resultCode == PermissionsActivity.PERMISSIONS_DENIED) {
            finish();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        } else {
            init();
        }
    }

    // 权限检测器
    private void startPermissionsActivity(Activity activity) {
        PermissionsActivity.startActivityForResult(activity, REQUEST_CODE, PERMISSIONS);
    }
}
