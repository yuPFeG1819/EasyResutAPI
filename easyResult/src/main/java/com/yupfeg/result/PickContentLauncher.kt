package com.yupfeg.result

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContract

/**
 * 获取系统内容的启动器包装类
 * * 支持获取相册图片、视频、音频、联系人，只有单选，在选择前会弹出选择目标弹窗
 *
 * @author yuPFeG
 * @date 2021/10/18
 */
@Suppress("unused")
class PickContentLauncher(
    caller : ActivityResultCaller
) : ActivityResultLauncherWrapper<String,Uri?>(caller, PickContentContract()){

    /**
     * 调起系统相册选择器
     * * 部分系统会显示Dialog选择
     * @param callback 图片选择回调，返回图片uri，如果未选择则为null
     * */
    fun launchPickImage(callback: ActivityResultCallback<Uri?>) = launch("image/*",callback)

    /**
     * 调起系统视频选择器
     * * 部分系统会显示Dialog选择
     * @param callback 视频选择回调，返回视频uri,如果未选择则为null
     * */
    fun launchPickVideo(callback: ActivityResultCallback<Uri?>) = launch("video/*", callback)
    /**
     * 调起系统音频选择器
     * * 部分系统会显示Dialog选择
     * @param callback 图片选择回调，返回图片uri，如果未选择则为null
     * */
    fun launchPickAudio(callback: ActivityResultCallback<Uri?>) = launch("audio/*", callback)

    /**
     * 调起系统联系人选择器
     * * 注意需要读取联系人权限
     * @param callback 选择回调，返回联系人uri，如果未选择则为null
     * */
    fun launchPickContact(callback: ActivityResultCallback<Uri?>)
        = launch(ContactsContract.Contacts.CONTENT_TYPE,callback)

}

/**
 * 通过[Intent.ACTION_PICK]调起系统内容选择器的Intent定义类
 * * 相比原生的`ActivityResultContracts.PickContact`，添加了允许访问其他类型的系统选择器
 * */
class PickContentContract : ActivityResultContract<String, Uri?>(){
    override fun createIntent(context: Context, input: String)
        = Intent(Intent.ACTION_PICK).setType(input)

    override fun parseResult(resultCode: Int, intent: Intent?) : Uri?{
        return if (intent == null || resultCode != Activity.RESULT_OK) null
        else intent.data!!
    }
}