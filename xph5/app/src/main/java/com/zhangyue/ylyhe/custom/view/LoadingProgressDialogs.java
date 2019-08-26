package com.zhangyue.ylyhe.custom.view;


import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;

import com.zhangyue.ylyhe.R;


public class LoadingProgressDialogs extends Dialog {

	public LoadingProgressDialogs(Context context) {
		this(context, R.style.H5ProgressDialog);
	}

	private LoadingProgressDialogs(Context context, int theme) {
		super(context, theme);
		this.setContentView(R.layout.loadingprogress);
		this.getWindow().getAttributes().gravity = Gravity.CENTER;
	}
}
