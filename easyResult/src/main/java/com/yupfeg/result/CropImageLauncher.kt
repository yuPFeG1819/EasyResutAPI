package com.yupfeg.result

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContract
import com.yupfeg.result.file.FileUriTools
import java.io.File

/**
 * 系统剪裁图片的配置类
 */
data class CropImageConfig @JvmOverloads constructor(
    /**
     * 需要剪裁的图片uri，通常为相册选择或相机拍照的文件uri,默认为null
     * * 如果使用图片文件地址，则需要使用[Uri.fromFile]方法转化为uri，注意在Android 7.0以上需要适配FileProvider
     * */
    var cropFileUri : Uri? = null,
    /**剪裁完成后输出的文件，默认为null，使用默认的输出文件地址*/
    var outputFile : File? = null,
    /**剪裁框宽度比例*/
    var aspectX : Int = 1,
    /**剪裁框高度比例*/
    var aspectY : Int = 1,
    /**剪裁输出图片的宽度*/
    var outputX : Int = DEF_OUT_X,
    /**剪裁输出推盘的高度*/
    var outputY : Int = DEF_OUT_Y,
    /**剪裁图片确认回调，回调剪裁完成后的图片文件对象，如果剪裁失败则为null*/
    var callBack : ((File?)->Unit) ?= null
){
    companion object{
        /**默认的系统剪裁输出的x轴像素*/
        private const val DEF_OUT_X = 800
        /**默认的系统剪裁输出的y轴像素*/
        private const val DEF_OUT_Y = 800
    }
}

/**
 * 系统剪裁图片的启动器
 * * 支持Kotlin DSL方式配置系统剪裁参数
 * @author yuPFeG
 * @date 2021/10/20
 */
@Suppress("unused")
open class CropImageLauncher(
    caller : ActivityResultCaller
) : ActivityResultLauncherWrapper<CropImageConfig,File?>(
    caller, CropImageContract()
){

    companion object{
        /**默认的相机拍照目录名称*/
        const val DEF_CROP_DIR_NAME = ".crop"
    }

    /**
     * 启动跳转到系统剪裁图片页
     * @param init 以kotlin dsl方式配置图片剪裁参数[CropImageConfig]
     * */
    fun launch(init : CropImageConfig.()->Unit){
        val config = CropImageConfig().also(init)
        config.outputFile?:run {
            //使用默认输出地址
            config.outputFile = createCropImgSaveFile()
        }
        launch(config) { result ->
            config.callBack?.invoke(result)
        }
    }

    /**
     * 创建图片剪裁保存的文件对象
     * @param dirName 保存目录名称
     * */
    open fun createCropImgSaveFile(dirName : String? = DEF_CROP_DIR_NAME) : File{
        dirName?.takeIf { it.isNotEmpty() }?.also {
            //存在二级目录名称，检查拍照文件保存的文件夹是否已创建
            val fileDir = FileUriTools.getDirOnAppFiles(requireContext(), dirName)
            // 用时间戳作为文件名，确保唯一性(临时文件地址)
            return File(fileDir, "clip_${System.currentTimeMillis()}.jpg")
        }
        //未设置目录名称，默认保存在私有文件的根目录
        val privateFiles= FileUriTools.getAppFilesDirPath(requireContext())
        return File("${privateFiles}${File.separator}${System.currentTimeMillis()}.jpg")
    }
}

/**
 * 系统剪裁图片的Intent Result协议类
 * */
open class CropImageContract : ActivityResultContract<CropImageConfig,File?>(){
    private var mOutputFile : File? = null

    override fun createIntent(context: Context, input: CropImageConfig): Intent {
        input.cropFileUri?:throw NullPointerException("crop image uri is null")
        input.outputFile?:throw NullPointerException("output file is null")

        return Intent("com.android.camera.action.CROP").apply {
            setDataAndType(input.cropFileUri, "image/*")
            // 设置裁剪
            putExtra("crop", "true")
            // aspectX aspectY 是宽高的比例
            putExtra("aspectX", input.aspectX)
            putExtra("aspectY", input.aspectY)
            // outputX outputY 是裁剪图片宽高
            putExtra("outputX", input.outputX)
            putExtra("outputY", input.outputY)
            putExtra("scale", true)
            //处理后的图片不进行在data中返回
            putExtra("return-data", false)
            //输出格式jpeg
            putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())

            //删除已存在的剪切输出文件
            if (input.outputFile?.exists() == true) input.outputFile!!.delete()
            mOutputFile = input.outputFile
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //7.0以上需要添加这一句表示对目标应用临时授权该Uri所代表的文件
                addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            }
            //裁剪输出继续使用 Uri.fromFile(file)，不需要使用FileProvider
            putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(input.outputFile))
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): File? {
        return if (resultCode == Activity.RESULT_OK) mOutputFile else null
    }

}