package com.xipu.h5.sdk;


import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.FrameLayout;

import com.tencent.smtt.sdk.WebView;
import com.xipu.h5.sdk.callback.OQuerySkuApi;
import com.xipu.h5.sdk.callback.OLoginApi;
import com.xipu.h5.sdk.callback.OPayApi;
import com.xipu.h5.sdk.config.H5Config;
import com.xipu.h5.sdk.util.H5Utils;

import java.lang.reflect.Method;
import java.util.List;

public class H5 {

    private static H5 instance;

    private static Object thirdSdk = null;

    public static H5 getInstance() {
        if (instance == null) {
            synchronized (H5.class) {
                if (instance == null) {
                    instance = new H5();
                }
            }
        }
        return instance;
    }


    public H5() {
        if (H5Config.TYPE_H5SDK == H5Utils.getSDKType()) {
            thirdSdk = TFactory.getClass(H5Config.H5SDK);
        } else if (H5Config.TYPE_OSDK == H5Utils.getSDKType()) {
            thirdSdk = TFactory.getClass(H5Config.OSDK);
        } else {
            thirdSdk = TFactory.getClass(H5Config.H5SDK);
        }
        Log.d(H5Utils.TAG, "thirdSdk: " + thirdSdk.getClass());
    }

    public void onActivate(Activity activity,String values) {
        Log.d(H5Utils.TAG, "H5 onActivate()");
        if (thirdSdk != null) {
            try {
                Method login = thirdSdk.getClass().getMethod("onActivate", Activity.class,String.class);
                login.invoke(thirdSdk, activity,values);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onCreateRole(Activity activity, String values) {
        Log.d(H5Utils.TAG, "H5 onCreateRole()");
        if (thirdSdk != null) {
            try {
                Method login = thirdSdk.getClass().getMethod("onCreateRole", Activity.class, String.class);
                login.invoke(thirdSdk, activity, values);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onLoginRole(Activity activity, String values) {
        Log.d(H5Utils.TAG, "H5 onLoginRole()");
        if (thirdSdk != null) {
            try {
                Method login = thirdSdk.getClass().getMethod("onLoginRole", Activity.class, String.class);
                login.invoke(thirdSdk, activity, values);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onUpdateRole(Activity activity, String values) {
        Log.d(H5Utils.TAG, "H5 onUpdateRole()");
        if (thirdSdk != null) {
            try {
                Method login = thirdSdk.getClass().getMethod("onUpdateRole", Activity.class, String.class);
                login.invoke(thirdSdk, activity, values);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void addOnLoginListener(Activity activity, OLoginApi oLoginApi) {
        Log.d(H5Utils.TAG, "H5 addOnLoginListener()");
        if (thirdSdk != null) {
            try {
                Method login = thirdSdk.getClass().getMethod("addOnLoginListener", Activity.class, OLoginApi.class);
                login.invoke(thirdSdk, activity, oLoginApi);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void addQuerySkuListDetailsListener(List<String> produceIds, OQuerySkuApi skuApi) {
        Log.d(H5Utils.TAG, "HC addQuerySkuListDetailsListener()");
        if (thirdSdk != null) {
            try {
                Method shareLine = thirdSdk.getClass().getMethod("addQuerySkuListDetailsListener", List.class, OQuerySkuApi.class);
                shareLine.invoke(thirdSdk, produceIds, skuApi);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void addOnPayListener(Activity activity, String values, OPayApi oPayApi) {
        Log.d(H5Utils.TAG, "H5 addOnPayListener()");
        if (thirdSdk != null) {
            try {
                Method onCreate = thirdSdk.getClass().getMethod("addOnPayListener", Activity.class, String.class, OPayApi.class);
                onCreate.invoke(thirdSdk, activity, values, oPayApi);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onSwitchAccount() {
        Log.d(H5Utils.TAG, "H5 switchAccount()");
        if (thirdSdk != null) {
            try {
                Method onCreate = thirdSdk.getClass().getMethod("onSwitchAccount");
                onCreate.invoke(thirdSdk);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onReportJRTT(Activity activity, String reportType, String values) {
        Log.d(H5Utils.TAG, "H5 onReportJRTT()");
        if (thirdSdk != null) {
            try {
                Method onCreate = thirdSdk.getClass().getMethod("onReportJRTT", Activity.class, String.class, String.class);
                onCreate.invoke(thirdSdk, activity, reportType, values);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onReportGDT(Activity activity, String reportType, String values) {
        Log.d(H5Utils.TAG, "H5 onReportGDT()");
        if (thirdSdk != null) {
            try {
                Method onCreate = thirdSdk.getClass().getMethod("onReportGDT", Activity.class, String.class, String.class);
                onCreate.invoke(thirdSdk, activity, reportType, values);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onReportTuia(Activity activity) {
        Log.d(H5Utils.TAG, "H5 onReportTuia()");
        if (thirdSdk != null) {
            try {
                Method onCreate = thirdSdk.getClass().getMethod("onReportTuia", Activity.class);
                onCreate.invoke(thirdSdk, activity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onTTAdInit(Activity activity, WebView mWebView, FrameLayout mExpressContainer, String values) {
        Log.d(H5Utils.TAG, "H5 onTTAdInit()");
        if (thirdSdk != null) {
            try {
                Method onCreate = thirdSdk.getClass().getMethod("onTTAdInit", Activity.class, WebView.class, FrameLayout.class, String.class);
                onCreate.invoke(thirdSdk, activity, mWebView, mExpressContainer, values);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onOpenTTBannerAd(Activity activity, String values) {
        Log.d(H5Utils.TAG, "H5 onOpenTTBannerAd()");
        if (thirdSdk != null) {
            try {
                Method onCreate = thirdSdk.getClass().getMethod("onOpenTTBannerAd", Activity.class, String.class);
                onCreate.invoke(thirdSdk, activity, values);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onCloseTTBannerAd(String values) {
        Log.d(H5Utils.TAG, "H5 onCloseTTBannerAd()");
        if (thirdSdk != null) {
            try {
                Method onCreate = thirdSdk.getClass().getMethod("onCloseTTBannerAd", String.class);
                onCreate.invoke(thirdSdk,values);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onOpenTTInteractionAd(Activity activity, String values) {
        Log.d(H5Utils.TAG, "H5 onOpenTTInteractionAd()");
        if (thirdSdk != null) {
            try {
                Method onCreate = thirdSdk.getClass().getMethod("onOpenTTInteractionAd",Activity.class, String.class);
                onCreate.invoke(thirdSdk,activity,values);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onOpenTTRewardVideoAd(Activity activity, String values) {
        Log.d(H5Utils.TAG, "H5 onOpenTTRewardVideoAd()");
        if (thirdSdk != null) {
            try {
                Method onCreate = thirdSdk.getClass().getMethod("onOpenTTRewardVideoAd",Activity.class, String.class);
                onCreate.invoke(thirdSdk,activity,values);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onOpenTTFullScreenVideoAd(Activity activity, String values) {
        Log.d(H5Utils.TAG, "H5 onOpenTTFullScreenVideoAd()");
        if (thirdSdk != null) {
            try {
                Method onCreate = thirdSdk.getClass().getMethod("onOpenTTFullScreenVideoAd",Activity.class, String.class);
                onCreate.invoke(thirdSdk,activity,values);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onGetScreenSize(Activity activity, String values) {
        Log.d(H5Utils.TAG, "H5 onGetScreenSize()");
        if (thirdSdk != null) {
            try {
                Method onCreate = thirdSdk.getClass().getMethod("onGetScreenSize",Activity.class, String.class);
                onCreate.invoke(thirdSdk,activity,values);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onCreate(Activity activity, Intent intent) {
        Log.d(H5Utils.TAG, "H5 onCreate()");
        if (thirdSdk != null) {
            try {
                Method onCreate = thirdSdk.getClass().getMethod("onCreate", Activity.class, Intent.class);
                onCreate.invoke(thirdSdk, activity, intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onStart() {
        Log.d(H5Utils.TAG, "H5 onStart()");
        if (thirdSdk != null) {
            try {
                Method onStart = thirdSdk.getClass().getMethod("onStart");
                onStart.invoke(thirdSdk);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onRestart(Activity activity) {
        Log.d(H5Utils.TAG, "H5 onRestart()");
        if (thirdSdk != null) {
            try {
                Method onRestart = thirdSdk.getClass().getMethod("onRestart", Activity.class);
                onRestart.invoke(thirdSdk, activity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onResume(Activity activity) {
        Log.d(H5Utils.TAG, "H5 onResume()");
        if (thirdSdk != null) {
            try {
                Method onResume = thirdSdk.getClass().getMethod("onResume", Activity.class);
                onResume.invoke(thirdSdk,
                        activity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onNewIntent(Intent intent) {
        Log.d(H5Utils.TAG, "H5 onNewIntent()");
        if (thirdSdk != null) {
            try {
                Method onNewIntent = thirdSdk.getClass().getMethod("onNewIntent", Intent.class);
                onNewIntent.invoke(thirdSdk, intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onPause(Activity activity) {
        Log.d(H5Utils.TAG, "H5 onPause()");
        if (thirdSdk != null) {
            try {
                Method onPause = thirdSdk.getClass().getMethod("onPause", Activity.class);
                onPause.invoke(thirdSdk, activity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onStop(Activity activity) {
        Log.d(H5Utils.TAG, "H5 onStop()");
        if (thirdSdk != null) {
            try {
                Method onStop = thirdSdk.getClass().getMethod("onStop", Activity.class);
                onStop.invoke(thirdSdk, activity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onDestroy(Activity activity) {
        Log.d(H5Utils.TAG, "H5 onDestroy()");
        if (thirdSdk != null) {
            try {
                Method onDestroy = thirdSdk.getClass().getMethod("onDestroy", Activity.class);
                onDestroy.invoke(thirdSdk, activity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(H5Utils.TAG, "H5 onActivityResult()");
        if (thirdSdk != null) {
            try {
                Method onActivityResult = thirdSdk.getClass().getMethod("onActivityResult", int.class, int.class,
                        Intent.class);
                onActivityResult.invoke(thirdSdk, requestCode, resultCode, data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
