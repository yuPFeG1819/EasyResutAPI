package com.yupfeg.resulttest

import android.Manifest
import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import com.yupfeg.result.*
import com.yupfeg.result.contact.ContactsTools
import com.yupfeg.result.file.getRealFilePathFromFileUri
import com.yupfeg.result.file.getUriFromFile
import com.yupfeg.result.permission.RequestPermissionLauncher
import java.io.File

/**
 * 传统方式使用的演示页面
 * @author yuPFeG
 * @date 2021/11/29
 */
class NormalUseActivity : BaseRequestActivity(){

    private val mRequestPermissionLauncher = RequestPermissionLauncher(this)
    private val mTestResultActivityLauncher = StartActivityResultLauncher(this)

    private val mTakePictureLauncher = TakePictureLauncher(this)
    private val mCropImageLauncher = CropImageLauncher(this)
    private val mPickContentLauncher = PickContentLauncher(this)
    private val mGetContentLauncher = GetMultiContentsLauncher(this)
    private val mCallPhoneLauncher = CallPhoneLauncher(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_normal)
        initView()
    }

    private fun initView(){
        //测试请求多个权限
        findViewById<View>(R.id.btn_normal_request_multi_permission).setOnClickListener {
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
        findViewById<View>(R.id.btn_normal_navi_activity).setOnClickListener {
            mTestResultActivityLauncher.launch<TestResultApiActivity>{resultIntent->
                resultIntent?.extras?.also {
                    showShortToast("接收返回值${it["key"]}")
                }
            }
        }

        //系统相机拍照
        findViewById<View>(R.id.btn_normal_take_picture).setOnClickListener {
            takePictureAndCrop()
        }

        //选择联系人
        findViewById<View>(R.id.btn_normal_pick_contact).setOnClickListener {
            pickContact()
        }

        //pick content 选择图片
        findViewById<View>(R.id.btn_normal_pick_content).setOnClickListener {
            pickSingleImageAndCrop()
        }

        //get multi content 选择多张图片
        findViewById<View>(R.id.btn_normal_get_image_content).setOnClickListener {
            selectMultiImage()
        }

        //拨号界面
        findViewById<View>(R.id.btn_normal_call_phone).setOnClickListener {
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
                val localFilePath = getRealFilePathFromFileUri(this,uri)
                Log.d("logger","pick content方式选择了图片 \n uri : ${uri}\n file : $localFilePath")
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

    // </editor-fold>

    // <editor-fold desc="获取联系人">

    /**
     * 选择联系人
     * */
    @SuppressLint("MissingPermission")
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