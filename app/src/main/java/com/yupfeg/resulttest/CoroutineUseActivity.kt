package com.yupfeg.resulttest

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.yupfeg.result.*
import com.yupfeg.result.contact.ContactsTools
import com.yupfeg.result.file.getUriFromFile
import com.yupfeg.result.permission.RequestPermissionLauncher
import kotlinx.coroutines.launch
import java.io.File

/**
 * 使用Kotlin协程的演示页
 * @author yuPFeG
 * @date
 */
class CoroutineUseActivity : BaseRequestActivity(){

    private val mRequestPermissionLauncher = RequestPermissionLauncher(this)
    private val mTestResultActivityLauncher = StartActivityResultLauncher(this)

    private val mTakePictureLauncher = TakePictureLauncher(this)
    private val mCropImageLauncher = CropImageLauncher(this)
    private val mPickContentLauncher = PickContentLauncher(this)
    private val mGetContentLauncher = GetMultiContentsLauncher(this)
    private val mCallPhoneLauncher = CallPhoneLauncher(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coroutine)
        initView()
    }

    private fun initView(){
        //测试请求多个权限
        findViewById<View>(R.id.btn_coroutine_request_multi_permission).setOnClickListener {
            testRequestMultiPermission()
        }

        //测试跳转页面
        findViewById<View>(R.id.btn_coroutine_navi_activity).setOnClickListener {
            lifecycleScope.launch {
                val resultIntent = mTestResultActivityLauncher
                    .launchAwaitIntentOrNull<TestResultApiActivity>()
                resultIntent?.extras?.also {
                    showShortToast("接收返回值${it["key"]}")
                }
            }
        }

        //系统相机拍照
        findViewById<View>(R.id.btn_coroutine_take_picture).setOnClickListener {
            takePictureAndCrop()
        }

        //选择联系人
        findViewById<View>(R.id.btn_coroutine_pick_contact).setOnClickListener {
            pickContact()
        }

        //pick content 选择图片
        findViewById<View>(R.id.btn_coroutine_pick_content).setOnClickListener {
            pickSingleImageAndCrop()
        }

        //get multi content 选择多张图片
        findViewById<View>(R.id.btn_coroutine_get_image_content).setOnClickListener {
            selectMultiImage()
        }

        //拨号界面
        findViewById<View>(R.id.btn_coroutine_call_phone).setOnClickListener {
            mCallPhoneLauncher.launch("1012111")
        }
    }

    // <editor-fold desc="请求权限">

    private fun testRequestMultiPermission() = lifecycleScope.launch{
        val isSuccess = requestPermissionAwait(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (isSuccess){
            showShortToast("所有权限请求成功")
        }
    }

    private suspend fun requestPermissionAwait(vararg permission : String) : Boolean{
        //以 kotlin dsl方式直观的构筑权限请求
        val result = mRequestPermissionLauncher.launchRequestAwait {
            permissions = arrayOf(*permission)
            //仅在权限被拒绝后显示请求理由弹窗
            isShowRationalDialogAfterDefined = true
            rationaleDialogFragment = mRationalDialogFragment
            forwardSettingDialogFragment = mNaviSettingsTipDialogFragment
        }
        //返回权限集合为空，表示所有权限都已允许
        return result.isNullOrEmpty()
    }

    // </editor-fold>

    // <editor-fold desc="选择图片">

    /**
     * 执行拍照并剪裁操作
     */
    private fun takePictureAndCrop() = lifecycleScope.launch {
        val isSuccess = requestPermissionAwait(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (!isSuccess){
            showShortToast("您需要允许相机权限")
            return@launch
        }
        val filePath = mTakePictureLauncher.launchAwaitOrNull()
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
        val fileUri = getUriFromFile(this@CoroutineUseActivity,photoFile)
        cropImage(fileUri)
    }

    /**
     * 选择单张图片并剪裁
     * */
    private fun pickSingleImageAndCrop() = lifecycleScope.launch {
        val isSuccess = requestPermissionAwait(Manifest.permission.READ_EXTERNAL_STORAGE)
        if (!isSuccess){
            showShortToast("您需要允许读取本地储存权限")
            return@launch
        }
        val imgUri = mPickContentLauncher.launchPickImageAwaitOrNull()
        imgUri?:return@launch
        cropImage(imgUri)
    }

    /**
     * 执行剪裁图片操作
     * @param uri 原始图片文件uri
     * */
    private suspend fun cropImage(uri: Uri){
        val clipFile = mCropImageLauncher.launchAwaitOrNull {
            cropFileUri = uri
        }
        clipFile?:run {
            showShortToast("剪裁失败")
            return
        }
        showShortToast("剪裁成功${clipFile.absoluteFile}")
    }

    /**
     * 选择多张图片
     * */
    private fun selectMultiImage() = lifecycleScope.launch {
        val isSuccess = requestPermissionAwait(Manifest.permission.READ_EXTERNAL_STORAGE)
        if (!isSuccess){
            showShortToast("您需要允许本地储存权限")
            return@launch
        }

        val uriList = mGetContentLauncher.launchImageAwaitOrNull()
        if (uriList.isNullOrEmpty()) return@launch
        showShortToast("选择了${uriList.size}张图片")
    }

    // </editor-fold>

    // <editor-fold desc="获取联系人">

    /**
     * 选择联系人
     * */
    private fun pickContact() = lifecycleScope.launch {
        val isSuccess = requestPermissionAwait(Manifest.permission.READ_CONTACTS)
        if (!isSuccess){
            showShortToast("您需要允许读取联系人权限")
            return@launch
        }

        val uri = mPickContentLauncher.launchPickContactAwaitOrNull()
        uri?:return@launch
        val pair = ContactsTools.queryContactPhoneFromUri(this@CoroutineUseActivity,uri)
        showShortToast("选择了${pair?.first?:""}")
    }

    // </editor-fold>

}