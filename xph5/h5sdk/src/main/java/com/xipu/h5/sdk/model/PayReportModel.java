package com.xipu.h5.sdk.model;

import android.os.Parcel;
import android.os.Parcelable;


public class PayReportModel extends BaseModel implements Parcelable {

    private String is_ysdk_report;
    private String ysdk_report_amount;
    private String is_appfly_report;
    private String appfly_report_amount;
    private String product_id;
    private String outTradeNO;
    private boolean isCallBack;

    public PayReportModel() {
    }

    protected PayReportModel(Parcel in) {
        is_ysdk_report = in.readString();
        ysdk_report_amount = in.readString();
        is_appfly_report = in.readString();
        appfly_report_amount = in.readString();
        product_id = in.readString();
        outTradeNO = in.readString();
    }

    public static final Creator<PayReportModel> CREATOR = new Creator<PayReportModel>() {
        @Override
        public PayReportModel createFromParcel(Parcel in) {
            return new PayReportModel(in);
        }

        @Override
        public PayReportModel[] newArray(int size) {
            return new PayReportModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(is_ysdk_report);
        dest.writeString(ysdk_report_amount);
        dest.writeString(is_appfly_report);
        dest.writeString(appfly_report_amount);
        dest.writeString(product_id);
        dest.writeString(outTradeNO);
    }

    public String getIs_ysdk_report() {
        return retString(is_ysdk_report);
    }

    public void setIs_ysdk_report(String is_ysdk_report) {
        this.is_ysdk_report = is_ysdk_report;
    }

    public String getYsdk_report_amount() {
        return retString(ysdk_report_amount);
    }

    public void setYsdk_report_amount(String ysdk_report_amount) {
        this.ysdk_report_amount = ysdk_report_amount;
    }

    public String getIs_appfly_report() {
        return retString(is_appfly_report);
    }

    public void setIs_appfly_report(String is_appfly_report) {
        this.is_appfly_report = is_appfly_report;
    }

    public String getAppfly_report_amount() {
        return retString(appfly_report_amount);
    }

    public void setAppfly_report_amount(String appfly_report_amount) {
        this.appfly_report_amount = appfly_report_amount;
    }

    public String getProduct_id() {
        return retString(product_id);
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getOutTradeNO() {
        return retString(outTradeNO);
    }

    public void setOutTradeNO(String outTradeNO) {
        this.outTradeNO = outTradeNO;
    }

    public boolean isCallBack() {
        return isCallBack;
    }

    public void setCallBack(boolean callBack) {
        isCallBack = callBack;
    }

    @Override
    public String toString() {
        return "PayReportModel{" +
                "is_ysdk_report='" + is_ysdk_report + '\'' +
                ", ysdk_report_amount='" + ysdk_report_amount + '\'' +
                ", is_appfly_report='" + is_appfly_report + '\'' +
                ", appfly_report_amount='" + appfly_report_amount + '\'' +
                ", product_id='" + product_id + '\'' +
                ", outTradeNO='" + outTradeNO + '\'' +
                ", isCallBack=" + isCallBack +
                '}';
    }
}
