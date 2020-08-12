package com.startobj.util.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.text.TextUtils;

/**
 * Http参数
 * 
 * @Explain
 * @Version 1.0
 * @CreateDate 2016-08-16 22:09:54
 * @Author Eagle Email:lizhengpei@gmail.com
 */
public class SORequestParams {
	private static final String TAG = SORequestParams.class.getName();

	private String mUrl;
    private Map<String, String> mParams;
	private String mParamsStr;
	private List<Map.Entry<String, String>> mInfoIds;

    public SORequestParams(String url, Map<String, String> params) {
		if (TextUtils.isEmpty(url))
			throw new RuntimeException("RequestParams String url is null");
		if (params == null)
			throw new RuntimeException("RequestParams HashMap<String, String> params is null");
		this.mParams = params;
		this.mUrl = url;
//		this.mUrl = url.toLowerCase();
		sortParams();
		buildParams();
	}

	public SORequestParams(String url, String params) {
		if (TextUtils.isEmpty(url))
			throw new RuntimeException("RequestParams String url is null");
		if (TextUtils.isEmpty(params))
			throw new RuntimeException("RequestParams HashMap<String, String> params is null");
		this.mParamsStr = params;
		this.mUrl = url;
//		this.mUrl = url.toLowerCase();
	}

	/**
	 * 参数排序
	 */
	private void sortParams() {
		mInfoIds = new ArrayList<>(mParams.entrySet());
		Collections.sort(mInfoIds, new Comparator<Map.Entry<String, String>>() {
			public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
                return (o1.getKey()).compareTo(o2.getKey());
			}
		});
	}

	/**
	 * 构造参数
	 */
	private void buildParams() {
		if (mInfoIds == null || mInfoIds.size() == 0)
			throw new RuntimeException("RequestParams List<Map.Entry<String, String>> mInfoIds is null");
		else {
			appendParams();
		}
	}

	/**
	 * 追加参数
	 */
	private void appendParams() {
		StringBuilder paramsBuilder = new StringBuilder();
		for (int i = 0; i < mInfoIds.size(); i++)
			try {
				String key = mInfoIds.get(i).getKey();
				String value = mInfoIds.get(i).getValue();
				if (TextUtils.isEmpty(value))
					paramsBuilder.append(key + "=" + value + "&");
				else
					paramsBuilder.append(key + "=" + URLEncoder.encode(value, "UTF-8") + "&");
			} catch (UnsupportedEncodingException e) {
			}
		String paramsStr = paramsBuilder.toString();
		mParamsStr = paramsStr.substring(0, paramsStr.length() - 1);
	}

	public String getUrl() {
		return mUrl;
	}

	public String getParamsStr() {
		return mParamsStr;
	}
}
