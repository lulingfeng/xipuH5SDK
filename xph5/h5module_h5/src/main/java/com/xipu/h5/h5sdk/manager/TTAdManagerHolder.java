package com.xipu.h5.h5sdk.manager;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConfig;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdDislike;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTFullScreenVideoAd;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;
import com.bytedance.sdk.openadsdk.TTRewardVideoAd;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebView;
import com.xipu.h5.sdk.model.AdConfig;
import com.xipu.h5.sdk.util.H5Utils;
import com.xipu.h5.sdk.util.TTAdUtils;
import com.xipu.h5.sdk.util.ZYJSONObject;

import org.json.JSONException;

import java.util.List;

/**
 * 可以用一个单例来保存TTAdManager实例，在需要初始化sdk的时候调用
 */
public class TTAdManagerHolder {

    private static boolean sInit;

    private static TTAdManagerHolder instance;

    private static WebView mWebView;

    private FrameLayout mExpressContainer;

    private AdConfig mAdConfig;

    private AdSlot mAdSlot;
    /*
     * 加载广告容错count
     */
    private int mLoadCount;

    private TTNativeExpressAd mTTAd; // 个性化 banner
    private TTRewardVideoAd mTTRewardVideoAd; // 激励视频

    private Handler handler;

    /*
     * 加载 Banner 广告
     */
    private long startTime;

    private Activity mActivity;

    private static final int CLOSE_BANNER = 0x1;
    private static final int SHOW_REWARD_VIDEO = 0x2;
    private static final int SHOW_FULLSCREEN_VIDEO = 0x3;
    private static final int SHOW_INTERACTION_VIDEO = 0x4;

    public static TTAdManagerHolder getInstance() {
        if (instance == null) {
            synchronized (TTAdManagerHolder.class) {
                if (instance == null) {
                    instance = new TTAdManagerHolder();
                }
            }
        }
        return instance;
    }

    public com.bytedance.sdk.openadsdk.TTAdManager get() {
        if (!sInit) {
            throw new RuntimeException("TTAdSdk is not init, please check.");
        }
        return TTAdSdk.getAdManager();
    }

    public void init(Context context, String appid) {
        doInit(context, appid);
    }

    //step1:接入网盟广告sdk的初始化操作，详情见接入文档和穿山甲平台说明
    private void doInit(Context context, String appid) {
        if (!sInit) {
            TTAdSdk.init(context, buildConfig(context, appid));
            sInit = true;
        }
    }

    private TTAdConfig buildConfig(Context context, String appid) {
        return new TTAdConfig.Builder()
                .appId(appid)
                .useTextureView(true) //使用TextureView控件播放视频,默认为SurfaceView,当有SurfaceView冲突的场景，可以使用TextureView
                .appName(TTAdUtils.getInstance().getTTAdAppName(context))
                .titleBarTheme(TTAdConstant.TITLE_BAR_THEME_DARK)
                .allowShowNotify(true) //是否允许sdk展示通知栏提示
                .allowShowPageWhenScreenLock(true) //是否在锁屏场景支持展示广告落地页
                .debug(true) //测试阶段打开，可以通过日志排查问题，上线时去除该调用
                .directDownloadNetworkType(TTAdConstant.NETWORK_STATE_WIFI, TTAdConstant.NETWORK_STATE_3G) //允许直接下载的网络状态集合
                .supportMultiProcess(true)//是否支持多进程
                .needClearTaskReset()
                .build();
    }

    /**
     * 设置 视图
     *
     * @param mWebView
     */
    public void setWebView(WebView mWebView) {
        this.mWebView = mWebView;
    }

    public void setContainer(FrameLayout mFrameLayout) {
        this.mExpressContainer = mFrameLayout;
    }

    public void closeBanner(){
        if (mExpressContainer != null) {
            mExpressContainer.removeAllViews();
        }
    }

    /**
     * 加载banner广告
     *
     * @param activity
     * @param mTTAdNative
     * @param adConfig
     */
    public void loadBannerAd(Activity activity,TTAdNative mTTAdNative, AdConfig adConfig) {
        this.mActivity = activity;
        this.mAdConfig = adConfig;
        mLoadCount = 1;
        mAdSlot = new AdSlot.Builder()
                .setCodeId(adConfig.getAd_id())
                .setExpressViewAcceptedSize(adConfig.getWidth(), adConfig.getHeight())
                .setSupportDeepLink(true)
                .setAdCount(adConfig.getCount())
                .build();
        loadBannerAdInfo(mTTAdNative);
    }

