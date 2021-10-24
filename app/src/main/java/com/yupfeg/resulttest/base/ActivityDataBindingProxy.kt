package com.yupfeg.resulttest.base

import androidx.activity.ComponentActivity
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * [ComponentActivity]拓展函数，通过属性委托的by关键字，获取dataBinding的对象
 * @param T layout对应的DataBinding自动生成类
 * @param layoutId 布局id
 */
@Suppress("unused")
inline fun <reified T : ViewDataBinding> ComponentActivity.bindingActivity(
    @LayoutRes layoutId : Int
) = ActivityDataBindingDelegate<T>(layoutId,this.lifecycle)

/**
 * [ComponentActivity]的DataBinding委托类
 * @param layoutId 布局Id
 * @param lifecycle [ComponentActivity]的lifecycle，仅用于订阅生命周期
 * @param endLifecycle 销毁binding对象的生命周期
 * @author yuPFeG
 * @date 2021/03/09
 */
class ActivityDataBindingDelegate<T : ViewDataBinding>(
    @LayoutRes val layoutId : Int,
    lifecycle : Lifecycle,
    endLifecycle : Lifecycle.State = Lifecycle.State.DESTROYED
) : ReadOnlyProperty<ComponentActivity,T>{

    private var mViewBinding : T? = null

    init {
        lifecycle.addObserver(LifecycleEndObserver(endLifecycle){
            //在Lifecycle.State.DESTROYED 将会销毁binding数据，防止内存泄漏
            mViewBinding = null
        })
    }

    override fun getValue(thisRef: ComponentActivity, property: KProperty<*>): T {
        return mViewBinding ?: prepareViewBindingInstance(thisRef)
    }

    private fun prepareViewBindingInstance(thisRef: ComponentActivity) : T{
        return DataBindingUtil.setContentView<T>(thisRef, layoutId).apply{
            mViewBinding = this
            lifecycleOwner = thisRef
        }
    }

}