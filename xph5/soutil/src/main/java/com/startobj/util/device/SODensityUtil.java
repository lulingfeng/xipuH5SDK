package com.startobj.util.device;

import java.lang.reflect.Method;

import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

public final class SODensityUtil {

	private static float density = -1F;
	private static int widthPixels = -1;
	private static int heightPixels = -1;

	private SODensityUtil() {
	}

	public static float getDensity(Context context) {
		if (density <= 0F) {
			density = context.getResources().getDisplayMetrics().density;
		}
		return density;
	}

	public static int dip2px(Context context, float dpValue) {
		return (int) (dpValue * getDensity(context) + 0.5F);
	}

	public static int px2dip(Context context, float pxValue) {
		return (int) (pxValue / getDensity(context) + 0.5F);
	}

	// 将px值转换为sp值，保证文字大小不变
	public static int px2sp(Context context, float pxValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (pxValue / fontScale + 0.5f);
	}

	// 将sp值转换为px值，保证文字大小不变
	public static int sp2px(Context context, float spValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (spValue * fontScale + 0.5f);
	}

	public static int getScreenWidth(Context context) {
		if (widthPixels <= 0) {
			widthPixels = context.getResources().getDisplayMetrics().widthPixels;
		}
		return widthPixels;
	}

	public static int getScreenHeight(Context context) {
		if (heightPixels <= 0) {
			heightPixels = context.getResources().getDisplayMetrics().heightPixels;
		}
		return heightPixels;
	}

	// 获取当前横竖屏状态
	public static boolean isScreenLandscape(Context context) {
		Configuration mConfiguration = context.getResources().getConfiguration(); // 获取设置的配置信息
		int ori = mConfiguration.orientation; // 获取屏幕方向
		if (ori == mConfiguration.ORIENTATION_LANDSCAPE) {
			// 横屏
			return true;
		} else if (ori == mConfiguration.ORIENTATION_PORTRAIT) {
			// 竖屏
			return false;
		}
		return false;
	}

	// 获取虚拟功能键高度
	public static int getVirtualBarHeigh(Context context) {
		int vh = 0;
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = windowManager.getDefaultDisplay();
		DisplayMetrics dm = new DisplayMetrics();
		try {
			@SuppressWarnings("rawtypes")
			Class c = Class.forName("android.view.Display");
			@SuppressWarnings("unchecked")
			Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
			method.invoke(display, dm);
			vh = dm.heightPixels - windowManager.getDefaultDisplay().getHeight();
			if (vh == 0) {
				vh = dm.widthPixels - windowManager.getDefaultDisplay().getWidth();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return vh;
	}
}