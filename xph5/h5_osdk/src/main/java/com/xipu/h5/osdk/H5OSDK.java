package com.xipu.h5.osdk;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

import com.startobj.util.common.SOCommonUtil;
import com.startobj.util.toast.SOToastUtil;
import com.xipu.h5.osdk.callback.OBindAccountApi;
import com.xipu.h5.osdk.callback.OLoginApi;
import com.xipu.h5.osdk.callback.OPayApi;
import com.xipu.h5.osdk.callback.OSDKApi;
import com.xipu.h5.osdk.callback.OShareApi;
import com.xipu.h5.osdk.manager.FacebookManager;
import com.xipu.h5.osdk.manager.GoogleManager;
import com.xipu.h5.osdk.manager.LineManager;
import com.xipu.h5.osdk.utils.OLoginTypeUtils;
import com.xipu.h5.osdk.utils.OSDKUtils;

import java.util.HashMap;
import java.util.List;

public class H5OSDK implements OSDKApi {

    public static HashMap<String, String> mLoginMap;

    private OLoginApi mLoginApi;

    private OBindAccountApi mBindAccountApi;

    private OShareApi mShareApi;

    private OPayApi mPayApi;

    private static H5OSDK instance;

    private static Activity mActivity;

    /*
     * 单例
     */
    public static H5OSDK getInstance() {
        if (instance == null) {
            synchronized (H5OSDK.class) {
                if (instance == null) {
                    instance = new H5OSDK();
                }
            }
        }
        return instance;
    }

    /**
     * 登录
     *
     * @param oLoginType
     * @param oLoginApi
     */
    public void addOnOverseaLoginListener(String oLoginType, OLoginApi oLoginApi) {
        this.mLoginApi = oLoginApi;

        switch (oLoginType) {
            case OLoginTypeUtils.GOOGLE:
                GoogleManager.getInstance().googleLogin(false);
                break;
            case OLoginTypeUtils.FACEBOOK:
                FacebookManager.getInstance().facebookLogin(false);
                break;
            case OLoginTypeUtils.LINE:
                LineManager.getInstance().lineLogin(false);
                break;
        }
    }

    /**
     * 绑定
     *
     * @param oLoginType
     * @param oBindAccountApi
     */
    public void addOnOverseaLoginBindAccountListener(String oLoginType, OBindAccountApi oBindAccountApi) {
        this.mBindAccountApi = oBindAccountApi;

        switch (oLoginType) {
            case OLoginTypeUtils.GOOGLE:
                GoogleManager.getInstance().googleLogin(true);
                break;
            case OLoginTypeUtils.FACEBOOK:
                FacebookManager.getInstance().facebookLogin(true);
                break;
            case OLoginTypeUtils.LINE:
                LineManager.getInstance().lineLogin(true);
                break;
        }
    }

    /**
     * FB 分享
     *
     * @param oShareType
     * @param oShareApi
     */
    public void addOnOverseaShareFbListener(List<Bitmap> bitmaps, List<String> localVideoUrlList, String linkUrl,
                                            String content, String tag, String oShareType, OShareApi oShareApi) {
        this.mShareApi = oShareApi;
        try {
            FacebookManager.getInstance().shareFacebook(bitmaps, localVideoUrlList, linkUrl, content, tag, oShareType);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(OSDKUtils.TAG, "OSDK facebook share onError: " + e.getMessage());
            SOToastUtil.showLong(SOCommonUtil.getRes4LocaleStr(mActivity, "share_failed") + ": " + e.getMessage());
        }
    }

    /**
     * Line 分享
     *
     * @param oShareType
     * @param oShareApi
     */
    public void addOnOverseaShareLineListener(String localUrl, String content, String oShareType, OShareApi oShareApi) {
        this.mShareApi = oShareApi;
        try {
            LineManager.getInstance().shareLine(localUrl, content, oShareType);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(OSDKUtils.TAG, "OSDK line share onError: " + e.getMessage());
            SOToastUtil.showLong(SOCommonUtil.getRes4LocaleStr(mActivity, "share_failed") + ": " + e.getMessage());
        }
    }

