package com.startobj.util.http;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

/**
 * @Explain
 * @Version 1.0
 * @CreateDate 2016/01/20 2:18 PM
 * @Author Eagle Email:lizhengpei@gmail.com
 */
public class SOJsonMapper {
	@SuppressWarnings("unused")
	private static final String TAG = SOJsonMapper.class.getName();

	private SOJsonMapper() {
	}

	public static String[] fromJson(String result)
			throws IOException, SOContentEmptyException, SOServertReturnErrorException, JSONException {
		String errcode;
		if (TextUtils.isEmpty(result))
			throw new SOContentEmptyException("服务器返回Json数据");
		JSONObject jsonObject = new JSONObject(result);
		errcode = jsonObject.getString("errcode");

		if (TextUtils.isEmpty(errcode))
			throw new SOContentEmptyException("Json数据-errcode参数数据");
		else if (!"0".equals(errcode))
			throw new SOServertReturnErrorException(jsonObject.getString("errmsg"));
		else
			return new String[] { jsonObject.getString("errmsg"), jsonObject.getString("data"), errcode };
	}
}
