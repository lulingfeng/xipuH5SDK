package com.xipu.h5.osdk.utils;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.WindowManager;

public class SystemUiUtils {

    public static SystemUiUtils instance;

    /***
     * 单例模式
     * @aAuther bruce.li
     * @return instance
     */
    public static SystemUiUtils getInstance() {
        if (instance == null) {
            synchronized (SystemUiUtils.class) {
                if (instance == null) {
                    instance = new SystemUiUtils();
                }
            }
        }
        return instance;
    }

    public void hideSystemUi(Activity mActivity) {
        try {
            if (mActivity.getWindow() != null) {
                mActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                mActivity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hideSystemUi(Dialog mDialog) {
        try {
            if (mDialog.getWindow() != null) {
                mDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                mDialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                //   mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
