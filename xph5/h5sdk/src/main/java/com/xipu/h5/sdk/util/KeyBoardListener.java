package com.xipu.h5.sdk.util;

import android.app.Activity;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.startobj.util.device.SODensityUtil;
import com.tencent.smtt.sdk.WebView;

/**
 * 解决webView键盘遮挡问题的类 Created by zqy on 2016/11/14.
 */
public class KeyBoardListener {
    private Activity mActivity;
    private final OnChangeHeightListener mOnChangeHeightListener;
    private WebView mWebView;
    private int mY;
    private static int mHeight;

    private View mChildOfContent;
    private int usableHeightPrevious;
    private FrameLayout.LayoutParams frameLayoutParams;

    private static KeyBoardListener keyBoardListener;

    public static KeyBoardListener getInstance(Activity activity, WebView webView, OnChangeHeightListener onChangeHeightListener) {
        if (keyBoardListener == null) {
            keyBoardListener = new KeyBoardListener(activity, webView, onChangeHeightListener);
        }
        mHeight = SODensityUtil.getScreenHeight(activity);
        return keyBoardListener;
    }

    public KeyBoardListener(Activity activity, WebView webView, OnChangeHeightListener onChangeHeightListener) {
        this.mActivity = activity;
        this.mOnChangeHeightListener = onChangeHeightListener;
        this.mWebView = webView;
    }

    public interface OnChangeHeightListener {
        void onShow(int usableHeightNow);

        void onHidden();
    }

    public void init() {
        FrameLayout content = (FrameLayout) mActivity.findViewById(android.R.id.content);
        mChildOfContent = content.getChildAt(0);
        mWebView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mY = (int) event.getY();
                return false;
            }
        });
        mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                possiblyResizeChildOfContent();
            }
        });
        frameLayoutParams = (FrameLayout.LayoutParams) mChildOfContent.getLayoutParams();

    }

    private void possiblyResizeChildOfContent() {
        int usableHeightNow = computeUsableHeight();
        if (usableHeightNow != usableHeightPrevious) {
            int usableHeightSansKeyboard = mChildOfContent.getRootView().getHeight();
            int heightDifference = usableHeightSansKeyboard - usableHeightNow;
            if (heightDifference > (usableHeightSansKeyboard / 4)) {
                // keyboard probably just became visible
                mOnChangeHeightListener.onShow(usableHeightNow);
//                frameLayoutParams.height = usableHeightSansKeyboard - heightDifference;
                Log.d("websssss", "mY: " + mY);
                Log.d("websssss", "usableHeightNow: " + usableHeightNow);
                if (usableHeightNow < mY) {
                    double postion = mY * 1.0 / mHeight;
                    if (postion < 0.6 && postion > 0.4) {
                        frameLayoutParams.topMargin = -(usableHeightNow - 50);
                        frameLayoutParams.bottomMargin = usableHeightNow - 50;
                    } else {
                        frameLayoutParams.topMargin = -heightDifference;
                        frameLayoutParams.bottomMargin = heightDifference;
                    }
                }
            } else {
                // keyboard probably just became hidden
                mOnChangeHeightListener.onHidden();
//                frameLayoutParams.height = usableHeightSansKeyboard;

                frameLayoutParams.topMargin = 0;
                frameLayoutParams.bottomMargin = 0;
            }
            mChildOfContent.requestLayout();
            usableHeightPrevious = usableHeightNow;
        }
    }

    private int computeUsableHeight() {
        Rect r = new Rect();
        mChildOfContent.getWindowVisibleDisplayFrame(r);
        return r.bottom;
    }


}
