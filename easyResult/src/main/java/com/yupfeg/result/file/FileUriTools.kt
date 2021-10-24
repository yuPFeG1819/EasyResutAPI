package com.yupfeg.result.file

import android.content.Context
import android.os.Environment
import java.io.File

/**
 * 文件操作工具类
 * @author yuPFeG
 * @date 2019/11/8
 */
object FileUriTools {

    /**
     * 获取默认的app缓存目录地址
     * 默认缓存在APP包名下的应用私有cache目录
     * * 私有目录在Android 10以上不需要权限
     * @param context
     */
    @Suppress("unused")
    @JvmStatic
    fun getAppCacheDirPath(context: Context) : String{
        return if (hasExternalStorage()) {
            //获取应用私有cache目录的路径
            // /storage/emulated/0/Android/data/<application package>/cache
            context.applicationContext.externalCacheDir?.absolutePath
                ?: context.applicationContext.cacheDir.absolutePath
        } else {
            // /data/data/<application package>/cache
            context.applicationContext.cacheDir.absolutePath
        }
    }

    /**
     * 获取默认的APP文件下载目录地址
     * 默认缓存在APP包名下的应用私有文件目录
     * * 私有目录在Android 10以上不需要权限
     * @param context
     */
    @Suppress("unused")
    @JvmStatic
    fun getAppFilesDirPath(context: Context) : String{
        return if (hasExternalStorage()){
            // /storage/emulated/0/Android/data/<application package>/files
            context.applicationContext.getExternalFilesDir(null)?.absolutePath
                ?: context.applicationContext.filesDir.absolutePath
        }else{
            // /data/data/<application package>/files
            context.applicationContext.filesDir.absolutePath
        }
    }

    /**
     * 获取app私有文件目录下的指定目录文件对象
     * @param context
     * @param dirName 二级目录名称
     */
    @Suppress("unused")
    @JvmStatic
    fun getDirOnAppFiles(context: Context, dirName : String) : File{
        val fileDirPath = getAppFilesDirPath(context)
        val secDirPath = fileDirPath +File.separator+dirName
        //校验目标目录是否存在，不存在则创建该目录
        return mkdirIfNotFound(dirPath = secDirPath)
    }

    /**
     * 获取app私有缓存目录下的指定目录文件对象
     * @param context
     * @param dirName 二级文件夹名称
     */
    @Suppress("unused")
    @JvmStatic
    fun getDirOnAppCacheDir(context: Context, dirName : String) : File{
        val fileDirPath = getAppFilesDirPath(context)
        val secDirPath = fileDirPath +File.separator+dirName
        //校验目标目录是否存在，不存在则创建该目录
        return mkdirIfNotFound(dirPath = secDirPath)
    }

    /**
     * 判断外部存储是否存在
     */
    @JvmStatic
    fun hasExternalStorage(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    /**
     * 检查并建立指定的目录
     *
     * @param dirPath 目录的路径
     * @return 是否成功建立目录
     */
    @JvmStatic
    fun mkdirIfNotFound(dirPath: String) : File{
        val dirFile = File(dirPath)
        if (!dirFile.exists()){
            dirFile.mkdirs()
        }
        return dirFile
    }


}





