package com.viettel.vht.sdk.ui.gallery

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vht.sdkcore.utils.Utils.Companion.isVideo
import com.viettel.vht.sdk.databinding.ItemImageBinding
import com.viettel.vht.sdk.utils.gone
import com.viettel.vht.sdk.utils.visible
import java.io.File

class ImageAdapter(val list: MutableList<ItemDetailGallery>) :
    RecyclerView.Adapter<ImageAdapter.ViewHolder>() {
    var actionCheckedMode: (() -> Unit)? = null
    var itemClickEvent: ((ItemDetailGallery) -> Unit)? = null
    fun addDatas(datas: MutableList<ItemDetailGallery>) {
        list.clear()
        list.addAll(datas)
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemImageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindData(item: ItemDetailGallery) {

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list.get(position)
        holder.apply {
            if (File(item.path).isVideo()) {
                binding.imvPlay.visible()
                binding.imVideo.visible()
            } else {
                binding.imvPlay.gone()
                binding.imVideo.gone()
            }
            binding.imvChecked.isChecked = item.isChecked
            Glide.with(binding.root.context).load(item.path).into(binding.imvImage)
            if (item.isModeChecked) {
                binding.imvChecked.visible()
            } else {
                item.isChecked = false
                binding.imvChecked.gone()
            }
            binding.imvChecked.isChecked = item.isChecked
            binding.root.setOnClickListener {
                if (item.isModeChecked == true) {
                    binding.imvChecked.performClick()
                } else {
                    itemClickEvent?.invoke(item)
                }
            }
            binding.root.setOnLongClickListener(object : View.OnLongClickListener {
                override fun onLongClick(v: View?): Boolean {
                    actionCheckedMode?.invoke()
                    val isChecked = item.isChecked
                    item.isChecked = !isChecked
                    notifyDataSetChanged()
                    return true
                }
            })
            binding.imvChecked.setOnClickListener {
                val isChecked = item.isChecked
                item.isChecked = !isChecked
                notifyItemChanged(adapterPosition)
            }
        }
    }

    override fun getItemCount(): Int = list.size
}