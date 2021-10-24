package com.yupfeg.result.ext

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import com.yupfeg.result.StartActivityResultLauncher

/**
 * [StartActivityResultLauncher]的拓展函数，跳转到系统安装未知来源apk权限页面
 * @param callback 系统页面返回，true-表示允许安装权限
 */
@Suppress("unused")
fun StartActivityResultLauncher.launchAppDetailSettings(callback : ()->Unit){
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", requireContext().packageName, null)
    }
    this.launch(intent) { callback.invoke() }
}

/**
 * [StartActivityResultLauncher]的拓展函数，跳转到系统安装未知来源apk权限页面
 * @param callback 系统页面返回，true-表示允许安装权限
 */
@Suppress("unused")
fun StartActivityResultLauncher.launchInstallApkPermission(callback : (Boolean)->Unit){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
        //Android 8.0以上需要请求安装未知来源的apk权限
        if (requireContext().packageManager.canRequestPackageInstalls()){
            //已允许安装apk权限
            callback.invoke(true)
            return
        }
        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
            data = Uri.parse("package:${requireContext().packageName}")
        }
        this.launch(intent){
            callback.invoke(requireContext().packageManager.canRequestPackageInstalls())
        }
    }else{
        callback.invoke(true)
    }
}

/**
 * [StartActivityResultLauncher]的拓展函数，跳转到申请开启外部储存权限页面
 * @param callback 系统页面返回，true-表示允许管理外部储存权限
 * */
@Suppress("unused")
fun StartActivityResultLauncher.launchManageExternalStorePermission(callback: (Boolean)->Unit){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
        !Environment.isExternalStorageManager()) {
        //Android 11.0以上需要请求外部储存权限
        val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
        launch(intent){
            callback.invoke(Environment.isExternalStorageManager())
        }
    }else{
        callback.invoke(true)
    }
}


/**
 * [StartActivityResultLauncher]的拓展函数，跳转到系统悬浮窗权限页面
 * @param callback 系统页面返回，true-表示允许悬浮窗权限
 * */
@Suppress("unused")
fun StartActivityResultLauncher.launchSystemAlertWindowPermission(callback : (Boolean)->Unit){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
        !Settings.canDrawOverlays(requireContext())) {
        //Android 6.0以上需要请求悬浮窗权限
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
            data = Uri.parse("package:${requireContext().packageName}")
        }
        launch(intent) {
            //返回是否允许设置悬浮窗
            callback.invoke(Settings.canDrawOverlays(requireContext()))
        }
    }else{
        //Android 6.0以下不需要申请权限，允许悬浮窗权限
        callback.invoke(true)
    }
}

/**
 * [StartActivityResultLauncher]的拓展函数，跳转到修改系统设置权限页
 * * 如改变屏幕亮度
 * @param callback 系统页面返回，true-表示允许修改系统设置
 * */
@Suppress("unused")
fun StartActivityResultLauncher.launchWriteSettingPermission(callback: (Boolean)->Unit){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
        !Settings.System.canWrite(requireContext())) {
        //Android 6.0以上需要请求修改系统设置权限
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
            data = Uri.parse("package:${requireContext().packageName}")
        }
        launch(intent){
            //返回是否允许修改系统设置
            callback.invoke(Settings.System.canWrite(requireContext()))
        }
    }else{
        callback.invoke(true)
    }
}