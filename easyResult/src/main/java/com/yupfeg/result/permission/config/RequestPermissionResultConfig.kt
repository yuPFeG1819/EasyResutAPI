package com.yupfeg.result.permission.config

/**
 * 动态权限请求的请求结果配置类
 * * 仅在普通方式请求权限时使用
 * @author yuPFeG
 * @date 2021/11/29
 */
class RequestPermissionResultConfig : RequestPermissionConfig(){

    /**
     * 所有权限都允许的回调
     * */
    @JvmField
    var onAllGrantedAction : (()->Unit)? = null

    /**
     * 当次请求被拒绝权限回调，返回已被拒绝的权限集合
     * * 包含永久拒绝不再提醒的权限，需要引导用户到系统权限设置页
     */
    @JvmField
    var onDeniedAction : ((List<String>)->Unit)? = null
}