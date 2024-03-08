package com.viettel.vht.sdk.ui.jfcameradetail.cloud.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.vht.sdkcore.utils.getColorCompat
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.ItemHistoryByCloudStorageBinding
import com.viettel.vht.sdk.model.camera_cloud.CloudStorageRegistered
import com.viettel.vht.sdk.utils.gone
import com.viettel.vht.sdk.utils.visible
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class HistoryBuyCloudAdapter(
    val useId: String? = "",
    var clickItem: ((data: CloudStorageRegistered) -> Unit)? = null
) : PagingDataAdapter<CloudStorageRegistered, HistoryBuyCloudAdapter.MyViewHolder>(
    diffCallback
) {

    class MyViewHolder(private val binding: ItemHistoryByCloudStorageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(
            useId: String? = "",
            item: CloudStorageRegistered,
            clickItem: ((data: CloudStorageRegistered) -> Unit)? = null
        ) {
            binding.apply {
                root.setOnClickListener {
                    clickItem?.invoke(item)
                }
                val isPostpaid = item.isPostpaid()
                tvServiceName.text = item.infoService?.vi?.capitalize()
                if (!isPostpaid) {
                    tvServicePrice.text = "${DecimalFormat("#,###,###,###").format(item.price)}đ"
                    tvServicePrice.visible()
                } else {
                    tvServicePrice.gone()
                }
                if (!isPostpaid) {
                    tvServiceEffect.text = "${
                        item.startDateTime?.times(1000L)?.toDate()
                    }-${item.endDateTime?.times(1000L)?.toDate()}"
                    tvServiceEffect.visible()
                    tvTitleEffect.visible()
                } else {
                    tvServiceEffect.visible()
                    tvTitleEffect.visible()
                    if (item.serviceStatus == 0) {
                        tvServiceEffect.text = "${
                            item.startDateTime?.times(1000L)?.toDate()
                        }-${item.endDateTime?.times(1000L)?.toDate()}"
                    } else {
                        tvServiceEffect.text = "${
                            item.startDateTime?.times(1000L)?.toDate()
                        } - --/--/---"
                    }

                }
                tvServiceType.text = if (isPostpaid) "Trả sau" else "Trả trước"
                tvServiceStatus.text = item.serviceStatus?.let { getServiceStatus(it) }
                when (item.serviceStatus) {
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
                if (item.isGiftReceive(useId)) {
                    imvGift.visible()
                    imvGift.setImageResource(R.drawable.ic_gift_receive)

                } else {
                    if (item.isGiftRelatives()) {
                        imvGift.visible()
                        imvGift.setImageResource(R.drawable.ic_gift_give)
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

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<CloudStorageRegistered>() {
            override fun areItemsTheSame(
                oldItem: CloudStorageRegistered,
                newItem: CloudStorageRegistered
            ): Boolean {
                return oldItem.orderId == newItem.orderId
            }

            override fun areContentsTheSame(
                oldItem: CloudStorageRegistered,
                newItem: CloudStorageRegistered
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        getItem(position)?.let { item ->
            holder.bind(useId, item, clickItem)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ItemHistoryByCloudStorageBinding>(
            inflater,
            R.layout.item_history_by_cloud_storage,
            parent,
            false
        )
        return MyViewHolder(binding)
    }


}