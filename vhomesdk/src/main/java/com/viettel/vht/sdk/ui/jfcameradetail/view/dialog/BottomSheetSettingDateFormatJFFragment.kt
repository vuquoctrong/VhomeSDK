package com.viettel.vht.sdk.ui.jfcameradetail.view.dialog

import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentBottomSheetSettingDateFormatJfBinding
import com.viettel.vht.sdk.ui.jfcameradetail.view.SettingCameraJFViewModel
import com.viettel.vht.sdk.utils.BaseBottomSheetFragmentNotModel

class BottomSheetSettingDateFormatJFFragment :
    BaseBottomSheetFragmentNotModel<FragmentBottomSheetSettingDateFormatJfBinding>() {

    private val viewModel: SettingCameraJFViewModel by activityViewModels()

    companion object {
        const val TAG = "BottomSheetSettingDateFormatJFFragment"
        fun newInstance(): BottomSheetSettingDateFormatJFFragment =
            BottomSheetSettingDateFormatJFFragment()
    }

    override val layoutId: Int
        get() = R.layout.fragment_bottom_sheet_setting_date_format_jf

    override fun initView() {
        binding.cbMMDDYYYY.isVisible = viewModel.dateFormatState.value is DateFormatJF.MMDDYYYY
        binding.cbDDMMYYYY.isVisible = viewModel.dateFormatState.value is DateFormatJF.DDMMYYYY
        binding.cbYYYYMMDD.isVisible = viewModel.dateFormatState.value is DateFormatJF.YYYYMMDD
    }

    override fun initControl() {
        binding.tvMMDDYYYY.setOnClickListener {
            if (!binding.cbMMDDYYYY.isVisible) {
                viewModel.setDateFormatState(DateFormatJF.MMDDYYYY())
            }
            dismiss()
        }
        binding.tvDDMMYYYY.setOnClickListener {
            if (!binding.cbDDMMYYYY.isVisible) {
                viewModel.setDateFormatState(DateFormatJF.DDMMYYYY())
            }
            dismiss()
        }
        binding.tvYYYYMMDD.setOnClickListener {
            if (!binding.cbYYYYMMDD.isVisible) {
                viewModel.setDateFormatState(DateFormatJF.YYYYMMDD())
            }
            dismiss()
        }
    }
}

sealed class DateFormatJF {
    val valueInApp: String
        get() = when (this) {
            is MMDDYYYY -> valueApp
            is DDMMYYYY -> valueApp
            is YYYYMMDD -> valueApp
        }
    val valueInJF: String
        get() = when (this) {
            is MMDDYYYY -> valueJF
            is DDMMYYYY -> valueJF
            is YYYYMMDD -> valueJF
        }

    class MMDDYYYY(val valueJF: String = "MMDDYY", val valueApp: String = "MM-DD-YYYY") :
        DateFormatJF()

    class DDMMYYYY(val valueJF: String = "DDMMYY", val valueApp: String = "DD-MM-YYYY") :
        DateFormatJF()

    class YYYYMMDD(val valueJF: String = "YYMMDD", val valueApp: String = "YYYY-MM-DD") :
        DateFormatJF()

    companion object {
        fun valueOf(valueJF: String?) = when (valueJF) {
            "MMDDYY" -> MMDDYYYY()
            "DDMMYY" -> DDMMYYYY()
            "YYMMDD" -> YYYYMMDD()
            else -> MMDDYYYY()
        }
    }
}
