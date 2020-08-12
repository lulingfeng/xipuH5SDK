package com.xipu.h5.osdk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.android.billingclient.api.SkuDetails;
import com.startobj.util.common.SOCommonUtil;
import com.startobj.util.network.SONetworkUtil;
import com.startobj.util.toast.SOToastUtil;
import com.xipu.h5.osdk.callback.OBindAccountCallback;
import com.xipu.h5.osdk.callback.OLoginCallBack;
import com.xipu.h5.osdk.callback.OLoginUserCallBack;
import com.xipu.h5.sdk.BSDK;
import com.xipu.h5.sdk.callback.OPayApi;
import com.xipu.h5.osdk.callback.ORandomAccountCallback;
import com.xipu.h5.osdk.callback.OOverseaAccountLoginCallback;
import com.xipu.h5.osdk.callback.OSDKApi;
import com.xipu.h5.osdk.callback.OShareApi;
import com.xipu.h5.osdk.config.OSDKConfig;
import com.xipu.h5.osdk.dialog.OBindAccountDialog;
import com.xipu.h5.osdk.dialog.OH5Dialog;
import com.xipu.h5.osdk.dialog.OLoginDialog;
import com.xipu.h5.osdk.dialog.OXiPuLoginDialog;
import com.xipu.h5.osdk.manager.FacebookManager;
import com.xipu.h5.osdk.manager.GoogleManager;
import com.xipu.h5.osdk.manager.LineManager;
import com.xipu.h5.osdk.utils.OLoginTypeUtils;
import com.xipu.h5.osdk.utils.ONetWorkUtils;
import com.xipu.h5.osdk.utils.OSDKUtils;
import com.xipu.h5.osdk.utils.SystemUiUtils;
import com.xipu.h5.sdk.callback.OQuerySkuApi;
import com.xipu.h5.sdk.callback.OLoginApi;
import com.xipu.h5.sdk.model.ConfigModule;
import com.xipu.h5.sdk.model.OSkuDetails;
import com.xipu.h5.sdk.model.PayReportModel;
import com.xipu.h5.sdk.model.UserModel;
import com.xipu.h5.sdk.util.AppConfigUtils;
import com.xipu.h5.osdk.utils.OAppsFlyerUtils;
import com.xipu.h5.sdk.util.H5Utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OSDK extends BSDK implements OSDKApi {

    public static HashMap<String, String> mLoginMap;

    private static OLoginApi mLoginApi;

    private static OShareApi mShareApi;

    private static OPayApi mPayApi;

    private static OQuerySkuApi oSkuApi;

    private static OXiPuLoginDialog mOXiPuLoginDialog;

    private static OLoginDialog mOLoginDialog;

    /*
     * 匿名帐号绑定框
     */
    private static OBindAccountDialog mOBindAccountDialog;

    /*
     * 小概率存在多个游客账户选择框
     */
    private static AlertDialog.Builder builder;

    private static AlertDialog overseaHintDialog;

    private static OH5Dialog mH5Dialog;

    private static OSDK mInstance;

    private static Activity mActivity;

    private static String mProduct_id;

    /**
     * 单例
     */
    public static OSDK getInstance() {
        if (mInstance == null) {
            synchronized (OSDK.class) {
                if (mInstance == null) {
                    mInstance = new OSDK();
                }
            }
        }
        return mInstance;
    }

    /**
     * 登录
     *
     * @param activity
     * @param oLoginApi
     */
    public void addOnLoginListener(Activity activity, OLoginApi oLoginApi) {
        this.mActivity = activity;
        this.mLoginApi = oLoginApi;

        //添加代码以打印出密钥哈希值，请尝试
        try {
            PackageInfo info = activity.getPackageManager().getPackageInfo(
                    activity.getPackageName(),
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
        onLogin(activity);
    }

    /**
     * FB 分享
     *
     * @param oShareType
     * @param oShareApi
     */
    public void addOnShareFbListener(List<Bitmap> bitmaps, List<String> localVideoUrlList, String linkUrl,
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
    public void addOnShareLineListener(String localUrl, String content, String oShareType, OShareApi oShareApi) {
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
     *
     * @param activity
     * @param values
     * @param oPayApi
     */
    public void addOnPayListener(Activity activity, String values, OPayApi oPayApi) {
        this.mPayApi = oPayApi;
        JSONObject jsonObject = JSON.parseObject(values);
        if (jsonObject.containsKey("product_id")) {
            //   mProduct_id = jsonObject.getString("product_id");
            mProduct_id = "xpx99";
        } else {
            onPayFailure(SOCommonUtil.getRes4LocaleStr(mActivity, "o_Payment_failed_no_payment_product_ID_passed_in"));
            return;
        }
        HashMap<String, String> paramsMap = new HashMap<>();

        if (mLoginMap != null) {
            paramsMap.putAll(mLoginMap);
        }
        paramsMap.putAll(JSON.parseObject(jsonObject.getString("role"), new TypeReference<HashMap<String, String>>() {
        }));
        paramsMap.put("product_id", mProduct_id);
        paramsMap.put("callback", jsonObject.getString("extras"));
        onPay(activity, paramsMap);
    }

    /**
     * 发起批量查询
     *
     * @param productIds
     * @param skuApi
     */
    public void addQuerySkuListDetailsListener(List<String> productIds, OQuerySkuApi skuApi) {
        this.oSkuApi = skuApi;
        GoogleManager.getInstance().querySkuListDetails(productIds);
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
    public OPayApi getPayApi() {
        return mPayApi;
    }

    public OQuerySkuApi getSkuApi() {
        return oSkuApi;
    }

    private Handler checkConfigHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (!H5Utils.isIsAppConfigInit()) {
                AppConfigUtils.getInstance().loadConfig(mActivity, checkConfigHandle);
            } else {
                onLogin(mActivity);
            }
        }
    };

    @Override
    public void onModuleInit(ConfigModule configModule) {
        super.onModuleInit(configModule);
    }

    @Override
    public void onActivate(Activity activity) {
        OAppsFlyerUtils.getInstance().reportAFActivate(activity);
    }

    @Override
    public void onLogin(Activity activity) {
        Log.d(OSDKUtils.TAG, "onLogin()");
        if (H5Utils.isIsAppConfigInit()) {
            if (H5Utils.isIsUseThirdSDK()) {
                onCheckThirdLogin();
            } else {
                showXiPuLoginDialog();
            }
        } else {
            checkConfigHandle.sendEmptyMessage(0);
        }
    }

    @Override
    public void onCheckThirdLogin() {
        if (!GoogleManager.getInstance().checkGoogleAccessToken(false)) {
            if (!FacebookManager.getInstance().checkFacebookAccessToken(false)) {
                LineManager.getInstance().checkLineAccessToken();
            }
        }
    }

    private void showXiPuLoginDialog() {
        Log.d(OSDKUtils.TAG, "showXiPuLoginDialog");
        if (mOXiPuLoginDialog == null) {
            mOXiPuLoginDialog = new OXiPuLoginDialog(mActivity, new OLoginUserCallBack() {
                @Override
                public void onLogin(String account, String password) {
                    overseaLogin(account, password, null);
                }
            });
        }
        if (mOXiPuLoginDialog != null) {
            mOXiPuLoginDialog.show();
        }
    }

    /*
     * 喜扑登录
     */
    private void overseaLogin(final String username, final String password, View view) {
        if (!SONetworkUtil.isNetworkAvailable(mActivity)) {
            SOToastUtil.showShort(SOCommonUtil.getRes4LocaleStr(mActivity, "o_tip_network"));
            return;
        }
        ONetWorkUtils.getInstance().doOverseaLogin(mActivity, username, password, view, new OOverseaAccountLoginCallback() {
            @Override
            public void onSuccess() {
                guestLoginSuccess();
            }
        });
    }

    /*
     * 游客登陆成功
     */
    private void guestLoginSuccess() {
        OSDK.getInstance().mLoginMap = new HashMap<>();
        UserModel userModel = H5Utils.getUserModel();
        OSDK.getInstance().mLoginMap.put("third_openid", userModel.getOpenid());
        OSDK.getInstance().mLoginMap.put("third_type", OLoginTypeUtils.GUEST);
        OSDK.getInstance().mLoginMap.put("third_ext", "\"accesstoken\":\"" + userModel.getAccesstoken() + "\"}");
        showGuestBindThirdAccountDialog(mActivity);
    }

    @Override
    public void showOLoginDialog() {
        if (mOLoginDialog == null) {
            mOLoginDialog = new OLoginDialog(mActivity, new OLoginCallBack() {
                @Override
                public void onAbroadLogin(String loginType, final View view) {
                    switch (loginType) {
                        case OLoginTypeUtils.LINE:
                            H5Utils.clickSleep(view, 2000);
                            LineManager.getInstance().lineLogin(false);
                            break;
                        case OLoginTypeUtils.GOOGLE:
                            H5Utils.clickSleep(view, 2000);
                            GoogleManager.getInstance().googleLogin(false);
                            break;
                        case OLoginTypeUtils.FACEBOOK:
                            H5Utils.clickSleep(view, 2000);
                            FacebookManager.getInstance().facebookLogin(false);
                            break;
                        case OLoginTypeUtils.GUEST:
                            // 兼容老版本  判断游客绑定标记

                            List<UserModel> overseaAccList = OSDKUtils.getInstance().getOverseaAccList();
                            // 本地匿名帐号
                            if (overseaAccList.size() > 0) {
                                if (overseaAccList.size() == 1) {
                                    UserModel userModel = overseaAccList.get(0);
                                    if (userModel.isBindThirdAccount()) {
                                        SOToastUtil.showShort(SOCommonUtil.getRes4LocaleStr(mActivity, "o_please_use_third_account_for_login") + " ( " + userModel.getBind_account_type() + " )");
                                        return;
                                    }
                                    Log.d(OSDKUtils.TAG, "OSDK 本地匿名账号登入");
                                    overseaLogin(userModel.getUsername(), userModel.getPassword(), view);
                                } else {
                                    showOverseaAccHintDialog(view);
                                }
                            } else {
                                // 生成匿名帐号
                                Log.d(OSDKUtils.TAG, "OSDK 生成匿名帐号登入");
                                ONetWorkUtils.getInstance().createRandomAccount(mActivity, new ORandomAccountCallback() {
                                    @Override
                                    public void onSuccess(String userName, String password) {
                                        ONetWorkUtils.getInstance().doRegisterOverseaAccount(mActivity, userName, password, view, new OOverseaAccountLoginCallback() {
                                            @Override
                                            public void onSuccess() {
                                                guestLoginSuccess();
                                            }
                                        });
                                    }

                                });
                            }
                            break;
                    }
                }
            });
        }
        if (mOLoginDialog != null) {
            mOLoginDialog.show();
        }
    }

    private void dismissOverseaLoginDialog() {
        if (mOLoginDialog != null && mOLoginDialog.isShowing()) {
            mOLoginDialog.dismiss();
        }
    }

    /*
     * 小概率存在多游客账户情况
     * @param view
     */
    private void showOverseaAccHintDialog(final View view) {
        List<UserModel> overseaAccList = OSDKUtils.getInstance().getOverseaAccList();
        String[] overseaList = new String[overseaAccList.size()];
        for (int i = 0; i < overseaAccList.size(); i++) {
            overseaList[i] = overseaAccList.get(i).getUsername();
        }
        if (builder == null) {
            builder = new AlertDialog.Builder(mActivity);
            builder.setSingleChoiceItems(overseaList, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (dialogInterface != null) {
                        dialogInterface.dismiss();
                    }
                    UserModel userModel = H5Utils.getUserModelList().get(i);
                    if (userModel.isBindThirdAccount()) {
                        SOToastUtil.showShort(SOCommonUtil.getRes4LocaleStr(mActivity, "o_please_use_third_account_for_login") + " ( " + userModel.getBind_account_type() + " )");
                        return;
                    }
                    overseaLogin(userModel.getUsername(), userModel.getPassword(), view);
                }
            });
        }
        overseaHintDialog = builder.show();
    }

    private void dismissOverseaAccHintDialog() {
        if (overseaHintDialog != null) {
            overseaHintDialog.dismiss();
        }
    }


    /*
     * 游客登入绑定三方账户框
     */
    public void showGuestBindThirdAccountDialog(Activity activity) {
        Log.d(OSDKUtils.TAG, "OSDK showGuestBindThirdAccountDialog()");
        dismissOverseaLoginDialog();
        if (mOBindAccountDialog == null) {
            mOBindAccountDialog = new OBindAccountDialog(activity, new OLoginCallBack() {
                @Override
                public void onAbroadLogin(String loginType, View view) {
                    switch (loginType) {
                        case OLoginTypeUtils.LINE:
                            H5Utils.clickSleep(view, 2000);
                            LineManager.getInstance().lineLogin(true);
                            break;
                        case OLoginTypeUtils.GOOGLE:
                            H5Utils.clickSleep(view, 2000);
                            GoogleManager.getInstance().googleLogin(true);
                            break;
                        case OLoginTypeUtils.FACEBOOK:
                            H5Utils.clickSleep(view, 2000);
                            FacebookManager.getInstance().facebookLogin(true);
                            break;
                        default:
                            loginHandler.sendEmptyMessage(LOGIN_SUCCESS);
                            break;
                    }
                }
            });
        } else {
            mOBindAccountDialog.refreshView();
        }

        if (mOBindAccountDialog != null) {
            mOBindAccountDialog.show();
        }
    }

    /*
     * dismiss 游客绑定账户提示框
     */
    private void dismissGuestBindThirdAccountDialog() {
        if (mOBindAccountDialog != null) {
            mOBindAccountDialog.dismiss();
        }
    }

    /*
     * 显示绑定三方账户成功提示框
     */
    private void showBindThirdAccountSuccessHintDialog(Activity activity) {
        if (mH5Dialog == null) {
            mH5Dialog = new OH5Dialog.Builder(activity)
                    .setTitle(SOCommonUtil.getRes4LocaleStr(activity, "o_prompt"))
                    .setMessage(SOCommonUtil.getRes4LocaleStr(activity, "o_third_bind_hint"))
                    .setConfirm(SOCommonUtil.getRes4LocaleStr(activity, "o_understood"))
                    .setCanceable(false)
                    .setPositiveButton(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dismissBindThirdAccountHintDialog();
                            Log.d(OSDKUtils.TAG, "loginHandler: " + loginHandler);
                            loginHandler.sendEmptyMessage(LOGIN_SUCCESS);
                        }
                    }).create();
        }
        if (mH5Dialog != null) {
            mH5Dialog.show();
        }
    }

    /*
     * dismiss 绑定账户成功提示框
     */
    private void dismissBindThirdAccountHintDialog() {
        if (mH5Dialog != null) {
            mH5Dialog.dismiss();
        }
    }

    @Override
    public void onSwitchAccount() {
        Log.d(OSDKUtils.TAG, "onSwitchAccount()");
        onLogOut();
        super.onSwitchAccount();
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

    // 成功回调
    @Override
    public void onLoginSuccess(HashMap<String, Object> resMaps) {
        Log.d(OSDKUtils.TAG, "loginSuccess()");
        handlerPaySuccessOrder();
        GoogleManager.getInstance().queryPurchase();
        dismissOverseaLoginDialog();
        dismissBindThirdAccountHintDialog();
        dismissGuestBindThirdAccountDialog();
        dismissOverseaAccHintDialog();
        SystemUiUtils.getInstance().hideSystemUi(mActivity);

        if (resMaps != null) {
            if (!TextUtils.isEmpty((String) resMaps.get("is_newuser")) && "1".equals(resMaps.get("is_newuser"))) {
                OAppsFlyerUtils.getInstance().reportAFRegistration(mActivity, mLoginMap.get("third_type"));
            }
        }
        OAppsFlyerUtils.getInstance().reportAFLogin(mActivity);

        if (getLoginApi() != null) {
            getLoginApi().onSuccess(resMaps);
        }
    }

    @Override
    public void onLoginFailure() {
        onLogOut();
        onLogin(mActivity);
    }

    @Override
    public void onCreateRole(Activity activity, String values) {
        try {
            JSONObject jsonObject = JSON.parseObject(values);
            OAppsFlyerUtils.getInstance().reportAFCreateRole(activity, jsonObject.getString("role_id"), jsonObject.getString("server_id"));
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(OSDKUtils.TAG, "onCreateRole() Exception: " + e.getMessage());
        }
    }

    @Override
    public void onLoginRole(Activity activity, String values) {
        try {
            JSONObject jsonObject = JSON.parseObject(values);
            OAppsFlyerUtils.getInstance().reportAFLoginRole(activity, jsonObject.getString("role_id"), jsonObject.getString("server_id"));
        }catch (Exception e){
            e.printStackTrace();
            Log.e(OSDKUtils.TAG, "onLoginRole() Exception: " + e.getMessage());
        }
    }

    @Override
    public void onUpdateRole(Activity activity, String values) {
        // ... 暂不处理
    }

    // 保存账号信息
    @Override
    public void thirdSaveUserInfo(Activity activity) {
        Log.d(OSDKUtils.TAG, "thirdSaveUserInfo()");
        OSDKUtils.getInstance().saveOverseaUserEntityList(activity);
    }

    @Override
    public void onThirdLoginSuccess(Activity activity, HashMap<String, String> loginMaps, int sdkType) {
        super.onThirdLoginSuccess(activity, loginMaps, sdkType);
    }

    @Override
    public void onThirdLoginFailure(String loginType, String msg) {
        SOToastUtil.showShort(msg);
        onLoginFailure();
    }

    @Override
    public void onBindThirdAccount(HashMap<String, String> loginMaps) {
        ONetWorkUtils.getInstance().doGuestBindThirdAccount(mActivity, loginMaps, new OBindAccountCallback() {
            @Override
            public void onSuccess() {
                dismissGuestBindThirdAccountDialog();
                showBindThirdAccountSuccessHintDialog(mActivity);
            }

            @Override
            public void onFailure() {
                onLogOut();
            }
        });
    }

    /*
     * 批量查询成功回调
     * @param skuDetailsList
     */
    public void querySkuDetailsResponse(List<SkuDetails> skuDetailsList) {
        Log.d(OSDKUtils.TAG, "osdk querySkuDetailsResponse()" + getSkuApi());
        if (getSkuApi() != null) {
            List<OSkuDetails> oSkuDetailList = new ArrayList<>();
            for (SkuDetails details : skuDetailsList) {
                OSkuDetails skuDetail = new OSkuDetails();
                skuDetail.setProductId(details.getSku());
                skuDetail.setPrice(details.getPrice());
                skuDetail.setPrice_amount_micros("" + details.getPriceAmountMicros());
                skuDetail.setPrice_currency_code(details.getPriceCurrencyCode());
                skuDetail.setTitle(details.getTitle());
                skuDetail.setDescription(details.getDescription());
                oSkuDetailList.add(skuDetail);
            }
            getSkuApi().onSkuDetailsResponse(oSkuDetailList);
        }
    }

    /*
     * 批量查询失败
     * @param msg
     */
    public void querySkuDetailsFailed(String msg) {
        Log.d(OSDKUtils.TAG, "osdk querySkuDetailsFailed()" + getSkuApi());
        if (getSkuApi() != null) {
            getSkuApi().onSkuDetailsFailed(msg);
        }
    }

    @Override
    public void handlerPaySuccessOrder() {
        super.handlerPaySuccessOrder();
    }

    @Override
    public void onPay(final Activity activity, HashMap<String, String> paramsMap) {
        super.onPay(activity, paramsMap);
    }

    @Override
    public void onThirdSDKPay(Activity activity, HashMap<String, String> map) {
        Log.e(OSDKUtils.TAG, "onThirdSDKPay" + map.toString());
        if (map.containsKey("out_trade_no")) {
            String out_trade_no = map.get("out_trade_no");
            if (!TextUtils.isEmpty(out_trade_no)) {
                if (!TextUtils.isEmpty(mProduct_id)) {
                    GoogleManager.getInstance().googlePay(mProduct_id, out_trade_no);
                } else {
                    onPayFailure(SOCommonUtil.getRes4LocaleStr(mActivity, "o_Payment_failed_payment_product_id_is_empty"));
                }
            } else {
                onPayFailure(SOCommonUtil.getRes4LocaleStr(mActivity, "o_Payment_failed_order_number_cannot_be_empty"));
            }
        } else {
            onPayFailure(SOCommonUtil.getRes4LocaleStr(mActivity, "o_Payment_failed_no_order_number"));
        }
    }

    // 支付成功 校验订单
    @Override
    public void onSendPaySuccess(HashMap<String, String> payMaps, String send_pay_out_order_no, boolean isQueryOrder) {
        super.onSendPaySuccess(payMaps, send_pay_out_order_no, isQueryOrder);
    }

    // 支付成功回调
    @Override
    public void onPaySuccess(PayReportModel payReportModel) {
        Log.d(OSDKUtils.TAG, "OSDK paySuccess()");
        if (payReportModel != null) {
            if (TextUtils.equals("1", payReportModel.getIs_appfly_report())) {
                OAppsFlyerUtils.getInstance().reportAFPurchase(mActivity, payReportModel.getOutTradeNO(), payReportModel.getAppfly_report_amount(), payReportModel.getProduct_id());
            }
        }

        if (payReportModel.isCallBack()) {
            if (getPayApi() != null) {
                getPayApi().onPaySuccess();
            }
        }
    }

    @Override
    public void onPayCancel() {
        if (getPayApi() != null) {
            getPayApi().onPayCancel();
        }
    }

    @Override
    public void onPayFailure(String msg) {
        if (getPayApi() != null) {
            getPayApi().onPayFailure(msg);
        }
    }

    @Override
    public void onShareSuccess(String platform) {
        OSDK.getInstance().getShareApi().onSuccess(platform);
    }

    @Override
    public void onShareCancel(String platform) {
        OSDK.getInstance().getShareApi().onCancel(platform);
    }

    @Override
    public void onShareFailure(String platform, String msg) {
        OSDK.getInstance().getShareApi().onFailure(platform, msg);
    }

    @Override
    public void onCreate(Activity activity, Intent intent) {
        Log.d(OSDKUtils.TAG, "onCreate()");
        this.mActivity = activity;

        ConfigModule configModule = new ConfigModule();
        configModule.setNotify_url(OSDKConfig.SEND_PAY_SUCCESS);
        onModuleInit(configModule);

        OSDKUtils.getInstance().loadUserList(activity, null);

        GoogleManager.getInstance().initGoogleLogin(activity);
        FacebookManager.getInstance().initFacebookLogin(activity);
        LineManager.getInstance().initLineLogin(activity);

        super.onCreate(activity, intent);
    }

    @Override
    public void onStart() {
        Log.d(OSDKUtils.TAG, "onStart()");
        super.onStart();
    }

    @Override
    public void onRestart(Activity activity) {
        Log.d(OSDKUtils.TAG, "onRestart()");
        super.onRestart(activity);
    }

    @Override
    public void onResume(Activity activity) {
        Log.d(OSDKUtils.TAG, "onResume()");
        super.onResume(activity);
    }

    @Override
    public void onPause(Activity activity) {
        Log.d(OSDKUtils.TAG, "onPause()");
        super.onPause(activity);
    }

    @Override
    public void onStop(Activity activity) {
        Log.d(OSDKUtils.TAG, "onStop()");
        super.onStop(activity);
    }

    @Override
    public void onDestroy(Activity activity) {
        Log.d(OSDKUtils.TAG, "onDestroy()");
        GoogleManager.getInstance().googlePayDestroy();
        FacebookManager.getInstance().facebookDestroy();
        LineManager.getInstance().lineDestroy();
        super.onDestroy(activity);
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.d(OSDKUtils.TAG, "onNewIntent()");
        super.onNewIntent(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(OSDKUtils.TAG, "onActivityResult()");
        GoogleManager.getInstance().onActivityResult(requestCode, resultCode, data);
        FacebookManager.getInstance().onActivityResult(requestCode, resultCode, data);
        LineManager.getInstance().onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

}
