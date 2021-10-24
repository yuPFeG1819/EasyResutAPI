package com.yupfeg.resulttest.base

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import java.io.Serializable

/**
 * 系统联系人相关工具类
 * @author yuPFeG
 * @date 2021/10/20
 */
@Suppress("unused")
object ContactsTools {
    /**
     * 通讯录的列表实体
     * @author yuPFeG
     * @date 2021/09/02
     */
    data class ContactUserBean(
        /**本地通讯录id*/
        val contactId : Long,
        /**本地通讯录的备注名称*/
        val localUserName : String? = null,
        /**本地通讯录的手机号*/
        val phone : String,
    ) : Serializable

    /**
     * 查询本地通讯录联系人数据
     * * 推荐在子线程执行
     * @param context
     * @return 系统通讯录的实体集合
     * */
    @JvmStatic
    fun queryLocalContactsList(context: Context) : List<ContactUserBean>{
        val contactsList = mutableListOf<ContactUserBean>()
        val cursor = context.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,null,
            null,null
        )
        cursor?:return contactsList

        while (cursor.moveToNext()){
            //取得联系人名称、手机号、id
            val contactId = cursor.getString(
                cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
            )
            val name = cursor.getString(
                cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)
            )
            val phone = obtainFirstPhoneString(context, contactId)

            //过滤手机号为空的联系人
            if (phone.isNullOrEmpty()) continue

            val userBean = ContactUserBean(
                contactId = contactId.toLongOrNull()?:-1,
                localUserName = name,
                phone = phone
            )
            contactsList.add(userBean)
        }
        cursor.close()
        return contactsList
    }

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