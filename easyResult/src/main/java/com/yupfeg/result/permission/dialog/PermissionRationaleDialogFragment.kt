package com.yupfeg.result.permission.dialog

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.yupfeg.result.permission.PermissionGroupConstant

/**
 * 权限请求理由说明的基类弹窗Fragment
 * * 在使用`RequestPermissionLauncher`时，设置请求原因说明弹窗
 * @author yuPFeG
 * @date 2021/10/15
 */
abstract class PermissionRationaleDialogFragment : DialogFragment(){

    /**
     * 当前需要请求的权限集合
     * * 在子类添加需要在该弹窗中解释请求理由的权限
     * */
    @Suppress("MemberVisibilityCanBePrivate")
    protected var requestPermissions : List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            dismiss()
        }
    }

    /**
     * 设置需要解释请求理由的权限
     * * 用于给弹窗展示需要说明的权限
     * * 在请求多个权限时，可能只有部分权限被允许了
     * * 仅供在内部调用
     * @param permissions 需要说明的权限集合 (过滤已允许和永久拒绝的)
     * */
    internal fun setRequestPermissions(permissions: List<String>){
        requestPermissions = permissions
        doOnRequestPermissionUpdate(permissions)
    }

    /**
     * 获取确认（允许）申请权限的View，
     * * 内部会设置点击事件，子类只需要提供该控件对象
     * * 该控件点击后会进行选择权限
     * */
    abstract fun getPositiveView() : View


    /**
     * 获取取消（不允许）申请权限的View
     * * 内部会设置点击事件，子类只需要提供该控件对象
     * * 如果是强制性进行权限请求，则返回null
     * */
    abstract fun getNegativeView() : View?

    /**
     * 需要解释请求理由的权限变化时调用
     * * 子类实现用以更新权限说明展示
     * @param permissions 权限集合(过滤已允许和永久拒绝的)
     * */
    abstract fun doOnRequestPermissionUpdate(permissions: List<String>)

    /**
     * 获取当前权限所对应的权限组名称
     * @param permission
     * @return
     */
    protected fun getPermissionGroup(permission : String) : String?{
        val currentVersion = Build.VERSION.SDK_INT
        return when {
            currentVersion < Build.VERSION_CODES.Q -> {
                //android 10以下，能直接获取权限组名称
                try {
                    requireContext().packageManager.getPermissionInfo(permission, 0).group
                } catch (e: PackageManager.NameNotFoundException) {
                    null
                }
            }
            currentVersion == Build.VERSION_CODES.Q -> {
                PermissionGroupConstant.PERMISSION_MAP_Q[permission]
            }
            currentVersion == Build.VERSION_CODES.R -> {
                PermissionGroupConstant.PERMISSION_MAP_R[permission]
            }
            currentVersion == Build.VERSION_CODES.S -> {
                PermissionGroupConstant.PERMISSION_MAP_S[permission]
            }
            else -> PermissionGroupConstant.PERMISSION_MAP_S[permission]
        }
    }
}