package com.zhangyue.ylyhe.ui;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.startobj.util.common.SOCommonUtil;
import com.zhangyue.ylyhe.custom.view.H5Dialog;
import com.zhangyue.ylyhe.util.PermissionsChecker;
import com.zhangyue.ylyhe.util.permission.PermissionUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * 权限获取页面
 */
public class PermissionsActivity extends Activity {

    public static final int PERMISSIONS_GRANTED = 0; // 权限授权
    public static final int PERMISSIONS_DENIED = 1; // 权限拒绝

    private static final int PERMISSION_REQUEST_CODE = 0; // 系统权限管理页面的参数
    private static final String EXTRA_PERMISSIONS = "me.chunyu.clwang.permission.extra_permission"; // 权限参数
    private static final String PACKAGE_URL_SCHEME = "package:"; // 方案

    private PermissionsChecker mChecker; // 权限检测器
    private boolean isRequireCheck; // 是否需要系统权限检测, 防止和系统提示框重叠
    private H5Dialog.Builder builder;
    private H5Dialog dialog;
    private H5Dialog.Builder openAppDetailsBuilder;
    private H5Dialog openAppDetailsDialog;

    // 启动当前权限页面的公开接口
    public static void startActivityForResult(Activity activity, int requestCode, String... permissions) {
        Intent intent = new Intent(activity, PermissionsActivity.class);
        intent.putExtra(EXTRA_PERMISSIONS, permissions);
        ActivityCompat.startActivityForResult(activity, intent, requestCode, null);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() == null || !getIntent().hasExtra(EXTRA_PERMISSIONS)) {
            throw new RuntimeException("PermissionsActivity需要使用静态startActivityForResult方法启动!");
        }
        setContentView(SOCommonUtil.getRes4Lay(this, "activity_permissions"));
        createMissingPermissionDialog();
        mChecker = new PermissionsChecker(this);
        isRequireCheck = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        obtainPermissions();
    }

    private void obtainPermissions() {
        if (isRequireCheck) {
            String[] permissions = getPermissions();
            if (mChecker.lacksPermissions(permissions)) {
                requestPermissions(permissions); // 请求权限
            } else {
                allPermissionsGranted(); // 全部权限都已获取
            }
        } else {
            isRequireCheck = true;
        }
    }

    // 返回传递的权限参数
    private String[] getPermissions() {
        return getIntent().getStringArrayExtra(EXTRA_PERMISSIONS);
    }

    // 请求权限兼容低版本
    private void requestPermissions(String... permissions) {
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
    }

    // 全部权限均已获取
    private void allPermissionsGranted() {
        setResult(PERMISSIONS_GRANTED);
        finish();
    }

    /**
     * 用户权限处理, 如果全部获取, 则直接过. 如果权限缺失, 则提示Dialog.
     *
     * @param requestCode  请求码
     * @param permissions  权限
     * @param grantResults 结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE && doRequestPermissionsResult(this, permissions, grantResults)) {
//        if (requestCode == PERMISSION_REQUEST_CODE && hasAllPermissionsGranted(grantResults)) {
            isRequireCheck = true;
            allPermissionsGranted();
        } else {
            isRequireCheck = false;
            showMissingPermissionDialog();
            List<String> deniedPermission = new ArrayList<>();
            //如果选择了“不再询问”，则弹出“权限指导对话框”
            for (int i = 0; i < permissions.length; i++) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                    deniedPermission.add(permissions[i]);
                }
            }
            if (deniedPermission.size() > 0) {
                String name = PermissionUtils.getInstance().getPermissionNames(deniedPermission);
                openAppDetails(name);
            }
        }
    }

    // 含有全部的权限
    private boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    private void createMissingPermissionDialog() {
        builder = new H5Dialog.Builder(this);
        builder.setTitle(SOCommonUtil.S(this, "help")).setMessage(SOCommonUtil.S(this, "help_text")).setConfirm(SOCommonUtil.S(this, "confirm"));
        builder.setCanceable(false);
        builder.setPositiveButton(new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
//                startAppSettings();
                isRequireCheck = true;
                obtainPermissions();
            }
        });
    }

    // 显示缺失权限提示
    private void showMissingPermissionDialog() {
        if (dialog == null) {
            dialog = builder.create();
            dialog.show();
//            dialog.dismiss();
        }
    }

    public boolean doRequestPermissionsResult(Activity activity, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //已全部授权
        if (hasAllPermissionsGranted(grantResults)) {
            return true;
        }
        return false;
    }

    // 启动应用的设置
    private void startAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse(PACKAGE_URL_SCHEME + getPackageName()));
        startActivity(intent);
    }

    /**
     * 打开APP详情页面，引导用户去设置权限
     *
     * @param permissionNames 权限名称（如是多个，使用\n分割）
     */
    public void openAppDetails(String permissionNames) {

        StringBuilder sb = new StringBuilder();
        sb.append(PermissionUtils.PermissionTip1);
        sb.append(permissionNames);
        sb.append(PermissionUtils.PermissionTip2);

        if (openAppDetailsBuilder == null) {
            openAppDetailsBuilder = new H5Dialog.Builder(this);
            openAppDetailsBuilder.setTitle(SOCommonUtil.S(this, "help")).setMessage(sb.toString()).setConfirm(PermissionUtils.PermissionDialogPositiveButton);
            openAppDetailsBuilder.setCanceable(false);
            openAppDetailsBuilder.setPositiveButton(new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    isRequireCheck = true;
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    startActivity(intent);
                }
            });
        }
        if (openAppDetailsDialog == null)
            openAppDetailsDialog = openAppDetailsBuilder.create();
        openAppDetailsDialog.show();
    }
}
