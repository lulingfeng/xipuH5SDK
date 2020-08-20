package com.xipu.h5.osdk.manager;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import com.linecorp.linesdk.LineProfile;
import com.linecorp.linesdk.Scope;
import com.linecorp.linesdk.api.LineApiClient;
import com.linecorp.linesdk.api.LineApiClientBuilder;
import com.linecorp.linesdk.auth.LineAuthenticationParams;
import com.linecorp.linesdk.auth.LineLoginApi;
import com.linecorp.linesdk.auth.LineLoginResult;
import com.startobj.util.common.SOCommonUtil;
import com.startobj.util.toast.SOToastUtil;
import com.xipu.h5.osdk.OSDK;
import com.xipu.h5.osdk.utils.OLoginCodeUtils;
import com.xipu.h5.osdk.utils.OLoginTypeUtils;
import com.xipu.h5.osdk.utils.OSDKUtils;
import com.xipu.h5.osdk.utils.OSharePlatform;
import com.xipu.h5.osdk.utils.OShareType;
import com.xipu.h5.osdk.utils.ThreadManager;
import com.xipu.h5.sdk.config.H5Config;
import com.xipu.h5.sdk.util.H5Utils;

import java.util.Arrays;
import java.util.HashMap;


/**
 * @author bruce
 * Line
 */
public class LineManager {

    private static LineManager mInstance;
    /**
     * 上下文
     */
    private Activity mActivity;
    /**
     * 是否绑定
     */
    private boolean mIsBindAccount;
    /**
     * line login
     */
    private LineApiClient mLineApiClient;

