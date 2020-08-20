package com.xipu.h5.sdk.model;

import java.io.Serializable;

public class ConfigModule implements Serializable {

    private String notify_url;

    public String getNotify_url() {
        return notify_url;
    }

    public void setNotify_url(String notify_url) {
        this.notify_url = notify_url;
    }

    @Override
    public String toString() {
        return "ConfigModule{" +
                "notify_url='" + notify_url + '\'' +
                '}';
    }
}
