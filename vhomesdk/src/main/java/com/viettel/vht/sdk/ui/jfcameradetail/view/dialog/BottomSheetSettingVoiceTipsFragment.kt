package com.viettel.vht.sdk.ui.jfcameradetail.view.dialog

import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.lib.sdk.bean.VoiceTipBean
import com.vht.sdkcore.adapter.BaseAdapter
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentBottomSheetSettingVoiceTipsBinding
import com.viettel.vht.sdk.databinding.ItemSettingVoiceTipsBinding
import com.viettel.vht.sdk.ui.jfcameradetail.SettingAlarmFeatureCameraJFViewModel
import com.viettel.vht.sdk.utils.BaseBottomSheetTimePickerFragment

class BottomSheetSettingVoiceTipsFragment :
    BaseBottomSheetTimePickerFragment<FragmentBottomSheetSettingVoiceTipsBinding>(),
    VoiceTipsAdapter.OnVoiceTipsListener {

    private val viewModel: SettingAlarmFeatureCameraJFViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    private var voiceTipsAdapter: VoiceTipsAdapter? = null

    override val layoutId: Int = R.layout.fragment_bottom_sheet_setting_voice_tips

    override fun initView() {
        voiceTipsAdapter = VoiceTipsAdapter(this, viewModel.voiceTip.value)
        binding.rvListVoice.adapter = voiceTipsAdapter
        voiceTipsAdapter?.submitList(viewModel.voiceTipList)
    }

    override fun initControl() {
        super.initControl()
        binding.layoutRoot.setOnClickListener {
            dismiss()
        }
    }

    override fun listTimePicker(): List<View> = listOf(binding.rvListVoice)

    override fun onVoiceTipsClick(item: VoiceTipBean) {
        if (item.voiceEnum != viewModel.voiceTip.value?.voiceEnum) {
            viewModel.setVoiceTipAlarm(item)
        }
        dismiss()
    }

    companion object {
        const val TAG = "BottomSheetSettingVoiceTipsFragment"
        fun newInstance() = BottomSheetSettingVoiceTipsFragment()
    }
}

class VoiceTipsAdapter(
    private val onVoiceTipsListener: OnVoiceTipsListener,
    private val voiceSelected: VoiceTipBean? = null
) : BaseAdapter<VoiceTipBean, ItemSettingVoiceTipsBinding>() {
    override val itemLayoutRes = R.layout.item_setting_voice_tips

    override fun bindMultiTime(holder: BaseViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.apply {
            tvTitle.text = item.voiceText
            checkBox.isVisible = item.voiceEnum == voiceSelected?.voiceEnum
            root.setOnClickListener {
                onVoiceTipsListener.onVoiceTipsClick(item)
            }
        }
    }

    override fun bindSingleTime(holder: BaseViewHolder, position: Int) {}

    interface OnVoiceTipsListener {
        fun onVoiceTipsClick(item: VoiceTipBean)
    }
}
