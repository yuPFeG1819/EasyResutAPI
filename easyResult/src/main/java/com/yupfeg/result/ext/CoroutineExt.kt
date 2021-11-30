package com.yupfeg.result.ext

import androidx.core.app.ActivityOptionsCompat
import com.yupfeg.result.ActivityResultLauncherWrapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * [ActivityResultLauncherWrapper]的拓展函数，开启Intent并挂起等待结果返回
 * @param input intent的输入参数
 * @param options 可选参数，intent跳转配置，如设置启动动画等
 * @return intent返回值，不允许为null
 * @throws NullPointerException 返回值为null，则抛出异常
 */
@Throws(NullPointerException::class)
suspend fun <Input,Output> ActivityResultLauncherWrapper<Input,Output>.launchAwait(
    input: Input?, options : ActivityOptionsCompat? = null
)
    = suspendCancellableCoroutine<Output> {cont->
        //启动Launcher
        launch(input,options){ result->
            result?:run {
                cont.resumeWithException(
                    NullPointerException("launch result is null,check your code")
                )
                return@launch
            }
            //恢复挂起函数
            cont.resume(result)
        }
    }

/**
 * [ActivityResultLauncherWrapper]的拓展函数，开启Intent并挂起等待结果返回
 * @param input intent的输入参数
 * @param options 可选参数，intent跳转配置，如设置启动动画等
 * @return Intent返回值，允许为null
 * */
suspend fun <Input,Output> ActivityResultLauncherWrapper<Input,Output>.launchAwaitOrNull(
    input: Input?,options : ActivityOptionsCompat? = null
) = suspendCancellableCoroutine<Output?> {cont->
        //启动Launcher
        launch(input,options){ result->
            cont.resume(result)
        }
    }

/**
 * [ActivityResultLauncherWrapper]的拓展函数，开启Intent，并将返回值转化为flow数据流
 * * Intent返回值为null时，flow数据流会抛出异常
 * @param input intent的输入参数
 * @param options 可选参数，intent跳转配置，如设置启动动画等
 * */
@Suppress("unused")
fun <Input,Output> ActivityResultLauncherWrapper<Input,Output>.launchFlow(
    input: Input?,options : ActivityOptionsCompat? = null
) : Flow<Output>{
    return flow { emit(launchAwait(input,options)) }
}

/**
 * [ActivityResultLauncherWrapper]的拓展函数，开启Intent，并将返回值转化为flow数据流，允许返回值为null
 * @param input intent的输入参数
 * @param options 可选参数，intent跳转配置，如设置启动动画等
 * */
@Suppress("unused")
fun <Input,Output> ActivityResultLauncherWrapper<Input,Output>.launchNullableFlow(
    input: Input?,options : ActivityOptionsCompat? = null
) : Flow<Output?> {
    return flow { emit(launchAwaitOrNull(input,options)) }
}

