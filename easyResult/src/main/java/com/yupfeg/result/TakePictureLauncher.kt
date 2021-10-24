package com.yupfeg.result

import android.net.Uri
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import com.yupfeg.result.file.FileUriTools
import com.yupfeg.result.file.getUriFromFile
import java.io.File

/**
 * 调起系统相机的启动器类
 * @author yuPFeG
 * @date 2021/10/19
 */
@Suppress("unused")
open class TakePictureLauncher(
    caller: ActivityResultCaller
) : ActivityResultLauncherWrapper<Uri,Boolean>(
    caller, ActivityResultContracts.TakePicture()
){

    companion object{
        /**默认的相机拍照目录名称*/
        const val DEF_CAMERA_DIR = "photo"
    }

    /**
     * 调起系统相机拍照
     * @param callBack 系统相机拍照结果回调，返回保存拍照文件的完整地址，如果拍照不成功或者文件不存在，则返回null
     * */
    open fun launch(callBack : ActivityResultCallback<String?>) {
        val saveFile = createCameraSaveFile()
        launch(getUriFromFile(requireContext(),saveFile)) { isSuccess ->
            if (isSuccess && saveFile.exists()) {
                callBack.onActivityResult(saveFile.absolutePath)
            }else{
                callBack.onActivityResult(null)
            }
        }
    }

    /**
     * 创建拍照保存的文件对象
     * @param dirName 保存目录名称
     * */
    open fun createCameraSaveFile(dirName : String? = DEF_CAMERA_DIR) : File{
        dirName?.takeIf { it.isNotEmpty() }?.also {
            //存在二级目录名称，检查拍照文件保存的文件夹是否已创建
            val fileDir = FileUriTools.getDirOnAppFiles(requireContext(), dirName)
            // 用日期作为文件名，确保唯一性
            return File(fileDir, "${System.currentTimeMillis()}.jpg")
        }
        //未设置目录名称，默认保存在私有文件的根目录
        val privateCache = FileUriTools.getAppFilesDirPath(requireContext())
        return File("${privateCache}${File.separator}${System.currentTimeMillis()}.jpg")
    }

}