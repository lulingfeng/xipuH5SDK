package com.xipu.h5.h5sdk.manager;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import com.xipu.h5.sdk.util.H5Utils;
import com.xipu.h5.sdk.util.ParamUtil;

public class TuiaManager {

    private static TuiaManager instance;

    public static TuiaManager getInstance() {
        if (instance == null) {
            synchronized (TuiaManager.class) {
                if (instance == null) {
                    instance = new TuiaManager();
                }
            }
        }
        return instance;
    }

    public void tuiaActivate(Activity activity) {
        if (H5Utils.getIsFirst(activity)) {
            H5Utils.setIsFirst(activity);
            if (ParamUtil.isIsUseTuia()) {
                // 推啊 安装上报
                ClipboardManager myClipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = myClipboard.getPrimaryClip();
                if (clipData != null && clipData.getItemCount() > 0) {
                    // 从数据集中获取（粘贴）第一条文本数据
                    String text = clipData.getItemAt(0).getText().toString();
                    String tuiaID = "";
                    if (text.contains("tuia=")) {
                        tuiaID = text.replace("tuia=", "");
                        ParamUtil.setTuiaID(activity, tuiaID);
                    }
                }
                H5Utils.tuiaApi(activity, "2");
            }
        }
    }

    public void sendTuiaPayInfo(Activity activity) {
        if (ParamUtil.isIsUseTuia()) {
            H5Utils.tuiaApi(activity, "6");
        }
    }
}
