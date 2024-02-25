package com.viettel.vht.sdk.ui.jfcameradetail.view.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.DialogConfirmSaveMediaBinding

class ConfirmSaveMediaDialog  : DialogFragment(){

    companion object {
        const val TAG = "ConfirmSaveMediaDialog"
        const val MEDIA_TYPE = "MEDIA_TYPE"
        const val MEDIA_TYPE_VIDEO = 0
        const val MEDIA_TYPE_PHOTO = 1

        fun newInstance(mediaType: Int): ConfirmSaveMediaDialog {
            val confirmSaveMediaDialog = ConfirmSaveMediaDialog()
            val args = Bundle()
            args.putInt(MEDIA_TYPE, mediaType)
            confirmSaveMediaDialog.arguments = args
            return confirmSaveMediaDialog
        }
    }

    private lateinit var binding: DialogConfirmSaveMediaBinding
    private var onConfirmSaveMediaListener: OnConfirmSaveMediaListener? = null
    private var mMediaType = MEDIA_TYPE_VIDEO

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_confirm_save_media, container, false)
        mMediaType = arguments?.getInt(MEDIA_TYPE) ?: MEDIA_TYPE_VIDEO
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnCancel.setOnClickListener{
            onConfirmSaveMediaListener?.onCancel()
            dismiss()
        }

        binding.btnSave.setOnClickListener{
            onConfirmSaveMediaListener?.onSaveToLibrary()
            dismiss()
        }

        val message = if(mMediaType == MEDIA_TYPE_VIDEO) getString(com.vht.sdkcore.R.string.dialog_confirm_save_video)
                        else getString(com.vht.sdkcore.R.string.dialog_confirm_save_photo)
        binding.tvMessage.text = message
    }

    fun setTitle(title:String){
        binding.tvMessage.text = title
    }

    interface OnConfirmSaveMediaListener{
        fun onSaveToLibrary()
        fun onCancel()
    }

    fun setOnClickListener(onConfirmSaveMediaListener: OnConfirmSaveMediaListener) {
        this.onConfirmSaveMediaListener = onConfirmSaveMediaListener
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setCancelable(false)
    }
}