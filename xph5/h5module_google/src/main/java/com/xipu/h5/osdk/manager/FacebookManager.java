package com.xipu.h5.osdk.manager;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareContent;
import com.facebook.share.model.ShareHashtag;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.model.ShareVideo;
import com.facebook.share.model.ShareVideoContent;
import com.facebook.share.widget.ShareDialog;
import com.startobj.util.common.SOCommonUtil;
import com.startobj.util.toast.SOToastUtil;
import com.xipu.h5.osdk.OSDK;
import com.xipu.h5.osdk.utils.OLoginTypeUtils;
import com.xipu.h5.osdk.utils.OSDKUtils;
import com.xipu.h5.osdk.utils.OSharePlatform;
import com.xipu.h5.osdk.utils.OShareType;
import com.xipu.h5.osdk.dialog.OH5Dialog;
import com.xipu.h5.sdk.config.H5Config;
import com.xipu.h5.sdk.util.H5Utils;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * @author bruce
 * Facebook
 */
public class FacebookManager {

    /**
     * 上下文
     */
    private Activity mActivity;
    /**
     * 是否绑定
     */
    private boolean mIsBindAccount;
    /**
     * facebook login
     */
    private LoginManager mLoginManager;
    /**
     * facebook Callback
     */
    private CallbackManager mCallbackManager;
    /**
     * 提示框
     */
    private OH5Dialog mH5Dialog;
    /**
     * facebook share
     */
    private ShareDialog mShareDialog;
    /**
     * 分享Content
     */
    private ShareContent shareContent;

    private static FacebookManager mInstance;

    public static FacebookManager getInstance() {
        if (mInstance == null) {
            synchronized (FacebookManager.class) {
                if (mInstance == null) {
                    mInstance = new FacebookManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 检测 facebook 登录状态并登录
     *
     * @return
     */
    public boolean checkFacebookAccessToken(boolean isBind) {
        this.mIsBindAccount = isBind;
        try {
            AccessToken accessToken = AccessToken.getCurrentAccessToken();
            boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
            Log.d(OSDKUtils.TAG, "facebook already login " + isLoggedIn);
            if (isLoggedIn) {
                setLoginMap(new HashSet<String>(), new HashSet<String>(), accessToken);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(OSDKUtils.TAG, "checkFacebookAccessToken() Exception: " + e.getMessage());
        }
        return false;
    }

    /**
     * 初始化 facebook
     *
     * @param activity
     */
    public void initFacebookLogin(Activity activity) {
        this.mActivity = activity;
        mCallbackManager = CallbackManager.Factory.create();
        mLoginManager = LoginManager.getInstance();
        mLoginManager.registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        setLoginMap(loginResult.getRecentlyGrantedPermissions(), loginResult.getRecentlyDeniedPermissions(), loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Log.d(OSDKUtils.TAG, "facebook onCancel");
                        if (mIsBindAccount) {
                            SOToastUtil.showShort(SOCommonUtil.getRes4LocaleStr(mActivity, "o_facebook_bind_onCancel"));
                        } else {
                            SOToastUtil.showShort(SOCommonUtil.getRes4LocaleStr(mActivity, "o_facebook_onCancel"));
                        }
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Log.d(OSDKUtils.TAG, "facebook onError " + exception.toString());
                        if (mIsBindAccount) {
                            SOToastUtil.showShort(SOCommonUtil.getRes4LocaleStr(mActivity, "o_facebook_bind_onError"));
                        } else {
                            OSDK.getInstance().onThirdLoginFailure(OLoginTypeUtils.FACEBOOK, SOCommonUtil.getRes4LocaleStr(mActivity, "o_facebook_onError") + ": " + exception.getMessage());
                        }
                    }
                });

        mShareDialog = new ShareDialog(activity);
        mShareDialog.registerCallback(mCallbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                // 分享成功回调
                OSDK.getInstance().onShareSuccess(OSharePlatform.FACEBOOK);
            }

            @Override
            public void onCancel() {
                OSDK.getInstance().onShareCancel(OSharePlatform.FACEBOOK);
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(OSDKUtils.TAG, "facebook share onError" + error.toString());
                OSDK.getInstance().onShareFailure(OSharePlatform.FACEBOOK, error.getMessage());
            }
        });

    }

    /**
     * login 传参
     *
     * @param recentlyGrantedPermissions
     * @param recentlyDeniedPermissions
     * @param accessToken
     */
    private void setLoginMap(final Set<String> recentlyGrantedPermissions, final Set<String> recentlyDeniedPermissions, final AccessToken accessToken) {
        H5Utils.showProgress(mActivity,false,0);
        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                OSDK.getInstance().mLoginMap = new HashMap<>();
                com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
                jsonObject.put("userId", accessToken.getUserId());
                jsonObject.put("applicationId", accessToken.getApplicationId());
                jsonObject.put("graphDomain", accessToken.getUserId());
                jsonObject.put("expires", accessToken.getExpires());
                jsonObject.put("lastRefresh", accessToken.getLastRefresh());
                jsonObject.put("source", accessToken.getSource());
                jsonObject.put("permissions", accessToken.getPermissions());
                jsonObject.put("dataAccessExpirationTime", accessToken.getDataAccessExpirationTime());
                jsonObject.put("declinedPermissions", accessToken.getDeclinedPermissions());
                jsonObject.put("expiredPermissions", accessToken.getExpiredPermissions());
                jsonObject.put("accesstoken", accessToken.getToken());
                jsonObject.put("recentlyDeniedPermissions", recentlyDeniedPermissions);
                jsonObject.put("recentlyGrantedPermissions", recentlyGrantedPermissions);
                Log.d(OSDKUtils.TAG, "onCompleted" + object);
                if (object != null) {
                    Iterator<String> keys = object.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        jsonObject.put(key, object.optString(key));
                    }
                    OSDK.getInstance().mLoginMap.put("third_email", object.optString("email"));
                }

                OSDK.getInstance().mLoginMap.put("third_openid", accessToken.getUserId());
                OSDK.getInstance().mLoginMap.put("third_type", OLoginTypeUtils.FACEBOOK);
                OSDK.getInstance().mLoginMap.put("third_ext", jsonObject.toString());
                if (mIsBindAccount) {
                    OSDK.getInstance().onBindThirdAccount(OSDK.getInstance().mLoginMap);
                } else {
                    OSDK.getInstance().onThirdLoginSuccess(mActivity,OSDK.getInstance().mLoginMap, H5Config.TYPE_OSDK);
                }
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link,gender,birthday,email,picture,locale,updated_time,timezone,age_range,first_name,last_name");
        request.setParameters(parameters);
        request.executeAsync();
    }

