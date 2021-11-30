package com.yupfeg.resulttest

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.yupfeg.result.permission.RequestPermissionLauncher
import com.yupfeg.result.permission.dialog.DefaultRationaleDialogFragment

/**
 *
 * @author yuPFeG
 * @date 2021/11/29
 */
open class BaseRequestActivity : AppCompatActivity(){

    protected val mRationalDialogFragment : DefaultRationaleDialogFragment
            by lazy(LazyThreadSafetyMode.NONE){
                createRationalDialog()
            }

    protected val mNaviSettingsTipDialogFragment : DefaultRationaleDialogFragment
            by lazy(LazyThreadSafetyMode.NONE){
                createNaviSettingTipDialog()
            }

    override fun onDestroy() {
        super.onDestroy()

        if(mRationalDialogFragment.isAdded){
            mRationalDialogFragment.dismiss()
        }

        if (mNaviSettingsTipDialogFragment.isAdded){
            mNaviSettingsTipDialogFragment.dismiss()
        }
    }

    // <editor-fold desc="请求权限">


    private fun createRationalDialog() : DefaultRationaleDialogFragment{
        return DefaultRationaleDialogFragment(
            reason = "您需要允许权限才能继续",
            positiveText = "允许",
            negativeText = "拒绝",
            reasonTextColor = ContextCompat.getColor(this,android.R.color.black),
            tintColor = getThemeColor(R.attr.colorPrimary)
        )
    }

    private fun createNaviSettingTipDialog() : DefaultRationaleDialogFragment{
        return DefaultRationaleDialogFragment(
            reason = "您需要到系统设置开启权限才能继续",
            positiveText = "确认",
            negativeText = "取消",
            reasonTextColor = ContextCompat.getColor(this,android.R.color.black),
            tintColor = getThemeColor(R.attr.colorPrimary)
        )
    }

    /**
     * [Context]的拓展函数，获取当前主题颜色属性
     * @param attr 颜色属性
     */
    protected fun Context.getThemeColor(vararg attr: Int): Int {
        val array: TypedArray = theme.obtainStyledAttributes(attr)
        val color = array.getColor(0, Color.TRANSPARENT)
        array.recycle()
        return color
    }

    // </editor-fold>

    protected fun showShortToast(text : String){
        Toast.makeText(this,text, Toast.LENGTH_SHORT).show()
    }
}