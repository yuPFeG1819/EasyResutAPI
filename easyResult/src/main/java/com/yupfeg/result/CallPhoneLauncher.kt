package com.yupfeg.result

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContract

/**
 * 系统拨号页面的启动器
 * * 不需要设置拨号权限
 *
 * @author yuPFeG
 * @date 2021/10/20
 */
@Suppress("unused")
class CallPhoneLauncher(
    caller: ActivityResultCaller
) : ActivityResultLauncherWrapper<String?,Unit>(
    caller, CallPhoneContract()
){
    /**
     * 启动拨打电话
     * @param phone 电话号码
     * */
    fun launch(phone: String?) = launch(phone,null)
}

internal class CallPhoneContract : ActivityResultContract<String?,Unit>(){
    override fun createIntent(context: Context, phone: String?): Intent {
        phone?:throw NullPointerException("phone number is empty,check your code")
        //调起系统的拨号界面，并不去直接拨打电话，不需要权限
        return Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phone}")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?) {}

}