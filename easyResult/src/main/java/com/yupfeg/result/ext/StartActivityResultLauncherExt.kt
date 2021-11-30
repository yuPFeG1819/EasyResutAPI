package com.yupfeg.result.ext

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.result.ActivityResult
import com.yupfeg.result.StartActivityResultLauncher

// <editor-fold desc="系统应用详情设置页">

/**
 * [StartActivityResultLauncher]的拓展函数，跳转到系统应用详情设置
 * @param callback 系统页面返回
 */
@Suppress("unused")
fun StartActivityResultLauncher.launchAppDetailSettings(callback : ()->Unit){
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", requireContext().packageName, null)
    }
    launch(intent) {
        callback.invoke()
    }
}

/**
 * [StartActivityResultLauncher]的拓展函数，应用详情设置页，并挂起等待页面返回
 * @return 系统页面返回
 * */
@Suppress("unused")
suspend fun StartActivityResultLauncher.launchAppDetailSettingAwait() : ActivityResult{
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", requireContext().packageName, null)
    }
    return launchAwait(intent)
}

// </editor-fold>

// <editor-fold desc="未知来源APK权限">

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
 * [StartActivityResultLauncher]的拓展函数，跳转到系统安装未知来源apk权限页面，并挂起等待页面返回
 * @return 是否允许安装apk权限
 * */
@Suppress("unused")
suspend fun StartActivityResultLauncher.launchInstallApkPermissionAwait() : Boolean{
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
        //Android 8.0以上需要请求安装未知来源的apk权限
        if (requireContext().packageManager.canRequestPackageInstalls()){
            //已允许安装apk权限
            return true
        }
        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
            data = Uri.parse("package:${requireContext().packageName}")
        }
        launchAwaitOrNull(intent)
        return requireContext().packageManager.canRequestPackageInstalls()
    }
    return true
}

// </editor-fold>

// <editor-fold desc="Android R的外部储存权限页面">

/**
 * [StartActivityResultLauncher]的拓展函数，跳转到申请开启外部储存权限页面
 * * 只在Android R后才生效，默认都为true
 * @param callback 系统页面返回，true-表示允许管理外部储存权限
 * */
@Suppress("unused")
fun StartActivityResultLauncher.launchExternalStorePermission(callback: (Boolean)->Unit){
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
 * [StartActivityResultLauncher]的拓展函数，跳转到申请系统外部储存权限页面，并挂起等待页面返回
 * * 只在Android R后才生效，默认都为true
 * @return 页面返回后，校验再次是否允许开启外部储存权限，true-表示允许开启储存权限
 */
@Suppress("unused")
suspend fun StartActivityResultLauncher.launchExternalStorePermissionAwait() : Boolean{
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
        !Environment.isExternalStorageManager()) {
        //Android 11.0以上需要请求外部储存权限
        val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
        launchAwaitOrNull(intent)
        return Environment.isExternalStorageManager()
    }
    return true
}

// </editor-fold>

// <editor-fold desc="系统悬浮窗口权限页">

/**
 * [StartActivityResultLauncher]的拓展函数，跳转到系统悬浮窗权限页面
 * @param callback 系统页面返回，true-表示允许悬浮窗权限
 * */
@Suppress("unused")
fun StartActivityResultLauncher.launchAlertWindowPermission(callback : (Boolean)->Unit){
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
    }
    //Android 6.0以下不需要申请权限，允许悬浮窗权限
    callback.invoke(true)
}

/**
 * [StartActivityResultLauncher]的拓展函数，跳转到系统悬浮窗权限页面，挂起并等待页面返回
 * @return 系统页面返回后，继续校验是否允许权限，true-表示允许悬浮窗权限
 * */
@Suppress("unused")
suspend fun StartActivityResultLauncher.launchAlertWindowPermissionAwait() : Boolean{
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
        !Settings.canDrawOverlays(requireContext())) {
        //Android 6.0以上需要请求悬浮窗权限
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
            data = Uri.parse("package:${requireContext().packageName}")
        }
        launchAwaitOrNull(intent)
        //返回是否允许设置悬浮窗
        return Settings.canDrawOverlays(requireContext())
    }
    //Android 6.0以下不需要申请权限，允许悬浮窗权限
    return true
}

// </editor-fold>

// <editor-fold desc="修改系统设置权限">

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
    }
    callback.invoke(true)
}

/**
 * [StartActivityResultLauncher]的拓展函数，跳转到修改系统设置权限页，挂起并等待页面返回
 * * 如改变屏幕亮度
 * @return 系统页面返回后，继续校验是否允许权限，true-表示允许修改系统设置
 * */
@Suppress("unused")
suspend fun StartActivityResultLauncher.launchWriteSettingPermissionAwait() : Boolean{
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
        !Settings.System.canWrite(requireContext())) {
        //Android 6.0以上需要请求修改系统设置权限
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
            data = Uri.parse("package:${requireContext().packageName}")
        }
        launchAwaitOrNull(intent)
        //返回是否允许修改系统设置
        return Settings.System.canWrite(requireContext())
    }
    return true
}

// </editor-fold>

// <editor-fold desc="系统定位功能页">

/**
 * [Context]的拓展函数，设备是否开启了定位功能
 * */
val Context.isLocationEnabled: Boolean
    get() = (getSystemService(Context.LOCATION_SERVICE) as LocationManager)
        .isProviderEnabled(LocationManager.GPS_PROVIDER)

/**
 * [StartActivityResultLauncher]的拓展函数，跳转到开启系统定位功能页
 * @param callback 页面返回回调，设备是否开启的定位功能
 * */
@Suppress("unused")
fun StartActivityResultLauncher.launchEnableLocation(callback: (Boolean) -> Unit){
    if (this.requireContext().isLocationEnabled){
        callback(true)
        return
    }
    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    launch(intent){
        callback(requireContext().isLocationEnabled)
    }
}

/**
 * [StartActivityResultLauncher]的拓展函数，跳转到系统定位功能页，并挂起等待页面返回
 * @return 设备是否开启了定位功能
 * */
@Suppress("unused")
suspend fun StartActivityResultLauncher.launchEnableLocationAwait() : Boolean{
    if (this.requireContext().isLocationEnabled) return true
    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    launchAwaitOrNull(intent)
    //再次校验是否开启了定位功能
    return requireContext().isLocationEnabled
}

// </editor-fold>