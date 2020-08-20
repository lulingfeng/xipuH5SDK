package com.startobj.util.toast;

import android.content.Context;
import android.graphics.Color;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.startobj.util.common.SOCommonUtil;

/**
 * @author lulingfeng
 * @project_name SOToastUtil
 * @email lingfeng.lu@xipu.com
 * @create_time 2019/11/30 11:09
 * @describe 吐司工具类：避免内容出现排队等待现象，实现实时更新吐司，实现退出活动销毁吐司
 */
public class SOToastUtil {
    private static Context context;
    private static Toast toast;
    private static long currentTime;
    private static final String TAG = SOToastUtil.class.getSimpleName();

    /**
     * @param content 显示内容
     * @param isLong  是否显示时间长
     */
    private static void show(String content, boolean isLong) {
        Log.e(TAG, "show: ");
        if (TextUtils.isEmpty(content)) {
            return;
        }
        SpannableString spannableString = new SpannableString(content);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.WHITE);
        spannableString.setSpan(colorSpan, 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (System.currentTimeMillis() - currentTime < 500)
            return;
        currentTime = System.currentTimeMillis();
        if (toast == null) {
            Log.e(TAG, "show:null");
            toast = Toast.makeText(context, null, isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
        }
        toast.setText(spannableString);
        View view = toast.getView();
        view.setBackgroundResource(SOCommonUtil.getRes4Dra(context, "hc_toast_show"));
        view.setAlpha(0.8f);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    /**
     * @param context 上下文
     *                Application初始化
     */
    public static void init(Context context) {
        SOToastUtil.context = context.getApplicationContext();
    }

    /**
     * @param content 显示内容
     *                外界调用
     */
    public static void showShort(String content) {
        show(content, false);
    }

    /**
     * @param content 显示内容
     *                外界调用
     */
    public static void showLong(String content) {
        show(content, true);
    }

    /**
     * 销毁Toast
     */
    public static void destoryToast() {
        if (toast != null) {
            toast.cancel();
            toast = null;
        }
    }
}