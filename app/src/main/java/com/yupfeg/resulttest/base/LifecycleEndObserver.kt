package com.yupfeg.resulttest.base

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

/**
 * 对指定生命周期视图生命周期结束的监听
 * @author yuPFeG
 * @date 2021/03/15
 */
class LifecycleEndObserver(
    private val endState : Lifecycle.State = Lifecycle.State.DESTROYED,
    private val onEndAction : ()->Unit
) : LifecycleEventObserver {

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        val currentState = source.lifecycle.currentState
        if (currentState == Lifecycle.State.DESTROYED) {
            //视图已销毁
            source.lifecycle.removeObserver(this)
            onEndAction.invoke()
            return
        }
        //在指定生命周期会销毁引用，防止内存泄漏
        if (currentState == endState) {
            onEndAction.invoke()
        }
    }

}