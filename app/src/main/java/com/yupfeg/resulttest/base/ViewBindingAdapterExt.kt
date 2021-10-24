package com.yupfeg.resulttest.base

import android.view.View
import androidx.databinding.BindingAdapter

/**
 * [View]拓展函数，对View设置防抖点击事件，默认为500ms
 * * DataBinding专用函数，所有DataBinding属性在xml都需要以["@{}"]赋值，否则会报错
 * @param onClickListener 点击事件
 */
@Suppress("unused")
@BindingAdapter(value = ["onViewSingleClick"])
fun View.bindViewSingleClick(onClickListener: View.OnClickListener?){
    onClickListener?.let {
        this.setOnClickListener (it)
    }
}