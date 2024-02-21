package com.viettel.vht.sdk.ui.jftechaddcamera

import android.os.Bundle
import android.view.View
import com.vht.sdkcore.utils.dialog.BaseCustomDialog
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.DialogAddDeviceErrorBinding


//@AndroidEntryPoint
class AddDeviceErrorDialog(val message: String, val okAction: () -> Unit) :
    BaseCustomDialog<DialogAddDeviceErrorBinding>(R.layout.dialog_add_device_error) {

    companion object {
        const val TAG = "AddDeviceErrorDialog"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvMessage.text = message
        binding.btnOk.setOnClickListener {
            okAction()
            dismiss()
        }
    }


}