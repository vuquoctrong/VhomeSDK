package com.viettel.vht.sdk.utils.custom

import androidx.fragment.app.FragmentManager
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentBottomSheetQualityBinding
import com.viettel.vht.sdk.utils.BaseBottomSheetFragmentNotModel
import com.viettel.vht.sdk.utils.gone
import com.viettel.vht.sdk.utils.visible

class BottomSheetQualityFragment :
    BaseBottomSheetFragmentNotModel<FragmentBottomSheetQualityBinding>() {
    var onSlected: ((Int) -> Unit)? = null
    var level = 0
    var goneFHD = false
    var goneSD = false
    var isChangeHDtoFHD = false

    companion object {
        fun newInstance(
            goneFHD: Boolean = false,
            goneSD: Boolean = false,
            childFragmentManager: FragmentManager,
            level: Int,
            isChangeHDtoFHD: Boolean = false,
            onSlected: ((Int) -> Unit)?
        ) {
            BottomSheetQualityFragment().apply {
                this.onSlected = onSlected
                this.level = level
                this.goneFHD = goneFHD
                this.goneSD = goneSD
                this.isChangeHDtoFHD = isChangeHDtoFHD
            }.show(
                childFragmentManager,
                "BottomSheetQualityFragment"
            )
        }
    }

    override val layoutId: Int
        get() = R.layout.fragment_bottom_sheet_quality

    override fun initView() {
        when (level) {
            0 -> binding.tvSD.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_checked,
                0
            )
            1 -> binding.tvHD.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_checked,
                0
            )
            2 -> binding.tvFHD.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_checked,
                0
            )
            3 -> binding.tv2k.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_checked,
                0
            )
            else -> binding.tvFHD.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_checked,
                0
            )
        }
        if (goneSD) {
            binding.containerSD.gone()
        } else {
            binding.tvHD.text = getString(com.vht.sdkcore.R.string.quality_hd_new)
            binding.imvHD.setImageResource(R.drawable.ic_quality_hd)
            binding.containerSD.visible()
        }
        if (goneFHD) {
            binding.containerFHD.gone()
        } else {
            binding.containerFHD.visible()
        }
        if (isChangeHDtoFHD) {
            changeHDtoFHDCameraJF()
        }
    }

    override fun initControl() {
        binding.container2k.setOnClickListener {
            onSlected?.invoke(3)
            dismiss()
        }
        binding.containerFHD.setOnClickListener {
            onSlected?.invoke(2)
            dismiss()
        }
        binding.containerHD.setOnClickListener {
            onSlected?.invoke(1)
            dismiss()
        }
        binding.containerSD.setOnClickListener {
            onSlected?.invoke(0)
            dismiss()
        }
    }

    private fun changeHDtoFHDCameraJF() {
        binding.tvHD.text = getString(com.vht.sdkcore.R.string.quality_fhd_new)
        binding.imvHD.setImageResource(R.drawable.ic_quality_fhd)
    }
}