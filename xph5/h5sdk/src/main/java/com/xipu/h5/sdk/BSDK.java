package com.xipu.h5.sdk;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.startobj.util.common.SOCommonUtil;
import com.startobj.util.http.SOCallBack;
import com.startobj.util.http.SOHttpConnection;
import com.startobj.util.http.SOJsonMapper;
import com.startobj.util.http.SORequestParams;
import com.startobj.util.http.SOServertReturnErrorException;
import com.startobj.util.network.SONetworkUtil;
import com.startobj.util.string.SOStringUtil;
import com.startobj.util.toast.SOToastUtil;
import com.xipu.h5.sdk.callback.BSDKApi;
import com.xipu.h5.sdk.config.H5Config;
import com.xipu.h5.sdk.model.ConfigModule;
import com.xipu.h5.sdk.model.PayReportModel;
import com.xipu.h5.sdk.model.RequestModel;
import com.xipu.h5.sdk.model.UserModel;
import com.xipu.h5.sdk.util.H5OrderFileUtils;
import com.xipu.h5.sdk.util.H5Utils;
import com.xipu.h5.sdk.util.ThreadManager;
import com.xipu.h5.sdk.util.ZYJSONObject;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public abstract class BSDK implements BSDKApi {

    private static ConfigModule mConfigModule;
    // 请求队列
    private static List<RequestModel> mRequestModels = new ArrayList<RequestModel>();

    private String THREAD_NAME_SEND_PAY_SUCCESS = "sendPaySuccess";

    private static HashMap<String, String> mThirdLoginMaps;

    private static String mOutTradeNo;

    private static Activity mActivity;

    protected final int LOGIN_SUCCESS = 1001;
    protected final int LOGIN_FAILURE = 1002;

    protected Handler loginHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case LOGIN_SUCCESS:
                    loginSuccessCallBack();
                    break;
                case LOGIN_FAILURE:
                    onLoginFailure();
                    break;
            }
        }
    };

    private void loginSuccessCallBack() {
        UserModel userModel = H5Utils.getUserModel();
        HashMap<String, Object> map = new HashMap<>();
        if (userModel != null) {
            map.put("sign", userModel.getSign());
            map.put("openid", userModel.getOpenid());
            map.put("timestamp", userModel.getTimes());
            map.put("is_newuser", userModel.getIs_newuser());
            map.put("accesstoken", userModel.getAccesstoken());
            map.put("device_id", H5Utils.getDevice_id(mActivity));
        }
        map.put("identity_status", H5Utils.getIdentityStatus());

        Log.d(H5Utils.TAG, "identity_status: " + H5Utils.getIdentityStatus());
        Log.d(H5Utils.TAG, "sign: " + userModel.getSign());
        Log.d(H5Utils.TAG, "openid: " + userModel.getOpenid());
        Log.d(H5Utils.TAG, "timestamp: " + userModel.getTimes());
        Log.d(H5Utils.TAG, "is_newuser: " + userModel.getIs_newuser());
        Log.d(H5Utils.TAG, "accesstoken: " + userModel.getAccesstoken());
        Log.d(H5Utils.TAG, "device_id: " + H5Utils.getDevice_id(mActivity));
        onLoginSuccess(map);
    }

    @Override
    public void onModuleInit(ConfigModule configModule) {
        this.mConfigModule = configModule;
    }

    @Override
    public void onActivate(Activity activity, String values) {
        try {
            com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(values);
            H5Utils.setDevice_id(activity, jsonObject.getString("device_id"));
        } catch (Exception e) {
            Log.e(H5Utils.TAG, "onActivate() Exception: " + e.getMessage());
        }
    }

    @Override
    public void onLogin(Activity activity) {

    }

    @Override
    public void onSwitchAccount() {
        H5Utils.setUserModel(null);
    }

    @Override
    public void onPay(final Activity activity, HashMap<String, String> paramsMap) {
        Log.d(H5Utils.TAG, "pay()");
        if (!SONetworkUtil.isNetworkAvailable(activity)) {
            SOToastUtil.showShort(SOCommonUtil.getRes4LocaleStr(activity, "o_tip_network"));
            return;
        }
        if (!paramsMap.containsKey("role_id") || !paramsMap.containsKey("server_id")) {
            SOToastUtil.showShort("pay() roleModel is null");
            Log.d(H5Utils.TAG, "pay() roleModel is null");
            return;
        }
        H5Utils.showProgress(activity, false, 0);

        String amount = TextUtils.isEmpty(paramsMap.get("amount")) ? "" : paramsMap.get("amount");

        paramsMap.putAll(H5Utils.getCommonParams(activity));

        String timestamp = (int) (System.currentTimeMillis() / 1000) + "";

        paramsMap.put("app_id", H5Utils.getAppID());
        paramsMap.put("accesstoken", H5Utils.getAccesstoken());
        paramsMap.put("timestamp", timestamp);
        paramsMap.put("app_id", H5Utils.getAppID());
        String openid = "";
        if (paramsMap.containsKey("openid")) {
            openid = paramsMap.get("openid");
        }
        if (!TextUtils.isEmpty(openid) && paramsMap.containsKey("third_openid")) {
            openid = paramsMap.get("third_openid");
        }
        paramsMap.put("thirdsdk", H5Utils.getSDKType() + "");
        String sign = SOStringUtil.Md5(
                "openid=" + openid +
                        "&amount=" + amount +
                        "&app_id=" + H5Utils.getAppID() +
                        "&timestamp=" + timestamp +
                        "|||appsecret=" + "");
        paramsMap.put("sign", sign);

        String url = H5Config.THIRD_PAY_URL;
        Log.d(H5Utils.TAG, "pay() params: " + paramsMap.toString());
        SORequestParams params = new SORequestParams(url, paramsMap);
        SOHttpConnection.get(activity, params, new SOCallBack.SOCommonCallBack<String>() {
            @Override
            public void onSuccess(String result) {
                Log.e(H5Utils.TAG, "pay() onSuccess: " + result);
                try {
                    String[] datas = SOJsonMapper.fromJson(result);
                    if (datas.length != 0) {
                        if (!TextUtils.isEmpty(datas[1]) && !"null".equals(datas[1])) {
                            ZYJSONObject jsonObject = new ZYJSONObject(datas[1]);
                            if (jsonObject.has("out_trade_no")) {
                                mOutTradeNo = jsonObject.getStringDef("out_trade_no");
                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put("out_trade_no", mOutTradeNo);
                                if (jsonObject.has("third_result")) {
                                    String third_result = jsonObject.getStringDef("third_result");
                                    map.put("third_result", third_result);
                                }
                                if (jsonObject.has("coin_amount")) {
                                    String coin_amount = jsonObject.getStringDef("coin_amount");
                                    map.put("coin_amount", coin_amount);
                                }
                                onThirdSDKPay(activity, map);
                            }
                        } else {
                            SOToastUtil.showShort(SOCommonUtil.getRes4LocaleStr(activity, "h5_tip_server_error"));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(H5Utils.TAG, "pay() 解析异常" + e.getMessage());
                    if (e instanceof SOServertReturnErrorException) {
                        SOToastUtil.showShort(e.getMessage());
                    } else {
                        SOToastUtil.showShort(SOCommonUtil.getRes4LocaleStr(activity, "h5_tip_server_error"));
                    }
                }
            }

            @Override
            public void onHttpError(Throwable ex, boolean isOnCallback) {
                Log.e(H5Utils.TAG, "onHttpError() pay:" + ex.getMessage());
                SOToastUtil.showShort("pay onHttpError: " + ex.getMessage());
            }

            @Override
            public void onCodeError(CodeErrorException cex) {
                Log.e(H5Utils.TAG, "onCodeError() pay:" + cex.getMessage());
                SOToastUtil.showShort("pay onCodeError: " + cex.getMessage());
            }

            @Override
            public void onFinished() {
                Log.e(H5Utils.TAG, "支付接口回调结束");
                H5Utils.cancelProgress();
            }
        });

    }

    @Override
    public void onCreateRole(Activity activity, String values) {

    }

    @Override
    public void onLoginRole(Activity activity, String values) {

    }

    @Override
    public void onUpdateRole(Activity activity, String values) {

    }

    @Override
    public void onThirdLoginSuccess(final Activity mActivity, HashMap<String, String> loginMaps, final int sdkType) {
        mThirdLoginMaps = loginMaps;
        mThirdLoginMaps.putAll(H5Utils.getCommonParams(mActivity));
        mThirdLoginMaps.put("app_id", H5Utils.getAppID());
        Log.d(H5Utils.TAG, "onThirdLoginSuccess() params: " + mThirdLoginMaps.toString());
        SORequestParams params = new SORequestParams(H5Config.THIRD_LOGIN_URL, mThirdLoginMaps);
        SOHttpConnection.get(mActivity, params, new SOCallBack.SOCommonCallBack<String>() {

            @Override
            public void onSuccess(String result) {
                try {
                    Log.d(H5Utils.TAG, "onThirdLoginSuccess() :" + result);
                    String[] datas = SOJsonMapper.fromJson(result);
                    if (datas.length != 0) {
                        if (!TextUtils.isEmpty(datas[1]) && !"null".equals(datas[1])) {
                            JSONObject jsonObject = new JSONObject(datas[1]);
                            UserModel userModel = JSON.parseObject(jsonObject.getString("userentity"), UserModel.class);
                            if (jsonObject.has("device_id")) {
                                H5Utils.setDevice_id(mActivity, jsonObject.getString("device_id"));
                            }
                            if (userModel != null) {
                                userModel.setAccount_type(mThirdLoginMaps.get("third_type"));
                                H5Utils.setUserModel(userModel);
                                if (sdkType == H5Config.TYPE_OSDK) {
                                    thirdSaveUserInfo(mActivity);
                                } else {
                                    // ... 基类保存账号
                                }
                                loginHandler.sendEmptyMessage(LOGIN_SUCCESS);
                            } else {
                                SOToastUtil.showShort("onThirdLoginSuccess() userModel == null");
                                loginHandler.sendEmptyMessage(LOGIN_FAILURE);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    SOToastUtil.showShort("onThirdLoginSuccess() Exception: " + e.getMessage());
                    loginHandler.sendEmptyMessage(LOGIN_FAILURE);
                }
            }

            @Override
            public void onHttpError(Throwable ex, boolean isOnCallback) {
                Log.e(H5Utils.TAG, "HttpError onThirdLoginSuccess():" + ex.getMessage());
                SOToastUtil.showShort("onThirdLoginSuccess() onHttpError: " + ex.getMessage());
                loginHandler.sendEmptyMessage(LOGIN_FAILURE);
            }

            @Override
            public void onCodeError(CodeErrorException cex) {
                Log.e(H5Utils.TAG, "CodeError onThirdLoginSuccess():" + cex.getMessage());
                SOToastUtil.showShort("onThirdLoginSuccess() onCodeError: " + cex.getMessage());
                loginHandler.sendEmptyMessage(LOGIN_FAILURE);
            }

            @Override
            public void onFinished() {
                H5Utils.cancelProgress();
            }
        });
    }

    @Override
    public void onThirdLoginFailure(String loginType, String msg) {

    }

    @Override
    public void onSendPaySuccess(HashMap<String, String> payMaps, String send_pay_out_order_no, boolean isQueryOrder) {
        payMaps.put("isQueryOrder", "" + isQueryOrder);
        H5OrderFileUtils.getInstance().addSendPaySuccessOrderNo(mActivity, send_pay_out_order_no, payMaps);
        handlerPaySuccessOrder();
    }

    public void handlerPaySuccessOrder() {
        Log.d(H5Utils.TAG, "handlerPaySuccessOrder()");
        HashMap<String, String> orderMaps = H5OrderFileUtils.getInstance().getSendPaySuccessOrderNoList(mActivity);
        if (orderMaps.size() <= 0)
            return;
        for (String order : orderMaps.keySet()) {
            HashMap<String, String> paramsMap = new HashMap<>();
            if (mThirdLoginMaps != null && !mThirdLoginMaps.isEmpty()) {
                paramsMap.putAll(mThirdLoginMaps);
            } else {
                paramsMap.putAll(H5Utils.getCommonParams(mActivity));
                paramsMap.put("app_id", H5Utils.getAppID());
            }
            if (!TextUtils.isEmpty(order)) {
                paramsMap.put("out_trade_no", order);
            }
            paramsMap.put("accesstoken", H5Utils.getAccesstoken());
            try {
                paramsMap.putAll(JSON.parseObject(orderMaps.get(order), new TypeReference<HashMap<String, String>>() {
                }));
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(H5Utils.TAG, "handlerPaySuccessOrder() Exception: " + e.getMessage());
            }

            boolean isCallBack = !TextUtils.isEmpty(mOutTradeNo) && mOutTradeNo.equals(order) ? true : false;
            boolean isQueryOrder = paramsMap.get("isQueryOrder").equals("true") ? true : false;
            Log.d(H5Utils.TAG, "isCallBack: " + isCallBack + ",isQueryOrder: " + isQueryOrder);
            Log.d(H5Utils.TAG, "jointUrl paramsMap: " + paramsMap.toString());
            RequestModel re = new RequestModel(H5Utils.joinUrl(mConfigModule.getNotify_url()), paramsMap,
                    THREAD_NAME_SEND_PAY_SUCCESS, System.currentTimeMillis(), 0, isCallBack, isQueryOrder);
            mRequestModels.add(re);
        }
    }

    @Override
    public void onCreate(Activity activity, Intent intent) {
        Log.d(H5Utils.TAG, "onCreate()");
        this.mActivity = activity;
        runRequestThread(activity);
    }

    @Override
    public void onStart() {
        Log.d(H5Utils.TAG, "onStart()");
    }

    @Override
    public void onRestart(Activity activity) {
        Log.d(H5Utils.TAG, "onRestart()");
    }

    @Override
    public void onResume(Activity activity) {
        Log.d(H5Utils.TAG, "onResume()");
    }

    @Override
    public void onPause(Activity activity) {
        Log.d(H5Utils.TAG, "onPause()");
    }

    @Override
    public void onStop(Activity activity) {
        Log.d(H5Utils.TAG, "onStop()");
    }

    @Override
    public void onDestroy(Activity activity) {
        Log.d(H5Utils.TAG, "onDestroy()");
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.d(H5Utils.TAG, "onNewIntent()");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(H5Utils.TAG, "onActivityResult()");
    }

    public abstract void onLoginSuccess(HashMap<String, Object> map);

    public abstract void onLoginFailure();

    public abstract void thirdSaveUserInfo(Activity activity);

    public abstract void onThirdSDKPay(Activity activity, HashMap<String, String> map);

    public abstract void onPaySuccess(PayReportModel payReportModel1);

    public abstract void onPayCancel();

    public abstract void onPayFailure(String msg);


    /*
     * 线程处理请求发送
     */
    private void runRequestThread(final Activity activity) {
        ThreadManager.getInstance().getSinglePoolThread().execute(new Runnable() {
            HttpURLConnection mConn;

            @Override
            public void run() {

                while (true) {
                    judgeNetwork(activity);
                    judgeHasList();
                    RequestModel re = mRequestModels.get(0);
                    mRequestModels.remove(0);
                    long time = System.currentTimeMillis();
                    if (re.getCount() == 0) {
                        sendRequest(activity, re);
                    } else if (Math.abs(re.getTimestamp() - time) >= 1000 * 15 && re.getCount() == 1) {
                        sendRequest(activity, re);
                    } else if (Math.abs(re.getTimestamp() - time) >= 1000 * 30 && re.getCount() == 2) {
                        sendRequest(activity, re);
                    } else if (Math.abs(re.getTimestamp() - time) >= 1000 * 60 && re.getCount() == 3) {
                        sendRequest(activity, re);
                    } else if (Math.abs(re.getTimestamp() - time) >= 1000 * 60 * 5 && re.getCount() == 4) {
                        sendRequest(activity, re);
                    } else if (Math.abs(re.getTimestamp() - time) >= 1000 * 60 * 30 && re.getCount() == 5) {
                        sendRequest(activity, re);
                    } else if (Math.abs(re.getTimestamp() - time) >= 1000 * 60 * 120 && re.getCount() == 6) {
                        sendRequest(activity, re);
                    }
                }
            }

            /*
             * 判断请求集合是否有值 无请求 睡1秒
             */
            private void judgeHasList() {
                while (mRequestModels.isEmpty()) {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            /*
             * 判断网络 无网络下 睡1秒
             *
             * @param activity
             */
            private void judgeNetwork(final Activity activity) {
                while (true) {
                    if (!SONetworkUtil.isNetworkAvailable(activity)) {
                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        break;
                    }
                }
            }

            /*
             * 发送请求
             *
             * @param activity
             * @param re
             */
            private void sendRequest(final Activity activity, final RequestModel re) {
                Log.d(H5Utils.TAG, "sendRequest() tag: " + re.getTag());
                int mTimeOut = 8000;
                SORequestParams params = new SORequestParams(re.getUrl(), re.getParams());
                try {
                    URL mUrl = new URL(params.getUrl());
//                    Log.e(HCUtils.TAG, "请求内容 url " + params.getUrl());
//                    Log.e(HCUtils.TAG, "请求参数 param " + params.getParamsStr());
                    mConn = (HttpURLConnection) mUrl.openConnection();
                    mConn.setRequestMethod("POST");
                    mConn.setUseCaches(false);
                    mConn.setConnectTimeout(mTimeOut);
                    mConn.setReadTimeout(mTimeOut);
                    if (params.getParamsStr() != null) {
                        OutputStream os = mConn.getOutputStream();
                        os.write(params.getParamsStr().getBytes());
                        os.flush();
                        os.close();
                    }

                    Log.d(H5Utils.TAG, "sendRequest() code " + mConn.getResponseCode());
                    if (200 == mConn.getResponseCode()) {
                        if (THREAD_NAME_SEND_PAY_SUCCESS.equals(re.getTag())) {
                            StringBuilder buf = SOHttpConnection.obtainDatas(mConn);
                            Log.e(H5Utils.TAG, "check order: " + re.getParams().get("out_trade_no") + " status: " + buf);
                            if ("success".equals(buf.toString())) {
                                if (re.isQueryOrder() && !TextUtils.isEmpty(re.getParams().get("out_trade_no"))) {
                                    Timer timer = new Timer();
                                    timer.schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            onQueryPayOrder(activity, re.getParams().get("out_trade_no"), re.isCallBack());
                                        }
                                    }, 5000);
                                }
                            } else {
                                if (re.isCallBack()) {
                                    onPayFailure("");
                                }
                            }
                        }
                    } else {
                        re.setCount(re.getCount() + 1);
                        mRequestModels.add(re);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    mConn.disconnect();
                }

            }
        });
    }

    /*
     * 查询订单
     * @param outTradeNO
     * @param isCallBack
     */
    public void onQueryPayOrder(final Activity activity, final String outTradeNO, final boolean isCallBack) {
        Log.d(H5Utils.TAG, "onQueryPayOrder()");
        HashMap<String, String> paramsMap = new HashMap<String, String>();
        paramsMap.put("out_trade_no", outTradeNO);
        SORequestParams params = new SORequestParams(H5Config.QUERY_ORDER_URL, paramsMap);
        SOHttpConnection.get(activity, params, new SOCallBack.SOCommonCallBack<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(H5Utils.TAG, "onQueryPayOrder() onSuccess " + result);
                try {
                    String[] datas = SOJsonMapper.fromJson(result);
                    JSONObject dataResult = new JSONObject(datas[1]);
                    if (dataResult.has("status")) {
                        if (dataResult.getInt("status") >= 2) {
                            PayReportModel payReportModel1 = JSON.parseObject(dataResult.toString(), PayReportModel.class);
                            payReportModel1.setOutTradeNO(outTradeNO);
                            payReportModel1.setCallBack(isCallBack);
                            onPaySuccess(payReportModel1);
                            H5OrderFileUtils.getInstance().removeSendPaySuccessOrderNo(activity, outTradeNO);
                        } else {
                            if (isCallBack) {
                                onPayFailure("");
                            }
                        }
                    } else {
                        if (isCallBack) {
                            onPayFailure("");
                        }
                    }
                } catch (Exception e) {
                    if (isCallBack) {
                        onPayFailure("");
                    }
                }
            }

            @Override
            public void onHttpError(Throwable ex, boolean isOnCallback) {
                Log.d(H5Utils.TAG, "onQueryPayOrder onHttpError:" + ex.getMessage());
                if (isCallBack) {
                    onPayFailure("");
                }
            }

            @Override
            public void onCodeError(CodeErrorException cex) {
                Log.d(H5Utils.TAG, "onQueryPayOrder onCodeError:" + cex.getMessage());
                if (isCallBack) {
                    onPayFailure("");
                }
            }

            @Override
            public void onFinished() {
                Log.d(H5Utils.TAG, "onQueryPayOrder onFinished");
            }
        });
    }
}
