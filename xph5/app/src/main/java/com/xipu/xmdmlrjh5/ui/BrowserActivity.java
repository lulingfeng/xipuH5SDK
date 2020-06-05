package com.xipu.xmdmlrjh5.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.bytedance.applog.GameReportHelper;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdDislike;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTFullScreenVideoAd;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;
import com.bytedance.sdk.openadsdk.TTRewardVideoAd;
import com.qq.gdt.action.ActionType;
import com.qq.gdt.action.GDTAction;
import com.startobj.util.device.SODensityUtil;
import com.startobj.util.http.SORequestParams;
import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.sdk.CookieManager;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.xipu.xmdmlrjh5.R;
import com.xipu.xmdmlrjh5.config.H5Config;
import com.xipu.xmdmlrjh5.config.TTAdManagerHolder;
import com.xipu.xmdmlrjh5.util.AdConfig;
import com.xipu.xmdmlrjh5.util.H5Utils;
import com.xipu.xmdmlrjh5.util.KeyBoardListener;
import com.xipu.xmdmlrjh5.util.ParamUtil;
import com.xipu.xmdmlrjh5.util.TTAdUtils;
import com.xipu.xmdmlrjh5.util.ZYJSONObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;

@SuppressLint("NewApi")
public class BrowserActivity extends Activity {
    private final static String SP_JRTT = "sp_jrtt";
    private final static String SP_PAYINFO = "payinfo_openid";
    protected String TAG = "MainActivity";
    private WebView mWebView;
    private FrameLayout mExpressContainer;
    private FrameLayout mRoot;
    private String mUrl;
    private Handler mZfbHandler;
    private Handler mAliHandler;
    private Handler mWxHandler;
    private ValueCallback<Uri> uploadFile;
    private String mOpenID;
    private String mOutTradeNo;

    private TTAdNative mTTAdNative;
    private TTNativeExpressAd mTTAd; // 个性化 banner
    private TTRewardVideoAd mTTRewardVideoAd; // 激励视频
    private AdSlot mAdSlot;
    /*
     * 加载 Banner 广告
     */
    private long startTime;
    private boolean mHasShowDownloadActive = false;
    /*
     * 加载广告容错count
     */
    private int mLoadCount;

