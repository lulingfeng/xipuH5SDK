package com.xipu.h5.sdk.model;

import android.text.TextUtils;

class BaseModel {
    private String accesstoken;
    private String sign;
    private String timestamp;
    private String openid;

    public String getAccesstoken() {
        return accesstoken;
    }

    public void setAccesstoken(String accesstoken) {
        this.accesstoken = accesstoken;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }
    public String retString(String key) {
        return TextUtils.isEmpty(key) ? "" : key;
    }
}
