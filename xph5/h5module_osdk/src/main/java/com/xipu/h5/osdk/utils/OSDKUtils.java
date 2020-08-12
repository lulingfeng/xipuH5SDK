package com.xipu.h5.osdk.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.startobj.util.common.SOCommonUtil;
import com.startobj.util.file.SOFileUtil;
import com.xipu.h5.osdk.config.OSDKConfig;
import com.xipu.h5.sdk.config.H5Config;
import com.xipu.h5.sdk.model.UserModel;
import com.xipu.h5.sdk.util.H5Utils;
import com.xipu.h5.sdk.util.SharedPreferencesUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class OSDKUtils {

    private static OSDKUtils mInstance;
    //  public static String TAG = "H5_OSDK";
    public static String TAG = "H5SDK_TAG";
    private String mGoogleWebClientId;
    private String mLineChannelId;
    // AppsFlyerDevKey
    private String mAppsFlyerDevKey;

    public static OSDKUtils getInstance() {
        if (mInstance == null) {
            synchronized (OSDKUtils.class) {
                if (mInstance == null) {
                    mInstance = new OSDKUtils();
                }
            }
        }
        return mInstance;
    }

    public String getGoogleWebClientId() {
        return mGoogleWebClientId;
    }

    public void setGoogleWebClientId(String mGoogleWebClientId) {
        this.mGoogleWebClientId = mGoogleWebClientId;
    }

    public String getLineChannelId() {
        return mLineChannelId;
    }

    public void setLineChannelId(String mLineChannelId) {
        this.mLineChannelId = mLineChannelId;
    }

    public String getAppsFlyerDevKey() {
        return mAppsFlyerDevKey;
    }

    public void setAppsFlyerDevKey(String mAppsFlyerDevKey) {
        this.mAppsFlyerDevKey = mAppsFlyerDevKey;
    }

    public void loadThirdConfig(Context context) {
        getGoogleClientId(context);
        getLineChannelId(context);
        getAF_DEV_KEY(context);
    }

    private String getGoogleClientId(Context context) {
        String mGoogleClientId = "";
        try {
            mGoogleClientId = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA)
                    .metaData.get("xp_google_web_client_id").toString();
            setGoogleWebClientId(mGoogleClientId);
        } catch (Exception e) {
            Log.e(TAG, "获取 google web client id 出错 " + e.getMessage());
        }
        if (TextUtils.isEmpty(mGoogleClientId))
            Toast.makeText(context, "google web client id NULL", Toast.LENGTH_SHORT).show();
        return mGoogleClientId;
    }

    private String getLineChannelId(Context context) {
        String mLineChannelId = "";
        try {
            mLineChannelId = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA)
                    .metaData.get("xp_line_channel_id").toString();
            setLineChannelId(mLineChannelId);
        } catch (Exception e) {
            Log.e(TAG, "获取 line channel id 出错 " + e.getMessage());
        }
        if (TextUtils.isEmpty(mLineChannelId))
            Toast.makeText(context, "line channel id NULL", Toast.LENGTH_SHORT).show();
        return mLineChannelId;
    }

    private String getAF_DEV_KEY(Context context) {
        String mAppsFlyerDevKey = "";
        try {
            mAppsFlyerDevKey = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA)
                    .metaData.get("xp_af_dev_key").toString();
            setAppsFlyerDevKey(mAppsFlyerDevKey);
        } catch (Exception e) {
            Log.e(OSDKUtils.TAG, "获取 af_dev_key 出错 " + e.getMessage());
        }
        if (TextUtils.isEmpty(mAppsFlyerDevKey))
            Toast.makeText(context, "af_dev_key NULL", Toast.LENGTH_SHORT).show();
        return mAppsFlyerDevKey;
    }

    public boolean checkApkExist(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName))
            return false;
        try {
            ApplicationInfo info = context.getPackageManager()
                    .getApplicationInfo(packageName,
                            PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }


    public synchronized void addGoogleOrderInfo(Context context, String orderNo, HashMap<String, String> thirdPayMaps) {
        if (!SOCommonUtil.hasContext(context))
            return;
        HashMap<String, String> maps = getGoogleOrderInfoList(context);
        maps.put(orderNo, JSON.toJSONString(thirdPayMaps));
        Log.d(TAG, "addGoogleOrderInfo: " + maps.toString() + "，size: " + maps.size());
        saveGoogleOrderInfo(context, maps);
    }

    public synchronized HashMap<String, String> getGoogleOrderInfoList(Context context) {
        HashMap<String, String> maps = new HashMap<String, String>();
        if (!SOCommonUtil.hasContext(context))
            return maps;

        String json = SharedPreferencesUtil.getInstance(context).getString(OSDKConfig.GOOGLE_ORDER_INFO, "");
        if (TextUtils.isEmpty(json))
            return maps;
        try {
            maps.putAll(JSON.parseObject(json, new TypeReference<HashMap<String, String>>() {
            }));
            Log.d(TAG, "getGoogleOrderInfoList: " + maps.toString() + "，size: " + maps.size());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return maps;
        }
    }

    public synchronized void removeGoogleOrderInfo(Context context, String orderNo) {
        HashMap<String, String> maps = getGoogleOrderInfoList(context);
        if (maps.containsKey(orderNo)) {
            maps.remove(orderNo);
            saveGoogleOrderInfo(context, maps);
        }
    }

    private synchronized void saveGoogleOrderInfo(Context context, HashMap<String, String> maps) {
        if (!SOCommonUtil.hasContext(context))
            return;
        Log.d(TAG, "saveGoogleOrderInfo(): " + maps.toString() + "，size: " + maps.size());
        SharedPreferences.Editor editor = SharedPreferencesUtil.getInstance(context).edit();
        editor.putString(OSDKConfig.GOOGLE_ORDER_INFO,
                JSON.toJSONString(maps, false)).apply();
    }


    /**
     * 读取记录的用户列表 比对SDK版本，当前版本则读取，非当前版本则删除文件。
     */
    public void loadUserList(Context context, Handler checkConfigHandle) {
        Log.d(OSDKUtils.TAG, "OSDK loadUserList()");
        if (!SOCommonUtil.hasContext(context))
            return;
        try {
            String raw = "";
            String sdNewPath = SOFileUtil.obtainFilePath(context, H5Config.SDK_FOLDER_LOCATION,
                    OSDKConfig.SDK_OVERSEA_FILE_NAME);
            String appNewPath = context.getFilesDir() + File.separator + OSDKConfig.SDK_OVERSEA_FILE_NAME;

            Log.d(OSDKUtils.TAG, "OSDK read oversea account file");
            if (SOFileUtil.isFileExist(sdNewPath)) {  // sd卡文件存在 优先读取 sd 卡数据
                raw = SOFileUtil.readFileSdcardFile(sdNewPath);
                if (!TextUtils.isEmpty(raw)) {
                    SOFileUtil.writeFileSdcardFile(appNewPath, raw, false);  // 备份
                }
            } else {  // sd卡文件不存在 读取应用目录下文件数据
                if (SOFileUtil.isFileExist(appNewPath)) {
                    raw = SOFileUtil.readFileSdcardFile(appNewPath);
                    if (!TextUtils.isEmpty(raw)) {
                        SOFileUtil.writeFileSdcardFile(sdNewPath, raw, false);  // 备份
                    }
                }
            }

            Log.d(OSDKUtils.TAG, "OSDK sdNewPath: " + sdNewPath);
            Log.d(OSDKUtils.TAG, "OSDK appNewPath: " + appNewPath);


            if (TextUtils.isEmpty(raw)) {
                return;
            }

            int f = raw.indexOf("[") + 1;
            int l = raw.indexOf("]");
            String subStr = "";
            if (l != -1) {
                subStr = raw.substring(f, l);
            }
            if (H5Config.SDK_VERSION.equals(subStr) || "".equals(subStr)) {
                String decrypt = "";
                if ("".equals(subStr)) {
                    decrypt = SOFileUtil.decrypt(raw.substring("log_msg:".length()));
                } else {
                    decrypt = SOFileUtil.decrypt(raw.substring(l + 1));
                }

                List<UserModel> userModels = JSON.parseArray(decrypt, UserModel.class);

                if (userModels == null) {
                    userModels = new ArrayList<>();
                }

                setUserModelList(context, userModels);
                Log.d(OSDKUtils.TAG, "OSDK readOverseaAccFile: " + ",UserModelList size: " + H5Utils.getUserModelList().size());
            } else {
                SOFileUtil.deleteFile(sdNewPath);
            }
            if (checkConfigHandle != null) {
                checkConfigHandle.sendEmptyMessage(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取海外游客账户
     *
     * @return
     */
    public List<UserModel> getOverseaAccList() {
        List<UserModel> overseaList = new ArrayList<>();
        for (UserModel userModel : H5Utils.getUserModelList()) {
            if (userModel != null && userModel.getAccount_type().equalsIgnoreCase(OLoginTypeUtils.GUEST)) {
                overseaList.add(userModel);
            }
        }
        return overseaList;
    }

    public void setUserModelList(Context activity, List<UserModel> userModelList) {
        H5Utils.getUserModelList().addAll(userModelList);
        persistOverseaUserList(activity);
    }

    // 更新用户列表
    public void saveOverseaUserEntityList(Activity activity) {
        for (int i = 0; i < H5Utils.getUserModelList().size(); i++) {
            Log.d(OSDKUtils.TAG, i + "-------" + H5Utils.getUserModelList().get(i));
            if (H5Utils.getUserModelList().get(i) == null) {
                H5Utils.getUserModelList().remove(i);
                continue;
            }
            if (!TextUtils.isEmpty(H5Utils.getUserModelList().get(i).getOpenid()) && !TextUtils.isEmpty(H5Utils.getUserModel().getOpenid())
                    && H5Utils.getUserModelList().get(i).getOpenid().equals(H5Utils.getUserModel().getOpenid()) &&
                    !TextUtils.isEmpty(H5Utils.getUserModelList().get(i).getAccount_type()) && !TextUtils.isEmpty(H5Utils.getUserModel().getAccount_type()) &&
                    H5Utils.getUserModelList().get(i).getAccount_type().equalsIgnoreCase(H5Utils.getUserModel().getAccount_type())) {
                Log.d(OSDKUtils.TAG, H5Utils.getUserModelList().get(i).getOpenid() + "-------" + H5Utils.getUserModel().getOpenid());
                H5Utils.getUserModelList().remove(i);
                break;
            }
        }

        H5Utils.getUserModelList().add(H5Utils.getUserModel());
        Collections.reverse(H5Utils.getUserModelList());
        persistOverseaUserList(activity);
    }

    // 持久化存储用户列表至App/File&SD卡
    private void persistOverseaUserList(Context activity) {
        try {
            String[] path = {activity.getFilesDir() + File.separator + OSDKConfig.SDK_OVERSEA_FILE_NAME,
                    SOFileUtil.obtainFilePath(activity, H5Config.SDK_FOLDER_LOCATION, OSDKConfig.SDK_OVERSEA_FILE_NAME)};

            for (int i = 0; i < path.length; i++) {
                SOFileUtil.writeFileSdcardFile(path[i], "log_msg:[" + H5Config.SDK_VERSION + "]"
                        + SOFileUtil.encrypt(JSON.toJSONString(H5Utils.getUserModelList())), false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