    private void loadBannerAdInfo(final TTAdNative mTTAdNative) {
        mTTAdNative.loadBannerExpressAd(mAdSlot, new TTAdNative.NativeExpressAdListener() {
            @Override
            public void onError(int code, String message) {
                Log.d(H5Utils.TAG, "onError: " + mAdSlot.toString());
                if (mLoadCount >= 3) {
                    onTTCallback(mActivity, setTTCallBackParams("bannerLoadError", code, message, null, null, null, null, null));
                    mExpressContainer.removeAllViews();
                } else {
                    ++mLoadCount;
                    loadBannerAdInfo(mTTAdNative);
                }
            }

            @Override
            public void onNativeExpressAdLoad(List<TTNativeExpressAd> ads) {
                if (ads == null || ads.size() == 0) {
                    onTTCallback(mActivity, setTTCallBackParams("bannerLoadError", null, "广告数量为0", null, null, null, null, null));
                    return;
                }
                mTTAd = ads.get(0);
                mTTAd.setSlideIntervalTime(30 * 1000);
                bindBannerAdListener(ads.get(0));
                startTime = System.currentTimeMillis();
                mTTAd.render();
            }
        });
    }

    private void bindBannerAdListener(final TTNativeExpressAd ad) {
        ad.setExpressInteractionListener(new TTNativeExpressAd.ExpressAdInteractionListener() {
            @Override
            public void onAdClicked(View view, int type) {
                Log.d(H5Utils.TAG, "onAdClicked()");
                onTTCallback(mActivity, setTTCallBackParams("bannerClick", null, null, type, null, null, null, null));
            }

            @Override
            public void onAdShow(View view, int type) {
                Log.d(H5Utils.TAG, "onAdShow()");
                onTTCallback(mActivity, setTTCallBackParams("bannerShow", null, null, type, null, null, null, null));
            }

            @Override
            public void onRenderFail(View view, String msg, int code) {
                Log.d(H5Utils.TAG, "render fail:" + (System.currentTimeMillis() - startTime));
                onTTCallback(mActivity, setTTCallBackParams("bannerLoadError", code, msg, null, null, null, null, null));
            }

            @Override
            public void onRenderSuccess(View view, float width, float height) {
                Log.d(H5Utils.TAG, "render suc:" + (System.currentTimeMillis() - startTime));
                onTTCallback(mActivity, setTTCallBackParams("bannerLoadSuccess", null, null, null, width, height, null, null));
                Log.d(H5Utils.TAG,"mExpressContainer: "+mExpressContainer);
                        mExpressContainer.removeAllViews();
                        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        layoutParams.setMargins(mAdConfig.getLeft(), mAdConfig.getTop(), 0, 0);
                        mExpressContainer.addView(view, layoutParams);
            }
        });
        //dislike设置
        bindDislike(ad);
        if (ad.getInteractionType() != TTAdConstant.INTERACTION_TYPE_DOWNLOAD) {
            return;
        }
        ad.setDownloadListener(new TTAppDownloadListener() {
            @Override
            public void onIdle() {
                Log.d(H5Utils.TAG, "onIdle()");
            }

            @Override
            public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                Log.d(H5Utils.TAG, "onDownloadActive()");
            }

            @Override
            public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
                Log.d(H5Utils.TAG, "onDownloadPaused()");
            }

            @Override
            public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
                Log.d(H5Utils.TAG, "onDownloadFailed()");
            }

            @Override
            public void onInstalled(String fileName, String appName) {
                Log.d(H5Utils.TAG, "onInstalled()");
            }

