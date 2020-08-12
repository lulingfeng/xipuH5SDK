package com.xipu.h5.sdk.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @Author Eagle Email:lizhengpei@gmail.com
 */
public class UserModel extends BaseModel implements Parcelable {
    private String openid;
    private String username;
    private String password;
    private String accesstoken;
    private String phone;
    private String has_update_pwd;
    private String is_ysdk_report;
    private String sign;
    private String timestamp;
    private String is_newuser;
    private String third_user_info;
    private String account_type;
    private String bind_account_type;
    private boolean isGuest;
    private boolean isBindThirdAccount;

    public UserModel() {
    }

    public UserModel(String openid, String username, String password) {
        super();
        this.openid = openid;
        this.username = username;
        this.password = password;
    }

    protected UserModel(Parcel in) {
        openid = in.readString();
        username = in.readString();
        password = in.readString();
        accesstoken = in.readString();
        phone = in.readString();
        has_update_pwd = in.readString();
        is_ysdk_report = in.readString();
        sign = in.readString();
        timestamp = in.readString();
        is_newuser = in.readString();
        third_user_info = in.readString();
        account_type = in.readString();
        bind_account_type = in.readString();
    }

    public static final Creator<UserModel> CREATOR = new Creator<UserModel>() {
        @Override
        public UserModel createFromParcel(Parcel in) {
            return new UserModel(in);
        }

        @Override
        public UserModel[] newArray(int size) {
            return new UserModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(openid);
        dest.writeString(username);
        dest.writeString(password);
        dest.writeString(accesstoken);
        dest.writeString(phone);
        dest.writeString(has_update_pwd);
        dest.writeString(is_ysdk_report);
        dest.writeString(sign);
        dest.writeString(timestamp);
        dest.writeString(is_newuser);
        dest.writeString(third_user_info);
        dest.writeString(account_type);
        dest.writeString(bind_account_type);
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getUsername() {
        return retString(username);
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return retString(password);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAccesstoken() {
        return retString(accesstoken);
    }

    public void setAccesstoken(String accesstoken) {
        this.accesstoken = accesstoken;
    }

    public String getPhone() {
        return retString(phone);
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getHas_update_pwd() {
        return retString(has_update_pwd);
    }

    public void setHas_update_pwd(String has_update_pwd) {
        this.has_update_pwd = has_update_pwd;
    }

    public String getSign() {
        return retString(sign);
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getTimes() {
        return retString(timestamp);
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getIs_newuser() {
        return retString(is_newuser);
    }

    public void setIs_newuser(String is_newuser) {
        this.is_newuser = is_newuser;
    }

    public String getIs_ysdk_report() {
        return retString(is_ysdk_report);
    }

    public void setIs_ysdk_report(String is_ysdk_report) {
        this.is_ysdk_report = is_ysdk_report;
    }

    public String getThird_user_info() {
        return retString(third_user_info);
    }

    public void setThird_user_info(String third_user_info) {
        this.third_user_info = third_user_info;
    }

    public String getAccount_type() {
        return retString(account_type);
    }

    public void setAccount_type(String account_type) {
        this.account_type = account_type;
    }

    public String getBind_account_type() {
        return retString(bind_account_type);
    }

    public void setBind_account_type(String bind_account_type) {
        this.bind_account_type = bind_account_type;
    }

    public boolean isGuest() {
        return isGuest;
    }

    public void setGuest(boolean guest) {
        isGuest = guest;
    }

    public boolean isBindThirdAccount() {
        return isBindThirdAccount;
    }

    public void setBindThirdAccount(boolean bindThirdAccount) {
        isBindThirdAccount = bindThirdAccount;
    }

    @Override
    public String toString() {
        return "UserModel{" +
                "openid='" + openid + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", accesstoken='" + accesstoken + '\'' +
                ", phone='" + phone + '\'' +
                ", has_update_pwd='" + has_update_pwd + '\'' +
                ", is_ysdk_report='" + is_ysdk_report + '\'' +
                ", sign='" + sign + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", is_newuser='" + is_newuser + '\'' +
                ", third_user_info='" + third_user_info + '\'' +
                ", account_type='" + account_type + '\'' +
                ", bind_account_type='" + bind_account_type + '\'' +
                ", isGuest=" + isGuest +
                ", isBindThirdAccount=" + isBindThirdAccount +
                '}';
    }
}