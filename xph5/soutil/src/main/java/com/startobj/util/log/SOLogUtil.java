package com.startobj.util.log;

import com.startobj.util.common.SOCommonUtil;

import android.util.Log;

/**
 * @Explain
 * @Version 1.0
 * @CreateDate 2016-08-22 21:29:12
 * @Author Eagle Email:lizhengpei@gmail.com
 */
public class SOLogUtil {
	public final static int V = 0;
	public final static int D = 1;
	public final static int I = 2;
	public final static int W = 3;
	public final static int E = 4;
	public static boolean mDebug = true;
	private static String mTag;
	private static String mMsg;

	public static void v(String tag, String msg, Boolean ishttp) {
		handleMsg(tag, msg, ishttp);
		log(V, mTag, mMsg);
	}

	public static void d(String tag, String msg, Boolean ishttp) {
		handleMsg(tag, msg, ishttp);
		log(D, mTag, mMsg);
	}

	public static void i(String tag, String msg, Boolean ishttp) {
		handleMsg(tag, msg, ishttp);
		log(I, mTag, mMsg);
	}

	public static void w(String tag, String msg, Boolean ishttp) {
		handleMsg(tag, msg, ishttp);
		log(W, mTag, mMsg);
	}

	public static void e(String tag, String msg, Boolean ishttp) {
		handleMsg(tag, msg, ishttp);
		log(E, mTag, mMsg);
	}

	private static void handleMsg(String tag, String msg, Boolean ishttp) {
		mTag = ishttp ? tag + " : Http : " : tag + " : Local : ";
		mMsg = SOCommonUtil.getMethodName() + " : " + msg;
	}

	private static void log(int mark, String tag, String msg) {
		if (!mDebug)
			return;
		switch (mark) {
		case V:
			Log.v(tag, msg);
			break;
		case D:
			Log.d(tag, msg);
			break;
		case I:
			Log.i(tag, msg);
			break;
		case W:
			Log.w(tag, msg);
			break;
		case E:
			Log.e(tag, msg);
			break;
		}
	}

	public static void setDebug(boolean mDebug) {
		SOLogUtil.mDebug = mDebug;
	}
}
