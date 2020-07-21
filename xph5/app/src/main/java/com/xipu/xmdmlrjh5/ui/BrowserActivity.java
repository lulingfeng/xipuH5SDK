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
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.bytedance.applog.GameReportHelper;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.qq.gdt.action.ActionType;
import com.qq.gdt.action.GDTAction;
import com.startobj.util.device.SODensityUtil;
import com.startobj.util.http.SORequestParams;
import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.xipu.h5.osdk.H5OSDK;
import com.xipu.xmdmlrjh5.R;
import com.xipu.xmdmlrjh5.config.H5Config;
import com.xipu.xmdmlrjh5.manager.TTAdManagerHolder;
import com.xipu.xmdmlrjh5.util.H5Utils;
import com.xipu.xmdmlrjh5.util.KeyBoardListener;
import com.xipu.xmdmlrjh5.util.ParamUtil;
import com.xipu.xmdmlrjh5.util.TTAdUtils;
import com.xipu.xmdmlrjh5.util.ZYJSONObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.HashMap;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        H5OSDK.getInstance().onCreate(this, getIntent());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
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
        H5OSDK.getInstance().onActivityResult(requestCode, resultCode, data);
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
    protected void onStart() {
        super.onStart();
        H5OSDK.getInstance().onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        H5OSDK.getInstance().onRestart(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        H5OSDK.getInstance().onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        H5OSDK.getInstance().onPause(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        H5OSDK.getInstance().onStop(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        H5OSDK.getInstance().onNewIntent(intent);
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            mWebView.destroy();
        }
        TTAdManagerHolder.getInstance().destroy();
        if (mExpressContainer != null) {
            mExpressContainer.removeAllViews();
            mExpressContainer = null;
        }
        super.onDestroy();
        H5OSDK.getInstance().onDestroy(this);
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

    private String setTTCallBackParams(Object width, Object height) {
        Object data = "{\"width\":" + width + ",\"height\":" + height + "}";
        Log.d(H5Utils.TAG, (String) data);
        return (String) data;
    }

    private class JsInterface {
        @JavascriptInterface
        public void newAccount(final String value) {
            Log.d(H5Utils.TAG, "newAccount=" + value);
            H5OSDK.getInstance().onLoginSuccess(value);
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
                    TTAdManagerHolder.getInstance().init(getApplicationContext(), dataResult.getStringDef("app_id"));
                    TTAdManager ttAdManager = TTAdManagerHolder.getInstance().get();
                    TTAdManagerHolder.getInstance().get().requestPermissionIfNecessary(BrowserActivity.this);
                    TTAdManagerHolder.getInstance().setWebView(mWebView);
                    TTAdManagerHolder.getInstance().setContainer(mExpressContainer);
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
            TTAdManagerHolder.getInstance().loadBannerAd(BrowserActivity.this, mTTAdNative, TTAdUtils.getInstance().getAdParams(values));
        }

        //加载 关闭Banner广告
        @JavascriptInterface
        public void closeTTBannerAd(final String values) {
            Log.d(H5Utils.TAG, "closeTTBannerAd: " + values);
            TTAdManagerHolder.getInstance().closeBanner();
        }

        //加载 插屏广告
        @JavascriptInterface
        public void openTTInteractionAd(final String values) {
            Log.d(H5Utils.TAG, "openTTInteractionAd: " + values);
            TTAdManagerHolder.getInstance().loadInteractionAd(BrowserActivity.this, mTTAdNative, TTAdUtils.getInstance().getAdParams(values));
        }

        //加载 激励视频
        @JavascriptInterface
        public void openTTRewardVideoAd(final String values) {
            Log.d(H5Utils.TAG, "openTTRewardVideoAd: " + values);
            TTAdManagerHolder.getInstance().loadRewardAd(BrowserActivity.this, mTTAdNative, TTAdUtils.getInstance().getAdParams(values));
        }

        //加载 全屏广告
        @JavascriptInterface
        public void openTTFullScreenVideoAd(final String values) {
            Log.d(H5Utils.TAG, "openTTFullScreenVideoAd: " + values);
            TTAdManagerHolder.getInstance().loadFullScreenVideoAd(BrowserActivity.this, mTTAdNative, TTAdUtils.getInstance().getAdParams(values));
        }

        @JavascriptInterface
        public void getScreenSize(final String values) {
            Log.d(H5Utils.TAG, "getScreenSize: " + values);
            screenSizeCallback(setTTCallBackParams(SODensityUtil.getScreenWidth(BrowserActivity.this), SODensityUtil.getScreenHeight(BrowserActivity.this)));
        }
    }

}