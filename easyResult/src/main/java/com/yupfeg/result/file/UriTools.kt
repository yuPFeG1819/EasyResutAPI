package com.yupfeg.result.file

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.FileUtils
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import java.io.*
import java.lang.IllegalArgumentException
import kotlin.math.roundToInt


/**
 * 获取本地文件的uri
 *
 * 7.0以上利用FileProvider获取;
 * 7.0以下使用默认方式Uri.fromFile获取
 * @param context  上下文
 * @param file     本地文件实例
 * @param fileProviderName Manifest内注册的provider节点内，android:authorities属性内容
 * 默认为包名+“.fileProvider”
 * @return 本地文件uri
 */
@Suppress("unused")
@JvmOverloads
fun getUriFromFile(
    context: Context,
    file: File,
    fileProviderName : String = "${context.packageName}.fileProvider"
): Uri {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        //7.0以上需要使用内容提供者授权分享uri
        //这里的第二个参数是在注册表注册的provider节点内，android:authorities属性内容
        FileProvider.getUriForFile(context, fileProviderName, file)
    } else {
        Uri.fromFile(file)
    }
}

/**
 * 从文件Uri中获取实际文件完整路径
 * @param context [Context]
 * @param uri 文件uri
 */
@Suppress("unused")
@SuppressLint("ObsoleteSdkInt")
@Throws(FileNotFoundException::class, IllegalArgumentException::class,IOException::class)
fun getRealFilePathFromFileUri(context: Context, uri: Uri) : String{
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
        //兼容4.4以下的低版本
        return getRealFilePathBeforeApiKitkat(context, uri)
    }
    else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
        //兼容Android10以上
        return uriToFilePathOnApiQ(context, uri)
    }
    else if (DocumentsContract.isDocumentUri(context, uri)){
        //文档类型的uri
        if (isExternalStorageDocument(uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":".toRegex()).toTypedArray()
            val type = split[0]
            if ("primary".equals(type, ignoreCase = true)) {
                @Suppress("DEPRECATION")
                return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
            }
        } else if (isDownloadsDocument(uri)) {
            val id = DocumentsContract.getDocumentId(uri)
            val contentUri = ContentUris.withAppendedId(
                Uri.parse("content://downloads/public_downloads"),
                id.toLongOrNull()?:0
            )
            return getDataColumnStringFromUri(context, contentUri, null, null)
        } else if (isMediaDocument(uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":".toRegex()).toTypedArray()
            val type = split[0]
            var contentUri: Uri? = null
            when (type) {
                "image" -> {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }
                "video" -> {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }
                "audio" -> {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
            }
            val selection = MediaStore.Images.Media._ID + "=?"
            val selectionArgs = arrayOf(split[1])
            if (contentUri == null) return ""
            return getDataColumnStringFromUri(context, contentUri, selection, selectionArgs)
        }
    }else if ("content" == uri.scheme?:"") {
        // Return the remote address
        if (isGooglePhotosUri(uri)) {
            return uri.lastPathSegment ?:""
        }
        return getDataColumnStringFromUri(context, uri, null, null)
    }
    // File
    else if ("file" == uri.scheme) {
        return uri.path ?:""
    }
    return ""
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is ExternalStorageProvider.
 */
private fun isExternalStorageDocument(uri: Uri): Boolean {
    return "com.android.externalstorage.documents" == uri.authority
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is DownloadsProvider.
 */
private fun isDownloadsDocument(uri: Uri): Boolean {
    return "com.android.providers.downloads.documents" == uri.authority
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is MediaProvider.
 */
private fun isMediaDocument(uri: Uri): Boolean {
    return "com.android.providers.media.documents" == uri.authority
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is Google Photos.
 */
private fun isGooglePhotosUri(uri: Uri): Boolean {
    return "com.google.android.apps.photos.content" == uri.authority
}


/**
 * 从文件uri中获取指定的系统数据库字段
 * * 仅在android 4.4到
 */
@Throws(IllegalArgumentException::class)
private fun getDataColumnStringFromUri(
    context: Context,
    uri: Uri,
    selection: String?,
    selectionArgs: Array<String>?
): String {
    var cursor: Cursor? = null
    @Suppress("DEPRECATION") val column = MediaStore.Images.Media.DATA
    val projection = arrayOf(column)
    try {
        cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
        if (null != cursor && cursor.moveToFirst()) {
            val index = cursor.getColumnIndexOrThrow(column)
            return cursor.getString(index)
        }
    } catch (e : Exception){
        throw IllegalArgumentException(e.message)
    }finally {
        cursor?.close()
    }
    return ""
}


/**
 * 从文件uri获取完整文件路径
 * * 仅适用于Android4.4以下
 * @param context [Context]
 * @param uri 文件uri
 */
@Throws(IllegalArgumentException::class)
private fun getRealFilePathBeforeApiKitkat(context: Context, uri: Uri): String {
    val scheme = uri.scheme
    var data: String? = null
    when {
        scheme.isNullOrEmpty() -> {
            data = uri.path?:""
        }
        ContentResolver.SCHEME_FILE == scheme -> {
            data = uri.path
        }
        ContentResolver.SCHEME_CONTENT == scheme -> {
            @Suppress("DEPRECATION")
            val projection = arrayOf(MediaStore.Images.ImageColumns.DATA)
            var cursor : Cursor? = null
            try {
                cursor = context.contentResolver.query(uri, projection, null,
                    null, null)
                if (null != cursor && cursor.moveToFirst()) {
                    @Suppress("DEPRECATION")
                    val index = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA)
                    if (index > -1) {
                        data = cursor.getString(index)
                    }
                }
            } finally {
                cursor?.close()
            }
        }
    }
    return data?:""
}


/**
 * Android 10以上，转换文件uri到实际文件路径字符串
 * @param context [Context]
 * @param uri 文件uri
 * @return 文件路径字符串，如果获取不到则返回""
 * */
@RequiresApi(Build.VERSION_CODES.Q)
@Throws(FileNotFoundException::class, IllegalArgumentException::class,IOException::class)
private fun uriToFilePathOnApiQ(context: Context, uri : Uri) : String{
    var file: File? = null
    //android10以上转换
    if (uri.scheme.equals(ContentResolver.SCHEME_FILE)) {
        file = File(uri.path?:"")
    } else if (uri.scheme.equals(ContentResolver.SCHEME_CONTENT)) {
        //把文件复制到沙盒目录
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(uri, null,
            null, null, null)
        cursor?.use {
            if (!cursor.moveToFirst()) return ""
            val displayName = cursor.getString(
                cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
            )
            var input : InputStream? = null
            var fos : FileOutputStream? = null
            try {
                input = contentResolver.openInputStream(uri)
                input?:return ""
                val cache = File(
                    context.externalCacheDir!!.absolutePath,
                    ((Math.random() + 1) * 1000).roundToInt()
                        .toString() + displayName
                )
                fos = FileOutputStream(cache)
                FileUtils.copy(input, fos)
                file = cache
            } finally {
                fos?.close()
                input?.close()
            }
        }
    }
    return file?.absolutePath?:""

}