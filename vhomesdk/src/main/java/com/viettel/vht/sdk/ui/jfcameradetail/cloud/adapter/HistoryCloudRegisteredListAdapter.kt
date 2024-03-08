package com.viettel.vht.sdk.ui.jfcameradetail.cloud.adapter

import com.vht.sdkcore.adapter.BaseAdapter
import com.vht.sdkcore.utils.getColorCompat
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.ItemHistoryCloudStorageBinding
import com.viettel.vht.sdk.model.camera_cloud.CloudStorageRegistered
import com.viettel.vht.sdk.utils.gone
import com.viettel.vht.sdk.utils.visible
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


class HistoryCloudRegisteredListAdapter() :
    BaseAdapter<CloudStorageRegistered, ItemHistoryCloudStorageBinding>() {

    override val itemLayoutRes: Int
        get() = R.layout.item_history_cloud_storage

    override fun bindSingleTime(holder: BaseViewHolder, position: Int) {
        val item = getItem(position)

        holder.itemBinding.apply {
            val isPostpaid = item.paymentMethod == "BCCS"
            tvServiceName.text = item.infoService?.vi?.capitalize()
            if (!isPostpaid) {
                tvServicePrice.text = "${DecimalFormat("#,###,###,###").format(item.price)}đ"
                tvServicePrice.visible()
            } else {
                tvServicePrice.gone()
            }
            if (!isPostpaid) {
                tvServiceEffect.text = root.context.getString(com.vht.sdkcore.R.string.date_effective) + " ${
                    item.startDateTime?.times(1000L)?.toDate()
                }-${item.endDateTime?.times(1000L)?.toDate()}"
                tvServiceEffect.visible()
            } else {
                tvServiceEffect.gone()
            }
            tvServiceType.text = if (isPostpaid) "Trả sau" else "Trả trước"
            tvServiceStatus.text = item.serviceStatus?.let { getServiceStatus(it) }
            if (item.serviceStatus == 1) {
                tvServiceStatus.setTextColor(root.context.getColorCompat(R.color.color_active_cloud))
            } else {
                tvServiceStatus.setTextColor(root.context.getColorCompat(R.color.color_F8214B))
            }
        }
    }

    override fun bindMultiTime(holder: BaseViewHolder, position: Int) {
        val item = getItem(position)

        holder.itemBinding.apply {
            val isPostpaid = item.paymentMethod == "BCCS"
            tvServiceName.text = item.infoService?.vi?.capitalize()
            if (!isPostpaid) {
                tvServicePrice.text = "${DecimalFormat("#,###,###,###").format(item.price)}đ"
                tvServicePrice.visible()
            } else {
                tvServicePrice.gone()
            }
            if (!isPostpaid) {
                tvServiceEffect.text = root.context.getString(com.vht.sdkcore.R.string.date_effective) + " ${
                    item.startDateTime?.times(1000L)?.toDate()
                }-${item.endDateTime?.times(1000L)?.toDate()}"
                tvServiceEffect.visible()
            } else {
                tvServiceEffect.gone()
            }
            tvServiceType.text = if (isPostpaid) "Trả sau" else "Trả trước"
            tvServiceStatus.text = item.serviceStatus?.let { getServiceStatus(it) }
            when(item.serviceStatus){
                0 -> {
                    tvServiceStatus.setTextColor(root.context.getColorCompat(R.color.color_4E4E4E))
                }
                1 -> {
                    tvServiceStatus.setTextColor(root.context.getColorCompat(R.color.color_active_cloud))
                }
                2 -> {
                    tvServiceStatus.setTextColor(root.context.getColorCompat(R.color.color_FF9C26))
                }
                else -> {
                    tvServiceStatus.setTextColor(root.context.getColorCompat(R.color.color_F8214B))
                }
            }
        }
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
    private fun Long.toDate(): String {
        val format = SimpleDateFormat("dd/MM/yyyy")
        return format.format(Date(this))
    }


}