            @Override
            public void onDownloadFinished(long totalBytes, String fileName, String appName) {
                Log.d(H5Utils.TAG, "onDownloadFinished()");
            }
        });
    }

    /*
     * 设置广告的不喜欢, 注意：强烈建议设置该逻辑，如果不设置dislike处理逻辑，则模板广告中的 dislike区域不响应dislike事件。
     * @param ad
     */
    private void bindDislike(TTNativeExpressAd ad) {
        //使用默认模板中默认dislike弹出样式
        ad.setDislikeCallback(mActivity, new TTAdDislike.DislikeInteractionCallback() {
            @Override
            public void onSelected(int position, String value) {
                Log.d(H5Utils.TAG, "点击 " + value);
                //用户选择不喜欢原因后，移除广告展示
                onTTCallback(mActivity, setTTCallBackParams("bannerDislike", null, null, null, null, null, value, null));
                mExpressContainer.removeAllViews();
            }

            @Override
            public void onCancel() {
                Log.d(H5Utils.TAG, "点击取消");
            }
        });
    }


    /*
     * 加载 插屏 广告
     */
    public void loadInteractionAd(Activity activity, TTAdNative mTTAdNative, AdConfig adConfig) {
        this.mActivity = activity;
        this.mAdConfig = adConfig;
        mLoadCount = 1;
        mAdSlot = new AdSlot.Builder()
                .setCodeId(adConfig.getAd_id()) //广告位id
                .setSupportDeepLink(true)
                .setAdCount(adConfig.getCount()) //请求广告数量为1到3条
                .setExpressViewAcceptedSize(adConfig.getWidth(), adConfig.getHeight()) //期望模板广告view的size,单位dp
                .build();
        loadInteractionAdInfo(mTTAdNative);
    }

    private void loadInteractionAdInfo(final TTAdNative mTTAdNative) {
        mTTAdNative.loadInteractionExpressAd(mAdSlot, new TTAdNative.NativeExpressAdListener() {
            @Override
            public void onError(int code, String message) {
                Log.d(H5Utils.TAG, "onError : " + code + "," + message);
                if (mLoadCount >= 3) {
                    onTTCallback(mActivity, setTTCallBackParams("interactionLoadError", code, message, null, null, null, null, null));
                } else {
                    ++mLoadCount;
                    loadInteractionAdInfo(mTTAdNative);
                }
            }

            @Override
            public void onNativeExpressAdLoad(List<TTNativeExpressAd> ads) {
                if (ads == null || ads.size() == 0) {
                    onTTCallback(mActivity, setTTCallBackParams("interactionLoadError", null, "插屏广告为空", null, null, null, null, null));
                    return;
                }
                mTTAd = ads.get(0);
                bindInteractionAdListener(mTTAd);
                startTime = System.currentTimeMillis();
                mTTAd.render();
            }
        });
    }

    private void bindInteractionAdListener(TTNativeExpressAd ad) {
        ad.setExpressInteractionListener(new TTNativeExpressAd.AdInteractionListener() {
            @Override
            public void onAdDismiss() {
                onTTCallback(mActivity, setTTCallBackParams("interactionClose", null, null, null, null, null, null, null));
            }

            @Override
            public void onAdClicked(View view, int type) {
                onTTCallback(mActivity, setTTCallBackParams("interactionClick", null, null, type, null, null, null, null));
            }

            @Override
            public void onAdShow(View view, int type) {
                onTTCallback(mActivity, setTTCallBackParams("interactionShow", null, null, type, null, null, null, null));
            }

            @Override
            public void onRenderFail(View view, String msg, int code) {
                Log.e("ExpressView", "render fail:" + (System.currentTimeMillis() - startTime));
                onTTCallback(mActivity, setTTCallBackParams("interactionLoadError", code, msg, null, null, null, null, null));
            }

            @Override
            public void onRenderSuccess(View view, float width, float height) {
                Log.e("ExpressView", "render suc:" + (System.currentTimeMillis() - startTime));
                //返回view的宽高 单位 dp
                onTTCallback(mActivity, setTTCallBackParams("interactionLoadSuccess", null, null, null, width, height, null, null));
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTTAd.showInteractionExpressAd(mActivity);
                    }
                });
            }
        });

        if (ad.getInteractionType() != TTAdConstant.INTERACTION_TYPE_DOWNLOAD) {
            return;
        }
        ad.setDownloadListener(new TTAppDownloadListener() {
            @Override
            public void onIdle() {
                Log.d(H5Utils.TAG, "onIdle()");
            }

            @Override
            public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                Log.d(H5Utils.TAG, "onDownloadActive()");
            }

            @Override
            public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
                Log.d(H5Utils.TAG, "onDownloadPaused()");
            }

            @Override
            public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
                Log.d(H5Utils.TAG, "onDownloadFailed()");
            }

            @Override
            public void onInstalled(String fileName, String appName) {
                Log.d(H5Utils.TAG, "onInstalled()");
            }

            @Override
            public void onDownloadFinished(long totalBytes, String fileName, String appName) {
                Log.d(H5Utils.TAG, "onDownloadFinished()");
            }
        });
    }


    /*
     * 加载 激励视频
     */
    public void loadRewardAd(Activity activity, TTAdNative mTTAdNative, AdConfig adConfig) {
        this.mActivity = activity;
        this.mAdConfig = adConfig;
        mLoadCount = 1;
        mAdSlot = new AdSlot.Builder()
                .setCodeId(adConfig.getAd_id())
                .setRewardName(adConfig.getReward_name())
                .setRewardAmount(adConfig.getReward_count())
                .setSupportDeepLink(true)
                .setUserID(adConfig.getUser_id())//用户id,必传参数
                .setMediaExtra("") //附加参数，可选
                .setOrientation(adConfig.getOrientation()) //必填参数，期望视频的播放方向：TTAdConstant.HORIZONTAL 或 TTAdConstant.VERTICAL
                .build();
        loadRewardAdInfo(mTTAdNative);
    }

    private void loadRewardAdInfo(final TTAdNative mTTAdNative) {
        mTTAdNative.loadRewardVideoAd(mAdSlot, new TTAdNative.RewardVideoAdListener() {
            @Override
            public void onError(int code, String message) {
                Log.d(H5Utils.TAG, "onError" + code + message);
                if (mLoadCount >= 3) {
                    onTTCallback(mActivity, setTTCallBackParams("rewardLoadError", code, message, null, null, null, null, null));
                } else {
                    ++mLoadCount;
                    loadRewardAdInfo(mTTAdNative);
                }
            }

            //视频广告加载后，视频资源缓存到本地的回调，在此回调后，播放本地视频，流畅不阻塞。
            @Override
            public void onRewardVideoCached() {
                Log.d(H5Utils.TAG, "onRewardVideoCached()");
                onTTCallback(mActivity, setTTCallBackParams("rewardCache", null, null, null, null, null, null, null));
            }

            //视频广告的素材加载完毕，比如视频url等，在此回调后，可以播放在线视频，网络不好可能出现加载缓冲，影响体验。
            @Override
            public void onRewardVideoAdLoad(TTRewardVideoAd ad) {
                Log.d(H5Utils.TAG, "onRewardVideoAdLoad()");
                onTTCallback(mActivity, setTTCallBackParams("rewardLoadSuccess", null, null, null, null, null, null, null));
                mTTRewardVideoAd = ad;
                mTTRewardVideoAd.setRewardAdInteractionListener(new TTRewardVideoAd.RewardAdInteractionListener() {

                    @Override
                    public void onAdShow() {
                        Log.d(H5Utils.TAG, "onAdShow()");
                        onTTCallback(mActivity, setTTCallBackParams("rewardShow", null, null, null, null, null, null, null));
                    }

                    @Override
                    public void onAdVideoBarClick() {
                        Log.d(H5Utils.TAG, "onAdVideoBarClick()");
                        onTTCallback(mActivity, setTTCallBackParams("rewardBarClick", null, null, null, null, null, null, null));
                    }

                    @Override
                    public void onAdClose() {
                        Log.d(H5Utils.TAG, "onAdClose()");
                        onTTCallback(mActivity, setTTCallBackParams("rewardClose", null, null, null, null, null, null, null));
                    }

                    //视频播放完成回调
                    @Override
                    public void onVideoComplete() {
                        Log.d(H5Utils.TAG, "onVideoComplete()");
                        onTTCallback(mActivity, setTTCallBackParams("rewardPlayEnd", null, null, null, null, null, null, null));
                    }

                    @Override
                    public void onVideoError() {
                        Log.d(H5Utils.TAG, "onVideoError()");
                    }

                    //视频播放完成后，奖励验证回调，rewardVerify：是否有效，rewardAmount：奖励梳理，rewardName：奖励名称
                    @Override
                    public void onRewardVerify(boolean rewardVerify, int rewardAmount, String rewardName) {
                        Log.d(H5Utils.TAG, "onRewardVerify()" + rewardVerify + "/" + rewardAmount + "/" + rewardName);
                        onTTCallback(mActivity, setTTCallBackParams("rewardVerify", null, null, null, null, null, null, rewardVerify));
                    }

                    @Override
                    public void onSkippedVideo() {
                        Log.d(H5Utils.TAG, "onSkippedVideo()");
                        onTTCallback(mActivity, setTTCallBackParams("rewardSkip", null, null, null, null, null, null, null));
                    }
                });
                mTTRewardVideoAd.setDownloadListener(new TTAppDownloadListener() {
                    @Override
                    public void onIdle() {
                        Log.d(H5Utils.TAG, "onIdle()");
                    }

                    @Override
                    public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                        Log.d(H5Utils.TAG, "onDownloadActive==totalBytes=" + totalBytes + ",currBytes=" + currBytes + ",fileName=" + fileName + ",appName=" + appName);
                    }

                    @Override
                    public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
                        Log.d(H5Utils.TAG, "onDownloadPaused===totalBytes=" + totalBytes + ",currBytes=" + currBytes + ",fileName=" + fileName + ",appName=" + appName);
                    }

                    @Override
                    public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
                        Log.d(H5Utils.TAG, "onDownloadFailed==totalBytes=" + totalBytes + ",currBytes=" + currBytes + ",fileName=" + fileName + ",appName=" + appName);
                    }

                    @Override
                    public void onDownloadFinished(long totalBytes, String fileName, String appName) {
                        Log.d(H5Utils.TAG, "onDownloadFinished==totalBytes=" + totalBytes + ",fileName=" + fileName + ",appName=" + appName);
                    }

                    @Override
                    public void onInstalled(String fileName, String appName) {
                        Log.d(H5Utils.TAG, "onInstalled==" + ",fileName=" + fileName + ",appName=" + appName);
                    }
                });
                Log.d(H5Utils.TAG, "handler: " + handler);
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTTRewardVideoAd.showRewardVideoAd(mActivity, TTAdConstant.RitScenes.CUSTOMIZE_SCENES, "scenes_test");
                        mTTRewardVideoAd = null;
                    }
                });
            }
        });
    }


    /*
     * 加载 全屏广告
     */
    public void loadFullScreenVideoAd(Activity activity, TTAdNative mTTAdNative, AdConfig adConfig) {
        this.mActivity = activity;
        this.mAdConfig = adConfig;
        mLoadCount = 1;
        mAdSlot = new AdSlot.Builder()
                .setCodeId(adConfig.getAd_id())
                .setSupportDeepLink(true)
                .setOrientation(adConfig.getOrientation())//必填参数，期望视频的播放方向：TTAdConstant.HORIZONTAL 或 TTAdConstant.VERTICAL
                .build();
        loadFullScreenVideoAdInfo(mTTAdNative);
    }

    private void loadFullScreenVideoAdInfo(final TTAdNative mTTAdNative) {
        mTTAdNative.loadFullScreenVideoAd(mAdSlot, new TTAdNative.FullScreenVideoAdListener() {
            @Override
            public void onError(int code, String message) {
                Log.d(H5Utils.TAG, "onError" + code + message);
                if (mLoadCount >= 3) {
                    onTTCallback(mActivity, setTTCallBackParams("fullScreenLoadError", code, message, null, null, null, null, null));
                } else {
                    ++mLoadCount;
                    loadFullScreenVideoAdInfo(mTTAdNative);
                }
            }

            @Override
            public void onFullScreenVideoCached() {
                Log.d(H5Utils.TAG, "onFullScreenVideoCached()");
                onTTCallback(mActivity, setTTCallBackParams("fullScreenCache", null, null, null, null, null, null, null));
            }

            @Override
            public void onFullScreenVideoAdLoad(final TTFullScreenVideoAd ttFullScreenVideoAd) {
                Log.d(H5Utils.TAG, "onFullScreenVideoAdLoad()");
                ttFullScreenVideoAd.setFullScreenVideoAdInteractionListener(new TTFullScreenVideoAd.FullScreenVideoAdInteractionListener() {
                    @Override
                    public void onAdShow() {
                        Log.d(H5Utils.TAG, "onAdShow()");
                        onTTCallback(mActivity, setTTCallBackParams("fullScreenShow", null, null, null, null, null, null, null));
                    }

                    @Override
                    public void onAdVideoBarClick() {
                        Log.d(H5Utils.TAG, "onAdVideoBarClick()");
                        onTTCallback(mActivity, setTTCallBackParams("fullScreenBarClick", null, null, null, null, null, null, null));
                    }

                    @Override
                    public void onAdClose() {
                        Log.d(H5Utils.TAG, "onAdClose()");
                        onTTCallback(mActivity, setTTCallBackParams("fullScreenClose", null, null, null, null, null, null, null));
                    }

                    @Override
                    public void onVideoComplete() {
                        Log.d(H5Utils.TAG, "onVideoComplete()");
                        onTTCallback(mActivity, setTTCallBackParams("fullScreenPlayEnd", null, null, null, null, null, null, null));
                    }

                    @Override
                    public void onSkippedVideo() {
                        Log.d(H5Utils.TAG, "onSkippedVideo()");
                        onTTCallback(mActivity, setTTCallBackParams("fullScreenSkip", null, null, null, null, null, null, null));
                    }
                });

                ttFullScreenVideoAd.setDownloadListener(new TTAppDownloadListener() {
                    @Override
                    public void onIdle() {
                        Log.d(H5Utils.TAG, "onIdle()");
                    }

                    @Override
                    public void onDownloadActive(long l, long l1, String s, String s1) {
                        Log.d(H5Utils.TAG, "onDownloadActive()");
                    }

                    @Override
                    public void onDownloadPaused(long l, long l1, String s, String s1) {
                        Log.d(H5Utils.TAG, "onDownloadPaused()");
                    }

                    @Override
                    public void onDownloadFailed(long l, long l1, String s, String s1) {
                        Log.d(H5Utils.TAG, "onDownloadFailed()");
                    }

                    @Override
                    public void onDownloadFinished(long l, String s, String s1) {
                        Log.d(H5Utils.TAG, "onDownloadFinished()");
                    }

                    @Override
                    public void onInstalled(String s, String s1) {
                        Log.d(H5Utils.TAG, "onInstalled()");
                    }
                });
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ttFullScreenVideoAd.showFullScreenVideoAd(mActivity);
                    }
                });
            }
        });
    }

    //宽高参数回调 H5
    public void screenSizeCallback(Activity activity,final String json) {
        if (mWebView != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mWebView.evaluateJavascript("XipuSDK.screenSizeCallback(" + json + ")", new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {
                              Log.d(H5Utils.TAG, "screenSizeCallback onReceiveValue:" + s);
                        }
                    });
                }
            });
        }
    }

    public String setTTCallBackParams(Object width, Object height) {
        Object data = "{\"width\":" + width + ",\"height\":" + height + "}";
        Log.d(H5Utils.TAG, (String) data);
        return (String) data;
    }

    private String setTTCallBackParams(String type, Object errCode, Object errMsg, Object adType, Object width, Object height, Object dislikeValue, Object rewardVerify) {
        Object data = "{\"type\":\"" + type + "\",\"data\":{\"errCode\":" + errCode + ",\"errMsg\":\"" + errMsg + "\",\"adType\":" + adType + ",\"width\":\"" + width + "\",\"height\":\"" + height + "\",\"dislikeValue\":\"" + dislikeValue + "\",\"rewardVerify\":\"" + rewardVerify + "\"}}";
        Log.d(H5Utils.TAG, (String) data);
        return (String) data;
    }

    // 广告参数回调 H5
    private void onTTCallback(Activity activity, final String json) {
        if (mWebView != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mWebView.evaluateJavascript("XipuSDK.onTTCallback(" + json + ")", new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {
                               Log.d(H5Utils.TAG, "onTTCallback onReceiveValue:" + s);
                        }
                    });
                }
            });
        }
    }

    public AdConfig getAdParams(final String values) {
        AdConfig adConfig = new AdConfig();
        if (!TextUtils.isEmpty(values)) {
            try {
                ZYJSONObject dataResult = new ZYJSONObject(values);
                adConfig.setAd_id(dataResult.getStringDef("ad_id"));
                adConfig.setCount(dataResult.optInt("count"));
                adConfig.setReward_name(dataResult.getStringDef("reward_name"));
                adConfig.setReward_count(dataResult.optInt("reward_count"));
                adConfig.setUser_id(dataResult.getStringDef("user_id"));
                adConfig.setExtra(dataResult.getStringDef("extra"));
                if (dataResult.getStringDef("orientation").contains("vertical")) {
                    adConfig.setOrientation(TTAdConstant.VERTICAL);
                } else if (dataResult.getStringDef("orientation").contains("horizontal")) {
                    adConfig.setOrientation(TTAdConstant.HORIZONTAL);
                }
                //获取坐标
                if (!TextUtils.isEmpty(dataResult.getStringDef("style"))) {
                    ZYJSONObject styleObject = new ZYJSONObject(dataResult.getStringDef("style"));
                    adConfig.setTop(styleObject.optInt("top"));
                    adConfig.setLeft(styleObject.optInt("left"));
                    adConfig.setWidth(styleObject.optInt("width"));
                    adConfig.setHeight(styleObject.optInt("height"));
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(H5Utils.TAG, "getBannerAdParams: " + e.getMessage());
            }
        }
        return adConfig;
    }

    public void destroy(){
        if (mTTAd != null) {
            mTTAd.destroy();
        }
    }
}
