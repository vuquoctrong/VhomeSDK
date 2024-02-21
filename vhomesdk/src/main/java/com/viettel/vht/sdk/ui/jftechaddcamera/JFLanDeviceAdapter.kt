package com.viettel.vht.sdk.ui.jftechaddcamera

import com.manager.db.XMDevInfo
import com.vht.sdkcore.adapter.BaseAdapter
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.ItemLanDeviceBinding

class JFLanDeviceAdapter() :
    BaseAdapter<XMDevInfo, ItemLanDeviceBinding>() {

    private var callback: Callback? = null

    override val itemLayoutRes: Int
        get() = R.layout.item_lan_device

    override fun bindSingleTime(holder: BaseViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.apply {
            tvName.text = item.devId
            imgCamera.setImageResource(if (item?.systemInfoBean?.deviceModel == "R80XV20") com.vht.sdkcore.R.drawable.ic_product_camera_hc23 else com.vht.sdkcore.R.drawable.ic_product_camera_hc33)
//            tvSerial.text = item.devId
            root.setOnClickListener {
                if (callback != null) {
                    callback?.onItemClick(item)
                }
//                onItemClick(item)
            }
        }
    }

    override fun bindMultiTime(holder: BaseViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.apply {
            imgCamera.setImageResource(if (item?.systemInfoBean?.deviceModel == "R80XV20") com.vht.sdkcore.R.drawable.ic_product_camera_hc23 else com.vht.sdkcore.R.drawable.ic_product_camera_hc33)
        }
    }

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    public interface Callback {
        fun onItemClick(xmDevInfo: XMDevInfo)
    }
}