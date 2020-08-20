package com.xipu.h5.sdk.model;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.HashMap;

public class RequestModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5086802860245303627L;
	private String uuid;
	private String url;
	private HashMap<String, String> params;
	private String tag;
	private long timestamp;
	private int count;
	boolean isCallBack;
	boolean isQueryOrder;

	public RequestModel(String url, HashMap<String, String> params, String tag, long timestamp, int count) {
		this.url = url;
		this.params = params;
		this.tag = tag;
		this.timestamp = timestamp;
		this.count = count;
	}

	public RequestModel(String url, HashMap<String, String> params, String tag, long timestamp, int count, boolean isCallBack, boolean isQueryOrder) {
		this.url = url;
		this.params = params;
		this.tag = tag;
		this.timestamp = timestamp;
		this.count = count;
		this.isCallBack = isCallBack;
		this.isQueryOrder = isQueryOrder;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getUrl() {
		return TextUtils.isEmpty(url)?"":url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public HashMap<String, String> getParams() {
		return params;
	}

	public void setParams(HashMap<String, String> params) {
		this.params = params;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public boolean isCallBack() {
		return isCallBack;
	}

	public void setCallBack(boolean callBack) {
		isCallBack = callBack;
	}

	public boolean isQueryOrder() {
		return isQueryOrder;
	}

	public void setQueryOrder(boolean queryOrder) {
		isQueryOrder = queryOrder;
	}
}
