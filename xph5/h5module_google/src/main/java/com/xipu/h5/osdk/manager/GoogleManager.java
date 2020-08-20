package com.xipu.h5.osdk.manager;


import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.startobj.util.common.SOCommonUtil;
import com.startobj.util.toast.SOToastUtil;
import com.xipu.h5.osdk.OSDK;
import com.xipu.h5.osdk.utils.OLoginCodeUtils;
import com.xipu.h5.osdk.utils.OLoginTypeUtils;
import com.xipu.h5.osdk.utils.OSDKUtils;
import com.xipu.h5.osdk.utils.ThreadManager;
import com.xipu.h5.sdk.config.H5Config;
import com.xipu.h5.sdk.util.H5Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * @author bruce
 * Google
 */
public class GoogleManager implements PurchasesUpdatedListener {
    private static GoogleManager mInstance;
    /**
     * 上下文
     */
    private Activity mActivity;
    /**
     * 是否绑定
     */
    private boolean mIsBindAccount;
    /**
     * 是否批量查询商品
     */
    private boolean mIsQueryProductIDList;
    /**
     * 是否查询商品
     */
    private boolean mIsQueryProductID;
    /**
     * 是否查询缓存商品
     */
    private boolean mIsQueryCachePurchase;
    /**
     * 商品列表
     */
    private List<SkuDetails> mSkuDetailsList = new ArrayList<>();
    /**
     * 查询商品ID
     */
    private List<String> mProductIdList = new ArrayList<>();
    /**
     * 当前支付订单号
     */
    private String mOut_orderNo;
    /**
     * 当前支付Id
     */
    private String mProduct_id;
    /**
     * 当前支付 商品详情
     */
    private SkuDetails mSkuDetails;
    /**
     * google login
     */
    private GoogleSignInClient mGoogleSignInClient;
    /**
     * Google 支付
     */
    private BillingClient mBillingClient;
    /**
     * 标识 初始化后是否支付
     */
    private boolean mIsPayHandler;
    /**
     * 连接Google服务容错次数
     */
    private int mConnectCount;
    /**
     * 消耗品确认回调
     */
    private ConsumeResponseListener mConsumeResponseListener = new ConsumeResponseListener() {

        @Override
        public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {

            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                HashMap<String, String> mSendPaySuccessOrderNoList = OSDKUtils.getInstance().getGoogleOrderInfoList(mActivity);
                for (String order : mSendPaySuccessOrderNoList.keySet()) {
                    try {
                        HashMap<String, String> orderMap = JSON.parseObject(mSendPaySuccessOrderNoList.get(order), new TypeReference<HashMap<String, String>>() {
                        });
                        if (orderMap.get("purchaseToken").equals(purchaseToken)) {
                            Log.e(OSDKUtils.TAG, "google Consume confirm success");
                            OSDK.getInstance().onSendPaySuccess(orderMap, order, true);
                            OSDKUtils.getInstance().removeGoogleOrderInfo(mActivity, order);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(OSDKUtils.TAG, "onConsumeResponse() 序列化订单异常" + e.getMessage());
                    }
                }
            } else {
                Log.e(OSDKUtils.TAG, "google Consume confirm failed");
            }
        }
    };

