package com.yupfeg.result.permission

import com.yupfeg.result.permission.dialog.PermissionRationaleDialogFragment


/**
 * 动态权限请求dsl配置类
 * @author yuPFeG
 * @date 2021/10/15
 */
class RequestPermissionConfig{

    /**
     * 请求的权限集合，集合为空则不会发起权限请求
     * */
    @JvmField
    var permissions : Array<String>? = null

    /**
     * 是否在权限请求前，显示权限请求原因的弹窗
     * * 需要配合[rationaleDialogFragment],该弹窗会在允许后，调用权限请求
     * */
    @JvmField
    var isShowRationalDialog : Boolean = false

    /**
     * 是否在权限请求拒绝后，显示权限请求原因的弹窗，
     * * * 需要配合[rationaleDialogFragment],该弹窗会在允许后，再次请求权限
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
     * 导航到系统设置页说明弹窗，仅在所有权限都为永久拒绝时生效，默认为null
     * * 在遇到永久拒绝的权限时，会弹出提示弹窗，并在确认后跳转到系统设置页
     * * 推荐外部不需要每次创建新对象，可以通过非空判断，在视图作用域范围内创建一次'单例'，
     * 在视图声明周期结束时，将其dismiss与置空
     * */
    @JvmField
    var forwardSettingDialogFragment : PermissionRationaleDialogFragment? = null

    /**
     * 所有权限都允许时的回调接口
     * */
    @JvmField
    var onAllGrantedAction : (()->Unit)? = null

    /**
     * 当次请求被拒绝权限回调，返回已被拒绝的权限集合
     * * 包含永久拒绝不再提醒的权限，需要引导用户到系统权限设置页
     */
    @JvmField
    var onDeniedAction : ((List<String>)->Unit)? = null

    /**
     * 设置需要请求的权限
     * @param permission 需要请求的权限
     * */
    @Suppress("unused")
    fun setRequestPermissions(vararg permission : String){
        permissions = arrayOf(*permission)
    }
}
