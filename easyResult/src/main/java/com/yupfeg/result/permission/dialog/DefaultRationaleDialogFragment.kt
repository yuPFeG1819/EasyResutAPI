package com.yupfeg.result.permission.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yupfeg.result.R

/**
 * 默认实现的权限请求理由说明弹窗Fragment
 * @author yuPFeG
 * @date 2021/10/21
 */
@Suppress("unused")
class DefaultRationaleDialogFragment @JvmOverloads constructor(
    /**申请原因文本*/
    private val reason : String,
    /**确认按钮文本*/
    private val positiveText : String,
    /**取消按钮文本*/
    private val negativeText : String,
    /**原因说明文本颜色*/
    @ColorInt
    private val reasonTextColor : Int = -1,
    /**弹窗内容主色调*/
    @ColorInt
    private val tintColor : Int = -1
) : PermissionRationaleDialogFragment(){

    private lateinit var tvBtnPositive : TextView
    private lateinit var tvBtnNegative : TextView

    private val mListAdapter : RationalePermissionListAdapter by lazy(LazyThreadSafetyMode.NONE) {
        RationalePermissionListAdapter(tintColor)
    }

    private val mRequestPermissions : MutableList<PermissionItemBean> = mutableListOf()

    // <editor-fold desc="生命周期">

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.easy_result_dialog_def_rationale,container,false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        initDialogWindow()
    }

    // </editor-fold>

    // <editor-fold desc="父类方法实现">

    /**
     * 获取确认（允许）申请权限的View，
     * * 内部会设置点击事件，子类只需要提供该控件对象
     * * 该控件点击后会进行选择权限
     * */
    override fun getPositiveView(): View = tvBtnPositive

    /**
     * 获取取消（不允许）申请权限的View
     * * 内部会设置点击事件，子类只需要提供该控件对象
     * * 如果是强制性进行权限请求，则返回null
     * */
    override fun getNegativeView(): View = tvBtnNegative

    /**
     * 需要解释请求理由的权限变化时调用
     * * 子类实现用以更新权限说明展示
     * @param permissions 权限集合(过滤已允许和永久拒绝的)
     * */
    override fun doOnRequestPermissionUpdate(permissions: List<String>) {
//        val newItemData = createPermissionListData()
        mListAdapter.setPermissionList(createPermissionListData())
    }

    // </editor-fold>

    private fun initView(viewGroup : View){
        val tvMessage = viewGroup.findViewById<TextView>(R.id.tv_def_rationale_dialog_message)
        val recyclerView = viewGroup.findViewById<RecyclerView>(
            R.id.recycler_def_rationale_dialog_permission_content
        )
        tvBtnPositive = viewGroup.findViewById(R.id.tv_btn_def_rationale_dialog_positive)
        tvBtnNegative = viewGroup.findViewById(R.id.tv_btn_def_rationale_dialog_negative)

        tvMessage.text = reason
        if (reasonTextColor != -1){
            tvMessage.setTextColor(reasonTextColor)
        }

        tvBtnPositive.text = positiveText
        tvBtnNegative.text = negativeText

        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.adapter = mListAdapter
    }

    /**设置弹窗整体的宽度*/
    private fun initDialogWindow(){
        dialog?.window?.apply {
            val params = attributes
            params.gravity = Gravity.CENTER    //设置dialog位置
            attributes = params
            //设置背景透明
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setDialogLayout()
            setWindowAnimations(R.style.EasyResultDefaultDialogAnim)
        }
    }

    /**设置dialog的尺寸*/
    private fun Window.setDialogLayout(){
        //获取屏幕宽度
        val screenWidth = getScreenWidth(context)
        //设置dialog宽度为屏幕宽度的指定百分比，高度自适应
        setLayout((screenWidth * 0.85f).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    /**
     * 创建弹窗列表展示数据集合
     * */
    @Suppress("MemberVisibilityCanBePrivate")
    private fun createPermissionListData() : List<PermissionItemBean>{
        val tempSet = HashSet<String>()
        val list = mutableListOf<PermissionItemBean>()
        for (permission in requestPermissions) {
            val permissionGroup = getPermissionGroup(permission)
            if (permissionGroup.isNullOrEmpty()) continue

            if (!tempSet.contains(permissionGroup)){
                tempSet.add(permissionGroup)
                val permissionGroupInfo = requireContext().packageManager
                    .getPermissionGroupInfo(permissionGroup, 0)
                val itemBean = PermissionItemBean(
                    icon = permissionGroupInfo.icon,
                    name = requireContext().getString(permissionGroupInfo.labelRes)
                )
                list.add(itemBean)
            }
        }
        return list
    }

    /**
     * 获取屏幕宽度
     * @param context
     */
    private fun getScreenWidth(context : Context) : Int{
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            val windowRect = windowManager.currentWindowMetrics.bounds
            windowRect.width()
        }else{
            val outMetrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getMetrics(outMetrics)
            outMetrics.widthPixels
        }
    }
}