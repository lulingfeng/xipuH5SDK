package com.xipu.h5.osdk.utils;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.startobj.util.common.SOCommonUtil;
import com.startobj.util.http.SOCallBack;
import com.startobj.util.http.SOHttpConnection;
import com.startobj.util.http.SOJsonMapper;
import com.startobj.util.http.SORequestParams;
import com.startobj.util.http.SOServertReturnErrorException;
import com.startobj.util.log.SOLogUtil;
import com.startobj.util.network.SONetworkUtil;
import com.startobj.util.toast.SOToastUtil;
import com.xipu.h5.osdk.callback.OBindAccountCallback;
import com.xipu.h5.osdk.callback.ORandomAccountCallback;
import com.xipu.h5.osdk.callback.OOverseaAccountLoginCallback;
import com.xipu.h5.sdk.config.H5Config;
import com.xipu.h5.sdk.model.UserModel;
import com.xipu.h5.sdk.util.H5Utils;
import com.xipu.h5.sdk.util.ZYJSONObject;

import org.json.JSONObject;

import java.util.HashMap;

public class ONetWorkUtils {

    private static ONetWorkUtils mInstance;

    public static ONetWorkUtils getInstance() {
        if (mInstance == null) {
            synchronized (ONetWorkUtils.class) {
                if (mInstance == null) {
                    mInstance = new ONetWorkUtils();
                }
            }
        }
        return mInstance;
    }

