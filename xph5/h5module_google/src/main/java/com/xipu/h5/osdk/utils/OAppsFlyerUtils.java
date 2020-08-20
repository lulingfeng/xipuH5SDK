package com.xipu.h5.osdk.utils;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import com.appsflyer.AFInAppEventParameterName;
import com.appsflyer.AFInAppEventType;
import com.appsflyer.AppsFlyerLib;
import com.appsflyer.AppsFlyerTrackingRequestListener;
import com.xipu.h5.sdk.util.H5Utils;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class OAppsFlyerUtils {

    private static OAppsFlyerUtils instance;

    private static final String AF_OPEN_ID = "af_open_id";
    private static final String AF_APP_ID = "af_app_id";
    private static final String AF_KID = "af_kid";
    private static final String AF_ROLE_ID = "af_role_id";
    private static final String AF_SERVER_ID = "af_server_id";

    public static OAppsFlyerUtils getInstance() {
        if (instance == null) {
            synchronized (OAppsFlyerUtils.class) {
                if (instance == null) {
                    instance = new OAppsFlyerUtils();
                }
            }
        }
        return instance;
    }

    /**
     * 激活
     *
     * @param mActivity
     */
    public void reportAFActivate(Activity mActivity) {
        try {
            if (mActivity == null)
                return;
            HashMap<String, Object> eventValue = new LinkedHashMap<>();
            eventValue.put(AF_APP_ID, H5Utils.getAppID());
            eventValue.put(AF_KID, TextUtils.isEmpty(H5Utils.getOaid()) ? H5Utils.getKid(mActivity) : H5Utils.getOaid());
            Log.d(H5Utils.TAG, "reportAFActivate() eventValue: " + eventValue.toString());
            AppsFlyerLib.getInstance().trackEvent(mActivity.getApplicationContext(), "af_activate", eventValue, new AppsFlyerTrackingRequestListener() {
                @Override
                public void onTrackingRequestSuccess() {
                    Log.d(H5Utils.TAG, "reportAFActivate() onTrackingRequestSuccess()");
                }

                @Override
                public void onTrackingRequestFailure(String s) {
                    Log.d(H5Utils.TAG, "reportAFActivate() onTrackingRequestFailure(): " + s);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(H5Utils.TAG, "reportAFActivate() Exception: " + e.getMessage());
        }
    }

    /**
     * 创角
     *
     * @param mActivity
     * @param role_id
     * @param server_id
     */
    public void reportAFCreateRole(Activity mActivity, String role_id, String server_id) {
        try {
            if (mActivity == null)
                return;
            HashMap<String, Object> eventValue = new LinkedHashMap<>();
            eventValue.put(AFInAppEventParameterName.CUSTOMER_USER_ID, H5Utils.getUserModel() != null && !TextUtils.isEmpty(H5Utils.getUserModel().getOpenid()) ? H5Utils.getUserModel().getOpenid() : "");
            eventValue.put(AF_OPEN_ID, H5Utils.getUserModel() != null && !TextUtils.isEmpty(H5Utils.getUserModel().getOpenid()) ? H5Utils.getUserModel().getOpenid() : "");
            eventValue.put(AF_APP_ID, H5Utils.getAppID());
            eventValue.put(AF_KID, TextUtils.isEmpty(H5Utils.getOaid()) ? H5Utils.getKid(mActivity) : H5Utils.getOaid());
            eventValue.put(AF_ROLE_ID, TextUtils.isEmpty(role_id) ? "" : role_id);
            eventValue.put(AF_SERVER_ID, TextUtils.isEmpty(server_id) ? "" : server_id);
            Log.d(H5Utils.TAG, "reportAFCreateRole() eventValue: " + eventValue.toString());
            AppsFlyerLib.getInstance().trackEvent(mActivity.getApplicationContext(), "af_create_role", eventValue, new AppsFlyerTrackingRequestListener() {
                @Override
                public void onTrackingRequestSuccess() {
                    Log.d(H5Utils.TAG, "reportAFCreateRole() onTrackingRequestSuccess()");
                }

                @Override
                public void onTrackingRequestFailure(String s) {
                    Log.d(H5Utils.TAG, "reportAFCreateRole() onTrackingRequestFailure(): " + s);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(H5Utils.TAG, "reportAFCreateRole() Exception: " + e.getMessage());
        }
    }

    /**
     * 角色登录
     *
     * @param mActivity
     * @param role_id
     * @param server_id
     */
    public void reportAFLoginRole(Activity mActivity, String role_id, String server_id) {
        try {
            if (mActivity == null)
                return;
            HashMap<String, Object> eventValue = new LinkedHashMap<>();
            eventValue.put(AFInAppEventParameterName.CUSTOMER_USER_ID, H5Utils.getUserModel() != null && !TextUtils.isEmpty(H5Utils.getUserModel().getOpenid()) ? H5Utils.getUserModel().getOpenid() : "");
            eventValue.put(AF_OPEN_ID, H5Utils.getUserModel() != null && !TextUtils.isEmpty(H5Utils.getUserModel().getOpenid()) ? H5Utils.getUserModel().getOpenid() : "");
            eventValue.put(AF_APP_ID, H5Utils.getAppID());
            eventValue.put(AF_KID, TextUtils.isEmpty(H5Utils.getOaid()) ? H5Utils.getKid(mActivity) : H5Utils.getOaid());
            eventValue.put(AF_ROLE_ID, TextUtils.isEmpty(role_id) ? "" : role_id);
            eventValue.put(AF_SERVER_ID, TextUtils.isEmpty(server_id) ? "" : server_id);
            Log.d(H5Utils.TAG, "reportAFLoginRole() eventValue: " + eventValue.toString());
            AppsFlyerLib.getInstance().trackEvent(mActivity.getApplicationContext(), "af_login_role", eventValue, new AppsFlyerTrackingRequestListener() {
                @Override
                public void onTrackingRequestSuccess() {
                    Log.d(H5Utils.TAG, "reportAFLoginRole() onTrackingRequestSuccess()");
                }

                @Override
                public void onTrackingRequestFailure(String s) {
                    Log.d(H5Utils.TAG, "reportAFLoginRole() onTrackingRequestFailure(): " + s);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(H5Utils.TAG, "reportAFLoginRole() Exception: " + e.getMessage());
        }
    }

    /**
     * 登录
     *
     * @param mActivity
     */
    public void reportAFLogin(Activity mActivity) {
        try {
            if (mActivity == null)
                return;
            HashMap<String, Object> eventValue = new LinkedHashMap<>();
            eventValue.put(AFInAppEventParameterName.CUSTOMER_USER_ID, H5Utils.getUserModel() != null && !TextUtils.isEmpty(H5Utils.getUserModel().getOpenid()) ? H5Utils.getUserModel().getOpenid() : "");
            eventValue.put(AF_OPEN_ID, H5Utils.getUserModel() != null && !TextUtils.isEmpty(H5Utils.getUserModel().getOpenid()) ? H5Utils.getUserModel().getOpenid() : "");
            eventValue.put(AF_APP_ID, H5Utils.getAppID());
            eventValue.put(AF_KID, TextUtils.isEmpty(H5Utils.getOaid()) ? H5Utils.getKid(mActivity) : H5Utils.getOaid());
            Log.d(H5Utils.TAG, "reportAFLogin() eventValue: " + eventValue.toString());
            AppsFlyerLib.getInstance().trackEvent(mActivity.getApplicationContext(), AFInAppEventType.LOGIN, eventValue, new AppsFlyerTrackingRequestListener() {
                @Override
                public void onTrackingRequestSuccess() {
                    Log.d(H5Utils.TAG, "reportAFLogin() onTrackingRequestSuccess()");
                }

                @Override
                public void onTrackingRequestFailure(String s) {
                    Log.d(H5Utils.TAG, "reportAFLogin() onTrackingRequestFailure(): " + s);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(H5Utils.TAG, "reportAFLogin() Exception: " + e.getMessage());
        }
    }

    /**
     * 注册
     *
     * @param mActivity
     * @param third_type
     */
    public void reportAFRegistration(Activity mActivity, String third_type) {
        try {
            if (mActivity == null)
                return;
            HashMap<String, Object> eventValue = new LinkedHashMap<>();
            eventValue.put(AFInAppEventParameterName.CUSTOMER_USER_ID, H5Utils.getUserModel() != null && !TextUtils.isEmpty(H5Utils.getUserModel().getOpenid()) ? H5Utils.getUserModel().getOpenid() : "");
            eventValue.put(AFInAppEventParameterName.REGSITRATION_METHOD, third_type);
            eventValue.put(AF_OPEN_ID, H5Utils.getUserModel() != null && !TextUtils.isEmpty(H5Utils.getUserModel().getOpenid()) ? H5Utils.getUserModel().getOpenid() : "");
            eventValue.put(AF_APP_ID, H5Utils.getAppID());
            eventValue.put(AF_KID, TextUtils.isEmpty(H5Utils.getOaid()) ? H5Utils.getKid(mActivity) : H5Utils.getOaid());
            Log.d(H5Utils.TAG, "reportAFRegistration() eventValue: " + eventValue.toString());
            AppsFlyerLib.getInstance().trackEvent(mActivity.getApplicationContext(), AFInAppEventType.COMPLETE_REGISTRATION, eventValue, new AppsFlyerTrackingRequestListener() {
                @Override
                public void onTrackingRequestSuccess() {
                    Log.d(H5Utils.TAG, "reportAFRegistration() onTrackingRequestSuccess()");
                }

                @Override
                public void onTrackingRequestFailure(String s) {
                    Log.d(H5Utils.TAG, "reportAFRegistration() onTrackingRequestFailure(): " + s);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(H5Utils.TAG, "reportAFRegistration() Exception: " + e.getMessage());
        }
    }

    /**
     * 付款
     *
     * @param mActivity
     * @param out_trade_no
     * @param amount
     * @param product_id
     */
    public void reportAFPurchase(Activity mActivity, String out_trade_no, String amount, String product_id) {
        try {
            if (mActivity == null)
                return;
            HashMap<String, Object> eventValue = new LinkedHashMap<>();
            eventValue.put(AFInAppEventParameterName.CUSTOMER_USER_ID, H5Utils.getUserModel() != null && !TextUtils.isEmpty(H5Utils.getUserModel().getOpenid()) ? H5Utils.getUserModel().getOpenid() : "");
            eventValue.put(AF_OPEN_ID, H5Utils.getUserModel() != null && !TextUtils.isEmpty(H5Utils.getUserModel().getOpenid()) ? H5Utils.getUserModel().getOpenid() : "");
            eventValue.put(AF_APP_ID, H5Utils.getAppID());
            eventValue.put(AF_KID, TextUtils.isEmpty(H5Utils.getOaid()) ? H5Utils.getKid(mActivity) : H5Utils.getOaid());
            eventValue.put(AFInAppEventParameterName.CURRENCY, "USD");
            eventValue.put(AFInAppEventParameterName.ORDER_ID, out_trade_no);
            try {
                eventValue.put(AFInAppEventParameterName.REVENUE, (Double.valueOf(amount) / 100));
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(H5Utils.TAG, "reportAFPurchase() price Exception :" + e.getMessage());
            }
            eventValue.put(AFInAppEventParameterName.CONTENT_ID, product_id);
            Log.d(H5Utils.TAG, "reportAFPurchase() eventValue: " + eventValue.toString());
            AppsFlyerLib.getInstance().trackEvent(mActivity.getApplicationContext(), AFInAppEventType.PURCHASE, eventValue, new AppsFlyerTrackingRequestListener() {
                @Override
                public void onTrackingRequestSuccess() {
                    Log.d(H5Utils.TAG, "reportAFPurchase() onTrackingRequestSuccess()");
                }

                @Override
                public void onTrackingRequestFailure(String s) {
                    Log.d(H5Utils.TAG, "reportAFPurchase() onTrackingRequestFailure(): " + s);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(H5Utils.TAG, "reportAFPurchase() Exception: " + e.getMessage());
        }
    }
}
