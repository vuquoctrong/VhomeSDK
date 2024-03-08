package com.viettel.vht.sdk.ui.jfcameradetail.cloud.adapter

import android.content.Context
import com.vht.sdkcore.adapter.BaseAdapter
import com.vht.sdkcore.pref.RxPreferences
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.getColorCompat
import com.vht.sdkcore.utils.visible
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.ItemCloudStorageBinding
import com.viettel.vht.sdk.model.camera_cloud.CloudStorageRegistered
import com.viettel.vht.sdk.utils.gone
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


class CloudRegisteredListAdapter constructor(val rxPreferences: RxPreferences, val onOffAutoChargingListener: (Boolean) -> Unit) :
    BaseAdapter<CloudStorageRegistered, ItemCloudStorageBinding>() {
    var isAutomaticRegisterCloud = false

    // thể hiện là gói đã hết hạn chưa và cần gia hạn
    var isExtendCloud = false
    override val itemLayoutRes: Int
        get() = R.layout.item_cloud_storage

    override fun bindSingleTime(holder: BaseViewHolder, position: Int) {
        val item = getItem(position)

        holder.itemBinding.apply {
            val isPostpaid = item.isPostpaid()
            tvServiceName.text = item.infoService?.vi?.capitalize()
            swTurnOffExtend.setOnClickListener {
                onOffAutoChargingListener.invoke(swTurnOffExtend.isChecked)
            }
            if (!isPostpaid) {
                tvServicePrice.text = "${DecimalFormat("#,###,###,###").format(item.price)}đ"
                tvServicePrice.visible()
                tvMethodPay.visible()
                tvContentMethodPay.visible()
                tvContentMethodPay.text = getNamePaymentMethods(item.moneySource?:"",root.context)
                if (currentList.size > 1) {
                    // thể hiện là nhiều hơn 1 gói cước, thì khi đó sẽ k gia hạn tự động dc
                    clAutoRenew.gone()
                } else {
                    clAutoRenew.visible()
                }

                swTurnOffExtend.isChecked = isAutomaticRegisterCloud
                if (isExtendCloud) {
                    // đây là gói đã hết hạn thì k cho bật tắt gia hạn => chỉ hiển thị cho mua thêm
                    swTurnOffExtend.isEnabled = false
                    swTurnOffExtend.isClickable = false
                    swTurnOffExtend.alpha = 0.6f
                }

            } else {
                tvServicePrice.gone()
                clAutoRenew.gone()
                tvMethodPay.visible()
                tvContentMethodPay.visible()
                tvContentMethodPay.text = root.context?.getString(com.vht.sdkcore.R.string.string_payment_counter)
            }
            if (!isPostpaid) {
                tvServiceEffect.text = "${item.startDateTime?.times(1000L)?.toDate()}-${item.endDateTime?.times(1000L)?.toDate()}"
                tvServiceEffect.visible()
                tvTitleEffect.visible()
            } else {
                tvServiceEffect.visible()
                tvTitleEffect.visible()
                if(item.serviceStatus == 0){
                    tvServiceEffect.text = "${
                        item.startDateTime?.times(1000L)?.toDate()
                    }-${item.endDateTime?.times(1000L)?.toDate()}"
                }else{
                    tvServiceEffect.text = "${
                        item.startDateTime?.times(1000L)?.toDate()
                    } - --/--/---"
                }
            }
            tvServiceType.text = if (isPostpaid) "Trả sau" else "Trả trước"
            tvServiceStatus.text = item.serviceStatus?.let { getServiceStatus(it) }
            if (item.serviceStatus == 1) {
                tvServiceStatus.setTextColor(root.context.getColorCompat(R.color.color_active_cloud))
            } else {
                tvServiceStatus.setTextColor(root.context.getColorCompat(R.color.color_F8214B))
            }
            if (item.isGiftReceive(rxPreferences.getUserId())){
                imvGift.visible()
                imvGift.setImageResource(R.drawable.ic_gift_receive)
                clAutoRenew.gone()
            }else{
                if(item.isGiftRelatives()){
                    imvGift.visible()
                    clAutoRenew.gone()
                    imvGift.setImageResource(R.drawable.ic_gift_give)
                }
            }
        }
    }

    override fun bindMultiTime(holder: BaseViewHolder, position: Int) {
        val item = getItem(position)

        holder.itemBinding.apply {
            val isPostpaid = item.isPostpaid()
            if (!isPostpaid) {
                swTurnOffExtend.isChecked = isAutomaticRegisterCloud
            }
        }
    }

    private fun getServiceStatus(status: Int): String {
        return when (status) {
            0 -> "Hết hạn"
            1 -> "Đang sử dụng"
            2 -> "Chưa kích hoạt"
            else -> ""
        }
    }

    private fun Long.toDate(): String {
        val format = SimpleDateFormat("dd/MM/yyyy")
        return format.format(Date(this))
    }

    private fun getNamePaymentMethods(type:String,context: Context):String{
        return  when(type){
            Define.PaymentMethodsCloud.TKG.type ->{
                return context.getString(com.vht.sdkcore.R.string.string_payment_tkg)
            }
            Define.PaymentMethodsCloud.VTT.type ->{
                return context.getString(com.vht.sdkcore.R.string.string_payment_vtt)
            }

            Define.PaymentMethodsCloud.NAPAS.type ->{
                return context.getString(com.vht.sdkcore.R.string.string_payment_napas)
            }
            Define.PaymentMethodsCloud.CYBER.type ->{
                return context.getString(com.vht.sdkcore.R.string.string_payment_cyber)
            }

            Define.PaymentMethodsCloud.OTHER.type ->{
                return context.getString(com.vht.sdkcore.R.string.string_payment_other)
            }
            else ->{
                context.getString(com.vht.sdkcore.R.string.string_payment_vtt)
            }
        }

    }


}


