package com.viettel.vht.sdk.ui.jfcameradetail.cloud.promotion

import com.vht.sdkcore.adapter.BaseAdapter
import com.vht.sdkcore.utils.Define
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.ItemCameraRegisterFreeBinding
import com.viettel.vht.sdk.model.camera_cloud.DataFreePricing


class CameraSelectFreeAdapter constructor( val onItemClickListener: (DataFreePricing) -> Unit) :
    BaseAdapter<DataFreePricing, ItemCameraRegisterFreeBinding>() {

    override val itemLayoutRes: Int
        get() = R.layout.item_camera_register_free

    override fun bindSingleTime(holder: BaseViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.apply {

            val typeIcon = when(item.type){
                Define.TYPE_DEVICE.CAMERA_JF -> if (item.cameraModel == Define.CAMERA_MODEL.CAMERA_JF_INDOOR) com.vht.sdkcore.R.drawable.ic_product_camera_hc23 else com.vht.sdkcore.R.drawable.ic_product_camera_hc33

                else ->{
                    com.vht.sdkcore.R.drawable.ic_product_camera_hc23
                }
            }

            imvIconCamera.setImageResource(typeIcon)
            tvItem.text = item.name

        }
    }

    override fun bindMultiTime(holder: BaseViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.apply {

            root.setOnClickListener {
                onSelectedItem(position)
                onItemClickListener.invoke(currentList[position])
            }
            if(item.isSelected){
                imvSelect.setImageResource(R.drawable.ic_enable_square)
            }else{
                imvSelect.setImageResource(R.drawable.ic_disable_square)
            }

        }
    }

    private fun onSelectedItem(position: Int) {
        if (currentList.isNotEmpty()) {
            currentList.forEachIndexed { index, dataFreePricing ->
                if (index == position){
                    dataFreePricing.isSelected = !dataFreePricing.isSelected
                }
            }

            submitList(currentList)
        }
    }

    fun onSelectedAll(isEnable : Boolean){
        if (currentList.isNotEmpty()) {

            currentList.forEach {
                it.isSelected = isEnable
            }
            submitList(currentList)
        }
    }
}
