package com.viettel.vht.sdk.ui.jftechaddcamera

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import com.vht.sdkcore.utils.dialog.BaseCustomDialog
import com.vht.sdkcore.utils.dialog.DialogType
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.DialogLoadingAddDeviceBinding
import com.viettel.vht.sdk.utils.showCustomNotificationDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoadingAddDeviceDialog(val messageFinish: String) :
    BaseCustomDialog<DialogLoadingAddDeviceBinding>(
        R.layout.dialog_loading_add_device
    ) {

    companion object {
        const val TAG = "LoadingAddDeviceDialog"
    }

    private lateinit var timer: CountDownTimer

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        timer = object : CountDownTimer(120000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes: Long = (millisUntilFinished / 1000) / 60
                val seconds: Int = ((millisUntilFinished / 1000) % 60).toInt()
                binding.tvTime.text =
                    "${String.format("%02d", minutes)}:${String.format("%02d", seconds)}"
            }

            override fun onFinish() {
                showCustomNotificationDialog(
                    title = messageFinish,
                    type = DialogType.ERROR
                ) {
                    dismiss()
                }
            }

        }
        timer.start()

    }

    override fun onStop() {
        super.onStop()
//        timer.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer.cancel()
    }
}