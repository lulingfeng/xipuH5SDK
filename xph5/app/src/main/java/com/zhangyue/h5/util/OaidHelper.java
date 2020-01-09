package com.zhangyue.h5.util;

import android.content.Context;
import android.util.Log;

import com.bun.miitmdid.core.ErrorCode;
import com.bun.miitmdid.core.IIdentifierListener;
import com.bun.miitmdid.core.MdidSdkHelper;
import com.bun.miitmdid.supplier.IdSupplier;
import com.startobj.util.toast.SOToastUtil;

/**
 * @author lulingfeng
 * @project_name OaidHelper
 * @email lingfeng.lu@xipu.com
 * @create_time 2019/12/2 14:24
 * @describe 通过反射回调，避免Android 9.0+无法加载类，获取oaid标识符。
 * 目前支持机型可以在 http://www.msa-alliance.cn/col.jsp?id=120 查看
 */
public class OaidHelper implements IIdentifierListener {
    private static String TAG = OaidHelper.class.getSimpleName();

    @Override
    public void OnSupport(boolean isSupport, IdSupplier idSupplier) {
        if (idSupplier == null) {
            Log.e(TAG, "OnSupport: ");
            return;
        }
        String oaid = idSupplier.getOAID();
        Log.e(TAG, "OnSupport:true " + oaid);
        idSupplier.shutDown();
        H5Utils.setOaid(oaid);
    }

    /**
     * @return 回调参数
     * 初始化SDK
     */
    private int initOaidSDK(Context context) throws Exception {
        return MdidSdkHelper.InitSdk(context, true, OaidHelper.this);
    }

    public void getDeviceOaid(Context context) {
        int errorCode = 0;
        try {
            errorCode = initOaidSDK(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (errorCode == ErrorCode.INIT_ERROR_DEVICE_NOSUPPORT) {//不支持的设备
            Log.e(TAG, "getDeviceOaid:不支持的设备 ");
            return;
        } else if (errorCode == ErrorCode.INIT_ERROR_LOAD_CONFIGFILE) {//加载配置文件出错
            Log.e(TAG, "getDeviceOaid:加载配置文件出错 ");
            return;
        } else if (errorCode == ErrorCode.INIT_ERROR_MANUFACTURER_NOSUPPORT) {//不支持的设备厂商
            Log.e(TAG, "getDeviceOaid: 不支持的设备厂商");
            return;
        } else if (errorCode == ErrorCode.INIT_ERROR_RESULT_DELAY) {//获取接口是异步的，结果会在回调中返回，回调执行的回调可能在工作线程
            Log.e(TAG, "getDeviceOaid: 获取接口是异步的，结果会在回调中返回");
            return;
        } else if (errorCode == ErrorCode.INIT_HELPER_CALL_ERROR) {//反射调用出错
            Log.e(TAG, "getDeviceOaid: 反射调用出错");
            return;
        }
    }

}
