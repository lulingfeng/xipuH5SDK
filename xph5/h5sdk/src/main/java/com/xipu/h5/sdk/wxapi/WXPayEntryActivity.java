package com.xipu.h5.sdk.wxapi;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;

import com.xipu.h5_sdk.R;


public class WXPayEntryActivity extends Activity /*implements IWXAPIEventHandler*/ {

    private static final String TAG = WXPayEntryActivity.class.getName();
   // private IWXAPI api;
    private ViewGroup viewGroup;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_empty);
        // 父View
//		viewGroup = (ViewGroup) findViewById(android.R.id.content);
//		viewGroup.setVisibility(View.VISIBLE);
//		viewGroup.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				finish();
//			}
//		});
//		api = WXAPIFactory.createWXAPI(this, H5Utils.getWxAppId());
//		api.handleIntent(getIntent(), this);
//
//		String content = this.getIntent().getStringExtra("content");
//		try {
//			JSONObject json = new JSONObject(content);
//			if (null != json && !json.has("retcode")) {
//				PayReq req = new PayReq();
//				req.appId = json.getString("appid");
//				req.partnerId = json.getString("partnerid");
//				req.prepayId = json.getString("prepayid");
//				req.nonceStr = json.getString("noncestr");
//				req.timeStamp = json.getString("timestamp");
//				req.packageValue = json.getString("package");
//				req.sign = json.getString("sign");
//				req.extData = "app data";
//				api.sendReq(req);
//			}
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
    }

//	@Override
//	protected void onNewIntent(Intent intent) {
//		super.onNewIntent(intent);
//		setIntent(intent);
//		api.handleIntent(intent, this);
//	}
//
//	@Override
//	public void onReq(BaseReq req) {
//	}
//
//	private void pushResult(int errCode) {
//		Intent i;
//		i = new Intent(WXPayEntryActivity.this, BrowserActivity.class);
//		i.putExtra("errCode", errCode);
//		setResult(H5Config.WX_PAY, i);
//		finish();
//	}
//
//	@Override
//	public void onResp(BaseResp resp) {
//		Log.d(TAG, "onPayFinish, errCode = " + resp.errCode);
//		if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
//			pushResult(resp.errCode);
//		}
//	}
//
//	@Override
//	protected void onPause() {
//		super.onPause();
//		viewGroup.setVisibility(View.VISIBLE);
//	}
}