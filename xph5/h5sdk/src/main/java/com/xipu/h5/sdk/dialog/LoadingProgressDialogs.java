package com.xipu.h5.sdk.dialog;


import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;

import com.xipu.h5_sdk.R;


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
