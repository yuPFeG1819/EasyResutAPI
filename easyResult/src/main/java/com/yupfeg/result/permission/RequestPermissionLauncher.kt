package com.yupfeg.result.permission

import androidx.activity.result.ActivityResultCaller
import com.yupfeg.result.permission.dialog.PermissionRationaleDialogFragment
import com.yupfeg.result.StartActivityResultLauncher
import com.yupfeg.result.ext.launchAppDetailSettingAwait
import com.yupfeg.result.ext.launchAppDetailSettings
import com.yupfeg.result.ext.launchAwait
import com.yupfeg.result.permission.config.RequestPermissionConfig
import com.yupfeg.result.permission.config.RequestPermissionResultConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * 动态请求权限的启动器
 * * 支持请求多个权限
 * @author yuPFeG
 * @date 2021/10/20
 */
@Suppress("unused")
open class RequestPermissionLauncher(
    caller : ActivityResultCaller
) : BaseRequestPermissionLauncher(caller){

    /**跳转到app系统设置页的启动器*/
    private val mSettingsLauncher = StartActivityResultLauncher(caller)

    // <editor-fold desc="对外普通函数">

    /**
     * 发起权限请求
     * @param init 以kotlin dsl的方式进行权限请求的配置
     * */
    open fun launchRequest(init : RequestPermissionResultConfig.()->Unit){
        val config = RequestPermissionResultConfig().also(init)
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
        val dialogFragment = requestConfig.rationaleDialogFragment
        dialogFragment ?:run {
            //不需要显示申请原因弹窗，直接请求权限
            performRequestPermission(requestConfig)
            return
        }
        val requestPermissions = requestConfig.permissions!!.toList()
        val needRequestList = getNeedRequestPermissions(requestPermissions)

        val callBackConfig = (requestConfig as? RequestPermissionResultConfig)
        if (needRequestList.isNullOrEmpty()){
            //如果所有权限已允许，不需要弹窗
            callBackConfig?.onAllGrantedAction?.invoke()
            return
        }

        //发起请求前，需要显示请求理由说明弹窗
        dialogFragment.showRationaleDialog(
            needRequestList,
            onPositive = {
                //申请权限
                performRequestPermission(requestConfig)
            },
            onNegative = {
                callBackConfig?.onDeniedAction?.invoke(needRequestList)
            }
        )
    }

    // </editor-fold>

    // <editor-fold desc="对外Kotlin协程支持">

    /**
     * 发起权限请求，并挂起等待请求结果
     * @param init 以kotlin dsl的方式进行权限请求的配置
     * @return 已拒绝的权限集合，如果为空集合，则表示全部权限已请求通过。
     */
    open suspend fun launchRequestAwait(init : RequestPermissionConfig.()->Unit) : List<String>{
        val config = RequestPermissionConfig().also(init)
        return launchRequestAwait(config)
    }

    /**
     * 发起权限请求，并挂起等待请求结果
     * @param requestConfig 请求权限配置类
     * @return 已拒绝的权限集合，如果为空集合，则表示全部权限已请求通过。
     * */
    open suspend fun launchRequestAwait(requestConfig: RequestPermissionConfig) : List<String>{
        if (requestConfig.permissions.isNullOrEmpty()) {
            throw NullPointerException("request permission is null ,check your code!")
        }

        val dialogFragment = requestConfig.rationaleDialogFragment
        dialogFragment?:run {
            //不需要显示申请原因弹窗，直接请求权限
            return performRequestPermissionAwait(requestConfig)
        }

        val requestPermissions = requestConfig.permissions!!.toList()
        val needRequestList = getNeedRequestPermissions(requestPermissions)
        if (needRequestList.isNullOrEmpty()){
            //所有权限已允许，不需要显示弹窗，直接返回
            return emptyList()
        }

        //发起请求前，需要显示请求理由说明弹窗
        val isPositive = dialogFragment.showRationaleDialogAwait(needRequestList)
        if (!isPositive){
            //拒绝申请权限，原样返回
            return requestPermissions
        }
        //申请权限并挂起等待结果
        return performRequestPermissionAwait(requestConfig)
    }

    // </editor-fold>

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
     * 执行动态权限请求
     * @param requestConfig 请求权限配置
     * */
    @Suppress("MemberVisibilityCanBePrivate")
    protected open fun performRequestPermission(requestConfig: RequestPermissionConfig){
        assert(requestConfig.permissions?.isNotEmpty() == true)
        launch(requestConfig.permissions){map->
            processRequestPermissionResult(requestConfig,map)
        }
    }

    /**
     * 执行动态权限请求，并挂起等待请求结果
     * @param requestConfig 请求权限配置
     * @return 已拒绝的权限集合，如果为空集合，则表示全部权限已请求通过。
     * */
    protected open suspend fun performRequestPermissionAwait(
        requestConfig: RequestPermissionConfig
    ) : List<String>{
        assert(requestConfig.permissions?.isNotEmpty() == true)
        val map = launchAwait(requestConfig.permissions)
        return processRequestPermissionResultAwait(requestConfig,map)
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
        val resultConfig = requestConfig as? RequestPermissionResultConfig
        val onAllGrantedAction = resultConfig?.onAllGrantedAction
        val onDeniedAction = resultConfig?.onDeniedAction

        if (!grantResults.containsValue(false)) {
            //快速通道，所有权限都通过
            onAllGrantedAction?:throw NullPointerException("granted action is null")
            onAllGrantedAction()
            return
        }
        //分类权限请求结果
        val triple = classifyRequestPermissionResult(grantResults)

        //快速通道，即不存在需要解释的权限也不存在永久拒绝的权限，直接返回已拒绝权限集合
        if (triple.first.isNullOrEmpty() && triple.second.isNullOrEmpty()){
            onDeniedAction?.invoke(triple.third)
            return
        }

        if (triple.first.isNotEmpty()){
            //存在需要解释请求理由的被拒绝权限
            val dialogFragment = if (requestConfig.isShowRationalDialogAfterDefined)
                requestConfig.rationaleDialogFragment else null
            //不需要显示解释理由弹窗，则直接返回所有拒绝的权限集合
            dialogFragment?: run{
                onDeniedAction?.invoke(triple.third)
                return
            }

            dialogFragment.showRationaleDialog(
                requestPermissions = triple.first,
                onPositive = {
                    //再次请求权限
                    performRequestPermission(requestConfig)
                },
                onNegative = {
                    onDeniedAction?.invoke(triple.third)
                }
            )
        }else if (triple.second.isNotEmpty()){
            //存在永久拒绝的权限
            val dialogFragment = if (requestConfig.isShowRationalDialogAfterDefined)
                requestConfig.forwardSettingDialogFragment else null
            //不需要提示执行永久拒绝的权限，则直接返回所有拒绝的权限集合
            dialogFragment?: run{
                onDeniedAction?.invoke(triple.third)
                return
            }

            dialogFragment.showRationaleDialog(
                requestPermissions = triple.first,
                onPositive = {
                    //引导跳转到App系统设置详情页，引导用户开启权限
                    mSettingsLauncher.launchAppDetailSettings {
                        //再次申请被永久拒绝的权限
                        performRequestPermission(requestConfig)
                    }
                },
                onNegative = {
                    onDeniedAction?.invoke(triple.third)
                }
            )
        }
    }

    /**
     * 处理请求权限结果，并挂起等待处理逻辑
     * @param requestConfig 权限请求配置
     * @param grantResults 权限请求返回结果
     * @return 已拒绝的权限集合，如果为空集合，则表示全部权限已请求通过。
     * */
    private suspend fun processRequestPermissionResultAwait(
        requestConfig: RequestPermissionConfig,
        grantResults: Map<String, Boolean>
    ) : List<String>{
        if (!grantResults.containsValue(false)) {
            //快速通道，所有权限都通过
            return emptyList()
        }
        //分类权限请求结果
        val triple = classifyRequestPermissionResult(grantResults)
        //快速通道，即不存在需要解释的权限也不存在永久拒绝的权限，直接返回所有已拒绝权限集合
        if (triple.first.isNullOrEmpty() && triple.second.isNullOrEmpty()) return triple.third

        if (triple.first.isNotEmpty()){
            //存在需要解释请求理由的被拒绝权限
            val dialogFragment = if (requestConfig.isShowRationalDialogAfterDefined)
                requestConfig.rationaleDialogFragment else null
            //不需要显示解释理由弹窗，则直接返回所有拒绝的权限集合
            dialogFragment?: return triple.third

            val isPositive = dialogFragment.showRationaleDialogAwait(triple.first)
            if (isPositive){
                //确认弹窗，再次发起权限请求
                return performRequestPermissionAwait(requestConfig)
            }
            return triple.third
        }else if (triple.second.isNotEmpty()){
            //存在永久拒绝的权限
            val dialogFragment = requestConfig.forwardSettingDialogFragment
            dialogFragment?: return triple.third

            val isPositive = dialogFragment.showRationaleDialogAwait(triple.first)
            if (isPositive){
                //弹窗确认，跳转到App系统设置详情页，引导用户开启权限
                mSettingsLauncher.launchAppDetailSettingAwait()
                //等待页面返回后，再次申请被永久拒绝的权限
                return performRequestPermissionAwait(requestConfig)
            }
            return triple.third
        }
        return emptyList()
    }

    /**
     * [PermissionRationaleDialogFragment]拓展方法，显示权限请求理由弹窗，并挂起等待弹窗结果
     * @param requestPermissions 当前需要请求的权限（过滤已允许的权限与永久拒绝的权限）
     * @return true-点击弹窗确认按钮，继续申请权限；false-点击弹窗取消按钮，中断申请流程
     * */
    private suspend fun PermissionRationaleDialogFragment.showRationaleDialogAwait(
        requestPermissions : List<String>
    ) = suspendCancellableCoroutine<Boolean> {cont->
        showRationaleDialog(
            requestPermissions,
            onPositive = { cont.resume(true) },
            onNegative = { cont.resume(false) }
        )
    }


}