package com.viettel.vht.sdk.ui.jfcameradetail.cloud.adapter

import android.view.View
import com.vht.sdkcore.adapter.BaseAdapter
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.ItemStorageTimeBinding
import com.viettel.vht.sdk.model.camera_cloud.CloudStoragePackage
import com.viettel.vht.sdk.utils.gone
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

class StorageTimeAdapter constructor( private val showCheckBox: Boolean = true, val onItemClickListener: (CloudStoragePackage) -> Unit) :
    BaseAdapter<CloudStoragePackage, ItemStorageTimeBinding>() {

    override val itemLayoutRes: Int
        get() = R.layout.item_storage_time

    override fun bindSingleTime(holder: BaseViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.apply {
            val day = TimeUnit.SECONDS.toDays(item.expired ?: 0)
            var text = String.format("%s ngày", day)
            if (item.descriptions != null && item.descriptions?.noteVi != null) {
                text = item.descriptions?.noteVi.toString()
            }
            tvTime.text = text.capitalize()
            tvPrice.text = "${DecimalFormat("#,###,###,###").format(item.price)}đ"
            if (!showCheckBox) {
                cbSelect.gone()
            }
            divider.visibility = if (position != (itemCount - 1)) View.VISIBLE else View.GONE
        }
    }

    override fun bindMultiTime(holder: BaseViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.apply {
            cbSelect.isChecked = item.isSelected
            layoutClick.setOnClickListener {
                onSelectedItem(position)
                onItemClickListener.invoke(currentList[position])
            }
        }
    }

    private fun onSelectedItem(position: Int) {
        if (currentList.isNotEmpty()) {
            var isNotSelect = true
            currentList.forEachIndexed { index, dataFreePricing ->
                if (index != position) {
                    if (dataFreePricing.isSelected) {
                        isNotSelect = false
                        return@forEachIndexed
                    }
                }

            }
            if (isNotSelect){
                currentList[position].isSelected = true
                return
            }
            currentList.forEachIndexed { index, cloudStoragePackage ->
                if (index == position){
                    cloudStoragePackage.isSelected = !cloudStoragePackage.isSelected
                }else{
                    cloudStoragePackage.isSelected = false
                }
            }
            submitList(currentList)
            notifyDataSetChanged()
        }
    }
}
