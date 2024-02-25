package com.viettel.vht.sdk.ui.jfcameradetail.call

import android.os.Bundle
import android.view.View
import com.vht.sdkcore.utils.dialog.BaseCustomDialog
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.DialogIncomingCallBinding

class IncomingCallDialog(val accept: () -> Unit) :
    BaseCustomDialog<DialogIncomingCallBinding>(R.layout.dialog_incoming_call) {
    companion object {
        const val TAG = "IncomingCallDialog"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnConversation.setOnClickListener {
            accept()
            dismiss()
        }
        binding.btnRefuse.setOnClickListener {
            dismiss()
        }
    }
}