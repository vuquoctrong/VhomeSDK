package com.viettel.vht.sdk.ui.jfcameradetail.smart_feature.view

import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.vht.sdkcore.adapter.BaseAdapter
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FunctionItemBinding
import com.viettel.vht.sdk.ui.jfcameradetail.smart_feature.model.FunctionViewItemElement


/**
 * Created by zhangyongyong on 2017-05-08-11:14.
 */


class FunctionViewAdapter : BaseAdapter<FunctionViewItemElement, FunctionItemBinding>(), View.OnClickListener{

    private var mItemClickListener: OnItemClickListener? = null

    override val itemLayoutRes: Int
        get() = R.layout.function_item



    override fun bindSingleTime(holder: BaseViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.apply {
            root.setOnClickListener(this@FunctionViewAdapter)
            functionViewName.text = item.name
            if (position < itemCount) {
                root.background = ResourcesCompat.getDrawable(holder.itemBinding.root.resources, R.drawable.smart_analyze_right_line, null)
            }
        }
    }

    override fun bindMultiTime(holder: BaseViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.apply {
            holder.itemBinding.root.tag = position
            if (item.isSelected) {
                functionViewIco.setImageResource(item.selectedResourceId)
                functionViewName.setTextColor(ContextCompat.getColor(root.context, com.xm.uilibrary.R.color.red))
            } else {
                functionViewIco.setImageResource(item.normalResourceId)
                functionViewName.setTextColor(ContextCompat.getColor(root.context, com.vht.sdkcore.R.color.text_black))
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(view: View?, position: Int, label: String?)
    }

    override fun onClick(v: View) {
        mItemClickListener?.onItemClick(v, (v.tag as Int), currentList.get(v.tag as Int).name)
    }

    fun setOnItemClickListener(mItemClickListener: OnItemClickListener?) {
        this.mItemClickListener = mItemClickListener
    }

    fun setItemSelected(position: Int) {
        if (currentList != null && position >= 0 && position < currentList.size) {
            resetState()
            currentList.get(position).isSelected = true
            submitList(currentList)
        }
    }

    fun setItemUnSelected() {
        if (currentList != null) {
            resetState()
            submitList(currentList)
        }
    }

    fun resetState() {
        for (element in currentList) {
            element.isSelected = false
        }
    }
}