    /*
     * 生成随机账号
     * @param activity
     * @param randomAccountCallback
     */
    public void createRandomAccount(final Activity activity, final ORandomAccountCallback randomAccountCallback) {
        if (!SONetworkUtil.isNetworkAvailable(activity)) {
            SOToastUtil.showShort(SOCommonUtil.getRes4LocaleStr(activity, "o_tip_network"));
            return;
        }
        H5Utils.showProgress(activity, false, 0);
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.putAll(H5Utils.getCommonParams(activity));
        paramsMap.put("app_id", H5Utils.getAppID());
        SORequestParams params = new SORequestParams(H5Config.CREATE_RANDOM_ACCOUNT, paramsMap);
        SOHttpConnection.get(activity, params, new SOCallBack.SOCommonCallBack<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    String[] datas = SOJsonMapper.fromJson(result);
                    JSONObject dataResult = new JSONObject(datas[1]);
                    Log.d(OSDKUtils.TAG, "随机账号:" + dataResult.toString());
                    if (dataResult.has("username") && dataResult.has("password")) {
                        String userName = TextUtils.isEmpty(dataResult.get("username").toString()) ? "" : dataResult.get("username").toString();
                        String password = TextUtils.isEmpty(dataResult.get("password").toString()) ? "" : dataResult.get("password").toString();
                        randomAccountCallback.onSuccess(userName, password);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(OSDKUtils.TAG, "随机账号json解析异常: ");
                    if (e instanceof SOServertReturnErrorException) {
                        SOToastUtil.showShort(e.getMessage());
                    } else
                        SOToastUtil.showShort(SOCommonUtil.getRes4LocaleStr(activity, "h5_tip_server_error"));
                }
            }

            @Override
            public void onHttpError(Throwable ex, boolean isOnCallback) {
                SOToastUtil.showShort("createRandomAccount onHttpError: " + ex.getMessage());
            }

            @Override
            public void onCodeError(CodeErrorException cex) {
                SOToastUtil.showShort("createRandomAccount onCodeError: " + cex.getMessage());
            }

            @Override
            public void onFinished() {
                H5Utils.cancelProgress();
            }

        });
    }


    public void doRegisterOverseaAccount(final Activity activity, final String username, final String password, final View view,
                                         final OOverseaAccountLoginCallback registerOverseaAccCallback) {
        if (!SONetworkUtil.isNetworkAvailable(activity)) {
            SOToastUtil.showShort(SOCommonUtil.getRes4LocaleStr(activity, "o_tip_network"));
            return;
        }
        if (view != null) {
            view.setEnabled(false);
        }
        H5Utils.showProgress(activity, false, 0);
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.putAll(H5Utils.getCommonParams(activity));
        paramsMap.put("app_id", H5Utils.getAppID());
        paramsMap.put("username", username);
        paramsMap.put("password", password);
        SORequestParams params = new SORequestParams(H5Config.REGISTERACCOUNT_URL, paramsMap);
        SOHttpConnection.get(activity, params, new SOCallBack.SOCommonCallBack<String>() {
            UserModel userModel;

            @Override
            public void onSuccess(String result) {
                try {
                    String[] datas = SOJsonMapper.fromJson(result);
                    if (datas.length != 0) {
                        ZYJSONObject jsonObject = new ZYJSONObject(datas[1]);
                        Log.d(OSDKUtils.TAG, "---doRegisterGuestAccount---onSuccess--" + jsonObject.toString());
                        String userstr = jsonObject.getStringDef("userentity");
                        userModel = JSON.parseObject(userstr, UserModel.class);
                        Log.d(OSDKUtils.TAG,"userModel: "+userModel);
                        if (userModel != null) {
                            userModel.setUsername(username);
                            userModel.setPassword(password);
                            userModel.setGuest(true);
                            userModel.setAccount_type(OLoginTypeUtils.GUEST);
                            H5Utils.setUserModel(userModel);
                            OSDKUtils.getInstance().saveOverseaUserEntityList(activity);
                        }
                        if (jsonObject.has("identity_status")) {
                            H5Utils.setIdentityStatus(jsonObject.getStringDef("identity_status"));
                        }
                        if (jsonObject.has("device_id")) {
                            H5Utils.setDevice_id(activity, jsonObject.getStringDef("device_id"));
                        }
                        registerOverseaAccCallback.onSuccess();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    SOLogUtil.i(OSDKUtils.TAG, e.toString(), SOLogUtil.mDebug);
                    if (e instanceof SOServertReturnErrorException) {
                        SOToastUtil.showShort(e.getMessage());
                    } else
                        SOToastUtil.showShort(SOCommonUtil.getRes4LocaleStr(activity, "h5_tip_server_error"));
                }
            }

            @Override
            public void onHttpError(Throwable ex, boolean isOnCallback) {
                SOToastUtil.showShort("doRegisterGuestAccount onHttpError: " + ex.getMessage());
            }

            @Override
            public void onCodeError(CodeErrorException cex) {
                SOToastUtil.showShort("doRegisterGuestAccount onCodeError: " + cex.getMessage());
            }

            @Override
            public void onFinished() {
                H5Utils.cancelProgress();
                if (view != null) {
                    view.setEnabled(true);
                }
            }
        });
    }

    /**
     * 绑定三方账号
     *
     * @param activity
     */
    public void doGuestBindThirdAccount(final Activity activity, final HashMap<String, String> thirdLoginMaps,
                                        final OBindAccountCallback bindAccountCallback) {

        if (!SONetworkUtil.isNetworkAvailable(activity)) {
            SOToastUtil.showShort(SOCommonUtil.getRes4LocaleStr(activity, "o_tip_network"));
            return;
        }
        H5Utils.showProgress(activity, false, 0);
        HashMap<String, String> paramsMap = H5Utils.getCommonParams(activity);
        if (thirdLoginMaps != null && !thirdLoginMaps.isEmpty()) {
            paramsMap.putAll(thirdLoginMaps);
        }
        paramsMap.put("accesstoken", H5Utils.getUserModel().getAccesstoken());
        paramsMap.put("app_id", H5Utils.getAppID());
        Log.d(OSDKUtils.TAG, "-doGuestBindThirdAccount--paramsMap---" + paramsMap.toString());
        SORequestParams params = new SORequestParams(H5Config.GUESTBINDTHIRDACCOUNT_URL, paramsMap);
        SOHttpConnection.get(activity, params, new SOCallBack.SOCommonCallBack<String>() {

            @Override
            public void onSuccess(String result) {
                try {
                    String[] datas = SOJsonMapper.fromJson(result);
                    if (datas.length != 0) {
                        H5Utils.getUserModel().setBindThirdAccount(true);
                        H5Utils.getUserModel().setBind_account_type(thirdLoginMaps.get("third_type"));
                        OSDKUtils.getInstance().saveOverseaUserEntityList(activity);
                        bindAccountCallback.onSuccess();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    SOLogUtil.i(OSDKUtils.TAG, e.toString(), SOLogUtil.mDebug);
                    if (e instanceof SOServertReturnErrorException) {
                        SOToastUtil.showShort(e.getMessage());
                    } else
                        SOToastUtil.showShort(SOCommonUtil.getRes4LocaleStr(activity, "h5_tip_server_error"));
                    bindAccountCallback.onFailure();
                }
            }

            @Override
            public void onHttpError(Throwable ex, boolean isOnCallback) {
                SOToastUtil.showShort("doGuestBindThirdAccount onHttpError: " + ex.getMessage());
                bindAccountCallback.onFailure();
            }

            @Override
            public void onCodeError(CodeErrorException cex) {
                SOToastUtil.showShort("doGuestBindThirdAccount onCodeError: " + cex.getMessage());
                bindAccountCallback.onFailure();
            }

            @Override
            public void onFinished() {
                H5Utils.cancelProgress();
            }
        });
    }

    public void doOverseaLogin(final Activity activity, final String username, final String password,
                               final View view, final OOverseaAccountLoginCallback overseaAccountLoginCallback) {
        if (!SONetworkUtil.isNetworkAvailable(activity)) {
            SOToastUtil.showShort(SOCommonUtil.getRes4LocaleStr(activity, "o_tip_network"));
            return;
        }
        if (TextUtils.isEmpty(username)) {
            SOToastUtil.showShort(SOCommonUtil.getRes4LocaleStr(activity, "hc_hint_username"));
            return;
        }
        if (TextUtils.isEmpty(password)) {
            SOToastUtil.showShort(SOCommonUtil.getRes4LocaleStr(activity, "hc_hint_password"));
            return;
        }
        if (view != null) {
            view.setEnabled(false);
        }
        H5Utils.showProgress(activity, false, 0);
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.putAll(H5Utils.getCommonParams(activity));
        paramsMap.put("app_id", H5Utils.getAppID());
        paramsMap.put("username", username);
        paramsMap.put("password", password);
        SORequestParams params = new SORequestParams(H5Config.LOGIN_URL, paramsMap);
        SOHttpConnection.get(activity, params, new SOCallBack.SOCommonCallBack<String>() {
            UserModel userModel;

            @Override
            public void onSuccess(String result) {
                try {
                    String[] datas = SOJsonMapper.fromJson(result);
                    if (datas.length != 0) {
                        ZYJSONObject jsonObject = new ZYJSONObject(datas[1]);
                        String userstr = jsonObject.getStringDef("userentity");
                        Log.d(OSDKUtils.TAG, "-----doOverseaLogin--onSuccess-------" + jsonObject.toString());
                        userModel = JSON.parseObject(userstr, UserModel.class);
                        if (userModel != null) {
                            userModel.setUsername(username);
                            userModel.setPassword(password);
                            userModel.setGuest(true);
                            userModel.setAccount_type(OLoginTypeUtils.GUEST);
                            H5Utils.setUserModel(userModel);
                            OSDKUtils.getInstance().saveOverseaUserEntityList(activity);
                        }

                        if (jsonObject.has("identity_status")) {
                            H5Utils.setIdentityStatus(jsonObject.getStringDef("identity_status"));
                        }
                        if (jsonObject.has("device_id")) {
                            H5Utils.setDevice_id(activity, jsonObject.getStringDef("device_id"));
                        }
                        overseaAccountLoginCallback.onSuccess();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    SOLogUtil.i(OSDKUtils.TAG, e.toString(), SOLogUtil.mDebug);
                    if (e instanceof SOServertReturnErrorException) {
                        SOToastUtil.showShort(e.getMessage());
                    } else
                        SOToastUtil.showShort(SOCommonUtil.getRes4LocaleStr(activity, "h5_tip_server_error"));
                }
            }

            @Override
            public void onHttpError(Throwable ex, boolean isOnCallback) {
                SOToastUtil.showShort("doLogin onHttpError: " + ex.getMessage());
            }

            @Override
            public void onCodeError(CodeErrorException cex) {
                SOToastUtil.showShort("doLogin onCodeError: " + cex.getMessage());
            }

            @Override
            public void onFinished() {
                H5Utils.cancelProgress();
                if (view != null) {
                    view.setEnabled(true);
                }
            }
        });
    }

}
