package com.xipu.h5.osdk.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.startobj.util.common.SOCommonUtil;
import com.xipu.h5.osdk.OSDK;
import com.xipu.h5.osdk.adapter.OUserAdapter;
import com.xipu.h5.osdk.callback.OLoginUserCallBack;
import com.xipu.h5.osdk.callback.OUserListCallBack;
import com.xipu.h5.osdk.utils.OSDKUtils;
import com.xipu.h5.osdk.utils.SystemUiUtils;
import com.xipu.h5.sdk.model.UserModel;

import java.util.ArrayList;

public class OXiPuLoginDialog extends Dialog implements View.OnClickListener {

    private Activity mActivity;
    private TextView title;
    private TextView account_hint,password_hint;
    private TextView account, password;
    private ImageView imageView;
    private Button confirm;
    private ListView listView;
    private RelativeLayout listView_root;
    private OUserAdapter mUserAdapter;
    private OLoginUserCallBack xiPuLoginDialogCallBack;

    public OXiPuLoginDialog(@NonNull Activity activity, OLoginUserCallBack loginDialogCallBack) {
        super(activity, SOCommonUtil.getRes4Sty(activity, "OSDKDialog"));
        this.mActivity = activity;
        this.xiPuLoginDialogCallBack = loginDialogCallBack;
        mUserAdapter = new OUserAdapter(mActivity, new ArrayList<UserModel>(), new OUserListCallBack() {
            @Override
            public void onItemClick(UserModel ue) {
                accountListItemClick(ue);
            }

            @Override
            public void onItemDelete(String account) {
                deleteUser(account);
            }

            @Override
            public void hideListView() {
                hideUserListView();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(SOCommonUtil.getRes4Lay(mActivity, "o_layout_oversea_xp_login"));
        setCancelable(false);
        initView();
    }

    private void initView() {
        title = findViewById(SOCommonUtil.getRes4Id(mActivity,"o_login_title"));

        account_hint = findViewById(SOCommonUtil.getRes4Id(mActivity,"o_account_hint"));
        account = findViewById(SOCommonUtil.getRes4Id(mActivity, "o_login_dialog_account"));

        password_hint = findViewById(SOCommonUtil.getRes4Id(mActivity,"o_password_hint"));
        password = findViewById(SOCommonUtil.getRes4Id(mActivity, "o_login_dialog_password"));

        imageView = findViewById(SOCommonUtil.getRes4Id(mActivity, "o_login_dialog_user_img"));
        confirm = findViewById(SOCommonUtil.getRes4Id(mActivity, "o_login_dialog_but"));
        listView = findViewById(SOCommonUtil.getRes4Id(mActivity, "o_login_dialog_list"));
        listView_root = findViewById(SOCommonUtil.getRes4Id(mActivity, "o_login_dialog_list_root"));
        imageView.setOnClickListener(this);
        confirm.setOnClickListener(this);
        listView.setAdapter(mUserAdapter);
      /*  if (OSDK.getUserModelList().size() > 0) {
            mUserAdapter.notifyUserDataChange(HCUtils.getUserModelList());
            account.setText(HCUtils.getUserModelList().get(0).getUsername());
            password.setText(HCUtils.getUserModelList().get(0).getPassword());
        }*/

        refreshView();

        SystemUiUtils.getInstance().hideSystemUi(this);
    }

    public void refreshView(){
        title.setText(SOCommonUtil.getRes4LocaleStr(mActivity,"o_login"));
        account_hint.setText(SOCommonUtil.getRes4LocaleStr(mActivity,"o_username"));
        password_hint.setText(SOCommonUtil.getRes4LocaleStr(mActivity,"o_password"));
        confirm.setText(SOCommonUtil.getRes4LocaleStr(mActivity,"o_confirm"));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == SOCommonUtil.getRes4Id(mActivity, "o_login_dialog_user_img")) {
            chooseHintListStatus();
        } else if (id == SOCommonUtil.getRes4Id(mActivity, "o_login_dialog_but")) {
            xiPuLoginDialogCallBack.onLogin(account.getText().toString(), password.getText().toString());
        }
    }

    /*
     * @Author bruce.li
     * @param userModel
     * listView 点击事件
     */
    public void accountListItemClick(UserModel userModel) {
        account.setText(userModel.getUsername());
        password.setText(userModel.getPassword());
        chooseHintListStatus();
    }

    /*
     * 清空账户信息
     *
     * @param name
     */
    public void deleteUser(String name) {
        if (TextUtils.isEmpty(name)) {
            return;
        }
        if (name.equals(account.getText().toString())) {
            account.setText("");
            password.setText("");
        }
    }

    /*
     * 隐藏userlistview
     */
    public void hideUserListView() {
        account.setText("");
        password.setText("");
        chooseHintListStatus();
    }

    /**
     * 状态转换器
     */
    private void chooseHintListStatus() {
        if (listView_root.getVisibility() == View.VISIBLE) {
            listView_root.setVisibility(View.GONE);
            imageView.setImageResource(SOCommonUtil.getRes4Mip(mActivity, "o_icon_down"));
        } else {
            listView_root.setVisibility(View.VISIBLE);
            imageView.setImageResource(SOCommonUtil.getRes4Mip(mActivity, "o_icon_up"));
        }
    }

    /**
     * 释放 适配器
     */
    public void releaseOUserAdapter() {
        if (mUserAdapter != null) {
            mUserAdapter.releaseAbroadUserAdapter();
        }
    }

    @Override
    public void show() {
        try {
            SystemUiUtils.getInstance().hideSystemUi(this);
            super.show();
            this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        } catch (Exception e) {
            Log.d(OSDKUtils.TAG, "show OXiPuLoginDialog" + e.getMessage());
        }
    }
}
