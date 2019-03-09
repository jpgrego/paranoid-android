package com.jpgrego.paranoidandroid.activities;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.fastaccess.permission.base.activity.BasePermissionActivity;
import com.fastaccess.permission.base.model.PermissionModel;
import com.fastaccess.permission.base.model.PermissionModelBuilder;
import com.jpgrego.paranoidandroid.R;

import java.util.ArrayList;
import java.util.List;

public final class PermissionsActivity extends BasePermissionActivity {

    @NonNull
    @Override
    protected List<PermissionModel> permissions() {
        final AppOpsManager appOpsManager =
                (AppOpsManager) getApplicationContext().getSystemService(Context.APP_OPS_SERVICE);
        final List<PermissionModel> permissions = new ArrayList<>();

        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(buildPermissionModel(R.string.permission_access_coarse_location_title,
                    R.string.permission_access_coarse_location_message,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    // Icon made by Pixel Buddha from www.flaticon.com
                    R.drawable.ic_map));
        }

        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(buildPermissionModel(R.string.permission_access_wifi_state_title,
                    R.string.permission_access_wifi_state_message,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    // Icon made by Pixel Buddha from www.flaticon.com
                    R.drawable.ic_wifi));
        }

        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(buildPermissionModel(R.string.permission_change_wifi_state_title,
                    R.string.permission_change_wifi_state_message,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    // Icon made by Pixel Buddha from www.flaticon.com
                    R.drawable.ic_wifi_change));
        }

        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(buildPermissionModel(R.string.permission_bluetooth_title,
                    R.string.permission_bluetooth_message,
                    Manifest.permission.BLUETOOTH,
                    // Icon made by Pixel Buddha from www.flaticon.com
                    R.drawable.ic_bluetooth));
        }

        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(buildPermissionModel(R.string.permission_bluetooth_admin_title,
                    R.string.permission_bluetooth_admin_message,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    // Icon made by Pixel Buddha from www.flaticon.com
                    R.drawable.ic_bluetooth));
        }

        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(buildPermissionModel(R.string.permission_read_phone_state_title,
                    R.string.permission_read_phone_state_message,
                    Manifest.permission.READ_PHONE_STATE,
                    // Icon made by Pixel Buddha from www.flaticon.com
                    R.drawable.ic_smartphone));
        }

        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(buildPermissionModel(R.string.permission_write_external_storage_title,
                    R.string.permission_write_external_storage_message,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    // Icon made by Pixel Buddha from www.flaticon.com
                    R.drawable.ic_folder));
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && appOpsManager != null) {
            final int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(), getApplicationContext().getPackageName());

            final boolean granted;

            if (mode == AppOpsManager.MODE_DEFAULT) {
                granted = (getApplicationContext().checkCallingOrSelfPermission(
                        android.Manifest.permission.PACKAGE_USAGE_STATS)
                        == PackageManager.PERMISSION_GRANTED);
            } else granted = mode == AppOpsManager.MODE_ALLOWED;

            if (!granted) {
                permissions.add(buildPermissionModel(R.string.permission_package_usage_stats_title,
                        R.string.permission_package_usage_stats_message,
                        Manifest.permission.PACKAGE_USAGE_STATS,
                        // Icon made by DinosoftLabs from www.flaticon.com
                        R.drawable.ic_send));
            }
        }

        return permissions;
    }

    private PermissionModel buildPermissionModel(final int titleID, final int messageID,
                                                 final String permissionName,
                                                 final int imageResourceId) {
        return PermissionModelBuilder.withContext(this)
                .withTitle(titleID)
                .withMessage(messageID)
                .withPermissionName(permissionName)
                .withImageResourceId(imageResourceId)
                .withCanSkip(true)
                .build();
    }

    @Override
    protected int theme() {
        return R.style.AppTheme;
    }

    @Override
    protected void onIntroFinished() {
        finish();
        startActivity(new Intent(this, WifiCellsBTActivity.class));
    }

    @Nullable
    @Override
    protected ViewPager.PageTransformer pagerTransformer() {
        return null;
    }

    @Override
    protected boolean backPressIsEnabled() {
        return false;
    }

    @Override
    protected void permissionIsPermanentlyDenied(@NonNull String permissionName) {
        Log.e("", "permission denied");
    }

    @Override
    protected void onUserDeclinePermission(@NonNull String permissionName) {
        Log.e("", "permission denied");
    }
}
