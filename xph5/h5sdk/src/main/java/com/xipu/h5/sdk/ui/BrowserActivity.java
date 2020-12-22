package com.xipu.h5.sdk.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.startobj.util.common.SOCommonUtil;
import com.startobj.util.http.SORequestParams;
import com.startobj.util.toast.SOToastUtil;
import com.xipu.h5.sdk.H5;
import com.xipu.h5.sdk.callback.OLoginApi;
import com.xipu.h5.sdk.callback.OPayApi;
import com.xipu.h5.sdk.util.ParamUtil;
import com.xipu.h5.sdk.util.ReportTypeUtils;
import com.xipu.h5_sdk.R;
import com.xipu.h5.sdk.config.H5Config;
import com.xipu.h5.sdk.util.H5Utils;
import com.xipu.h5.sdk.util.KeyBoardListener;

import java.net.URISyntaxException;
import java.util.HashMap;

@SuppressLint("NewApi")
public class BrowserActivity extends Activity {

    protected String TAG = "BrowserActivity";
    private WebView mWebView;
    private FrameLayout mExpressContainer;
    private FrameLayout mRoot;

    private String mUrl;
    private ValueCallback<Uri> uploadFile;
    private ValueCallback<Uri[]> uploadFileList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        H5.getInstance().onCreate(this, getIntent());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
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
                            toast = Toast.makeText(BrowserActivity.this, R.string.h5_ask_alipay_install,
                                    Toast.LENGTH_LONG);
                        else if (url.startsWith("weixin"))
                            toast = Toast.makeText(BrowserActivity.this, R.string.h5_ask_wechat_install,
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

            // Android >=5.0 文件上传、5.0以后支持多文件上传
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                BrowserActivity.this.uploadFileList = filePathCallback;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                startActivityForResult(Intent.createChooser(i, "test"), 0);
                return true;
            }

            // Android >=4.1
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

    /**
     * 产生Url
     *
     * @return
     */
    private String generateUrl() {
        StringBuffer sb = new StringBuffer(H5Config.GAME_URL);
        //  StringBuffer sb = new StringBuffer("http://testh5.xipu.com/play.php"); // demo
        //  StringBuffer sb = new StringBuffer("http://h5.xipu.com/play.php");
        sb.append("?app_id=" + ParamUtil.getAppId() + "&");
        //   sb.append("?app_id=38807b0c59747f0cb583c3a00a24a788&");
        SORequestParams params = new SORequestParams(H5Config.GAME_URL, H5Utils.getCommonParams(this));
        sb.append(params.getParamsStr());
        Log.d(H5Utils.TAG, "generateUrl: " + sb.toString());
        return sb.toString();
    }

