package com.viettel.vht.sdk.ui.jfcameradetail.view.dialog

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.vht.sdkcore.utils.eventbus.RxEvent
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentBottomSheetPresetBinding
import com.viettel.vht.sdk.ui.jfcameradetail.view.ItemPreset
import com.viettel.vht.sdk.ui.jfcameradetail.view.JFCameraDetailModel
import com.viettel.vht.sdk.utils.BaseBottomSheetFragmentNotModel
import com.viettel.vht.sdk.utils.Config

class BottomSheetPresetFragment :
    BaseBottomSheetFragmentNotModel<FragmentBottomSheetPresetBinding>() {

    private val viewModel: JFCameraDetailModel by viewModels({ requireParentFragment() })

    override val layoutId: Int
        get() = R.layout.fragment_bottom_sheet_preset

    override fun initView() {

    }

    override fun initControl() {
        binding.itemUpdate.setOnClickListener {
            val item = arguments?.getSerializable("PRESET") as ItemPreset
            viewModel.openOptionLiveEvent.value =
                RxEvent(Config.EventKey.EVENT_UPDATE_PRESET, item)
            dismiss()
        }
        binding.itemEdit.setOnClickListener {
            val item = arguments?.getSerializable("PRESET") as ItemPreset
            viewModel.openOptionLiveEvent.value =
                RxEvent(Config.EventKey.EVENT_EDIT_PRESET, item)
            dismiss()
        }
        binding.itemDelete.setOnClickListener {
            val item = arguments?.getSerializable("PRESET") as ItemPreset
            viewModel.openOptionLiveEvent.value =
                RxEvent(Config.EventKey.EVENT_DELETE_PRESET, item)
            dismiss()
        }
        binding.itemMultiple.setOnClickListener {
            val item = arguments?.getSerializable("PRESET") as ItemPreset
            viewModel.openOptionLiveEvent.value =
                RxEvent(Config.EventKey.EVENT_MULTIPLE_PRESET, item)
            dismiss()
        }
    }

    companion object {
        fun newInstance(item: ItemPreset) = BottomSheetPresetFragment().apply {
            arguments = Bundle().apply {
                putSerializable("PRESET", item)
            }

        }
    }
}