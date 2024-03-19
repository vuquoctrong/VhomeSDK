package com.viettel.vht.sdk.ui.gallery

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.viettel.vht.sdk.databinding.ItemGalleryBinding
import java.text.SimpleDateFormat
import java.util.*

class GalleryAdapter(val list:MutableList<ItemGallery>): RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {
    var actionCheckedMode: (() -> Unit)? = null
    var actionClickItem: ((String) -> Unit)? = null
    fun addDatas(datas:MutableList<ItemGallery>){
        list.clear()
        list.addAll(datas)
        notifyDataSetChanged()
    }
    class ViewHolder(val binding: ItemGalleryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindData(item: ItemGallery){

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGalleryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    fun doUnchecked(){
        list.forEach {
            it.listItem.forEach {
                it.isModeChecked = false
            }
        }
        notifyDataSetChanged()
    }

    fun selectAll(){
        list.forEach {
            it.listItem.forEach {
                it.isChecked = true
            }
        }
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val adapter = ImageAdapter(item.listItem.toMutableList())
        adapter.itemClickEvent = {
            actionClickItem?.invoke(it.path)
        }
        adapter.actionCheckedMode= {
            list.forEach {
                it.listItem.forEach {
                    it.isModeChecked = true
                }
            }
            actionCheckedMode?.invoke()
            notifyDataSetChanged()
        }
        holder.apply {
            binding.rcvImage.adapter = adapter
            binding.tvDate.text = SimpleDateFormat("dd/MM/yyyy").format(Date(item.dateTime))
            Log.d("TAG", "bindData: ${binding.tvDate.text}")
        }
    }

    fun getListChecked():List<String>{
        val listPath= mutableListOf<String>()
        list.forEach {
            val list=it.listItem.filter { it.isChecked }.map { it.path }
            listPath.addAll(list)
        }
        return listPath
    }

    override fun getItemCount(): Int = list.size
}