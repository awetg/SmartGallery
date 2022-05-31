package com.awetg.smartgallery.common.util

import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat

object PermissionUtil {
    const val PERMISSION_REQUEST_CODE = 1111

    private val requiredPermissions = arrayOf<String>(
        android.Manifest.permission.FOREGROUND_SERVICE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private fun getUnGrantedPermissions(activity: Activity): List<String> {
        fun isPermissionAvailable(permission: String): Boolean = activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED
        return requiredPermissions.filter {isPermissionAvailable(it)}
    }

    fun requestAllMissingPermissions(activity: Activity) {
        val unGrantedPermissions = getUnGrantedPermissions(activity)
        if (unGrantedPermissions.isNotEmpty()) {
            activity.requestPermissions(Array(unGrantedPermissions.size) {unGrantedPermissions[it]}, PERMISSION_REQUEST_CODE)
        }
    }

    fun allPermissionGranted(activity: Activity): Boolean {
        return getUnGrantedPermissions(activity).isEmpty()
    }

    // An activityType requesting permission can listen to onRequestPermissionsResult and take further action with this method if needed
    fun showDialogAndAsk(activity: Activity, title: String, message: String, onPositiveResponse: DialogInterface.OnClickListener? = null, onNegativeResponse: DialogInterface.OnClickListener? = null) {
        val alterDialog = AlertDialog.Builder(activity)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", onPositiveResponse)
            .setCancelable(false)
        if (onNegativeResponse != null) alterDialog.setNegativeButton("No", onNegativeResponse)
        alterDialog.show()
    }

    //    fun requestHighAPILevelPermissions() {
//        val missingPermissions = highLevelApiPermission.filter {
//            android.os.Build.VERSION.SDK_INT >= it.value && isPermissionAvailable(it.key)
//        }.map { it.key }
//        requestPermissions(Array(missingPermissions.size) {missingPermissions[it]})
//    }

    //        val permissionsWithRequestRationale = unGrantedPermissions.filter {
//            ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
//        }

    //    private val highLevelApiPermission = mapOf(
//        android.Manifest.permission.MANAGE_EXTERNAL_STORAGE to Build.VERSION_CODES.R,
//    )
}