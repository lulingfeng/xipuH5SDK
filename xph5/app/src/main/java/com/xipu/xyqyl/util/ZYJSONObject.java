package com.xipu.xyqyl.util;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

public class ZYJSONObject extends JSONObject {

	public ZYJSONObject(String string) throws JSONException {
		super(string);
	}

	public String getStringDef(String key) {
		return getStringDef(key, "");
	}

	public String getStringDef(String key, String def) {
		try {
			return this.has(key) && !TextUtils.isEmpty(this.getString(key)) ? this.getString(key) : def;
		} catch (JSONException e) {
			e.printStackTrace();
			return def;
		}
	}

	public String getCompareStringDef(String key1, String key2, String compare) {
		return getCompareStringDef(key1, key2, compare, "");
	}

	public String getCompareStringDef(String key1, String key2, String compare, String def) {
		try {
			return this.has(key1) && compare.equals(this.getString(key1)) && this.has(key2)
					&& !TextUtils.isEmpty(this.getString(key2)) ? this.getString(key2) : def;
		} catch (JSONException e) {
			e.printStackTrace();
			return def;
		}
	}
}
