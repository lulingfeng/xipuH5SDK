package com.xipu.h5.osdk.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

import com.startobj.util.common.SOCommonUtil;

/**
 * @Explain
 * @Version 1.0
 * @CreateDate 2016/05/19 下午4:21
 * @Author Eagle Email:lizhengpei@gmail.com
 */
public class OH5Dialog extends Dialog {
	public static final int BTN_STATE_CONFIRM = 1;
	public static final int BTN_STATE_CANCEL = 2;

	public OH5Dialog(Context context) {
		super(context);
	}

	public OH5Dialog(Context context, int themeResId) {
		super(context, themeResId);
	}

	protected OH5Dialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}

	@SuppressLint("InflateParams")
	public static class Builder {
		private Context mContext;
		private String mTitle;
		private String mConfirm;
		private String mCancel;
		private String mMessage;
		private int mBtnState = BTN_STATE_CANCEL;
		private OnClickListener mPositiveButtonClickListener;
		private OnClickListener mNegativeButtonClickListener;
		private boolean mCancelable = true;

		public Builder(Context mContext) {
			this.mContext = mContext;
			mTitle = SOCommonUtil.S(mContext, "dialog_tip");
		}

		public Builder setTitle(String title) {
			this.mTitle = title;
			return this;
		}

		public Builder setMessage(String message) {
			this.mMessage = message;
			return this;
		}

		public Builder setConfirm(String confirm) {
			this.mConfirm = confirm;
			return this;
		}

		public Builder setCancel(String cancel) {
			this.mCancel = cancel;
			return this;
		}

		public Builder setPositiveButton(OnClickListener listener) {
			this.mPositiveButtonClickListener = listener;
			return this;
		}

		public Builder setNegativeButton(OnClickListener listener) {
			this.mNegativeButtonClickListener = listener;
			return this;
		}

		public Builder setButtonState(int btnState) {
			this.mBtnState = btnState;
			return this;
		}

		public Builder setCanceable(boolean cancelable) {
			this.mCancelable = cancelable;
			return this;
		}

		public OH5Dialog create() {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final OH5Dialog xpDialog = new OH5Dialog(mContext, SOCommonUtil.getRes4Sty(mContext, "H5Dialog"));
			View layout = inflater.inflate(SOCommonUtil.getRes4Lay(mContext, "layout_dialog"), null);

			if (mTitle != null && !"".equals(mTitle)) {
				TextView titleView = (TextView) layout
						.findViewById(SOCommonUtil.getRes4Id(mContext, "tv_dialog_title"));
				titleView.setText(mTitle);
			}

			if (mMessage != null && !"".equals(mMessage)) {
				TextView messageView = (TextView) layout
						.findViewById(SOCommonUtil.getRes4Id(mContext, "tv_dialog_content"));
				messageView.setText(mMessage);
			}
			Button confirmView = (Button) layout
					.findViewById(SOCommonUtil.getRes4Id(mContext, "btn_dialog_confirm"));
			if (mConfirm != null && !"".equals(mConfirm))
				confirmView.setText(mConfirm);
			else
				confirmView.setVisibility(View.GONE);

			Button cancelView = (Button) layout.findViewById(SOCommonUtil.getRes4Id(mContext, "btn_dialog_cancel"));
			if (mCancel != null && !"".equals(mCancel))
				cancelView.setText(mCancel);
			else
				cancelView.setVisibility(View.GONE);

			if (mBtnState == BTN_STATE_CONFIRM) {
				confirmView.setTextColor(mContext.getResources().getColor(android.R.color.white));
				confirmView.setBackgroundResource(SOCommonUtil.getRes4Dra(mContext, "bg_tv_red"));
			} else if (mBtnState == BTN_STATE_CANCEL) {
				cancelView.setTextColor(mContext.getResources().getColor(android.R.color.white));
				cancelView.setBackgroundResource(SOCommonUtil.getRes4Dra(mContext, "bg_tv_red"));
			}

			if (mPositiveButtonClickListener != null) {
				Button btnOK = (Button) layout.findViewById(SOCommonUtil.getRes4Id(mContext, "btn_dialog_confirm"));
				btnOK.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mPositiveButtonClickListener.onClick(xpDialog, BUTTON_POSITIVE);
					}
				});
			}

			if (mNegativeButtonClickListener != null) {
				Button btnCancle = (Button) layout
						.findViewById(SOCommonUtil.getRes4Id(mContext, "btn_dialog_cancel"));
				btnCancle.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mNegativeButtonClickListener.onClick(xpDialog, BUTTON_NEGATIVE);
					}
				});
			}
			
			xpDialog.setCancelable(mCancelable);
			
			xpDialog.addContentView(layout, new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT));
			xpDialog.setContentView(layout);

			return xpDialog;
		}
	}
}
