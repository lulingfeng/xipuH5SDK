package com.xipu.h5.osdk.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.startobj.util.common.SOCommonUtil;
import com.xipu.h5.osdk.callback.OLoginCallBack;
import com.xipu.h5.osdk.utils.OLoginTypeUtils;
import com.xipu.h5.osdk.utils.OSDKUtils;
import com.xipu.h5.osdk.utils.SystemUiUtils;

public class OBindAccountDialog extends Dialog implements View.OnClickListener {

    private Activity mActivity;
    private LinearLayout line, google, facebook;
    private TextView bind_hint;
    private Button jump;
    private OLoginCallBack mOLoginCallBack;

    public OBindAccountDialog(@NonNull Activity activity, OLoginCallBack loginCallBack) {
        super(activity, SOCommonUtil.getRes4Sty(activity, "OSDKDialog"));
        this.mActivity = activity;
        this.mOLoginCallBack = loginCallBack;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(SOCommonUtil.getRes4Lay(mActivity, "o_layout_oversea_bind_account"));
        setCancelable(false);
        initView();
    }

    private void initView() {
        bind_hint = findViewById(SOCommonUtil.getRes4Id(mActivity, "o_bind_hint"));
        line = findViewById(SOCommonUtil.getRes4Id(mActivity, "o_bind_line"));
        google = findViewById(SOCommonUtil.getRes4Id(mActivity, "o_bind_google"));
        facebook = findViewById(SOCommonUtil.getRes4Id(mActivity, "o_bind_facebook"));
        jump = findViewById(SOCommonUtil.getRes4Id(mActivity, "o_bind_account_jump_but"));

        line.setOnClickListener(this);
        google.setOnClickListener(this);
        facebook.setOnClickListener(this);
        jump.setOnClickListener(this);

        refreshView();

        SystemUiUtils.getInstance().hideSystemUi(this);
    }

    public void refreshView() {
        bind_hint.setText(SOCommonUtil.getRes4LocaleStr(mActivity, "o_guest_hint"));
        jump.setText(SOCommonUtil.getRes4LocaleStr(mActivity, "o_jump"));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == SOCommonUtil.getRes4Id(mActivity, "o_bind_line")) {
            mOLoginCallBack.onAbroadLogin(OLoginTypeUtils.LINE, line);
        } else if (id == SOCommonUtil.getRes4Id(mActivity, "o_bind_google")) {
            mOLoginCallBack.onAbroadLogin(OLoginTypeUtils.GOOGLE, google);
        } else if (id == SOCommonUtil.getRes4Id(mActivity, "o_bind_facebook")) {
            mOLoginCallBack.onAbroadLogin(OLoginTypeUtils.FACEBOOK, facebook);
        } else if (id == SOCommonUtil.getRes4Id(mActivity, "o_bind_account_jump_but")) {
            mOLoginCallBack.onAbroadLogin("", jump);
            dismiss();
        }
    }

    @Override
    public void show() {
        try {
            SystemUiUtils.getInstance().hideSystemUi(this);
            super.show();
        } catch (Exception e) {
            Log.d(OSDKUtils.TAG, "show OBindAccountDialog" + e.getMessage());
        }
    }
}
