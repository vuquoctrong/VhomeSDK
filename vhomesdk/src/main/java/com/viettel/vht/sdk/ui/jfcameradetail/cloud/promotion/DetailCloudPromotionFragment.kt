package com.viettel.vht.sdk.ui.jfcameradetail.cloud.promotion

import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.vht.sdkcore.base.BaseFragment
import com.vht.sdkcore.network.Status
import com.vht.sdkcore.utils.dialog.DialogType
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentDetailPromotionBinding
import com.viettel.vht.sdk.navigation.AppNavigation
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DetailCloudPromotionFragment :
    BaseFragment<FragmentDetailPromotionBinding, DetailPromotionViewModel>() {

    @Inject
    lateinit var appNavigation: AppNavigation

    private val viewModel: DetailPromotionViewModel by viewModels()


    override val layoutId: Int
        get() = R.layout.fragment_detail_promotion

    override fun getVM(): DetailPromotionViewModel = viewModel





    override fun initView() {
        super.initView()
        viewModel.getListPricingPaymentCloud()
    }


    override fun bindingStateView() {
        super.bindingStateView()

        viewModel.dataListPricingCloud.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> showHideLoading(true)
                Status.ERROR -> showHideLoading(false)
                Status.SUCCESS -> {
                    showHideLoading(false)
                    it.data?.let { responseData ->
                        if (it.data?.code == 200) {
                            // thực hiện show các dòng camera.
                            val cloudFree = it.data?.data?.promotions
                            cloudFree?.let {
                                binding.tvNameCloudPromotion.text = "Miễn phí gói lưu trữ Cloud ${it.cloudPeriod} ngày"
                                binding.tvCloudDate.text = "${it.cloudTime?.div(30)} tháng"
                                binding.tvExpiryTime.text = it.ExpiryTime
                                val priceMonth = it.price?.toInt()?.div((it.cloudTime?.div(30)?:1))
                                binding.tvTitleNote2.text = getString(com.vht.sdkcore.R.string.string_content_two_promotion_cloud,it.cloudPeriod,it.cloudTime?.div(30),priceMonth.toString().format()+"đ/tháng")
                            }

                        } else {
                            showDialogError(
                                title = "Lỗi xảy ra",
                                type = DialogType.ERROR,
                                message = it.data?.message ?: "",
                                titleBtnConfirm = com.vht.sdkcore.R.string.ok,
                                onPositiveClick = {

                                }
                            )
                        }
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

        binding.btnAccepted.setOnClickListener {
            appNavigation.openRegisterPromotionFree()
        }

    }
    fun String.format(): String {
        val num = this.toDouble()
        val formatted = String.format("%,.0f", num)
        return formatted.replace(",", ".")
    }

}