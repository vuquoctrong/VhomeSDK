package com.viettel.vht.sdk.ui.jfcameradetail.cloud.adapter

import com.vht.sdkcore.adapter.BaseAdapter
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.getColorCompat
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.ItemCameraStatusCloudBinding
import com.viettel.vht.sdk.model.camera_cloud.DataStatusCloud


class CameraStatusCloudListAdapter constructor(val clickItem: (DataStatusCloud?) -> Unit) :
    BaseAdapter<DataStatusCloud, ItemCameraStatusCloudBinding>() {

    override val itemLayoutRes: Int
        get() = R.layout.item_camera_status_cloud

    override fun bindSingleTime(holder: BaseViewHolder, position: Int) {
        val item = getItem(position)

        holder.itemBinding.apply {

            imgCamera.setImageResource(getProductIcon(item.camera.model ?: ""))
            tvName.text = item.camera.name
            tvStatus.text = item.bill.serviceStatus?.let { getServiceStatus(it) }
            when (item.bill.serviceStatus) {
                0 -> {
                    tvStatus.setTextColor(root.context.getColorCompat(R.color.color_4E4E4E))
                }
                1 -> {
                    tvStatus.setTextColor(root.context.getColorCompat(R.color.color_active_cloud))
                }
                2 -> {
                    tvStatus.setTextColor(root.context.getColorCompat(R.color.color_FF9C26))
                }
                else -> {
                    tvStatus.setTextColor(root.context.getColorCompat(R.color.color_F8214B))
                }
            }

            this.root.setOnClickListener {
                clickItem.invoke(item)
            }
        }
    }

    override fun bindMultiTime(holder: BaseViewHolder, position: Int) {
        val item = getItem(position)


    }

    private fun getServiceStatus(status: Int): String {
        return when (status) {
            0 -> "Hết hạn"
            1 -> "Đang sử dụng"
            2 -> "Chưa kích hoạt"
            -1 -> "Chưa đăng ký"
            else -> "Chưa đăng ký"
        }
    }

    fun getProductIcon(type: String): Int {

        return when (type) {
            Define.CAMERA_MODEL.CAMERA_JF_INDOOR -> com.vht.sdkcore.R.drawable.ic_product_camera_hc23
            Define.CAMERA_MODEL.CAMERA_JF_OUTDOOR -> com.vht.sdkcore.R.drawable.ic_product_camera_hc33
            else -> {
                com.vht.sdkcore.R.drawable.ic_product_camera_hc23
            }
        }

    }

}