    /**
     * google 支付
     * @param product_id
     * @param out_order_no
     * @param oPayApi
     */
    public void addOnOverseaPayListener(String product_id, String out_order_no, OPayApi oPayApi) {
        this.mPayApi = oPayApi;
        GoogleManager.getInstance().googlePay(product_id, out_order_no);
    }

    @Override
    public OLoginApi getLoginApi() {
        return mLoginApi;
    }

    @Override
    public OShareApi getShareApi() {
        return mShareApi;
    }

    @Override
    public OBindAccountApi getBindAccountApi() {
        return mBindAccountApi;
    }

    @Override
    public OPayApi getPayApi() {
        return mPayApi;
    }

    // todo 回调登录
    @Override
    public void onLogin() {

    }

    @Override
    public void onCheckThirdLogin() {
        if (!GoogleManager.getInstance().checkGoogleAccessToken(false)) {
            if (!FacebookManager.getInstance().checkFacebookAccessToken(false)) {
                LineManager.getInstance().checkLineAccessToken();
            }
        }
    }

    @Override
    public void onLogOut() {
        try {
            GoogleManager.getInstance().googleLogOut();
            FacebookManager.getInstance().faceBookLogOut();
            LineManager.getInstance().lineLogOut();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(OSDKUtils.TAG, "OSDK thirdLogOut ERROR");
        }
    }

    @Override
    public void onLoginSuccess(String value) {
        GoogleManager.getInstance().initGooglePay(false, true);
    }

    @Override
    public void onThirdLoginSuccess(HashMap<String, String> loginMaps) {
        H5OSDK.getInstance().getLoginApi().onSuccess(loginMaps);
    }

    @Override
    public void onBindThirdAccount(HashMap<String, String> loginMaps) {
        H5OSDK.getInstance().getBindAccountApi().onSuccess(loginMaps);
    }

    @Override
    public void onThirdLoginFailure(String loginType, String msg) {
        H5OSDK.getInstance().getLoginApi().onFailure(loginType, msg);
    }

    @Override
    public void onPaySuccess(HashMap<String, String> payMaps, boolean isQueryOrder) {
        H5OSDK.getInstance().getPayApi().onPaySuccess(payMaps, isQueryOrder);
    }

    @Override
    public void onPayCancel() {
        H5OSDK.getInstance().getPayApi().onPayCancel();
    }

    @Override
    public void onPayFailure(String msg) {
        H5OSDK.getInstance().getPayApi().onPayFailure(msg);
    }

    @Override
    public void onShareSuccess(String platform) {
        H5OSDK.getInstance().getShareApi().onSuccess(platform);
    }

    @Override
    public void onShareCancel(String platform) {
        H5OSDK.getInstance().getShareApi().onCancel(platform);
    }

    @Override
    public void onShareFailure(String platform, String msg) {
        H5OSDK.getInstance().getShareApi().onFailure(platform, msg);
    }

    @Override
    public void onCreate(Activity activity, Intent intent) {
        this.mActivity = activity;
        GoogleManager.getInstance().initGoogleLogin(activity);
        FacebookManager.getInstance().initFacebookLogin(activity);
        LineManager.getInstance().initLineLogin(activity);
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onRestart(Activity activity) {

    }

    @Override
    public void onResume(Activity activity) {

    }

    @Override
    public void onPause(Activity activity) {

    }

    @Override
    public void onStop(Activity activity) {

    }

    @Override
    public void onDestroy(Activity activity) {
        GoogleManager.getInstance().googlePayDestroy();
        FacebookManager.getInstance().facebookDestroy();
        LineManager.getInstance().lineDestroy();
    }

    @Override
    public void onNewIntent(Intent intent) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        GoogleManager.getInstance().onActivityResult(requestCode, resultCode, data);
        FacebookManager.getInstance().onActivityResult(requestCode, resultCode, data);
        LineManager.getInstance().onActivityResult(requestCode, resultCode, data);
    }
}
