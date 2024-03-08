package com.viettel.vht.sdk.ui.jfcameradetail.cloud.adapter

import com.vht.sdkcore.adapter.BaseAdapter
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.ItemStoragePackageBinding


class StoragePackageAdapter constructor(private val onClickListener: (Int) -> Unit) :
    BaseAdapter<StoragePackageAdapter.Data, ItemStoragePackageBinding>() {

    data class Data(
        val period: Int,
        var isSelected: Boolean = false
    )

    override val itemLayoutRes: Int
        get() = R.layout.item_storage_package

    override fun bindSingleTime(holder: BaseViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.apply {
            tvItem.text = String.format("Lưu trữ %s ngày", item.period)
            root.setOnClickListener {
                onClickListener(item.period)
                selectedItem(position)
            }
            cbSelect.setOnClickListener {
                onClickListener(item.period)
                selectedItem(position)
            }
        }
    }

    private fun selectedItem(position: Int) {
        if (currentList.isNotEmpty()) {
            currentList.forEach { it.isSelected = false }
            currentList[position].isSelected = true
            submitList(currentList)
        }
    }

    override fun bindMultiTime(holder: BaseViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.apply {
            cbSelect.isChecked = item.isSelected
        }
    }
}