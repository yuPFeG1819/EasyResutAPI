package com.yupfeg.result

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.core.os.bundleOf

/**
 * 启动Activity跳转的启动器包装类
 * * 替换startActivityForResult的ResultAPI简化方案
 *
 * @author yuPFeG
 * @date 2021/10/18
 */
@Suppress("unused")
open class StartActivityResultLauncher(
    caller: ActivityResultCaller
) : ActivityResultLauncherWrapper<Intent, ActivityResult>(
    caller,ActivityResultContracts.StartActivityForResult()
){
    /**
     * 启动Intent，跳转Activity
     * @param pairs intent跳转传递的参数key-value
     * @param options intent跳转配置，如设置启动动画等
     * @param onActivityResult intent跳转返回回调，已过滤resultCode == Activity.RESULT_OK
     * */
    inline fun <reified T : Activity> launch(
        vararg pairs: Pair<String, *>,
        options: ActivityOptionsCompat?= null,
        noinline onActivityResult: (Intent?)->Unit
    ) = launch(
            clazz = T::class.java,
            options = options,
            extras = if (pairs.isNullOrEmpty()) null else bundleOf(*pairs),
            onActivityResult = onActivityResult
        )

    /**
     * 启动Intent，跳转Activity
     * @param clazz 目标Activity的class
     * @param extras intent跳转传递的参数key-value
     * @param options intent跳转配置，如设置启动动画等
     * @param onActivityResult intent跳转返回回调，已过滤resultCode == Activity.RESULT_OK
     * */
    @JvmOverloads
    fun <T : Activity> launch(
        clazz: Class<T>,
        extras: Bundle? = null,
        options: ActivityOptionsCompat?,
        onActivityResult: (Intent?)->Unit
    ) {
        val intent = Intent(requireContext(), clazz)
        extras?.let { intent.putExtras(it) }
        launch(intent,options) { result ->
            if (result?.resultCode ?: Activity.RESULT_CANCELED == Activity.RESULT_OK) {
                onActivityResult.invoke(result?.data)
            }
        }
    }

    /**
     * 启动Intent，跳转Activity
     * @param clazz 目标Activity的class
     * @param extras intent跳转传递的参数key-value
     * @param options intent跳转配置，如设置启动动画等
     * @param callBack intent跳转返回回调
     * */
    @Suppress("unused")
    @JvmOverloads
    fun <T : Activity>launch(
        clazz: Class<T>,
        extras: Bundle? = null,
        options: ActivityOptionsCompat?,
        callBack : ActivityResultCallback<ActivityResult>
    ){
        val intent = Intent(requireContext(), clazz)
        extras?.let { intent.putExtras(it) }
        launch(intent,options,callBack)
    }

}