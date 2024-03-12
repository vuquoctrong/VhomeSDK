package com.viettel.vht.sdk.ui.quickhistory

import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentBottomSheetChangePlaybackRateJfBinding
import com.viettel.vht.sdk.utils.BaseBottomSheetFragmentNotModel
import com.viettel.vht.sdk.utils.gone
import com.viettel.vht.sdk.utils.visible


class BottomSheetChangePlaybackRateJF :
    BaseBottomSheetFragmentNotModel<FragmentBottomSheetChangePlaybackRateJfBinding>() {

    private var onSelectPlaySpeed: OnSelectPlaySpeed? = null
    private var playSpeed: Int? = null

    override val layoutId: Int = R.layout.fragment_bottom_sheet_change_playback_rate_jf

    override fun initView() {
        binding.checkboxRate025x.gone()
        binding.checkboxRate05x.gone()
        binding.checkboxRate1x.gone()
        binding.checkboxRate2x.gone()
        binding.checkboxRate4x.gone()
        when (playSpeed) {
            -2 -> {
                binding.checkboxRate025x.visible()
            }

            -1 -> {
                binding.checkboxRate05x.visible()
            }

            0 -> {
                binding.checkboxRate1x.visible()
            }

            1 -> {
                binding.checkboxRate2x.visible()
            }

            2 -> {
                binding.checkboxRate4x.visible()
            }
        }
    }

    override fun initControl() {
        binding.btn4X.setOnClickListener {
            onSelectPlaySpeed?.onSelectPlaySpeed(2)
            dismiss()
        }
        binding.btn2X.setOnClickListener {
            onSelectPlaySpeed?.onSelectPlaySpeed(1)
            dismiss()
        }
        binding.btn1X.setOnClickListener {
            onSelectPlaySpeed?.onSelectPlaySpeed(0)
            dismiss()
        }
        binding.btn05X.setOnClickListener {
            onSelectPlaySpeed?.onSelectPlaySpeed(-1)
            dismiss()
        }
        binding.btn025X.setOnClickListener {
            onSelectPlaySpeed?.onSelectPlaySpeed(-2)
            dismiss()
        }
    }

    interface OnSelectPlaySpeed {
        fun onSelectPlaySpeed(playSpeed: Int)
    }

    companion object {
        const val TAG = "BottomSheetChangePlaybackRateJF"
        fun newInstance(onSelectPlaySpeed: OnSelectPlaySpeed, playSpeed: Int) =
            BottomSheetChangePlaybackRateJF().apply {
                this.onSelectPlaySpeed = onSelectPlaySpeed
                this.playSpeed = playSpeed
            }
    }
}
