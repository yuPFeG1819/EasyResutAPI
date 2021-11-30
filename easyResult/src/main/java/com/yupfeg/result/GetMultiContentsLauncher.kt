package com.yupfeg.result

import android.net.Uri
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import com.yupfeg.result.ext.launchAwaitOrNull

/**
 * 系统内容多项选择器的启动类
 * @author yuPFeG
 * @date 2021/10/21
 */
@Suppress("unused")
class GetMultiContentsLauncher(
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
     * 启动系统图片文件选择器（单选），并挂起等待选择的图片返回
     * @return 选择的图片Uri集合，未选择则为空集合
     * */
    suspend fun launchImageAwaitOrNull(): List<Uri?>
        = launchAwaitOrNull("image/*") ?: emptyList()

    /**
     * 启动视频文件的多项选择器
     * @param callback 图片选择回调，返回图片uri集合，如果未选择则为null
     * */
    fun launchVideo(callback: ActivityResultCallback<List<Uri?>>)
        = launch("video/*",callback)

    /**
     * 启动系统视频文件选择器（单选），并挂起等待选择的视频返回
     * @return 选择的视频Uri集合，未选择则为空集合
     * */
    suspend fun launchVideoAwaitOrNull(): List<Uri?>
            = launchAwaitOrNull("video/*") ?: emptyList()

    /**
     * 启动音频文件的多项选择器
     * @param callback 图片选择回调，返回图片uri集合，如果未选择则为null
     * */
    fun launchAudio(callback: ActivityResultCallback<List<Uri?>>)
        = launch("audio/*",callback)

    /**
     * 启动系统音频文件选择器（单选），并挂起等待选择的音频返回
     * @return 选择的音频Uri集合，未选择则为空集合
     * */
    suspend fun launchAudioAwaitOrNull(): List<Uri?>
            = launchAwaitOrNull("audio/*") ?: emptyList()
}