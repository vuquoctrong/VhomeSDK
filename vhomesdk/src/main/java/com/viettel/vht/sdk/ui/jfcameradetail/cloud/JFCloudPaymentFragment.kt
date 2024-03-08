package com.viettel.vht.sdk.ui.jfcameradetail.cloud

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.vht.sdkcore.base.BaseFragment
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentJfCloudPaymentBinding
import com.viettel.vht.sdk.navigation.AppNavigation
import com.viettel.vht.sdk.utils.gone
import com.viettel.vht.sdk.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class JFCloudPaymentFragment : BaseFragment<FragmentJfCloudPaymentBinding, JFCloudStorageViewModel>() {

    @Inject
    lateinit var appNavigation: AppNavigation

    private val viewModel: JFCloudStorageViewModel by activityViewModels()

    override val layoutId: Int
        get() = R.layout.fragment_jf_cloud_payment

    override fun getVM(): JFCloudStorageViewModel = viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun setOnClick() {
        super.setOnClick()
        binding.toolbar.setOnLeftClickListener {
            appNavigation.navigateUp()
        }
        binding.rlViettelPay.setOnClickListener {
            binding.cbViettelPay.isChecked = true
            binding.cbAtm.isChecked = false
            binding.cbInternationalCard.isChecked = false
        }
        binding.rlAtm.setOnClickListener {
            binding.cbViettelPay.isChecked = false
            binding.cbAtm.isChecked = true
            binding.cbInternationalCard.isChecked = false
        }
        binding.rlInternationalCard.setOnClickListener {
            binding.cbViettelPay.isChecked = false
            binding.cbAtm.isChecked = false
            binding.cbInternationalCard.isChecked = true
        }
        binding.btnCancel.setOnClickListener {
            appNavigation.navigateUp()
        }
        binding.rlHeaderPayInfo.setOnClickListener {
            val isExpand = !binding.llBodyPayInfo.isVisible
            handleCollapseExpandInfoPayment(isExpand)
        }
    }

    private fun handleCollapseExpandInfoPayment(isExpand: Boolean) {
        if (isExpand) {
            binding.llBodyPayInfo.visible()
            binding.divider.visible()
            binding.imgExpand.animate().rotationX(180F).setDuration(500).start()
        } else {
            binding.llBodyPayInfo.gone()
            binding.divider.gone()
            binding.imgExpand.animate().rotationX(0F).setDuration(500).start()
        }
    }


}