    private static final int CLOSE_BANNER = 0x1;
    private static final int SHOW_REWARD_VIDEO = 0x2;
    private static final int SHOW_FULLSCREEN_VIDEO = 0x3;
    private static final int SHOW_INTERACTION_VIDEO = 0x4;
    private AdConfig adConfig;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        initHandler();
        tuiaActivate();
        H5Utils.hideBottomUIMenu(this);
        mUrl = generateUrl();
        mRoot = findViewById(R.id.root);
        mWebView = findViewById(R.id.main_wv);
        mExpressContainer = (FrameLayout) findViewById(R.id.express_container);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);

        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        // 设置UserAgent
        String ua = mWebView.getSettings().getUserAgentString();
        H5Utils.setWebViewUA(ua);

        mWebView.getSettings().setUserAgentString(ua + "; KuaiGames-" + H5Config.SDK_VERSION);
        //自动播放音乐
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        }

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Uri uri = Uri.parse(url);
                if (url.startsWith("http") || url.startsWith("https")) {
                    if (uri.getPath().contains("h5pay/return_game"))
                        return true;
                    if (url.endsWith(".apk")) {
                        Intent intent = new Intent();
                        intent.setAction("android.intent.action.VIEW");
                        Uri content_url = Uri.parse(url);
                        intent.setData(content_url);
                        startActivity(intent);
                        return true;
                    }
                    return super.shouldOverrideUrlLoading(view, url);
                } else if (uri.getScheme().equals(H5Config.AGREEMENT_SCHEME)) {
                    if (uri.getHost().equals(H5Config.AGREEMENT_REFRESH_WINDOW)) {
                        view.loadUrl(url);
                    }
                    return true;
                } else {
                    try {
                        Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        intent.addCategory("android.intent.category.BROWSABLE");
                        intent.setComponent(null);
                        intent.setSelector(null);
                        startActivityIfNeeded(intent, -1);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                        Toast toast = null;
                        if (url.startsWith("alipay"))
                            toast = Toast.makeText(BrowserActivity.this, R.string.ask_alipay_install,
                                    Toast.LENGTH_LONG);
                        else if (url.startsWith("weixin"))
                            toast = Toast.makeText(BrowserActivity.this, R.string.ask_wechat_install,
                                    Toast.LENGTH_LONG);
                        if (toast != null) {
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                H5Utils.cancelProgress();
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String captureType) {

                BrowserActivity.this.uploadFile = uploadFile;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                startActivityForResult(Intent.createChooser(i, "test"), 0);
            }
        });

        H5Utils.showProgress(BrowserActivity.this);
        mWebView.addJavascriptInterface(new JsInterface(), "zyh5sdk");
        mWebView.loadUrl(mUrl);
        KeyBoardListener.getInstance(this, mWebView, new KeyBoardListener.OnChangeHeightListener() {
            @Override
            public void onShow(int usableHeightNow) {
            }

            @Override
            public void onHidden() {
            }
        }).init();
    }

    private void initHandler() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CLOSE_BANNER:
                        if (mExpressContainer != null) {
                            mExpressContainer.removeAllViews();
                        }
                        break;
                    case SHOW_REWARD_VIDEO:
                        mTTRewardVideoAd.showRewardVideoAd(BrowserActivity.this, TTAdConstant.RitScenes.CUSTOMIZE_SCENES, "scenes_test");
                        mTTRewardVideoAd = null;
                        break;
                    case SHOW_FULLSCREEN_VIDEO:
                        TTFullScreenVideoAd ttFullScreenVideoAd = (TTFullScreenVideoAd) msg.obj;
                        ttFullScreenVideoAd.showFullScreenVideoAd(BrowserActivity.this);
                        break;
                    case SHOW_INTERACTION_VIDEO:
                        mTTAd.showInteractionExpressAd(BrowserActivity.this);
                        break;
                }

            }
        };
    }


    private void tuiaActivate() {
        if (H5Utils.getIsFirst(this)) {
            H5Utils.setIsFirst(this);
            if (ParamUtil.isIsUseTuia()) {
                // 推啊 安装上报
                ClipboardManager myClipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = myClipboard.getPrimaryClip();
                if (clipData != null && clipData.getItemCount() > 0) {
                    // 从数据集中获取（粘贴）第一条文本数据
                    String text = clipData.getItemAt(0).getText().toString();
                    String tuiaID = "";
                    if (text.contains("tuia=")) {
                        tuiaID = text.replace("tuia=", "");
                        ParamUtil.setTuiaID(this, tuiaID);
                    }
                }
                H5Utils.tuiaApi(this, "2");
            }
        }
    }

    /**
     * 产生Url
     *
     * @return
     */
    private String generateUrl() {
        StringBuffer sb = new StringBuffer(H5Config.GAME_URL);
        //    StringBuffer sb = new StringBuffer("http://testh5.xipu.com/play.php");
        //  StringBuffer sb = new StringBuffer("http://h5.xipu.com/play.php");
        sb.append("?app_id=" + ParamUtil.getAppId() + "&");
        SORequestParams params = new SORequestParams(H5Config.GAME_URL, H5Utils.getCommonParams(this));
        sb.append(params.getParamsStr());
        Log.d(H5Utils.TAG, "generateUrl: " + sb.toString());
        return sb.toString();
    }

    /**
     * 退出询问框
     */
    private void confirmExit() {
        AlertDialog.Builder localBuilder = new AlertDialog.Builder(this);
        localBuilder.setTitle("提示");
        localBuilder.setMessage("是否退出游戏?");
        localBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
                BrowserActivity.this.finish();
                System.exit(0);
            }
        });
        localBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
            }
        });
        localBuilder.show();
    }

    @Override
    public void onBackPressed() {
        confirmExit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 0:
                    if (null != uploadFile) {
                        Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
                        uploadFile.onReceiveValue(result);
                        uploadFile = null;
                    }
                    break;
                case 1:
                    Uri uri = data.getData();
                    String path = uri.getPath();
                    break;
                default:
                    break;
            }
        } else if (resultCode == RESULT_CANCELED) {
            if (null != uploadFile) {
                uploadFile.onReceiveValue(null);
                uploadFile = null;
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            mWebView.destroy();
        }
        if (mTTAd != null) {
            mTTAd.destroy();
        }
        if (mExpressContainer != null) {
            mExpressContainer.removeAllViews();
            mExpressContainer = null;
        }
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void loadBannerAd(AdConfig adConfig) {
        this.adConfig = adConfig;
        mLoadCount = 1;
        mAdSlot = new AdSlot.Builder()
                .setCodeId(adConfig.getAd_id())
                .setExpressViewAcceptedSize(SODensityUtil.px2dip(BrowserActivity.this, adConfig.getWidth()), SODensityUtil.px2dip(BrowserActivity.this, adConfig.getHeight()))
                .setSupportDeepLink(true)
                .setAdCount(adConfig.getCount())
                .build();
        loadBannerAdInfo();
    }

    private void loadBannerAdInfo() {
        mTTAdNative.loadBannerExpressAd(mAdSlot, new TTAdNative.NativeExpressAdListener() {
            @Override
            public void onError(int code, String message) {
                Log.d(H5Utils.TAG, "onError: " + mAdSlot.toString());
                if (mLoadCount >= 3) {
                    onTTCallback(setTTCallBackParams("bannerLoadError", code, message, null, null, null, null, null));
                    mExpressContainer.removeAllViews();
                } else {
                    ++mLoadCount;
                    loadBannerAdInfo();
                }
            }

            @Override
            public void onNativeExpressAdLoad(List<TTNativeExpressAd> ads) {
                if (ads == null || ads.size() == 0) {
                    onTTCallback(setTTCallBackParams("bannerLoadError", null, "广告数量为0", null, null, null, null, null));
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
                onTTCallback(setTTCallBackParams("bannerClick", null, null, type, null, null, null, null));
            }

            @Override
            public void onAdShow(View view, int type) {
                Log.d(H5Utils.TAG, "onAdShow()");
                onTTCallback(setTTCallBackParams("bannerShow", null, null, type, null, null, null, null));
            }

            @Override
            public void onRenderFail(View view, String msg, int code) {
                Log.d(H5Utils.TAG, "render fail:" + (System.currentTimeMillis() - startTime));
                onTTCallback(setTTCallBackParams("bannerLoadError", code, msg, null, null, null, null, null));
            }

            @Override
            public void onRenderSuccess(View view, float width, float height) {
                Log.d(H5Utils.TAG, "render suc:" + (System.currentTimeMillis() - startTime));
                onTTCallback(setTTCallBackParams("bannerLoadSuccess", null, null, null, width, height, null, null));
                mExpressContainer.removeAllViews();
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(adConfig.getLeft(), adConfig.getTop(), 0, 0);
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
                mHasShowDownloadActive = false;
                Log.d(H5Utils.TAG, "onIdle()");
            }

            @Override
            public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                if (!mHasShowDownloadActive) {
                    mHasShowDownloadActive = true;
                    Log.d(H5Utils.TAG, "onDownloadActive()");
                }
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
        ad.setDislikeCallback(BrowserActivity.this, new TTAdDislike.DislikeInteractionCallback() {
            @Override
            public void onSelected(int position, String value) {
                Log.d(H5Utils.TAG, "点击 " + value);
                //用户选择不喜欢原因后，移除广告展示
                onTTCallback(setTTCallBackParams("bannerDislike", null, null, null, null, null, value, null));
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
    private void loadInteractionAd(AdConfig adConfig) {
        mLoadCount = 1;
        mAdSlot = new AdSlot.Builder()
                .setCodeId(adConfig.getAd_id()) //广告位id
                .setSupportDeepLink(true)
                .setAdCount(adConfig.getCount()) //请求广告数量为1到3条
                .setExpressViewAcceptedSize(SODensityUtil.px2dip(BrowserActivity.this, adConfig.getWidth()), SODensityUtil.px2dip(BrowserActivity.this, adConfig.getHeight()))
                .build();
        loadInteractionAdInfo();
    }

    private void loadInteractionAdInfo() {
        mTTAdNative.loadInteractionExpressAd(mAdSlot, new TTAdNative.NativeExpressAdListener() {
            @Override
            public void onError(int code, String message) {
                Log.d(H5Utils.TAG, "onError : " + code + "," + message);
                if (mLoadCount >= 3) {
                    onTTCallback(setTTCallBackParams("interactionLoadError", code, message, null, null, null, null, null));
                } else {
                    ++mLoadCount;
                    loadInteractionAdInfo();
                }
            }

            @Override
            public void onNativeExpressAdLoad(List<TTNativeExpressAd> ads) {
                if (ads == null || ads.size() == 0) {
                    onTTCallback(setTTCallBackParams("interactionLoadError", null, "插屏广告为空", null, null, null, null, null));
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
                onTTCallback(setTTCallBackParams("interactionClose", null, null, null, null, null, null, null));
            }

            @Override
            public void onAdClicked(View view, int type) {
                onTTCallback(setTTCallBackParams("interactionClick", null, null, type, null, null, null, null));
            }

            @Override
            public void onAdShow(View view, int type) {
                onTTCallback(setTTCallBackParams("interactionShow", null, null, type, null, null, null, null));
            }

            @Override
            public void onRenderFail(View view, String msg, int code) {
                Log.e("ExpressView", "render fail:" + (System.currentTimeMillis() - startTime));
                onTTCallback(setTTCallBackParams("interactionLoadError", code, msg, null, null, null, null, null));
            }

            @Override
            public void onRenderSuccess(View view, float width, float height) {
                Log.e("ExpressView", "render suc:" + (System.currentTimeMillis() - startTime));
                //返回view的宽高 单位 dp
                onTTCallback(setTTCallBackParams("interactionLoadSuccess", null, null, null, width, height, null, null));
                handler.sendEmptyMessage(SHOW_INTERACTION_VIDEO);
            }
        });

        if (ad.getInteractionType() != TTAdConstant.INTERACTION_TYPE_DOWNLOAD) {
            return;
        }
        ad.setDownloadListener(new TTAppDownloadListener() {
            @Override
            public void onIdle() {
                mHasShowDownloadActive = false;
                Log.d(H5Utils.TAG, "onIdle()");
            }

            @Override
            public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                if (!mHasShowDownloadActive) {
                    mHasShowDownloadActive = true;
                    Log.d(H5Utils.TAG, "onDownloadActive()");
                }
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
    private void loadRewardAd(AdConfig adConfig) {
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
        loadRewardAdInfo();
    }

    private void loadRewardAdInfo() {
        mTTAdNative.loadRewardVideoAd(mAdSlot, new TTAdNative.RewardVideoAdListener() {
            @Override
            public void onError(int code, String message) {
                Log.d(H5Utils.TAG, "onError" + code + message);
                if (mLoadCount >= 3) {
                    onTTCallback(setTTCallBackParams("rewardLoadError", code, message, null, null, null, null, null));
                } else {
                    ++mLoadCount;
                    loadRewardAdInfo();
                }
            }

            //视频广告加载后，视频资源缓存到本地的回调，在此回调后，播放本地视频，流畅不阻塞。
            @Override
            public void onRewardVideoCached() {
                Log.d(H5Utils.TAG, "onRewardVideoCached()");
                onTTCallback(setTTCallBackParams("rewardCache", null, null, null, null, null, null, null));
            }

            //视频广告的素材加载完毕，比如视频url等，在此回调后，可以播放在线视频，网络不好可能出现加载缓冲，影响体验。
            @Override
            public void onRewardVideoAdLoad(TTRewardVideoAd ad) {
                Log.d(H5Utils.TAG, "onRewardVideoAdLoad()");
                onTTCallback(setTTCallBackParams("rewardLoadSuccess", null, null, null, null, null, null, null));
                mTTRewardVideoAd = ad;
                mTTRewardVideoAd.setRewardAdInteractionListener(new TTRewardVideoAd.RewardAdInteractionListener() {

                    @Override
                    public void onAdShow() {
                        Log.d(H5Utils.TAG, "onAdShow()");
                        onTTCallback(setTTCallBackParams("rewardShow", null, null, null, null, null, null, null));
                    }

                    @Override
                    public void onAdVideoBarClick() {
                        Log.d(H5Utils.TAG, "onAdVideoBarClick()");
                        onTTCallback(setTTCallBackParams("rewardBarClick", null, null, null, null, null, null, null));
                    }

                    @Override
                    public void onAdClose() {
                        Log.d(H5Utils.TAG, "onAdClose()");
                        onTTCallback(setTTCallBackParams("rewardClose", null, null, null, null, null, null, null));
                    }

                    //视频播放完成回调
                    @Override
                    public void onVideoComplete() {
                        Log.d(H5Utils.TAG, "onVideoComplete()");
                        onTTCallback(setTTCallBackParams("rewardPlayEnd", null, null, null, null, null, null, null));
                    }

                    @Override
                    public void onVideoError() {
                        Log.d(H5Utils.TAG, "onVideoError()");
                    }

                    //视频播放完成后，奖励验证回调，rewardVerify：是否有效，rewardAmount：奖励梳理，rewardName：奖励名称
                    @Override
                    public void onRewardVerify(boolean rewardVerify, int rewardAmount, String rewardName) {
                        Log.d(H5Utils.TAG, "onRewardVerify()" + rewardVerify + "/" + rewardAmount + "/" + rewardName);
                        onTTCallback(setTTCallBackParams("rewardVerify", null, null, null, null, null, null, rewardVerify));
                    }

                    @Override
                    public void onSkippedVideo() {
                        Log.d(H5Utils.TAG, "onSkippedVideo()");
                        onTTCallback(setTTCallBackParams("rewardSkip", null, null, null, null, null, null, null));
                    }
                });
                mTTRewardVideoAd.setDownloadListener(new TTAppDownloadListener() {
                    @Override
                    public void onIdle() {
                        Log.d(H5Utils.TAG, "onIdle()");
                        mHasShowDownloadActive = false;
                    }

                    @Override
                    public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                        Log.d(H5Utils.TAG, "onDownloadActive==totalBytes=" + totalBytes + ",currBytes=" + currBytes + ",fileName=" + fileName + ",appName=" + appName);
                        if (!mHasShowDownloadActive) {
                            mHasShowDownloadActive = true;
                            Log.d(H5Utils.TAG, "下载中，点击下载区域暂停");
                        }
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
                handler.sendEmptyMessage(SHOW_REWARD_VIDEO);
            }
        });
    }

    /*
     * 加载 全屏广告
     */
    private void loadFullScreenVideoAd(AdConfig adConfig) {
        mLoadCount = 1;
        mAdSlot = new AdSlot.Builder()
                .setCodeId(adConfig.getAd_id())
                .setSupportDeepLink(true)
                .setOrientation(adConfig.getOrientation())//必填参数，期望视频的播放方向：TTAdConstant.HORIZONTAL 或 TTAdConstant.VERTICAL
                .build();
        loadFullScreenVideoAdInfo();
    }

    private void loadFullScreenVideoAdInfo() {
        mTTAdNative.loadFullScreenVideoAd(mAdSlot, new TTAdNative.FullScreenVideoAdListener() {
            @Override
            public void onError(int code, String message) {
                Log.d(H5Utils.TAG, "onError" + code + message);
                if (mLoadCount >= 3) {
                    onTTCallback(setTTCallBackParams("fullScreenLoadError", code, message, null, null, null, null, null));
                } else {
                    ++mLoadCount;
                    loadFullScreenVideoAdInfo();
                }
            }

            @Override
            public void onFullScreenVideoCached() {
                Log.d(H5Utils.TAG, "onFullScreenVideoCached()");
                onTTCallback(setTTCallBackParams("fullScreenCache", null, null, null, null, null, null, null));
            }

            @Override
            public void onFullScreenVideoAdLoad(TTFullScreenVideoAd ttFullScreenVideoAd) {
                Log.d(H5Utils.TAG, "onFullScreenVideoAdLoad()");
                ttFullScreenVideoAd.setFullScreenVideoAdInteractionListener(new TTFullScreenVideoAd.FullScreenVideoAdInteractionListener() {
                    @Override
                    public void onAdShow() {
                        Log.d(H5Utils.TAG, "onAdShow()");
                        onTTCallback(setTTCallBackParams("fullScreenShow", null, null, null, null, null, null, null));
                    }

                    @Override
                    public void onAdVideoBarClick() {
                        Log.d(H5Utils.TAG, "onAdVideoBarClick()");
                        onTTCallback(setTTCallBackParams("fullScreenBarClick", null, null, null, null, null, null, null));
                    }

                    @Override
                    public void onAdClose() {
                        Log.d(H5Utils.TAG, "onAdClose()");
                        onTTCallback(setTTCallBackParams("fullScreenClose", null, null, null, null, null, null, null));
                    }

                    @Override
                    public void onVideoComplete() {
                        Log.d(H5Utils.TAG, "onVideoComplete()");
                        onTTCallback(setTTCallBackParams("fullScreenPlayEnd", null, null, null, null, null, null, null));
                    }

                    @Override
                    public void onSkippedVideo() {
                        Log.d(H5Utils.TAG, "onSkippedVideo()");
                        onTTCallback(setTTCallBackParams("fullScreenSkip", null, null, null, null, null, null, null));
                    }
                });

                ttFullScreenVideoAd.setDownloadListener(new TTAppDownloadListener() {
                    @Override
                    public void onIdle() {
                        mHasShowDownloadActive = false;
                        Log.d(H5Utils.TAG, "onIdle()");
                    }

                    @Override
                    public void onDownloadActive(long l, long l1, String s, String s1) {
                        if (!mHasShowDownloadActive) {
                            mHasShowDownloadActive = true;
                            Log.d(H5Utils.TAG, "onDownloadActive()");
                        }
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
                Message message = Message.obtain();
                message.what = SHOW_FULLSCREEN_VIDEO;
                message.obj = ttFullScreenVideoAd;
                handler.sendMessage(message);
            }
        });
    }


    private String setTTCallBackParams(String type, Object errCode, Object errMsg, Object adType, Object width, Object height, Object dislikeValue, Object rewardVerify) {
        Object data = "{\"type\":\"" + type + "\",\"data\":{\"errCode\":" + errCode + ",\"errMsg\":\"" + errMsg + "\",\"adType\":" + adType + ",\"width\":\"" + width + "\",\"height\":\"" + height + "\",\"dislikeValue\":\"" + dislikeValue + "\",\"rewardVerify\":\"" + rewardVerify + "\"}}";
        Log.d(H5Utils.TAG, (String) data);
        return (String) data;
    }

    private String setTTCallBackParams(Object width, Object height) {
        Object data = "{\"width\":" + width + ",\"height\":" + height + "}";
        Log.d(H5Utils.TAG, (String) data);
        return (String) data;
    }

    public void sendJrttPayInfo(boolean is_report, int amount, String out_trade_no) {
        if (ParamUtil.isUseJrtt() && is_report) {
            Log.e(H5Utils.TAG, "jrtt pay");
            GameReportHelper.onEventPurchase("type", "钻石", "1", 1, "xipudemo", "人民币", true, amount / 100);
            HashMap<String, String> map = new HashMap<>();
            map.put("app_id", ParamUtil.getAppId());
            map.put("channel", H5Utils.getChannel());
            map.put("open_id", mOpenID);
            map.put("imei", H5Utils.mDeviceEntity.getImei1());
            map.put("jrtt_appname", ParamUtil.getJrttAppname());
            map.put("jrtt_channel", ParamUtil.getJrttChannel());
            map.put("jrtt_aid", ParamUtil.getJrttAid() + "");
            map.put("out_trade_no", out_trade_no);
            H5Utils.reportJrtt(this, map);
        }
    }


    private void sendJrttUserInfo(boolean is_report, boolean is_newuser, String open_id) {
        mOpenID = open_id;

        if (ParamUtil.isUseJrtt() && is_report && is_newuser) {
            Log.d(H5Utils.TAG, "jrtt register");
            GameReportHelper.onEventRegister("mobile", true); // 注册行为上报
            HashMap<String, String> map = new HashMap<>();
            map.put("app_id", ParamUtil.getAppId());
            map.put("channel", H5Utils.getChannel());
            map.put("open_id", mOpenID);
            map.put("imei", H5Utils.mDeviceEntity.getImei1());
            map.put("jrtt_appname", ParamUtil.getJrttAppname());
            map.put("jrtt_channel", ParamUtil.getJrttChannel());
            map.put("jrtt_aid", ParamUtil.getJrttAid() + "");
            H5Utils.reportJrtt(this, map);
        }
    }

    /**
     * 广点通注册
     */
    private void sendGDTRegister() {
        GDTAction.logAction(ActionType.START_APP);
        GDTAction.logAction(ActionType.REGISTER);
        HashMap<String, String> map = new HashMap<>();
        map.put("app_id", ParamUtil.getAppId());
        map.put("channel", H5Utils.getChannel());
        map.put("open_id", mOpenID);
        map.put("imei", H5Utils.mDeviceEntity.getImei1());
        map.put("androidid", H5Utils.mDeviceEntity.getAndroidID());
        map.put("oaid", H5Utils.getOaid());
        H5Utils.reportGDT(this, map);
        Log.d(H5Utils.TAG, "GDT REGISTER & START_APP");
    }

    /**
     * 广点通 支付上报
     *
     * @param is_ysdk_report
     * @param ysdk_report
     * @param ysdk_report_amount
     */
    public void sendGDTPayInfo(boolean is_ysdk_report, boolean ysdk_report,
                               int ysdk_report_amount) {
        Log.d(H5Utils.TAG, "---is_ysdk_report---" + is_ysdk_report + "--ysdk_report--" + ysdk_report);
        if (is_ysdk_report && ysdk_report) {
            try {
                JSONObject actionParam = new JSONObject();
                actionParam.put("value", ysdk_report_amount);
                GDTAction.logAction(ActionType.PURCHASE, actionParam);
                Log.e(H5Utils.TAG, "GDT PURCHASE " + ysdk_report_amount);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendTuiaPayInfo() {
        if (ParamUtil.isIsUseTuia()) {
            H5Utils.tuiaApi(this, "6");
        }
    }

    // 广告参数回调 H5
    public void onTTCallback(final String json) {
        if (mWebView != null) {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mWebView.evaluateJavascript("XipuSDK.onTTCallback(" + json + ")", new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {
                            //   Log.d(H5Utils.TAG, "onReceiveValue:" + s);
                        }
                    });
                }
            });
        }
    }

    //宽高参数回调 H5
    public void screenSizeCallback(final String json) {
        if (mWebView != null) {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mWebView.evaluateJavascript("XipuSDK.screenSizeCallback(" + json + ")", new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {
                            //  Log.d(H5Utils.TAG, "onReceiveValue:" + s);
                        }
                    });
                }
            });
        }
    }

    private class JsInterface {
        @JavascriptInterface
        public void newAccount(final String value) {
            Log.d(H5Utils.TAG, "newAccount=" + value);
            if (!TextUtils.isEmpty(value)) {
                try {
                    ZYJSONObject dataResult = new ZYJSONObject(value);
                    boolean is_report = dataResult.getInt("is_report") == 1 ? true : false; // 今日头条标识
                    boolean is_ysdk_report = dataResult.getInt("is_ysdk_report") == 1 ? true : false; // 广点通标识
                    boolean ysdk_report = dataResult.getInt("ysdk_report") == 1 ? true : false; // 广点通全局标识
                    boolean is_newuser = dataResult.getInt("is_newuser") == 1 ? true : false; // 新用户标识

                    String open_id = dataResult.getString("open_id");
                    sendJrttUserInfo(is_report, is_newuser, open_id);
                    if (is_newuser && is_ysdk_report && ysdk_report) {
                        sendGDTRegister();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(H5Utils.TAG, "newAccount Exception=" + e.toString());
                }
            }
        }

        @JavascriptInterface
        public void pay(final String value) {
//            value={"out_trade_no":"2019082311244842936"}1
        }

        @JavascriptInterface
        public void paySuccess(final String value) {
            Log.d(H5Utils.TAG, "paySuccess=" + value);
            if (!TextUtils.isEmpty(value)) {
                try {
                    ZYJSONObject dataResult = new ZYJSONObject(value);
                    boolean is_report = dataResult.getInt("is_report") == 1 ? true : false; //今日头条标识
                    int amount = dataResult.getInt("report_amount");
                    boolean is_ysdk_report = dataResult.getInt("is_ysdk_report") == 1 ? true : false; // 广点通0标识
                    int ysdk_report_amount = dataResult.getInt("ysdk_report_amount");
                    boolean ysdk_report = dataResult.getInt("ysdk_report") == 1 ? true : false; // 广点通全局标识
                    String out_trade_no = dataResult.getString("out_trade_no");

                    sendJrttPayInfo(is_report, amount, out_trade_no);
                    sendGDTPayInfo(is_ysdk_report, ysdk_report, ysdk_report_amount);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            sendTuiaPayInfo();
        }

        //初始化
        @JavascriptInterface
        public void TTAdInit(final String values) {
            if (!TextUtils.isEmpty(values)) {
                try {
                    Log.d(H5Utils.TAG, "TTAdInit: " + values);
                    ZYJSONObject dataResult = new ZYJSONObject(values);
                    TTAdManagerHolder.init(getApplicationContext(), dataResult.getStringDef("app_id"));
                    TTAdManager ttAdManager = TTAdManagerHolder.get();
                    TTAdManagerHolder.get().requestPermissionIfNecessary(BrowserActivity.this);
                    mTTAdNative = ttAdManager.createAdNative(BrowserActivity.this);
                } catch (JSONException e) {
                    Log.e(H5Utils.TAG, "TTAdInit:初始化失败 " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        //加载 banner广告
        @JavascriptInterface
        public void openTTBannerAd(final String values) {
            Log.d(H5Utils.TAG, "openTTBannerAd: " + values);
            loadBannerAd(TTAdUtils.getAdParams(values));
        }

        //加载 关闭Banner广告
        @JavascriptInterface
        public void closeTTBannerAd(final String values) {
            Log.d(H5Utils.TAG, "closeTTBannerAd: " + values);
            handler.sendEmptyMessage(CLOSE_BANNER);
        }

        //加载 插屏广告
        @JavascriptInterface
        public void openTTInteractionAd(final String values) {
            Log.d(H5Utils.TAG, "openTTInteractionAd: " + values);
            loadInteractionAd(TTAdUtils.getAdParams(values));
        }

        //加载 激励视频
        @JavascriptInterface
        public void openTTRewardVideoAd(final String values) {
            Log.d(H5Utils.TAG, "openTTRewardVideoAd: " + values);
            loadRewardAd(TTAdUtils.getAdParams(values));
        }

        //加载 全屏广告
        @JavascriptInterface
        public void openTTFullScreenVideoAd(final String values) {
            Log.d(H5Utils.TAG, "openTTFullScreenVideoAd: " + values);
            loadFullScreenVideoAd(TTAdUtils.getAdParams(values));
        }

        @JavascriptInterface
        public void getScreenSize(final String values) {
            Log.d(H5Utils.TAG, "getScreenSize: " + values);
            screenSizeCallback(setTTCallBackParams(SODensityUtil.getScreenWidth(BrowserActivity.this), SODensityUtil.getScreenHeight(BrowserActivity.this)));
        }
    }

}