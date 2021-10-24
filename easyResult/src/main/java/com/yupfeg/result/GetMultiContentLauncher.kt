package com.yupfeg.result

import android.net.Uri
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts

/**
 * 多选系统内容选择器的启动类
 * @author yuPFeG
 * @date 2021/10/21
 */
@Suppress("unused")
class GetMultiContentLauncher(
    caller : ActivityResultCaller
) : ActivityResultLauncherWrapper<String,List<Uri?>>(
    caller, ActivityResultContracts.GetMultipleContents()
){

    /**
     * 启动图片文件的多项选择器
     * @param callback 图片选择回调，返回图片uri集合，如果未选择则为null
     * */
    fun launchImage(callback: ActivityResultCallback<List<Uri?>>)
        = launch("image/*",callback)

    /**
     * 启动视频文件的多项选择器
     * @param callback 图片选择回调，返回图片uri集合，如果未选择则为null
     * */
    fun launchVideo(callback: ActivityResultCallback<List<Uri?>>)
        = launch("video/*",callback)

    /**
     * 启动音频文件的多项选择器
     * @param callback 图片选择回调，返回图片uri集合，如果未选择则为null
     * */
    fun launchAudio(callback: ActivityResultCallback<List<Uri?>>)
        = launch("audio/*",callback)
}