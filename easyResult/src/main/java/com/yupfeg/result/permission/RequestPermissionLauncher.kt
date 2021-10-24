package com.yupfeg.result.permission

import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.yupfeg.result.permission.dialog.PermissionRationaleDialogFragment
import com.yupfeg.result.ActivityResultLauncherWrapper
import com.yupfeg.result.StartActivityResultLauncher
import com.yupfeg.result.ext.launchAppDetailSettings

/**
 * 动态请求权限的启动器
 * * 支持请求多个权限
 * @author yuPFeG
 * @date 2021/10/20
 */
@Suppress("unused")
open class RequestPermissionLauncher(
    caller : ActivityResultCaller
) : ActivityResultLauncherWrapper<Array<String>, Map<String, Boolean>>(
    caller, ActivityResultContracts.RequestMultiplePermissions()
){

    /**跳转到app系统设置页的启动器*/
    private val mSettingsLauncher = StartActivityResultLauncher(caller)

    /**
     * 发起权限请求
     * @param init 以kotlin dsl的方式进行权限请求的配置
     * */
    open fun launchRequest(init : RequestPermissionConfig.()->Unit){
        val config = RequestPermissionConfig().also(init)
        launchRequest(config)
    }

    /**
     * 发起权限请求
     * @param requestConfig 请求权限配置类
     * */
    @Suppress("MemberVisibilityCanBePrivate")
    open fun launchRequest(requestConfig: RequestPermissionConfig){
        if (requestConfig.permissions.isNullOrEmpty()) {
            throw NullPointerException("request permission is null ,check your code!")
        }

        if (!requestConfig.isShowRationalDialog){
            //不需要显示申请原因弹窗，直接请求权限
            performRequestPermission(requestConfig)
            return
        }

        //发起请求前，需要显示请求理由说明弹窗
        val requestPermissions = requestConfig.permissions!!.toList()
        val deniedList = getNeedRequestPermissions(requestPermissions)
        requestConfig.rationaleDialogFragment?.also {dialogFragment->
            showRationaleDialog(
                deniedList,
                dialogFragment,
                onPositive = {
                    //申请权限
                    performRequestPermission(requestConfig)
                },
                onNegative = {
                    requestConfig.onDeniedAction?.invoke(deniedList)
                }
            )
        }?:run {
            requestConfig.onDeniedAction?.invoke(deniedList)
        }
    }

    /**
     * 获取需要申请（未允许）的权限集合
     * @param permissions 权限集合
     * */
    @Suppress("MemberVisibilityCanBePrivate")
    protected open fun getNeedRequestPermissions(permissions : List<String>) : List<String>{
        return permissions.filter { permission->
            !caller.checkPermissionGranted(permission)
        }
    }

    /**
     * 执行动态请求权限
     * @param requestConfig 请求权限配置
     * */
    @Suppress("MemberVisibilityCanBePrivate")
    protected open fun performRequestPermission(requestConfig: RequestPermissionConfig){
        launch(requestConfig.permissions!!){map->
            processRequestPermissionResult(requestConfig,map)
        }
    }

    /**
     * 处理权限请求回调结果
     * @param requestConfig 权限请求配置
     * @param grantResults 权限请求回调结果键值对
     * */
    private fun processRequestPermissionResult(
        requestConfig: RequestPermissionConfig,
        grantResults: Map<String, Boolean>
    ){
        if (!grantResults.containsValue(false)){
            //所有权限都通过
            requestConfig.onAllGrantedAction?.invoke()
            return
        }
        //需要显示请求权限说明的权限集合
        val showRationalPermissions = mutableListOf<String>()
        //永久拒绝的权限集合
        val permanentDeniedPermissions = mutableListOf<String>()
        val definedPermissions = mutableListOf<String>()
        for ((permission, granted) in grantResults) {
            if (granted) continue
            if (caller.shouldShowRequestPermissionRationale(permission)){
                //需要显示解释说明
                showRationalPermissions.add(permission)
            }else{
                //永久拒绝的权限
                permanentDeniedPermissions.add(permission)
            }
            definedPermissions.add(permission)
        }

        when {
            showRationalPermissions.isNotEmpty() -> {
                //存在需要解释请求理由的被拒绝权限
                processRationalePermissions(
                    requestConfig,
                    showRationalPermissions,
                    permanentDeniedPermissions
                )
            }
            permanentDeniedPermissions.isNotEmpty() -> {
                //存在永久拒绝的权限
                processPermanentDeniedPermissions(
                    requestConfig,
                    permanentDeniedPermissions,
                    definedPermissions
                )
            }
            else -> {
                //兜底的请求权限失败回调
                requestConfig.onDeniedAction?.invoke(definedPermissions)
            }
        }
    }

    /**
     * 处理需要解释请求原因的权限
     * @param requestConfig 请求参数配置
     * @param rationalePermission 需要解释理由的权限集合
     * @param definedPermissions 所有被拒绝的权限集合
     *
     * */
    private fun processRationalePermissions(
        requestConfig: RequestPermissionConfig,
        rationalePermission : List<String>,
        definedPermissions : List<String>
    ) {
        if (!requestConfig.isShowRationalDialogAfterDefined) {
            //不需要在拒绝后显示请求原因弹窗
            requestConfig.onDeniedAction?.invoke(definedPermissions)
            return
        }

        requestConfig.rationaleDialogFragment?.also {dialogFragment->
            showRationaleDialog(
                rationalePermission,
                dialogFragment,
                onPositive = {
                    //再次请求权限
                    performRequestPermission(requestConfig)
                },
                onNegative = {
                    requestConfig.onDeniedAction?.invoke(definedPermissions)
                }
            )
        }?:run {
            //不需要在拒绝后显示请求原因弹窗
            requestConfig.onDeniedAction?.invoke(definedPermissions)
        }
    }

    /**
     * 处理被永久拒绝的权限
     * @param requestConfig 请求参数配置
     * @param permanentDeniedPermissions 需要解释理由的权限集合
     * @param definedPermissions 所有被拒绝的权限集合
     * */
    private fun processPermanentDeniedPermissions(
        requestConfig: RequestPermissionConfig,
        permanentDeniedPermissions : List<String>,
        definedPermissions: List<String>
    ){
        requestConfig.forwardSettingDialogFragment?.also {dialogFragment->
            showRationaleDialog(
                permanentDeniedPermissions,
                dialogFragment,
                onPositive = {
                    //引导跳转到App系统设置详情页，引导用户开启权限
                    mSettingsLauncher.launchAppDetailSettings {
                        //再次申请被永久拒绝的权限
                        performRequestPermission(requestConfig)
                    }
                },
                onNegative = {
                    requestConfig.onDeniedAction?.invoke(definedPermissions)
                }
            )
        }?:run {
            requestConfig.onDeniedAction?.invoke(definedPermissions)
        }
    }

    /**
     * 显示权限请求理由弹窗
     * @param requestPermissions 当前需要请求的权限（过滤已允许的权限与永久拒绝的权限）
     * @param dialogFragment 申请理由说明弹窗
     * @param onPositive 确认按钮点击回调
     * @param onNegative 取消按钮点击回调
     * */
    private fun showRationaleDialog(
        requestPermissions : List<String>,
        dialogFragment : PermissionRationaleDialogFragment,
        onPositive : ()->Unit,
        onNegative : ()->Unit,
    ){
        dialogFragment.apply {
            showNow(caller.getFragmentManager(),"PermissionRationaleDialogFragment")
            dialogFragment.setRequestPermissions(requestPermissions)
            isCancelable = false
            //确认按钮
            getPositiveView().apply {
                isClickable = true
                setOnClickListener {
                    dialogFragment.dismiss()
                    onPositive.invoke()
                }
            }
            //取消按钮
            getNegativeView()?.apply {
                isClickable = true
                setOnClickListener {
                    dialogFragment.dismiss()
                    onNegative.invoke()
                }
            }
        }
    }

    /**
     * [ActivityResultCaller]的拓展函数，校验指定权限是否需要显示权限说明
     * @param permission 权限名称
     * */
    private fun ActivityResultCaller.shouldShowRequestPermissionRationale(permission: String) =
        when (this) {
            is Activity -> shouldShowRequestPermissionRationale(this, permission)
            is Fragment -> shouldShowRequestPermissionRationale(requireActivity(), permission)
            else -> false
        }

    /**
     * [ActivityResultCaller]的拓展函数，校验指定权限是否已同意
     * @param permission 权限名称
     * */
    private fun ActivityResultCaller.checkPermissionGranted(permission: String) : Boolean{
        return when (this) {
            is Activity -> {
                ActivityCompat.checkSelfPermission(
                    this,permission
                ) == PackageManager.PERMISSION_GRANTED
            }
            is Fragment -> {
                ActivityCompat.checkSelfPermission(
                    requireActivity(),permission
                ) == PackageManager.PERMISSION_GRANTED
            }
            else -> false
        }
    }

    /**
     * [ActivityResultCaller]的拓展函数，获取fragment的管理类对象
     * */
    protected open fun ActivityResultCaller.getFragmentManager() : FragmentManager {
        return when(this){
            is FragmentActivity -> this.supportFragmentManager
            is Fragment -> this.childFragmentManager
            else -> throw IllegalArgumentException(
                "ActivityResultCaller must is FragmentActivity or Fragment"
            )
        }
    }
}