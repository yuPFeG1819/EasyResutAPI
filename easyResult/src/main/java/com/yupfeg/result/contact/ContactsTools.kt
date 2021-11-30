package com.yupfeg.result.contact

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract

/**
 * 系统联系人相关工具类
 * @author yuPFeG
 * @date 2021/10/20
 */
@Suppress("unused")
object ContactsTools {
    /**
     * 查询指定联系人Uri的联系人信息
     * @param context [Context]
     * @param uri 联系人uri，由系统联系人选择返回
     * @return 联系人名称与联系人电话的键值对
     * */
    fun queryContactPhoneFromUri(context: Context, uri: Uri) : Pair<String,String>?{
        var pair : Pair<String,String>? = null
        val cursor = context.contentResolver.query(
            uri,null,null,null,null
        )
        cursor?:return null

        if (cursor.moveToFirst()){
            //取得联系人姓名
            val name = cursor.getString(
                cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)
            )
            val contactId = cursor.getString(
                cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
            )
            val phone = obtainFirstPhoneString(context, contactId)

            if (phone.isNullOrEmpty()) {
                cursor.close()
                return null
            }
            pair = Pair(name,phone)
        }
        cursor.close()
        return pair
    }


    /**
     * 尝试获取通讯录id内的第一个非空手机号
     * @param context [Context]
     * @param contactId 通讯录id
     * @return
     */
    private fun obtainFirstPhoneString(context: Context, contactId : String) : String?{
        //指定获取NUMBER这一列数据
        val phoneProjection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
        //根据联系人的ID获取此人的电话号码
        val phonesCursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            phoneProjection,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId,
            null,
            null
        )
        phonesCursor?:return null
        var userPhone : String? = null
        while (phonesCursor.moveToNext()){
            //只获取联系人的第一个不为空的手机号
            val userNumber = phonesCursor.getString(
                phonesCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            )
            if (userNumber.isNullOrEmpty()) continue
            //过滤特殊字符
            userPhone = userNumber.replace(" ","").replace("-","")
            break
        }
        phonesCursor.close()
        return userPhone
    }
}