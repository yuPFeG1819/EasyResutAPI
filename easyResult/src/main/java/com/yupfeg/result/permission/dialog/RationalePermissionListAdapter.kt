package com.yupfeg.result.permission.dialog

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import com.yupfeg.result.R

/**
 * 默认实现的请求权限列表adapter
 * @author yuPFeG
 * @date 2021/10/21
 */
internal class RationalePermissionListAdapter(
    @ColorInt
    private val tintColor : Int,
) : RecyclerView.Adapter<PermissionViewHolder>(){
    private val mDataList : MutableList<PermissionItemBean> = mutableListOf()

    @SuppressLint("NotifyDataSetChanged")
    fun setPermissionList(list : List<PermissionItemBean>){
        mDataList.clear()
        mDataList.addAll(list)
        notifyItemRangeChanged(0,mDataList.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PermissionViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.easy_result_recycler_item_permission, parent, false)
        return PermissionViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PermissionViewHolder, position: Int) {
        holder.setPermissionName(mDataList[position].name)
        holder.setPermissionIcon(mDataList[position].icon,tintColor)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }
}

internal data class PermissionItemBean(
    val icon : Int,
    val name : String
)

internal class PermissionViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

    private var mIvIcon : ImageView? = null
    private var mTvName : TextView? = null

    init {
        mIvIcon = itemView.findViewById(R.id.iv_permission_item_icon)
        mTvName = itemView.findViewById(R.id.tv_permission_item_name)
    }

    fun setPermissionName(name : String){
        mTvName?.text = name
    }

    fun setPermissionIcon(icon : Int,tintColor: Int){
        mIvIcon?.setImageResource(icon)
        if (tintColor != -1){
            mIvIcon?.setColorFilter(tintColor, PorterDuff.Mode.SRC_ATOP)
        }
    }
}