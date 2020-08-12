package com.startobj.util.common;

import java.io.File;
import java.lang.reflect.Field;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;

import com.startobj.util.lang.LocaleHelper;

/**
 * 公共工具类
 *
 * @author Eagle
 */
public class SOCommonUtil {
    /**
     * 是否有上下文对象
     *
     * @param context
     * @return
     */
    public static boolean hasContext(Context context) {
        return context != null;
    }

    /**
     * 获取调用此方法的方法名
     *
     * @return
     */
    public static String getMethodName() {
        return Thread.currentThread().getStackTrace()[4].getMethodName();
    }

    /**
     * 安装apk
     *
     * @param result
     */
    public static void installApk(Context context, File result) {
        if (hasContext(context) && result != null) {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            // 点打开，打开新版本应用的
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory("android.intent.category.DEFAULT");
            intent.setDataAndType(Uri.fromFile(result), "application/vnd.android.package-archive");

            context.startActivity(intent);
            // 提示完成打开
            // android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    /**
     * 获取PackageInfo对象
     *
     * @param context
     * @return
     */
    public static PackageInfo getPackageInfo(Context context) {
        if (hasContext(context)) {
            try {
                PackageInfo packInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                return packInfo;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 设置按钮冷却时间
     */
    private static long lastClickTime = 0l;

    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeCold = time - lastClickTime;
        if (0 < timeCold && timeCold < 1000) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    private static String S(Context context, int resid) {
        // 防止延时操作情况下，Context被回收，出现的运行时错误。
        if (resid == 0)
            return "";
        return hasContext(context) ? context.getResources().getString(resid) : "";
    }

    public static String S(Context context, String resName) {
        return S(context, getRes4Str(context, resName));
    }

    /*
     * 获取资源ID
     *
     * @param context
     * @param resType
     * @param resName
     * @return
     */
    private static int getAppRes(Context context, String resType, String resName) {
        return context.getResources().getIdentifier(resName, resType, context.getPackageName());
    }

    public static int getRes4Id(Context context, String resName) {
        return getAppRes(context, "id", resName);
    }

    public static int getRes4Lay(Context context, String resName) {
        return getAppRes(context, "layout", resName);
    }

    public static int getRes4Dra(Context context, String resName) {
        return getAppRes(context, "drawable", resName);
    }

    public static int getRes4Mip(Context context, String resName) {
        return getAppRes(context, "mipmap", resName);
    }

    public static int getRes4Str(Context context, String resName) {
        // 防止延时操作情况下，Context被回收，出现的运行时错误。
        if (context != null) {
            return getAppRes(context, "string", resName);
        }
        return android.R.string.httpErrorBadUrl;
    }

    // 获取当前语言环境的 string 资源
    public static String getRes4LocaleStr(Context context, String resName) {
        if (context != null) {
            Context localeContext = LocaleHelper.getInstance().getLocaleContext(context);
            return localeContext.getString(getAppRes(localeContext, "string", resName));
        }
        return "";
    }

    public static int getRes4Col(Context context, String resName) {
        return getAppRes(context, "color", resName);
    }

    public static int getRes4Dim(Context context, String resName) {
        return getAppRes(context, "dimen", resName);
    }

    public static int getRes4Sty(Context context, String resName) {
        return getAppRes(context, "style", resName);
    }

    public static int getRes4Anim(Context context, String resName) {
        return getAppRes(context, "anim", resName);
    }

    // 通过反射实现
    public static final int[] getRes4StyleableArray(Context context, String resName) {
        try {
            if (context == null)
                return null;
            Field field = Class.forName(context.getPackageName() + ".R$styleable").getDeclaredField(resName);
            int[] ret = (int[]) field.get(null);
            return ret;
        } catch (Throwable t) {
        }
        return null;
    }

    public static final int getRes4StyleableArrayIndex(Context context, String resName) {
        try {
            if (context == null)
                return 0;
            Field field = Class.forName(context.getPackageName() + ".R$styleable").getDeclaredField(resName);
            int ret = (Integer) field.get(null);
            return ret;
        } catch (Throwable t) {
        }
        return 0;
    }
}