    /**
     * facebook login
     */
    public void facebookLogin(boolean isBind) {
        mIsBindAccount = isBind;
        try {
            mLoginManager.logInWithReadPermissions(mActivity, Arrays.asList("public_profile", "email"));
        } catch (Exception e) {
            Log.e("facebook login ERROR", e.toString());
            if (mIsBindAccount) {
                SOToastUtil.showShort(SOCommonUtil.getRes4LocaleStr(mActivity, "o_facebook_bind_onError"));
            } else {
                SOToastUtil.showShort(SOCommonUtil.getRes4LocaleStr(mActivity, "o_facebook_login_exception") + ": " + e.getMessage());
            }
        }
    }

    /**
     * facebook 退出
     */
    public void faceBookLogOut() throws Exception {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null && !accessToken.isExpired()) {
            Log.d(OSDKUtils.TAG, "faceBookSingOut");
            mLoginManager.logOut();
        }
    }

    /**
     * facebook 分享
     */
    public void shareFacebook(List<Bitmap> photoUrlList, List<String> localVideoUrlList,
                              String linkUrl, String text, String tag, String type) throws Exception {
        String facebookPackageName = "com.facebook.katana";
        if (!type.equals(OShareType.LINK)) {
            if (!OSDKUtils.getInstance().checkApkExist(mActivity, facebookPackageName)) {
                OSDK.getInstance().onShareFailure(OSharePlatform.FACEBOOK, SOCommonUtil.getRes4LocaleStr(mActivity, "o_facebook_no_install"));
                return;
            }
        }
        switch (type) {
            case OShareType.IMAGE:
                if (photoUrlList.size() > 6) {
                    OSDK.getInstance().onShareFailure(OSharePlatform.FACEBOOK, SOCommonUtil.getRes4LocaleStr(mActivity, "o_facebook_share_size_too_long"));
                    return;
                }
                List<SharePhoto> photos = new ArrayList<>();
                for (Bitmap bitmap : photoUrlList) {
                    SharePhoto photo = new SharePhoto.Builder()
                            .setBitmap(bitmap)
                            .build();
                    photos.add(photo);
                }
                shareContent = new SharePhotoContent.Builder()
                        .addPhotos(photos)
                        .setShareHashtag(new ShareHashtag.Builder()
                                .setHashtag(text)
                                .build())
                        .build();
                break;
            case OShareType.VIDEO:
                if (localVideoUrlList.size() > 0 && !TextUtils.isEmpty(localVideoUrlList.get(0))) {
                    File file = new File(localVideoUrlList.get(0));
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    ShareVideo video = new ShareVideo.Builder()
                            .setLocalUrl(Uri.fromFile(file))
                            .build();
                    shareContent = new ShareVideoContent.Builder()
                            .setVideo(video)
                            .setShareHashtag(new ShareHashtag.Builder()
                                    .setHashtag(text)
                                    .build())
                            .build();
                } else {
                    SOToastUtil.showShort(SOCommonUtil.getRes4LocaleStr(mActivity, "o_facebook_share_video_null"));
                    return;
                }
                break;
            case OShareType.LINK:
                shareContent = new ShareLinkContent.Builder()
                        .setQuote(tag)
                        .setContentUrl(Uri.parse(linkUrl))
                        .setShareHashtag(new ShareHashtag.Builder()
                                .setHashtag(text)
                                .build()).build();
                break;
        }
        if (mShareDialog.canShow(shareContent)) {
            mShareDialog.show(shareContent);
        } else {
            showNoCanShowShareHintDialog();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * facebook NoCanShow 提示框
     */
    private void showNoCanShowShareHintDialog() {
        if (mH5Dialog == null) {
            mH5Dialog = new OH5Dialog.Builder(mActivity)
                    .setTitle(SOCommonUtil.getRes4LocaleStr(mActivity, "o_prompt"))
                    .setMessage(SOCommonUtil.getRes4LocaleStr(mActivity, "o_facebook_no_login_for_share"))
                    .setConfirm(SOCommonUtil.getRes4LocaleStr(mActivity, "o_understood"))
                    .setCanceable(false)
                    .setPositiveButton(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).create();
        }
        if (mH5Dialog != null) {
            mH5Dialog.show();
        }
    }

    /**
     * 释放
     */
    public void facebookDestroy() {
        Log.d(OSDKUtils.TAG, "facebookDestroy()");
        mLoginManager = null;
        mCallbackManager = null;
        mShareDialog = null;
        if (mH5Dialog != null) {
            if (mH5Dialog.isShowing()) {
                mH5Dialog.dismiss();
            }
            mH5Dialog = null;
        }
    }
}
