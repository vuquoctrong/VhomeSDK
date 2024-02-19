package com.vht.sdkcore.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import java.util.*

abstract class BaseSortableAdapter<T, ItemBinding : ViewBinding>() :
    RecyclerView.Adapter<BaseSortableAdapter<T, ItemBinding>.BaseViewHolder>() {

    protected val currentList: MutableList<T> = mutableListOf()

    protected abstract val itemLayoutRes: Int

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding: ItemBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), itemLayoutRes, parent, false)
        return BaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        onBindItem(holder, position)
    }

    protected abstract fun onBindItem(holder: BaseViewHolder, position: Int)

    override fun getItemCount(): Int = currentList.size

    fun onMove(from: Int, to: Int) {
        if (from < to) {
            for (index in from until to) {
                Collections.swap(currentList, index, index + 1)
            }
        } else {
            for (index in from downTo to + 1) {
                Collections.swap(currentList, index, index - 1)
            }
        }
        notifyItemMoved(from, to)
    }

    @SuppressLint("NotifyDataSetChanged")
    open fun submitList(data: List<T>) {
        currentList.apply {
            clear()
            addAll(data)
        }
        notifyDataSetChanged()
    }

    open fun currentList(): List<T> {
        return currentList.toList()
    }

    inner class BaseViewHolder(val itemBinding: ItemBinding) : RecyclerView.ViewHolder(itemBinding.root)

    companion object {
        const val TAG = "BaseSortableAdapter"
    }

}