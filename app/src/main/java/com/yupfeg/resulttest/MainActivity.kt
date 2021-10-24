package com.yupfeg.resulttest

import android.Manifest
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.yupfeg.result.*
import com.yupfeg.result.file.getUriFromFile
import com.yupfeg.result.permission.RequestPermissionLauncher
import com.yupfeg.result.permission.dialog.DefaultRationaleDialogFragment
import com.yupfeg.resulttest.base.ContactsTools
import com.yupfeg.resulttest.base.bindingActivity
import com.yupfeg.resulttest.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    private val mBinding : ActivityMainBinding by bindingActivity(R.layout.activity_main)

    private val mTestResultActivityLauncher = StartActivityResultLauncher(this)
    private val mRequestPermissionLauncher = RequestPermissionLauncher(this)

    private val mTakePictureLauncher = TakePictureLauncher(this)
    private val mCropImageLauncher = CropImageLauncher(this)

    private val mPickContentLauncher = PickContentLauncher(this)
    private val mGetContentLauncher = GetMultiContentLauncher(this)

    private val mRationalDialogFragment : DefaultRationaleDialogFragment
        by lazy(LazyThreadSafetyMode.NONE){ createRationalDialog() }

    private val mNaviSettingsTipDialogFragment : DefaultRationaleDialogFragment
        by lazy(LazyThreadSafetyMode.NONE){
            createNaviSettingTipDialog()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.config = BindingConfig()
    }

    inner class BindingConfig{
        fun testRequestPermission(){
            requestPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ){
                showShortToast("所有权限请求成功")
            }
        }

        fun testResultApiStartActivity(){
            //测试跳转页面
            mTestResultActivityLauncher.launch<TestResultApiActivity>{resultIntent->
                resultIntent?.extras?.also {
                    Toast.makeText(this@MainActivity,"接收返回值${it["key"]}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        fun takeSystemPicture(){
            performTakePictureAndCrop()
        }

        fun pickGallery(){
            performPickImage()
        }

        fun pickContact(){
            performPickContact()
        }

        /**在系统图片选择器内选择图片*/
        fun selectImageFromGallery(){
            performSelectImage()
        }
    }

    private fun performTakePictureAndCrop(){
        requestPermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ){
            mTakePictureLauncher.launch{filePath->
                filePath?:run {
                    showShortToast("拍照失败")
                    return@launch
                }
                showShortToast("拍照完成$filePath")
                val photoFile = File(filePath)
                if (!photoFile.exists()){
                    showShortToast("拍照失败")
                    return@launch
                }
                val fileUri = getUriFromFile(this,photoFile)
                cropImage(fileUri)
            }
        }
    }

    /**
     * 执行剪裁图片操作
     * @param uri 原始图片文件uri
     * */
    private fun cropImage(uri: Uri){
        mCropImageLauncher.launch {
            cropFileUri = uri
            callBack = {clipFile->
                clipFile?.also {
                    showShortToast("剪裁成功${it.absoluteFile}")
                } ?: run{
                    showShortToast("剪裁失败")
                }
            }
        }
    }

    private fun performPickImage(){
        requestPermissions(Manifest.permission.READ_EXTERNAL_STORAGE){
            mPickContentLauncher.launchPickImage{uri->
                uri?:return@launchPickImage
                cropImage(uri)
//                    val localFilePath = getRealFilePathFromFileUri(this@MainActivity,uri)
//                    logd("pick content方式选择了图片 \n uri : ${uri}\n file : $localFilePath")
            }
        }
    }

    private fun performPickContact(){
        requestPermissions(Manifest.permission.READ_CONTACTS){
            mPickContentLauncher.launchPickContact{ uri->
                uri?:return@launchPickContact
                val pair = ContactsTools.queryContactPhoneFromUri(this@MainActivity,uri)
                showShortToast("选择了${pair?.first?:""}")
            }
        }
    }

    private fun performSelectImage(){
        requestPermissions(Manifest.permission.READ_EXTERNAL_STORAGE){
            mGetContentLauncher.launchImage{uriList->
                if (uriList.isNullOrEmpty()) return@launchImage
                showShortToast("选择了${uriList.size}张图片")
            }
        }
    }

    private fun showShortToast(text : String){
        Toast.makeText(this,text,Toast.LENGTH_SHORT).show()
    }

    // <editor-fold desc="请求权限">

    /***
     * 请求权限
     * @param permission
     * @param onSuccess 请求权限成功回调
     */
    private fun requestPermissions(vararg permission : String, onSuccess : ()->Unit){
        mRequestPermissionLauncher.launchRequest {
            permissions = arrayOf(*permission)
            isShowRationalDialogAfterDefined = true
            rationaleDialogFragment = mRationalDialogFragment
            forwardSettingDialogFragment = mNaviSettingsTipDialogFragment
            onAllGrantedAction = onSuccess
            onDeniedAction = {
                showShortToast("您需要允许权限才能继续")
            }
        }
    }

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
    private fun Context.getThemeColor(vararg attr: Int): Int {
        val array: TypedArray = theme.obtainStyledAttributes(attr)
        val color = array.getColor(0, Color.TRANSPARENT)
        array.recycle()
        return color
    }

    // </editor-fold>

}