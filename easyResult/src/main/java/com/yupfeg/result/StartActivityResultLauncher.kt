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
import com.yupfeg.result.ext.launchAwait
import com.yupfeg.result.ext.launchAwaitOrNull

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
     * 启动Intent，跳转Activity，并接收返回值
     * @param T 需要跳转Activity的类名
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
     * 启动Intent，跳转Activity，并等待Activity返回
     * @param T 需要跳转Activity的类名
     * @param pairs intent跳转传递的参数key-value
     * @param options intent跳转配置，如设置启动动画等
     * @return 页面返回intent，可能为null，已过滤resultCode == Activity.RESULT_OK
     * */
    suspend inline fun <reified T : Activity> launchAwaitIntentOrNull(
        vararg pairs: Pair<String, *>,
        options: ActivityOptionsCompat?= null,
    ) = launchAwaitIntentOrNull(
        clazz = T::class.java,
        options = options,
        extras = if (pairs.isNullOrEmpty()) null else bundleOf(*pairs),
    )

    /**
     * 启动Intent，跳转Activity，并接收Activity返回值
     * @param clazz 目标Activity的class
     * @param extras intent跳转传递的参数key-value
     * @param options intent跳转配置，如设置启动动画等
     * @param onActivityResult intent跳转返回回调，已处理resultCode == Activity.RESULT_OK
     * */
    @JvmOverloads
    fun <T : Activity> launch(
        clazz: Class<T>,
        extras: Bundle? = null,
        options: ActivityOptionsCompat?,
        onActivityResult: (Intent?)->Unit
    ) {
        val intent = Intent(requireContext(), clazz)
        extras?.also { intent.putExtras(it) }
        launch(intent,options) { result ->
            if (result?.resultCode ?: Activity.RESULT_CANCELED == Activity.RESULT_OK) {
                onActivityResult.invoke(result?.data)
            }
        }
    }

    /**
     * 启动Intent，跳转Activity，并接收Activity返回值
     * @param clazz 目标Activity的class
     * @param extras intent跳转传递的参数key-value
     * @param options intent跳转配置，如设置启动动画等
     * @param callBack intent跳转返回回调
     * */
    @Suppress("unused")
    @JvmOverloads
    fun <T : Activity> launch(
        clazz: Class<T>,
        extras: Bundle? = null,
        options: ActivityOptionsCompat?,
        callBack : ActivityResultCallback<ActivityResult>
    ){
        val intent = Intent(requireContext(), clazz)
        extras?.also { intent.putExtras(it) }
        launch(intent,options,callBack)
    }

    /**
     * 挂起函数，启动Intent,跳转Activity，并等待Activity返回值
     * @param clazz 目标Activity的class
     * @param extras intent跳转传递的参数key-value
     * @param options intent跳转配置，如设置启动动画等
     * @return ActivityResult，需要外部手动过滤resultCode
     * */
    @JvmOverloads
    suspend fun <T : Activity> launchAwait(
        clazz: Class<T>,
        extras: Bundle? = null,
        options: ActivityOptionsCompat?,
    ) : ActivityResult{
        val intent = Intent(requireContext(), clazz)
        extras?.also { intent.putExtras(it) }
        return launchAwait(intent,options)
    }

    /**
     * 挂起函数，启动Intent,跳转Activity，并等待Activity返回值
     * @param clazz 目标Activity的class
     * @param extras intent跳转传递的参数key-value
     * @param options intent跳转配置，如设置启动动画等
     * @return Activity的返回值intent，已处理resultCode == Activity.RESULT_OK
     * */
    @JvmOverloads
    suspend fun <T : Activity> launchAwaitIntentOrNull(
        clazz: Class<T>,
        extras: Bundle? = null,
        options: ActivityOptionsCompat?,
    ) : Intent?{
        val intent = Intent(requireContext(), clazz)
        extras?.also { intent.putExtras(it) }
        val result = launchAwaitOrNull(intent,options)
        if (result?.resultCode ?: Activity.RESULT_CANCELED == Activity.RESULT_OK) {
            return result?.data
        }
        return null
    }
}