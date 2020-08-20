package com.xipu.h5.osdk.callback;


import com.xipu.h5.sdk.model.UserModel;

public interface OUserListCallBack {

    void onItemClick(UserModel ue);

    void onItemDelete(String account);

    void hideListView();
}
