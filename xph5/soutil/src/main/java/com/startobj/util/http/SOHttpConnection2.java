package com.startobj.util.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import android.app.Activity;

/**
 * Http请求工具类
 *
 * @Explain
 * @Version 1.0
 * @CreateDate 2016-08-16 22:09:54
 * @Author Eagle Email:lizhengpei@gmail.com
 */
public class SOHttpConnection2 {

	private static URL mUrl = null;
	private static String REQUEST_METHOD_POST = "POST";
	private static String REQUEST_METHOD_GET = "GET";

	public static void get(final Activity context, final SORequestParams params,
			final SOCallBack.SOCommonCallBack<String> commonCallBack, int... timeOut) {
		sendConn(context, params, commonCallBack, timeOut.length == 0 ? 1000 * 8 : timeOut[0], REQUEST_METHOD_GET);
	}

	public static void post(final Activity context, final SORequestParams params,
			final SOCallBack.SOCommonCallBack<String> commonCallBack, int... timeOut) {
		sendConn(context, params, commonCallBack, timeOut.length == 0 ? 1000 * 8 : timeOut[0], REQUEST_METHOD_POST);
	}

	private static void sendConn(final Activity context, final SORequestParams params,
			final SOCallBack.SOCommonCallBack<String> commonCallBack, final int timeOut, final String requestMethod) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					HttpURLConnection conn = initHttpConnection(params, timeOut, requestMethod);
					int code = conn.getResponseCode();
					if (200 == code) {
						final StringBuffer buf = obtainDatas(conn);
						success(context, commonCallBack, buf);
					} else {
						codeError(context,commonCallBack, code);
					}
				} catch (final Exception e) {
					httpError(context, commonCallBack, e);
				} finally {
					finished(context, commonCallBack);
				}
			}
		};
		new Thread(runnable).start();
	}

	/**
	 * 初始化HttpConnection参数
	 *
	 * @param params
	 * @param timeOut
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ProtocolException
	 */
	private static HttpURLConnection initHttpConnection(SORequestParams params, int timeOut, String requestMethod)
			throws MalformedURLException, IOException, ProtocolException {
		HttpURLConnection mConn = null;
		String url = "";
		if (REQUEST_METHOD_GET.endsWith(requestMethod))
			url = sendGetDatas(params);
		else if (REQUEST_METHOD_POST.endsWith(requestMethod))
			url = params.getUrl();
		mUrl = new URL(url);
		if (params.getUrl().contains("http")) {
			mConn = (HttpURLConnection) mUrl.openConnection();
		} else if (params.getUrl().contains("https")) {
			mConn = (HttpsURLConnection) mUrl.openConnection();
		}
		mConn.setRequestMethod(requestMethod);
		mConn.setUseCaches(false);
		int _timeout = obtainTimeOut(timeOut);
		mConn.setConnectTimeout(_timeout);
		mConn.setReadTimeout(_timeout);
		if (REQUEST_METHOD_POST.endsWith(requestMethod))
			sendPostDatas(mConn, params);
		return mConn;
	}

	/**
	 * 默认超时时间为8秒，如果参数值大于0则赋值
	 *
	 * @param timeOut
	 * @return
	 */
	private static int obtainTimeOut(final int timeOut) {
		return timeOut > 0 ? timeOut : 8000;
	}

	/**
	 * 发送Get参数数据
	 *
	 * @param params
	 * @throws IOException
	 */
	private static String sendGetDatas(SORequestParams params) {
		return params.getUrl() + "?" + params.getParamsStr();
	}

	/**
	 * 发送Post参数数据
	 * @param conn
	 *
	 * @param params
	 * @throws IOException
	 */
	private static void sendPostDatas(HttpURLConnection conn, SORequestParams params) throws IOException {
		if (params.getParamsStr() == null)
			return;
		OutputStream os = conn.getOutputStream();
		os.write(params.getParamsStr().getBytes());
		os.flush();
		os.close();
	}

	/**
	 * 获取服务器返回数据
	 * @param conn
	 *
	 * @return
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	private static StringBuffer obtainDatas(HttpURLConnection conn) throws IOException, UnsupportedEncodingException {
		// 当调用getInputStream方法时才真正将请求体数据上传至服务器
		InputStream stream = conn.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
		final StringBuffer buf = new StringBuffer();
		String line;
		while (null != (line = br.readLine())) {
			buf.append(line);
		}
		stream.close();
		conn.disconnect();
		return buf;
	}

	/**
	 * 成功返回
	 *
	 * @param context
	 * @param commonCallBack
	 * @param buf
	 */
	private static void success(final Activity context, final SOCallBack.SOCommonCallBack<String> commonCallBack,
			final StringBuffer buf) {
		context.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				commonCallBack.onSuccess(buf.toString());
			}
		});
	}

	/**
	 * 返回码错误
	 *
	 * @param commonCallBack
	 * @param code
	 */
	private static void codeError(final Activity context, final SOCallBack.SOCommonCallBack<String> commonCallBack, final int code) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                commonCallBack.onCodeError(new SOCallBack.CodeErrorException("Http CodeError,ResponseCode is :" + code));
            }
        });
	}

	/**
	 * 网络请求错误
	 *
	 * @param context
	 * @param commonCallBack
	 * @param e
	 */
	private static void httpError(final Activity context, final SOCallBack.SOCommonCallBack<String> commonCallBack,
			final Exception e) {
		context.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				commonCallBack.onHttpError(e, false);
			}
		});
	}

	/**
	 * 完成
	 *
	 * @param context
	 * @param commonCallBack
	 */
	private static void finished(final Activity context, final SOCallBack.SOCommonCallBack<String> commonCallBack) {
		context.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				commonCallBack.onFinished();
			}
		});
	}
}
