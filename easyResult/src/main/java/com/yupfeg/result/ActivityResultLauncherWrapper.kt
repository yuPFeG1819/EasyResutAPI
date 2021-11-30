package com.yupfeg.result

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment

/**
 * [ActivityResultLauncher]的拓展包装类，
 * 加强原有[ActivityResultLauncher]的功能，放便于外部调用
 *
 * * [ActivityResultCaller.registerForActivityResult]方法必须在`onStart`生命周期前调用，否则会抛出异常
 *
 * @author yuPFeG
 * @date 2021/10/18
 */
@Suppress("unused")
open class ActivityResultLauncherWrapper<Input,Output>(
    /**在`CompatActivity`与`Fragment`默认实现的注册器*/
    protected val caller: ActivityResultCaller,
    /**Result API中定义Intent输入输出行为的协议类*/
    contract : ActivityResultContract<Input,Output>
) {
    /**原始启动器*/
    private var originLauncher : ActivityResultLauncher<Input>? = null

    /**Intent启动回调*/
    private var callBack : ActivityResultCallback<Output>? = null

    init {
        originLauncher = caller.registerForActivityResult(contract){ result->
            callBack?.onActivityResult(result)
            callBack = null
        }
    }

    /**
     * 启动Intent跳转流程
     * @param input intent跳转传递的参数类型
     * @param callback intent跳转返回回调
     * */
    open fun launch(input: Input?,callback: ActivityResultCallback<Output>?){
        launch(input,null,callback)
    }

    /**
     * 启动Intent跳转流程
     * @param input intent跳转传递的参数类型
     * @param options intent跳转配置，如设置启动动画等
     * @param callback intent跳转返回回调
     * */
    open fun launch(
        input : Input?,
        options : ActivityOptionsCompat? = null,
        callback: ActivityResultCallback<Output>?
    ) {
        callBack = callback
        originLauncher?.launch(input, options)
    }

    /**
     * 回收启动器，并释放底层回调及其携带的引用
     * * 当启动器回调的生命周期，比[ActivityResultCaller]的注册生命周期还要长时，需要调用该方法，避免内存泄漏
     * */
    open fun unregister(){
        originLauncher?.unregister()
    }

    /**
     * 尝试获取Context
     * @throws IllegalArgumentException
     * */
    fun requireContext() = caller.getContext()

    /**
     * [ActivityResultCaller]的拓展函数，获取context
     * */
    protected open fun ActivityResultCaller.getContext() : Context
        = when(this){
            is ComponentActivity -> this
            is Fragment -> this.requireContext()
            else -> throw IllegalArgumentException(
                "ActivityResultCaller Context must from Activity or Fragment"
            )
        }

}