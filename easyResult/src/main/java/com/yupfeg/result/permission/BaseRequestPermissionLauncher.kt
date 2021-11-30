package com.yupfeg.result.permission

import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.yupfeg.result.ActivityResultLauncherWrapper
import com.yupfeg.result.permission.dialog.PermissionRationaleDialogFragment

/**
 * 动态请求权限的启动器的基类
 * @suppress 内部API，不要在外部代码中使用
 * @author yuPFeG
 * @date 2021/11/27
 */
abstract class BaseRequestPermissionLauncher(
    caller: ActivityResultCaller
) : ActivityResultLauncherWrapper<Array<String>, Map<String, Boolean>>(
    caller, ActivityResultContracts.RequestMultiplePermissions()
){
    companion object{
        const val RATIONALE_DIALOG_NAME = "PermissionRationaleDialogFragment"
    }

    /**
     * 对请求权限结果进行分类
     * @param grantResults 权限请求返回
     * @return [Triple]包装类，
     * first - 表示需要解释请求原因的权限集合，
     * second - 永久拒绝的权限集合，
     * third - 所有被拒绝的权限集合（包含前面两项）
     * */
    protected fun classifyRequestPermissionResult(
        grantResults: Map<String, Boolean>
    ) : Triple<List<String>,List<String>,List<String>>{
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

        return Triple(
            first = showRationalPermissions,
            second = permanentDeniedPermissions,
            third = definedPermissions
        )
    }

    /**
     * [PermissionRationaleDialogFragment]拓展方法，显示权限请求理由弹窗
     * @param requestPermissions 当前需要请求的权限（过滤已允许的权限与永久拒绝的权限）
     * @param onPositive 确认按钮点击回调
     * @param onNegative 取消按钮点击回调
     * */
    protected fun PermissionRationaleDialogFragment.showRationaleDialog(
        requestPermissions : List<String>,
        onPositive : ()->Unit,
        onNegative : ()->Unit,
    ){
        val dialogFragment = this
        showNow(caller.getFragmentManager(),RATIONALE_DIALOG_NAME)
        this.setRequestPermissions(requestPermissions)
        isCancelable = false //禁止返回键关闭弹窗
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

    /**
     * [ActivityResultCaller]的拓展函数，校验指定权限是否需要显示权限说明
     * @param permission 权限名称
     * */
    @Suppress("MemberVisibilityCanBePrivate")
    protected fun ActivityResultCaller.shouldShowRequestPermissionRationale(permission: String) =
        when (this) {
            is Activity -> ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
            is Fragment -> ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                permission
            )
            else -> false
        }

    /**
     * [ActivityResultCaller]的拓展函数，校验指定权限是否已同意
     * @param permission 权限名称
     * */
    protected fun ActivityResultCaller.checkPermissionGranted(permission: String) : Boolean{
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