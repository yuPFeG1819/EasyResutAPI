package com.yupfeg.result

import android.net.Uri
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts

/**
 * 系统内容的单选选择器启动类
 * @author yuPFeG
 * @date 2021/10/22
 */
@Suppress("unused")
class GetContentLauncher(
    caller: ActivityResultCaller
) : ActivityResultLauncherWrapper<String,Uri?>(
    caller,ActivityResultContracts.GetContent()
){

    /**
     * 启动系统图片文件选择器（单选）
     * @param callBack 图片选择回调，返回图片uri，如果未选择则为null
     * */
    @Suppress("unused")
    fun launchImage(callBack : ActivityResultCallback<Uri?>) = launch("image/*",callBack)

    /**
     * 启动系统视频文件选择器（单选）
     * @param callBack 视频选择回调，返回视频uri，如果未选择则为null
     */
    @Suppress("unused")
    fun launchVideo(callBack: ActivityResultCallback<Uri?>) = launch("video/*",callBack)

    /**
     * 启动系统音频文件选择器（单选）
     * @param callBack 音频选择回调，返回音频uri，如果未选择则为null
     * */
    @Suppress("unused")
    fun launchAudio(callBack: ActivityResultCallback<Uri?>) = launch("audio/*",callBack)
}