    public static LineManager getInstance() {
        if (mInstance == null) {
            synchronized (LineManager.class) {
                if (mInstance == null) {
                    mInstance = new LineManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 初始化 Line 登录
     */
    public void initLineLogin(Activity activity) {
        mActivity = activity;
        LineApiClientBuilder apiClientBuilder = new LineApiClientBuilder(mActivity, OSDKUtils.getInstance().getLineChannelId());
        mLineApiClient = apiClientBuilder.build();
    }

    /**
     * 校验 Line 登录状态
     */
    public void checkLineAccessToken() {
        ThreadManager.getInstance().getCachePoolThread().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mLineApiClient.verifyToken().isSuccess()) {
                        Log.d(OSDKUtils.TAG, "line already login");
                        H5Utils.showProgress(mActivity,false,0);
                        final String line_accessToken = mLineApiClient.getCurrentAccessToken().getResponseData().getTokenString();
                        final LineProfile profile = mLineApiClient.getProfile().getResponseData();
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                OSDK.getInstance().mLoginMap = new HashMap<>();
                                OSDK.getInstance().mLoginMap.put("third_openid", profile.getUserId());
                                OSDK.getInstance().mLoginMap.put("third_type", OLoginTypeUtils.LINE);
                                OSDK.getInstance().mLoginMap.put("third_ext", "{" +
                                        "\"lineProfile\":\"" + profile.toString() +
                                        "\",\"accesstoken\":\"" + line_accessToken +
                                        "\"}");
                                OSDK.getInstance().onThirdLoginSuccess(mActivity,OSDK.getInstance().mLoginMap, H5Config.TYPE_OSDK);
                            }
                        });

                    } else {
                        Log.d(OSDKUtils.TAG, "line no login");
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                OSDK.getInstance().showOLoginDialog();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(OSDKUtils.TAG, "line check token exception " + e.getMessage());
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            OSDK.getInstance().showOLoginDialog();
                        }
                    });
                }
            }
        });
    }

    /**
     * Line login
     */
    public void lineLogin(boolean isBind) {
        this.mIsBindAccount = isBind;
        try {
            // App-to-app login
            LineAuthenticationParams params = new LineAuthenticationParams.Builder()
                    .scopes(Arrays.asList(Scope.PROFILE))
                    .botPrompt(LineAuthenticationParams.BotPrompt.normal)
                    .build();

            Intent loginIntent = LineLoginApi.getLoginIntent(
                    mActivity,
                    OSDKUtils.getInstance().getLineChannelId(),
                    params);
            mActivity.startActivityForResult(loginIntent, OLoginCodeUtils.LINE_SIGN_IN);
        } catch (Exception e) {
            Log.e(OSDKUtils.TAG, "Line login ERROR: "+e.toString());
            if(mIsBindAccount){
                SOToastUtil.showShort(SOCommonUtil.getRes4LocaleStr(mActivity,"o_line_bind_onError"));
            }else {
                SOToastUtil.showShort(SOCommonUtil.getRes4LocaleStr(mActivity, "o_line_login_exception")+": "+e.getMessage());
            }
        }
    }


    /**
     * Line 退出
     */
    public void lineLogOut() throws Exception {
        ThreadManager.getInstance().getCachePoolThread().execute(new Runnable() {
            @Override
            public void run() {
                Log.d(OSDKUtils.TAG, "lineSingOut()");
                mLineApiClient.logout();
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OLoginCodeUtils.LINE_SIGN_IN) {

            try {
                LineLoginResult result = LineLoginApi.getLoginResultFromIntent(data);
                switch (result.getResponseCode()) {
                    case SUCCESS:
                        H5Utils.showProgress(mActivity,false,0);
                        OSDK.getInstance().mLoginMap = new HashMap<>();
                        OSDK.getInstance().mLoginMap.put("third_openid", result.getLineProfile().getUserId());
                        OSDK.getInstance().mLoginMap.put("third_type", OLoginTypeUtils.LINE);
                        OSDK.getInstance().mLoginMap.put("third_ext", "{" +
                                "\"lineCredential\":\"" + result.getLineCredential().toString() +
                                "\",\"lineIdToken\":\"" + result.getLineIdToken() +
                                "\",\"lineProfile\":\"" + result.getLineProfile().toString() +
                                "\",\"accesstoken\":\"" + result.getLineCredential().getAccessToken().getTokenString() +
                                "\"}");
                        if (mIsBindAccount) {
                            OSDK.getInstance().onBindThirdAccount(OSDK.getInstance().mLoginMap);
                        } else {
                            OSDK.getInstance().onThirdLoginSuccess(mActivity,OSDK.getInstance().mLoginMap,H5Config.TYPE_OSDK);
                        }
                        break;
                    case CANCEL:
                        // Login canceled by user
                        Log.e(OSDKUtils.TAG, "LINE Login Canceled by user");
                        if(mIsBindAccount){
                            SOToastUtil.showShort(SOCommonUtil.getRes4LocaleStr(mActivity,"o_line_bind_onCancel"));
                        }else {
                            SOToastUtil.showShort(SOCommonUtil.getRes4LocaleStr(mActivity,"o_line_login_canceled"));
                        }
                        break;

                    default:
                        // Login canceled due to other error
                        Log.d(OSDKUtils.TAG, "Line Login FAILED!" + result.getErrorData().toString());
                        if(mIsBindAccount){
                            SOToastUtil.showShort(SOCommonUtil.getRes4LocaleStr(mActivity,"o_line_bind_onError"));
                        }else {
                            OSDK.getInstance().onThirdLoginFailure(OLoginTypeUtils.LINE,SOCommonUtil.getRes4LocaleStr(mActivity, "o_line_login_error")+": "+result.getErrorData().getMessage());
                        }
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(OSDKUtils.TAG, "line login Exception" + e.toString());
                if(mIsBindAccount){
                    SOToastUtil.showShort(SOCommonUtil.getRes4LocaleStr(mActivity,"o_line_bind_onError"));
                }else {
                    SOToastUtil.showShort(SOCommonUtil.getRes4LocaleStr(mActivity, "o_line_login_exception")+": "+e.getMessage());
                }
            }
        } else if (requestCode == OLoginCodeUtils.LINE_SHARE) {
            // 分享成功
            OSDK.getInstance().onShareSuccess(OSharePlatform.LINE);
        }
    }

    /**
     * 分享 line
     *
     * @param localUrl
     * @param text
     */
    public void shareLine(String localUrl, String text, String type) throws Exception {
        String linePackageName = "jp.naver.line.android";
        if (!OSDKUtils.getInstance().checkApkExist(mActivity, linePackageName)) {
            OSDK.getInstance().onShareFailure(OSharePlatform.LINE,SOCommonUtil.getRes4LocaleStr(mActivity, "o_line_no_install"));
            return;
        }
        boolean isOlderVersion = false;
        ComponentName componentName;
        try {
            PackageInfo packageInfo = mActivity.getPackageManager().getPackageInfo(linePackageName, 0);
            if (packageInfo != null) {
                String[] arrays = packageInfo.versionName.split("\\.");
                if (arrays.length > 0) {
                    isOlderVersion = Integer.parseInt(arrays[0]) < 8;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (isOlderVersion) {
            componentName = new ComponentName(linePackageName, "jp.naver.line.android.activity.selectchat.SelectChatActivity");
        } else {
            componentName = new ComponentName(linePackageName, "jp.naver.line.android.activity.selectchat.SelectChatActivityLaunchActivity");
        }
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        if (type.equals(OShareType.IMAGE)) {
            shareIntent.setType("image/*");
            Uri uri = Uri.parse(localUrl);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        } else if (type.equals(OShareType.TEXT)) {
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        }
        shareIntent.setComponent(componentName);
        mActivity.startActivityForResult(Intent.createChooser(shareIntent, ""), OLoginCodeUtils.LINE_SHARE);
    }

    /**
     * 释放
     */
    public void lineDestroy() {
        Log.d(OSDKUtils.TAG, "lineDestroy()");
        mLineApiClient = null;
    }
}
