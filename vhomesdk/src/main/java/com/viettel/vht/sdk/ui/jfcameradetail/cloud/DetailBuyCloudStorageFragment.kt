package com.viettel.vht.sdk.ui.jfcameradetail.cloud

import androidx.fragment.app.viewModels
import com.vht.sdkcore.base.BaseFragment
import com.vht.sdkcore.network.Status
import com.vht.sdkcore.pref.RxPreferences
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.getColorCompat
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentDetailHistoryBuyCloudStorageBinding
import com.viettel.vht.sdk.model.camera_cloud.CloudStorageRegistered
import com.viettel.vht.sdk.navigation.AppNavigation
import com.viettel.vht.sdk.utils.gone
import com.viettel.vht.sdk.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class DetailBuyCloudStorageFragment :
    BaseFragment<FragmentDetailHistoryBuyCloudStorageBinding, HistoryBuyCloudStorageViewModel>() {

    @Inject
    lateinit var appNavigation: AppNavigation

    @Inject
    lateinit var rxPreferences: RxPreferences

    private val viewModel: HistoryBuyCloudStorageViewModel by viewModels()

    private var dataHistory: CloudStorageRegistered? = null

    override val layoutId: Int
        get() = R.layout.fragment_detail_history_buy_cloud_storage

    override fun getVM(): HistoryBuyCloudStorageViewModel = viewModel





    override fun initView() {
        super.initView()

        arguments?.let {
            if (it.containsKey(Define.BUNDLE_KEY.PARAM_ID)) {
                viewModel.deviceId = it.getString(Define.BUNDLE_KEY.PARAM_ID) ?: ""

            }

            if (it.containsKey(Define.BUNDLE_KEY.PARAM_DATA_DETAIL_HISTORY_CLOUD)) {
                dataHistory = it.getParcelable<CloudStorageRegistered>(Define.BUNDLE_KEY.PARAM_DATA_DETAIL_HISTORY_CLOUD)
            }


        }
        binding.tvDeviceName.text = dataHistory?.cameraSN

        setDataUI()
    }


    override fun bindingStateView() {
        super.bindingStateView()
        viewModel.informationAccount.observe(viewLifecycleOwner){
            when (it.status) {
                Status.LOADING -> {
                    showHideLoading(true)
                }
                Status.ERROR -> {
                    showHideLoading(false)

                }
                Status.SUCCESS -> {
                    showHideLoading(false)
                   it.data?.phone?.let {
                       binding.tvContentGiftStatus.text = it?:""
                   }

                }
            }
        }


    }

    override fun setOnClick() {
        super.setOnClick()
        binding.toolbar.setOnLeftClickListener {
            appNavigation.navigateUp()
        }

    }
    
    private fun setDataUI(){
       dataHistory?.let { data ->
           val isPostpaid = data.isPostpaid()
           binding.tvContentNameCloud.text = data.infoService?.vi?.capitalize()
           binding.tvContentCode.text = data.transactionId
           binding.tvContentClose.text = data.infoService?.noteVi
           if (!isPostpaid) {
               binding.tvContentMoneyTotal.text = "${DecimalFormat("#,###,###,###").format(data.price)}đ"
               binding.clPriceTotal.visible()
               binding.tvContentMoneySource.text = getNamePaymentMethods(data.moneySource?:"")
               binding.clTransactionType.visible()
               binding.clCloseCloud.visible()
           } else {
               binding.clPriceTotal.gone()
               binding.tvContentMoneySource.text = getString(com.vht.sdkcore.R.string.string_payment_counter)
               binding.clTransactionType.gone()
               binding.clCloseCloud.gone()
           }
           if(data.orderId?.contains("WLAUTO") == true){
               binding.tvContentTransactionType.text =getText(com.vht.sdkcore.R.string.string_payment_type_auto)
           }else{
               binding.tvContentTransactionType.text =getText(com.vht.sdkcore.R.string.string_payment_type_manual)
           }
           if (!isPostpaid) {
               binding.tvContentDate.text = "${
                   data.startDateTime?.times(1000L)?.toDate("dd/MM/yyyy")
               }-${data.endDateTime?.times(1000L)?.toDate("dd/MM/yyyy")}"
               binding.clDate.visible()
           } else {
               binding.clDate.visible()
               if(data.serviceStatus == 0){
                   binding.tvContentDate.text = "${
                       data.startDateTime?.times(1000L)?.toDate("dd/MM/yyyy")
                   }-${ data.endDateTime?.times(1000L)?.toDate("dd/MM/yyyy")}"
               }else{
                   binding.tvContentDate.text = "${
                       data.startDateTime?.times(1000L)?.toDate("dd/MM/yyyy")
                   } - --/--/---"
               }

           }

           if(data.invitationCode.isNullOrBlank()){
               binding.clReferCode.gone()
           }else{
               binding.clReferCode.visible()
               binding.tvContentCodeIntroduce.text = data.invitationCode?:""
           }
           binding.tvContentDateRegister.text = "${
               data.purchaseDateTime?.times(1000L)?.toDate("dd/MM/yyyy HH:mm:ss")
           }"
           binding.tvContentTypeCloud.text = if (isPostpaid) "Trả sau" else "Trả trước"
           binding.tvContentStatus.text = data.serviceStatus?.let { getServiceStatus(it) }

           when(data.serviceStatus){
               0 -> {
                   binding.tvContentStatus.setTextColor(requireContext().getColorCompat(R.color.color_4E4E4E))
               }
               1 -> {
                   binding.tvContentStatus.setTextColor(requireContext().getColorCompat(R.color.color_active_cloud))
               }
               2 -> {
                   binding.tvContentStatus.setTextColor(requireContext().getColorCompat(R.color.color_FF9C26))
               }
               else -> {
                   binding.tvContentStatus.setTextColor(requireContext().getColorCompat(R.color.color_F8214B))
               }
           }
           if(data.isGiftRelatives()){
               binding.clGiftStatus.visible()
           }else{
               binding.clGiftStatus.gone()
           }
            // tặng bởi
           if (data.isGiftReceive(rxPreferences.getUserId())){
               binding.clGiftStatus.visible()
               binding.tvTitleGiftStatus.text= getString(com.vht.sdkcore.R.string.string_gift_status)
               viewModel.getInformationAccount(data.userId?:"")
           }else{
               //tặng đến
               if(data.isGiftRelatives()){
                   binding.clGiftStatus.visible()
                   binding.tvTitleGiftStatus.text= getString(com.vht.sdkcore.R.string.string_gift_give_status)
                   viewModel.getInformationAccount(data.userUse?:"")
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

    private fun Long.toDate(format:String? = "dd/MM/yyyy HH:mm:ss"): String {
        val format = SimpleDateFormat(format)
        return format.format(Date(this))
    }

    private fun getNamePaymentMethods(type:String):String{
        return  when(type){
            Define.PaymentMethodsCloud.TKG.type ->{
                return getString(com.vht.sdkcore.R.string.string_payment_tkg)
            }
            Define.PaymentMethodsCloud.VTT.type ->{
                return getString(com.vht.sdkcore.R.string.string_payment_vtt)
            }

            Define.PaymentMethodsCloud.NAPAS.type ->{
                return getString(com.vht.sdkcore.R.string.string_payment_napas)
            }
            Define.PaymentMethodsCloud.CYBER.type ->{
                return getString(com.vht.sdkcore.R.string.string_payment_cyber)
            }

            Define.PaymentMethodsCloud.OTHER.type ->{
                return getString(com.vht.sdkcore.R.string.string_payment_other)
            }
            else ->{
                getString(com.vht.sdkcore.R.string.string_payment_vtt)
            }
        }

    }
}