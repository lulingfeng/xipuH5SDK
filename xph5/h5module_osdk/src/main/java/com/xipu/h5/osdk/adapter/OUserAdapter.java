package com.xipu.h5.osdk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.startobj.util.common.SOCommonUtil;
import com.xipu.h5.osdk.callback.OUserListCallBack;
import com.xipu.h5.sdk.model.UserModel;

import java.util.List;


public class OUserAdapter extends BaseAdapter {
    private Context mContext;
    private List<UserModel> mUsers;
    private LayoutInflater layoutInflater;
    private OUserListCallBack mOUserListCallBack;

    public OUserAdapter(Context context, List<UserModel> userModelList, OUserListCallBack oUserListCallBack) {
        super();
        this.mContext = context;
        this.mUsers = userModelList;
        this.mOUserListCallBack = oUserListCallBack;
        this.layoutInflater = LayoutInflater.from(context);
    }

    public void notifyUserDataChange(List<UserModel> userModelList) {
        if (mUsers.size() > 0) {
            mUsers.clear();
        }
        if (userModelList != null && userModelList.size() > 0) {
            mUsers.addAll(userModelList);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mUsers.size();
    }

    @Override
    public Object getItem(int position) {
        return mUsers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        UserListHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(SOCommonUtil.getRes4Lay(mContext, "o_layout_oversea_user_list_item"), parent,false);
            holder = new UserListHolder(convertView, position);
            convertView.setTag(holder);
        } else {
            holder = (UserListHolder) convertView.getTag();
        }
        final UserModel ue = mUsers.get(position);
        holder.mXpTv_userlist_name.setText(ue.getUsername());
        convertView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mOUserListCallBack.onItemClick(ue);
            }
        });
        return convertView;
    }

    public class UserListHolder {
        public TextView mXpTv_userlist_name;

        public UserListHolder(View view, int position) {
            super();
            mXpTv_userlist_name = (TextView) view.findViewById(SOCommonUtil.getRes4Id(mContext, "o_tv_user_list_name"));
        }
    }

    /***
     * 释放
     */
    public void releaseAbroadUserAdapter() {
        if (mContext != null) {
            mContext = null;
        }
        if (layoutInflater != null) {
            layoutInflater = null;
        }
        if (mUsers != null) {
            if (mUsers.size() > 0) {
                mUsers.clear();
            }
            mUsers = null;
        }
    }
}