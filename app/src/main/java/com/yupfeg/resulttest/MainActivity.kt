package com.yupfeg.resulttest

import android.Manifest
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.yupfeg.result.*
import com.yupfeg.result.contact.ContactsTools
import com.yupfeg.result.file.getUriFromFile
import com.yupfeg.result.permission.RequestPermissionLauncher
import com.yupfeg.result.permission.dialog.DefaultRationaleDialogFragment
import java.io.File

class MainActivity : AppCompatActivity() {


    private val mTestResultActivityLauncher = StartActivityResultLauncher(this)

    private val mRequestPermissionLauncher = RequestPermissionLauncher(this)

    private val mTakePictureLauncher = TakePictureLauncher(this)
    private val mCropImageLauncher = CropImageLauncher(this)
    private val mPickContentLauncher = PickContentLauncher(this)
    private val mGetContentLauncher = GetMultiContentLauncher(this)

    private val mCallPhoneLauncher = CallPhoneLauncher(this)

    private val mRationalDialogFragment : DefaultRationaleDialogFragment
        by lazy(LazyThreadSafetyMode.NONE){ createRationalDialog() }

    private val mNaviSettingsTipDialogFragment : DefaultRationaleDialogFragment
        by lazy(LazyThreadSafetyMode.NONE){
            createNaviSettingTipDialog()
        }

    // <editor-fold desc="视图生命周期">

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
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

    // </editor-fold>

    private fun initView(){
        //测试请求多个权限
        findViewById<View>(R.id.btn_main_test_request_multi_permission).setOnClickListener {
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

        //测试跳转页面
        findViewById<View>(R.id.btn_main_test_navi_activity).setOnClickListener {
            mTestResultActivityLauncher.launch<TestResultApiActivity>{resultIntent->
                resultIntent?.extras?.also {
                    showShortToast("接收返回值${it["key"]}")
                }
            }
        }

        //系统相机拍照
        findViewById<View>(R.id.btn_main_take_picture).setOnClickListener {
            takePictureAndCrop()
        }

        //选择联系人
        findViewById<View>(R.id.btn_main_pick_contact).setOnClickListener {
            pickContact()
        }

        //pick content 选择图片
        findViewById<View>(R.id.btn_main_pick_content).setOnClickListener {
            pickSingleImageAndCrop()
        }

        //get multi content 选择多张图片
        findViewById<View>(R.id.btn_main_get_image_content).setOnClickListener {
            selectMultiImage()
        }

        //拨号界面
        findViewById<View>(R.id.btn_main_call_phone).setOnClickListener {
            mCallPhoneLauncher.launch("1012111")
        }
    }

    // <editor-fold desc="请求权限">

    /***
     * 请求权限
     * @param permission
     * @param onSuccess 请求权限成功回调
     */
    private fun requestPermissions(vararg permission : String, onSuccess : ()->Unit){
        //以 kotlin dsl方式直观的构筑权限请求
        mRequestPermissionLauncher.launchRequest {
            permissions = arrayOf(*permission)
            //仅在权限被拒绝后显示请求理由弹窗
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

    // <editor-fold desc="选择图片">

    /**
     * 执行拍照并剪裁操作
     */
    private fun takePictureAndCrop(){
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
     * 选择单张图片并剪裁
     * */
    private fun pickSingleImageAndCrop(){
        requestPermissions(Manifest.permission.READ_EXTERNAL_STORAGE){
            mPickContentLauncher.launchPickImage{uri->
                uri?:return@launchPickImage
                cropImage(uri)
//                    val localFilePath = getRealFilePathFromFileUri(this@MainActivity,uri)
//                    logd("pick content方式选择了图片 \n uri : ${uri}\n file : $localFilePath")
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

    /**
     * 选择多张图片
     * */
    private fun selectMultiImage(){
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

    // </editor-fold>

    // <editor-fold desc="获取联系人">

    /**
     * 选择联系人
     * */
    private fun pickContact(){
        requestPermissions(Manifest.permission.READ_CONTACTS){
            mPickContentLauncher.launchPickContact{ uri->
                uri?:return@launchPickContact
                val pair = ContactsTools.queryContactPhoneFromUri(this,uri)
                showShortToast("选择了${pair?.first?:""}")
            }
        }
    }

    // </editor-fold>

}