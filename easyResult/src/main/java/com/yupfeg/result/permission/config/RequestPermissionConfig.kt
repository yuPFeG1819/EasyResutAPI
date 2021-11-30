package com.yupfeg.result.permission.config

import com.yupfeg.result.permission.dialog.PermissionRationaleDialogFragment


/**
 * 动态权限请求dsl配置类
 * @author yuPFeG
 * @date 2021/10/15
 */
open class RequestPermissionConfig{

    /**
     * 请求的权限集合，集合为空则不会发起权限请求
     * */
    @JvmField
    var permissions : Array<String>? = null

    /**
     * 是否在权限请求拒绝后，显示权限请求原因的弹窗，
     * * 需要配合[rationaleDialogFragment],该弹窗会在允许后，再次请求权限
     * * 配合[forwardSettingDialogFragment]，该弹窗会在允许后，跳转到系统应用权限页
     * */
    @JvmField
    var isShowRationalDialogAfterDefined : Boolean = false

    /**
     * 权限请求理由说明弹窗，默认为null
     * * 推荐外部不需要每次创建新对象，可以通过非空判断，在视图作用域范围内创建一次'单例'，
     * 在视图声明周期结束时，将其dismiss与置空
     * */
    @JvmField
    var rationaleDialogFragment : PermissionRationaleDialogFragment? = null

    /**
     * 导航到系统设置页说明弹窗，仅在所拒绝的权限都为永久拒绝时生效，默认为null
     * * 在遇到永久拒绝的权限时，会弹出提示弹窗，并在确认后跳转到系统设置页
     * * 推荐外部不需要每次创建新对象，可以通过非空判断，在视图作用域范围内创建一个'单例'，
     * 在视图声明周期结束时，将其dismiss与置空
     * */
    @JvmField
    var forwardSettingDialogFragment : PermissionRationaleDialogFragment? = null

    /**
     * 设置需要请求的权限
     * @param permission 需要请求的权限
     * */
    @Suppress("unused")
    fun setRequestPermissions(vararg permission : String){
        permissions = arrayOf(*permission)
    }
}
