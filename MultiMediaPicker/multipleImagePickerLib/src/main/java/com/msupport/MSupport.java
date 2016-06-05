
package com.msupport;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;

import com.rahul.media.R;

import java.util.ArrayList;
import java.util.List;

public class MSupport {

    private static boolean isMSupportDevice(Context ctx) {
        return Build.VERSION.SDK_INT >= MSupportConstants.SDK_VERSION;
    }

    public static boolean checkMultiplePermission(final Activity mActivity, String[] permissionsList2, final int requestCode) {
        if (MSupport.isMSupportDevice(mActivity)) {

            List<String> permissionsNeeded = new ArrayList<>();
            final List<String> permissionsList = new ArrayList<>();

            for (String permission : permissionsList2) {
                if (addPermission(mActivity, permission, permissionsList)) {
                    permissionsNeeded.add(MSupportConstants.getPermissionRationaleMessage(permission));
                }
            }

            if (permissionsList.size() > 0) {
                if (permissionsNeeded.size() > 0) {
                    // Need Rationale case
                    String message = "You need to grant access to " + permissionsNeeded.get(0);
                    for (int i = 1; i < permissionsNeeded.size(); i++)
                        message = message + ", " + permissionsNeeded.get(i);
                    showMessageOKCancel(mActivity, message,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    requestPermissions(permissionsList, null, mActivity,
                                            requestCode);
                                }
                            });
                    return false;
                }
                requestPermissions(permissionsList, null, mActivity,
                        requestCode);
                return false;
            } else
                return true;
        } else
            return true;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private static boolean addPermission(final Activity mActivity, String permission, List<String> permissionsList) {
        if (mActivity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (mActivity.shouldShowRequestPermissionRationale(permission))
                return true;
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static boolean checkPermission(final Activity mActivity, final Fragment fragment, String[] permissionSet,
                                          final int requestCode) {

        if (MSupport.isMSupportDevice(mActivity)) {

            final List<String> permissions = new ArrayList<>();
            for (final String aPermissionSet : permissionSet) {
                int hasPermission = mActivity.checkSelfPermission(aPermissionSet);
                if (hasPermission != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, aPermissionSet)) {

                        String msg = "";
                        if (aPermissionSet.equals(MSupportConstants.CAMERA)) {
                            msg = mActivity.getString(R.string.camera_permission_rationale);
                        } else if (aPermissionSet.equals(MSupportConstants.WRITE_EXTERNAL_STORAGE)) {
                            msg = mActivity.getString(R.string.storage_permission_rationale);
                        }

                        showMessageOKCancel(mActivity, msg,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        permissions.add(aPermissionSet);
                                        requestPermissions(permissions, fragment, mActivity, requestCode);
                                    }
                                });
                    } else {
                        permissions.add(aPermissionSet);
                        requestPermissions(permissions, fragment, mActivity, requestCode);
                    }
                }
            }

            if (!permissions.isEmpty()) {
                if (fragment != null)
                    fragment.requestPermissions(permissions.toArray(new String[permissions.size()]), requestCode);
                else
                    mActivity.requestPermissions(permissions.toArray(new String[permissions.size()]), requestCode);
                return false;
            } else
                return true;

        } else
            return true;
    }


    @TargetApi(23)
    public static boolean checkPermissionWithRationale(final Activity mActivity, final Fragment fragment,
                                                       final String permission, final int requestCode) {

        if (MSupport.isMSupportDevice(mActivity)) {

            final List<String> permissions = new ArrayList<>();
            int hasPermission = mActivity.checkSelfPermission(permission);

            if (hasPermission != PackageManager.PERMISSION_GRANTED)
                if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permission)) {

                    String msg = "";
                    if (requestCode == MSupportConstants.CAMERA_PERMISSIONS_REQUEST_CODE) {
                        msg = mActivity.getString(R.string.camera_permission_rationale);
                    } else if (requestCode == MSupportConstants.REQUEST_STORAGE_READ_WRITE) {
                        msg = mActivity.getString(R.string.storage_permission_rationale);
                    }

                    showMessageOKCancel(mActivity, msg,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    permissions.add(permission);
                                    requestPermissions(permissions, fragment, mActivity, requestCode);
                                }
                            });


                    return false;

                } else {
                    permissions.add(permission);
                    requestPermissions(permissions, fragment, mActivity, requestCode);
                    return false;
                }
            else
                return true;

        }
        return true;
    }

    @TargetApi(23)
    private static boolean requestPermissions(List<String> permissions, Fragment fragment, Activity mActivity, int requestCode) {
        if (!permissions.isEmpty()) {
            if (fragment != null)
                fragment.requestPermissions(permissions.toArray(new String[permissions.size()]), requestCode);
            else
                mActivity.requestPermissions(permissions.toArray(new String[permissions.size()]), requestCode);
            return false;
        } else
            return true;
    }

    private static void showMessageOKCancel(Activity activity, String message
            , DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(new ContextThemeWrapper(activity, R.style.Theme_AppCompat_Dialog_Alert))
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .create()
                .show();
    }


}