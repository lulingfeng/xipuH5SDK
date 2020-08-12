package com.startobj.util.softInput;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * 输入法工具类
 * 
 * @author Eagle
 *
 */
public class SOSoftInputUtil {
	private static Handler handler;

	/**
	 * 延时显示输入法
	 *
	 * @param view
	 *            推荐将EditText传入
	 * @param delayMs
	 *            延迟时间(毫秒)
	 */
	public static void showSoftInputDelay(final View view, final long delayMs) {
		handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				showSoftInput(view);
			}
		}, delayMs);
	}

	/**
	 * 延时隐藏输入法
	 *
	 * @param view
	 *            推荐将EditText传入
	 * @param delayMs
	 *            延迟时间(毫秒)
	 */
	public static void hideSoftInputDelay(final View view, final long delayMs) {
		handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				hideSoftInput(view);
			}
		}, delayMs);
	}

	/**
	 * 显示输入法
	 *
	 * @param view
	 *            推荐将EditText传入
	 */
	@SuppressLint("NewApi")
	public static void showSoftInput(View view) {
		if (view instanceof EditText) {
			view.requestFocus();
			((EditText) view).setCursorVisible(true);
		}
		InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInputFromInputMethod(view.getWindowToken(), InputMethodManager.SHOW_IMPLICIT);
	}

	/**
	 * 隐藏输入法
	 *
	 * @param view
	 *            推荐将EditText传入
	 */
	@SuppressLint("NewApi")
	public static void hideSoftInput(View view) {
		InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}
}
