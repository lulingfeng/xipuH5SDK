package com.xipu.xyqyl.util;

import android.text.TextUtils;

public class AdConfig {
    private String app_id;
    private String ad_id;
    private int count;
    private int intervalTime;
    private int top;
    private int left;
    private int width;
    private int height;
    private int orientation;
    private String reward_name;
    private int reward_count;
    private String user_id;
    private String extra;


    public void setAd_id(String ad_id) {
        this.ad_id = ad_id;
    }

    public void setApp_id(String app_id) {
        this.app_id = app_id;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setReward_count(int reward_count) {
        this.reward_count = reward_count;
    }

    public void setReward_name(String reward_name) {
        this.reward_name = reward_name;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public void setIntervalTime(int intervalTime) {
        this.intervalTime = intervalTime;
    }

    public String getAd_id() {
        return TextUtils.isEmpty(ad_id) ? "" : ad_id;
    }

    public String getApp_id() {
        return TextUtils.isEmpty(app_id) ? "" : app_id;
    }

    public int getIntervalTime() {
        return intervalTime;
    }

    public int getHeight() {
        return height;
    }

    public int getLeft() {
        return left;
    }

    public int getWidth() {
        return width;
    }

    public int getTop() {
        return top;
    }

    public int getCount() {
        return count;
    }

    public int getReward_count() {
        return reward_count;
    }

    public String getReward_name() {
        return TextUtils.isEmpty(reward_name) ? "" : reward_name;
    }

    public String getUser_id() {
        return TextUtils.isEmpty(user_id) ? "" : user_id;
    }

    public String getExtra() {
        return extra;
    }

    public int getOrientation() {
        return orientation;
    }

    @Override
    public String toString() {
        return "AdConfig{" +
                "app_id='" + app_id + '\'' +
                ", ad_id='" + ad_id + '\'' +
                ", count='" + count + '\'' +
                ", intervalTime='" + intervalTime + '\'' +
                ", top='" + top + '\'' +
                ", left='" + left + '\'' +
                ", width='" + width + '\'' +
                ", height='" + height + '\'' +
                ", reward_name='" + reward_name + '\'' +
                ", reward_count='" + reward_count + '\'' +
                ", user_id='" + user_id + '\'' +
                ", orientation='" + orientation + '\'' +
                ", extra='" + extra + '\'' +
                '}';
    }

}