    @Override
    protected void onStart() {
        super.onStart();
        H5.getInstance().onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        H5.getInstance().onRestart(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        H5.getInstance().onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        H5.getInstance().onPause(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        H5.getInstance().onStop(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        H5.getInstance().onNewIntent(intent);
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            mWebView.destroy();
        }
        if (mExpressContainer != null) {
            mExpressContainer.removeAllViews();
            mExpressContainer = null;
        }
        super.onDestroy();
        H5.getInstance().onDestroy(this);
    }

    @Override
    public void onBackPressed() {
        confirmExit();
    }

    /**
     * 退出询问框
     */
    private void confirmExit() {
        AlertDialog.Builder localBuilder = new AlertDialog.Builder(this);
        localBuilder.setTitle(SOCommonUtil.getRes4LocaleStr(this, "h5_prompt"));
        localBuilder.setMessage(SOCommonUtil.getRes4LocaleStr(this, "h5_is_exit"));
        localBuilder.setPositiveButton(SOCommonUtil.getRes4LocaleStr(this, "h5_confirm"), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
                BrowserActivity.this.finish();
                System.exit(0);
            }
        });
        localBuilder.setNegativeButton(SOCommonUtil.getRes4LocaleStr(this, "h5_cancel"), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
            }
        });
        localBuilder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        H5.getInstance().onActivityResult(requestCode, resultCode, data);
        Uri[] results = null;
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 0:
                    if (null != uploadFile) {
                        Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
                        uploadFile.onReceiveValue(result);
                        uploadFile = null;
                    }
                    if (null != uploadFileList) {
                        if (data != null) {
                            String dataString = data.getDataString();
                            ClipData clipData = data.getClipData();
                            if (clipData != null) {
                                results = new Uri[clipData.getItemCount()];
                                for (int i = 0; i < clipData.getItemCount(); i++) {
                                    ClipData.Item item = clipData.getItemAt(i);
                                    results[i] = item.getUri();
                                }
                            }
                            if (results != null) {
                                results = new Uri[]{Uri.parse(dataString)};
                            }
                        }
                        uploadFileList.onReceiveValue(results);
                        uploadFileList = null;
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

    private class JsInterface {
        @JavascriptInterface
        public void newAccount(final String value) {
            Log.d(H5Utils.TAG, "newAccount=" + value);
            if (!TextUtils.isEmpty(value)) {
                H5.getInstance().onReportJRTT(BrowserActivity.this, ReportTypeUtils.LOGIN, value);
                H5.getInstance().onReportGDT(BrowserActivity.this, ReportTypeUtils.LOGIN, value);
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
                H5.getInstance().onReportJRTT(BrowserActivity.this, ReportTypeUtils.PAY, value);
                H5.getInstance().onReportGDT(BrowserActivity.this, ReportTypeUtils.PAY, value);
            }
            H5.getInstance().onReportTuia(BrowserActivity.this);
        }

        //初始化
        @JavascriptInterface
        public void TTAdInit(final String values) {
            if (!TextUtils.isEmpty(values)) {
                H5.getInstance().onTTAdInit(BrowserActivity.this, mWebView, mExpressContainer, values);
            }
        }

        //加载 banner广告
        @JavascriptInterface
        public void openTTBannerAd(String values) {
            Log.d(H5Utils.TAG, "openTTBannerAd: " + values);
            H5.getInstance().onOpenTTBannerAd(BrowserActivity.this, values);
        }

        //加载 关闭Banner广告
        @JavascriptInterface
        public void closeTTBannerAd(final String values) {
            Log.d(H5Utils.TAG, "closeTTBannerAd: " + values);
            H5.getInstance().onCloseTTBannerAd(values);
        }

        //加载 插屏广告
        @JavascriptInterface
        public void openTTInteractionAd(final String values) {
            Log.d(H5Utils.TAG, "openTTInteractionAd: " + values);
            H5.getInstance().onOpenTTInteractionAd(BrowserActivity.this, values);
        }

        //加载 激励视频
        @JavascriptInterface
        public void openTTRewardVideoAd(final String values) {
            Log.d(H5Utils.TAG, "openTTRewardVideoAd: " + values);
            H5.getInstance().onOpenTTRewardVideoAd(BrowserActivity.this, values);
        }

        //加载 全屏广告
        @JavascriptInterface
        public void openTTFullScreenVideoAd(final String values) {
            Log.d(H5Utils.TAG, "openTTFullScreenVideoAd: " + values);
            H5.getInstance().onOpenTTFullScreenVideoAd(BrowserActivity.this, values);
        }

        // 获取屏幕宽高
        @JavascriptInterface
        public void getScreenSize(final String values) {
            Log.d(H5Utils.TAG, "getScreenSize: " + values);
            H5.getInstance().onGetScreenSize(BrowserActivity.this, values);
        }

        // 海外激活
        @JavascriptInterface
        public void cpInit(String values) {
            Log.d(H5Utils.TAG, "cpInit values: " + values);
            H5.getInstance().onActivate(BrowserActivity.this, values);
        }

        // 海外创角
        @JavascriptInterface
        public void cpCRole(String values) {
            Log.d(H5Utils.TAG, "cpCRole values: " + values);
            H5.getInstance().onCreateRole(BrowserActivity.this, values);
        }

        // 海外角色登录
        @JavascriptInterface
        public void cpLRole(String values) {
            Log.d(H5Utils.TAG, "cpLRole values: " + values);
            H5.getInstance().onLoginRole(BrowserActivity.this, values);
        }

        // 海外角色升级
        @JavascriptInterface
        public void cpURole(String values) {
            Log.d(H5Utils.TAG, "cpURole values: " + values);
            H5.getInstance().onUpdateRole(BrowserActivity.this, values);
        }

        // 海外切换账号
        @JavascriptInterface
        public void cpSwitchAccount(String values) {
            H5.getInstance().onSwitchAccount();
        }

        // 海外登录
        @JavascriptInterface
        public void cpLogin(String values) {
            H5.getInstance().addOnLoginListener(BrowserActivity.this, new OLoginApi() {
                @Override
                public void onSuccess(final HashMap<String, Object> maps) {
                    if (mWebView != null) {
                        BrowserActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mWebView.evaluateJavascript("HWSDK.onLogin(" + JSON.toJSONString(maps) + ")", new ValueCallback<String>() {
                                    @Override
                                    public void onReceiveValue(String s) {
                                        Log.d(H5Utils.TAG, "HWSDK.onLogin onReceiveValue:" + s);
                                    }
                                });
                            }
                        });
                    }
                }
            });
        }

        // 海外支付
        @JavascriptInterface
        public void cpPay(String values) {
            Log.d(H5Utils.TAG, "cpPay values: " + values);
            H5.getInstance().addOnPayListener(BrowserActivity.this, values, new OPayApi() {
                @Override
                public void onPaySuccess() {
                    if (mWebView != null) {
                        BrowserActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mWebView.evaluateJavascript("HWSDK.onPaySuccess()", new ValueCallback<String>() {
                                    @Override
                                    public void onReceiveValue(String s) {
                                        Log.d(H5Utils.TAG, "HWSDK.onPaySuccess onReceiveValue:" + s);
                                    }
                                });
                            }
                        });
                    }
                }

                @Override
                public void onPayCancel() {
                    if (mWebView != null) {
                        BrowserActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mWebView.evaluateJavascript("HWSDK.onPayCancel()", new ValueCallback<String>() {
                                    @Override
                                    public void onReceiveValue(String s) {
                                        Log.d(H5Utils.TAG, "HWSDK.onPayCancel onReceiveValue:" + s);
                                    }
                                });
                            }
                        });
                    }
                }

                @Override
                public void onPayFailure(String msg) {
                    SOToastUtil.showShort(msg);
                }
            });
        }

    }


}