package com.xipu.h5.osdk.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.startobj.util.common.SOCommonUtil;
import com.xipu.h5.osdk.callback.OLoginCallBack;
import com.xipu.h5.osdk.utils.OLoginTypeUtils;
import com.xipu.h5.osdk.utils.OSDKUtils;
import com.xipu.h5.osdk.utils.SystemUiUtils;

public class OLoginDialog extends Dialog implements View.OnClickListener {

    private Activity mActivity;
    private RelativeLayout line_Login, google_Login, facebook_Login, guest_login;
    private OLoginCallBack mLoginCallBack;

    public OLoginDialog(@NonNull Activity activity, OLoginCallBack loginCallBack) {
        super(activity, SOCommonUtil.getRes4Sty(activity, "OSDKDialog"));
        this.mActivity = activity;
        this.mLoginCallBack = loginCallBack;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(SOCommonUtil.getRes4Lay(mActivity, "o_layout_oversea_login"));
        setCancelable(false);
        initView();
    }

    private void initView() {
        line_Login = findViewById(SOCommonUtil.getRes4Id(mActivity, "o_line_login"));
        google_Login = findViewById(SOCommonUtil.getRes4Id(mActivity, "o_google_login"));
        facebook_Login = findViewById(SOCommonUtil.getRes4Id(mActivity, "o_facebook_login"));
        guest_login = findViewById(SOCommonUtil.getRes4Id(mActivity, "o_guest_login"));

        line_Login.setOnClickListener(this);
        google_Login.setOnClickListener(this);
        facebook_Login.setOnClickListener(this);
        guest_login.setOnClickListener(this);

        SystemUiUtils.getInstance().hideSystemUi(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == SOCommonUtil.getRes4Id(mActivity, "o_line_login")) {
            mLoginCallBack.onAbroadLogin(OLoginTypeUtils.LINE, line_Login);
        } else if (id == SOCommonUtil.getRes4Id(mActivity, "o_google_login")) {
            mLoginCallBack.onAbroadLogin(OLoginTypeUtils.GOOGLE, google_Login);
        } else if (id == SOCommonUtil.getRes4Id(mActivity, "o_facebook_login")) {
            mLoginCallBack.onAbroadLogin(OLoginTypeUtils.FACEBOOK, facebook_Login);
        } else if (id == SOCommonUtil.getRes4Id(mActivity, "o_guest_login")) {
            mLoginCallBack.onAbroadLogin(OLoginTypeUtils.GUEST, guest_login);
        }
    }

    @Override
    public void show() {
        try {
            SystemUiUtils.getInstance().hideSystemUi(this);
            super.show();
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        } catch (Exception e) {
            Log.d(OSDKUtils.TAG, "show OLoginDialog" + e.getMessage());
        }
    }
}