    /**
     * 初始化完成进行支付
     */
    private Handler mGooglePayHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            googlePay();
        }
    };

    /**
     * google 支付初始化 Listener
     */
    private BillingClientStateListener mBillingClientStateListener = new BillingClientStateListener() {
        @Override
        public void onBillingSetupFinished(BillingResult billingResult) {
            Log.d(OSDKUtils.TAG, "onBillingSetupFinished: " + billingResult.getResponseCode());
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                // The BillingClient is ready. You can query purchases here.
                Log.d(OSDKUtils.TAG, "The BillingClient is ready");
                if (mIsQueryCachePurchase) {
                    queryPurchase();
                }
                if (mIsQueryProductID) {
                    querySkuDetails();
                }
                if (mIsQueryProductIDList) {
                    querySkuDetails();
                }
            }
        }

        @Override
        public void onBillingServiceDisconnected() {
            // Try to restart the connection on the next request to
            // Google Play by calling the startConnection() method.
            Log.d(OSDKUtils.TAG, "onBillingServiceDisconnected");
            if (mConnectCount >= 3) {
                SOToastUtil.showShort("Google Pay onBillingServiceDisconnected");
                return;
            }
            ++mConnectCount;
            mBillingClient.startConnection(this);
        }
    };

    public static GoogleManager getInstance() {
        if (mInstance == null) {
            synchronized (GoogleManager.class) {
                if (mInstance == null) {
                    mInstance = new GoogleManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 初始化google 登录
     */
    public void initGoogleLogin(Activity activity) {
        this.mActivity = activity;
        // google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(OSDKUtils.getInstance().getGoogleWebClientId())
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);
    }

    /**
     * 初始化 google 支付
     */
    public void initGooglePay() {
        Log.d(OSDKUtils.TAG, "initGooglePay()");
        try {
            if (mBillingClient == null) {
                mBillingClient = BillingClient.newBuilder(mActivity)
                        .enablePendingPurchases()
                        .setListener(this).build();
            }

            if (mBillingClient.isReady()) {
                if (mIsQueryCachePurchase) {
                    queryPurchase();
                }
                if (mIsQueryProductID) {
                    querySkuDetails();
                }
                if (mIsQueryProductIDList) {
                    querySkuDetails();
                }
            } else {
                mConnectCount = 0;
                mBillingClient.startConnection(mBillingClientStateListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
            SOToastUtil.showShort("Google Pay init exception: " + e.getMessage());
            Log.d(OSDKUtils.TAG, "Google Pay init exception" + e.getMessage());
        }
    }

    /**
     * 当前环境是否支持购买
     * 影响原因： 未使用国外网络/当前地区不支持购买/google 服务未更新最新 等
     *
     * @param feature
     * @return
     */
    public boolean isFeatureSupported(@BillingClient.FeatureType String feature) {

        BillingResult result = mBillingClient.isFeatureSupported(feature);

        if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            Log.d(OSDKUtils.TAG, "isFeatureSupported: isFeatureSupported = true");
            return true;
        } else {
            Log.e(OSDKUtils.TAG, "isFeatureSupported: isFeatureSupported = false , errorMsg : " + result.getDebugMessage());
            return false;
        }
    }


    // login 相关

    /**
     * 检测 google 登录状态
     *
     * @return
     */
    public boolean checkGoogleAccessToken(boolean isBind) {
        this.mIsBindAccount = isBind;
        try {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(mActivity);
            Log.d(OSDKUtils.TAG, "google already login " + (account != null ? true : false));
            if (account != null) {
                updateUI(account, "");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(OSDKUtils.TAG, "checkGoogleAccessToken() Exception: " + e.getMessage());
        }
        return false;
    }

    /**
     * google login
     */
    public void googleLogin(boolean isBind) {
        this.mIsBindAccount = isBind;
        try {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            mActivity.startActivityForResult(signInIntent, OLoginCodeUtils.GOOGLE_SIGN_IN);
        } catch (Exception e) {
            Log.e("google login ERROR", e.toString());
            if (mIsBindAccount) {
                SOToastUtil.showShort(SOCommonUtil.getRes4LocaleStr(mActivity, "o_google_bind_onError"));
            } else {
                SOToastUtil.showShort(SOCommonUtil.getRes4LocaleStr(mActivity, "o_google_login_exception") + ": " + e.getMessage());
            }
        }
    }

    /**
     * google 退出
     */
    public void googleLogOut() throws Exception {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(mActivity);
        if (account != null) {
            Log.d(OSDKUtils.TAG, "googleSignOut");
            mGoogleSignInClient.signOut()
                    .addOnCompleteListener((Executor) this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            revokeAccess();
                        }
                    });
        }
    }

    /**
     * google 断开账户
     */
    private void revokeAccess() {
        mGoogleSignInClient.revokeAccess()
                .addOnCompleteListener((Executor) this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                    }
                });
    }

    /**
     * google登录 检查返回结果
     *
     * @param completedTask
     */
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        Log.d(OSDKUtils.TAG, "handleSignInResult()");
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            updateUI(account, "");
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.e(OSDKUtils.TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null, e.getStatusCode() + "," + e.getMessage());
        }
    }

    /**
     * google 登录
     *
     * @param account == null?失败:成功
     */
    private void updateUI(GoogleSignInAccount account, String errorMsg) {
        Log.d(OSDKUtils.TAG, "updateUI()");
        if (account != null) {
            H5Utils.showProgress(mActivity, false, 0);
            OSDK.getInstance().mLoginMap = new HashMap<>();
            OSDK.getInstance().mLoginMap.put("third_openid", account.getId());
            OSDK.getInstance().mLoginMap.put("third_type", OLoginTypeUtils.GOOGLE);
            OSDK.getInstance().mLoginMap.put("third_ext", "{" +
                    "\"account\":\"" + account.toJson() +
                    "\"}");
            if (mIsBindAccount) {
                OSDK.getInstance().onBindThirdAccount(OSDK.getInstance().mLoginMap);
            } else {
                OSDK.getInstance().onThirdLoginSuccess(mActivity, OSDK.getInstance().mLoginMap, H5Config.TYPE_OSDK);
            }
        } else {
            if (mIsBindAccount) {
                SOToastUtil.showShort(SOCommonUtil.getRes4LocaleStr(mActivity, "o_google_bind_onError"));
            } else {
                OSDK.getInstance().onThirdLoginFailure(OLoginTypeUtils.GOOGLE, SOCommonUtil.getRes4LocaleStr(mActivity, "o_google_login_error") + ": " + errorMsg);
            }
        }
    }

    // pay 相关

    /**
     * 支付状态改变回调
     *
     * @param billingResult
     * @param purchases
     */
    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        Log.d(OSDKUtils.TAG, "onPurchasesUpdated(): " + billingResult.getResponseCode());
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) { // 支付取消
            // Handle an error caused by a user cancelling the purchase flow.
            OSDK.getInstance().onPayCancel();
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) { // 存在未确认商品
            queryPurchase();
        } else {
            // Handle any other error codes.
            OSDK.getInstance().onPayFailure(SOCommonUtil.getRes4LocaleStr(mActivity, "o_google_pay_error") + ";code: " + billingResult.getResponseCode());
        }
    }

    public void querySkuListDetails(List<String> productList) {
        Log.d(OSDKUtils.TAG, "querySkuListDetails()");
        this.mIsQueryProductIDList = true;
        this.mIsQueryProductID = false;
        this.mIsQueryCachePurchase = false;
        this.mIsPayHandler = false;
        this.mProductIdList.clear();
        this.mProductIdList.addAll(productList);
        if (!checkGooglePayInit()) {
            return;
        }
        querySkuDetails();
    }

    /**
     * 查询商品
     */
    private void querySkuDetails() {
        Log.d(OSDKUtils.TAG, "querySkuDetails()");
        try {
            if (!checkGooglePayInit()) {
                return;
            }
            SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
            params.setSkusList(mProductIdList).setType(BillingClient.SkuType.INAPP);
            mBillingClient.querySkuDetailsAsync(params.build(), new SkuDetailsResponseListener() {
                @Override
                public void onSkuDetailsResponse(BillingResult billingResult,
                                                 List<SkuDetails> skuDetailsList) {
                    // Process the result.
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                        mSkuDetailsList = skuDetailsList;
                        if (mIsPayHandler) {
                            mIsPayHandler = false;
                            mGooglePayHandler.sendEmptyMessage(0);
                        }
                        Log.d(OSDKUtils.TAG, "IsQueryProductIDList: " + mIsQueryProductIDList);
                        if (mIsQueryProductIDList) {
                            mIsQueryProductIDList = false;
                            OSDK.getInstance().querySkuDetailsResponse(skuDetailsList);
                        }
                        if (mIsQueryProductID) {
                            Log.d(OSDKUtils.TAG, "Find product details: " + skuDetailsList.toString());
                        }
                    } else {
                        Log.d(OSDKUtils.TAG, "Google pay query failed: " + billingResult.getDebugMessage());
                        if (mIsQueryProductIDList) {
                            mIsQueryProductIDList = false;
                            OSDK.getInstance().querySkuDetailsFailed(SOCommonUtil.getRes4LocaleStr(mActivity, "o_google_query_product_exception") + "(" + billingResult.getResponseCode() + ")");
                        } else {
                            OSDK.getInstance().onPayFailure(SOCommonUtil.getRes4LocaleStr(mActivity, "o_google_query_product_exception") + "(" + billingResult.getResponseCode() + ")");
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            if (mIsQueryProductIDList) {
                mIsQueryProductIDList = false;
                OSDK.getInstance().querySkuDetailsFailed(SOCommonUtil.getRes4LocaleStr(mActivity, "o_google_query_product_exception") + "(" + e.getMessage() + ")");
            } else {
                OSDK.getInstance().onPayFailure(SOCommonUtil.getRes4LocaleStr(mActivity, "o_google_query_product_exception"));
            }
        }
    }

    /**
     * 查询已购买商品是否存在未确认的消耗品  避免无法购买
     */
    public void queryPurchase() {
        try {
            this.mIsQueryCachePurchase = true;
            this.mIsQueryProductID = false;
            this.mIsQueryProductIDList = false;

            if (!checkGooglePayInit()) {
                return;
            }

            Purchase.PurchasesResult purchasesResult = mBillingClient.queryPurchases(BillingClient.SkuType.INAPP);
            Log.d(OSDKUtils.TAG, "Query purchased items size: " + purchasesResult.getPurchasesList().size());
            if (purchasesResult.getPurchasesList().size() <= 0) {
                return;
            }
            HashMap<String, String> mGoogleOrderNoList = OSDKUtils.getInstance().getGoogleOrderInfoList(mActivity);
            for (Purchase purchase : purchasesResult.getPurchasesList()) {
                Log.d(OSDKUtils.TAG, "Query purchased items " + purchase.toString());
                boolean isMatchOrder = false;
                for (String order : mGoogleOrderNoList.keySet()) {
                    try {
                        HashMap<String, String> orderMap = JSON.parseObject(mGoogleOrderNoList.get(order), new TypeReference<HashMap<String, String>>() {
                        });
                        if (orderMap.get("purchaseToken").equals(purchase.getPurchaseToken()) &&
                                orderMap.get("orderId").equals(purchase.getOrderId()) && !purchase.isAcknowledged()) {
                            isMatchOrder = true;
                            OSDK.getInstance().onSendPaySuccess(setSendPayParam(purchase), order, false);
                            consumePay(purchase.getPurchaseToken(), order);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(OSDKUtils.TAG, "queryPurchase() 序列化订单异常: " + e.getMessage());
                    }
                }
                if (!isMatchOrder) {
                    OSDK.getInstance().onSendPaySuccess(setSendPayParam(purchase), purchase.getOrderId(), false);
                    OSDKUtils.getInstance().addGoogleOrderInfo(mActivity, purchase.getOrderId(), setSendPayParam(purchase));
                    consumePay(purchase.getPurchaseToken(), null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(OSDKUtils.TAG, "Query purchased items exception");
        }
    }

    /**
     * google 支付 初始化参数
     */
    public void googlePay(String product_id, String out_orderNo) {
        Log.d(OSDKUtils.TAG, "googlePay()");
        mProduct_id = product_id;
        mProductIdList.clear();
        mProductIdList.add(mProduct_id);
        mOut_orderNo = out_orderNo;

        this.mIsQueryCachePurchase = false;
        this.mIsQueryProductID = true;
        this.mIsQueryProductIDList = false;
        this.mIsPayHandler = true;
        querySkuDetails();
    }

    /**
     * 检测google服务连接
     *
     * @return
     */
    private boolean checkGooglePayInit() {
        if (mBillingClient == null) {
            initGooglePay();
            return false;
        }

        if (!mBillingClient.isReady()) {
            mConnectCount = 0;
            mBillingClient.startConnection(mBillingClientStateListener);
            return false;
        } else {
            return true;
        }
    }

    /**
     * 发起支付
     */
    private void googlePay() {

        if (!checkGooglePayInit()) {
            return;
        }

        if (!isFeatureSupported(BillingClient.FeatureType.IN_APP_ITEMS_ON_VR)) {
            OSDK.getInstance().onPayFailure(SOCommonUtil.getRes4LocaleStr(mActivity, "o_payment_is_not_supported_in_the_current_environment"));
            return;
        }
        ThreadManager.getInstance().getCachePoolThread().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!mBillingClient.isReady()) {
                        mConnectCount = 0;
                        mBillingClient.startConnection(mBillingClientStateListener);
                    }

                    for (SkuDetails skuDetails : mSkuDetailsList) {
                        String sku = skuDetails.getSku();
                        if (mProduct_id.equals(sku)) {
                            mSkuDetails = skuDetails;
                        }
                    }
                    BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                            .setSkuDetails(mSkuDetails)
                            .build();
                    mBillingClient.launchBillingFlow(mActivity, flowParams);

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(OSDKUtils.TAG, "Google initiated payment exception");
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            OSDK.getInstance().onPayFailure("Google initiated payment exception");
                        }
                    });
                }
            }
        });
    }

    /**
     * 确认交易
     *
     * @param purchase
     */
    private void handlePurchase(Purchase purchase) {
        Log.d(OSDKUtils.TAG, "handlePurchase(): " + purchase);
        try {
            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                // Grant entitlement to the user.
                OSDKUtils.getInstance().addGoogleOrderInfo(mActivity, mOut_orderNo, setSendPayParam(purchase));
                OSDK.getInstance().onSendPaySuccess(setSendPayParam(purchase), mOut_orderNo, false);
                consumePay(purchase.getPurchaseToken(), mOut_orderNo);
            } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
                // ...等待支付
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(OSDKUtils.TAG, "Google confirms abnormal payment: " + e.getMessage());
            OSDK.getInstance().onPayFailure(SOCommonUtil.getRes4LocaleStr(mActivity, "o_google_confirms_abnormal_payment"));
        }
    }

    /**
     * set pay param
     *
     * @param purchase
     * @return
     */
    private HashMap<String, String> setSendPayParam(Purchase purchase) {
        HashMap<String, String> paramsMap = new HashMap<>();
        try {
            paramsMap.put("originalJson", purchase.getOriginalJson());
            paramsMap.put("signature", purchase.getSignature());
            paramsMap.put("sku", purchase.getSku());
            paramsMap.put("orderId", purchase.getOrderId());
            paramsMap.put("packageName", purchase.getPackageName());
            paramsMap.put("purchaseToken", purchase.getPurchaseToken());
            paramsMap.put("purchaseState", "" + purchase.getPurchaseState());
            paramsMap.put("purchaseTime", "" + purchase.getPurchaseTime());
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(OSDKUtils.TAG, "setSendPayParam() Exception: " + e.getMessage());
        }
        return paramsMap;
    }

    /**
     * 后台支付成功回调之后  发起消耗品确认购买
     *
     * @param purchaseToken
     * @param developerPayload
     */
    private void consumePay(String purchaseToken, String developerPayload) {
        ConsumeParams consumeParams =
                ConsumeParams.newBuilder()
                        .setPurchaseToken(purchaseToken)
                        .setDeveloperPayload(developerPayload)
                        .build();
        mBillingClient.consumeAsync(consumeParams, mConsumeResponseListener);

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OLoginCodeUtils.GOOGLE_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            try {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleSignInResult(task);
            } catch (Exception e) {
                if (mIsBindAccount) {
                    SOToastUtil.showShort(SOCommonUtil.getRes4LocaleStr(mActivity, "o_google_bind_onError"));
                } else {
                    SOToastUtil.showShort(SOCommonUtil.getRes4LocaleStr(mActivity, "o_google_login_error") + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * 释放
     */
    public void googlePayDestroy() {
        Log.d(OSDKUtils.TAG, "googlePayDestroy()");
        if (mBillingClient != null && mBillingClient.isReady()) {
            Log.d(OSDKUtils.TAG, "BillingClient can only be used once -- closing connection");
            // BillingClient can only be used once.
            // After calling endConnection(), we must create a new BillingClient.
            mBillingClient.endConnection();
        }
        mBillingClient = null;
        mGoogleSignInClient = null;
        mGooglePayHandler.removeCallbacksAndMessages(null);
        mGooglePayHandler = null;
        mBillingClientStateListener = null;
    }
}
