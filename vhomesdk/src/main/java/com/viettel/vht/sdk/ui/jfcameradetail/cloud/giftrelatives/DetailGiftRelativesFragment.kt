package com.viettel.vht.sdk.ui.jfcameradetail.cloud.giftrelatives

import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.viewModels
import com.vht.sdkcore.base.BaseFragment
import com.vht.sdkcore.pref.RxPreferences
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.dialog.DialogType
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentDetailGiftRelativesBinding
import com.viettel.vht.sdk.navigation.AppNavigation
import dagger.hilt.android.AndroidEntryPoint
import java.util.regex.Pattern
import javax.inject.Inject

@AndroidEntryPoint
class DetailGiftRelativesFragment :
    BaseFragment<FragmentDetailGiftRelativesBinding, DetailGiftRelativesViewModel>() {

    @Inject
    lateinit var mainNavigation: AppNavigation

    @Inject
    lateinit var rxPreferences: RxPreferences

    private val viewModel: DetailGiftRelativesViewModel by viewModels()

    override val layoutId: Int
        get() = R.layout.fragment_detail_gift_relatives

    override fun getVM(): DetailGiftRelativesViewModel = viewModel


    override fun initView() {
        super.initView()
        validateEditText()
    }

    override fun onPause() {
        super.onPause()
        viewModel.edtSerialCameraLiveData.value = binding.edtReceiveSerialCamera.getTextEditText()
        viewModel.edtPhoneLiveData.value = binding.edtReceivePhone.getTextEditText()
    }


    override fun bindingStateView() {
        super.bindingStateView()

        viewModel.edtPhoneLiveData.observe(viewLifecycleOwner){
            it?.let {
                binding.edtReceivePhone.setTextEditText(it)
            }
        }

        viewModel.edtSerialCameraLiveData.observe(viewLifecycleOwner){
            it?.let {
                binding.edtReceiveSerialCamera.setTextEditText(it)
            }
        }

        viewModel.validateSuccessLiveData.observe(viewLifecycleOwner) {
            if (it == true) {
                binding.btnNext.isEnabled = true
                binding.btnNext.isClickable = true
                binding.btnNext.setBackgroundResource(R.drawable.bg_corner_8_border_enable)

            } else {
                binding.btnNext.isEnabled = false
                binding.btnNext.isClickable = false
                binding.btnNext.setBackgroundResource(R.drawable.bg_corner_8_border_disable)
            }
        }

        viewModel.mActionState.observe(viewLifecycleOwner) {
            when (it) {
                is GiftRelativesActionState.InformationCloudSuccess -> {
                    if(it.isSuccess){
                        val bundle = Bundle().apply {
                            putString(Define.BUNDLE_KEY.PARAM_ID, it.data?.deviceId)
                            putString(Define.BUNDLE_KEY.PARAM_NAME, it.data?.deviceName)
                            putString(Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL, it.data?.serial)
                            putString(Define.BUNDLE_KEY.PARAM_DEVICE_TYPE, it.data?.cameraModel)
                            putString(Define.BUNDLE_KEY.PARAM_USE_ID_TO_GIFT_RELATIVES, it.data?.userId)
                            putBoolean(Define.BUNDLE_KEY.PARAM_OPEN_TO_GIFT_RELATIVES, true)
                        }
                        mainNavigation.openRegisterCloudToDetailGiftRelatives(bundle)
                    }else{
                        showDialogError(
                            title = getString(com.vht.sdkcore.R.string.error_add_camera),
                            type = DialogType.ERROR,
                            message = it.error?:"",
                            titleBtnConfirm = com.vht.sdkcore.R.string.ok,
                            onPositiveClick = {

                            }
                        )
                    }

                }
            }
        }

    }

    override fun setOnClick() {
        super.setOnClick()
        binding.toolbar.setOnLeftClickListener {
            mainNavigation.navigateUp()
        }
        binding.btnNext.setOnClickListener {
            viewModel.checkInformationCloudRelative(binding.edtReceivePhone.getTextEditText(),binding.edtReceiveSerialCamera.getTextEditText())
        }

    }

    private fun validateEditText() {
        binding.edtReceivePhone.setMaxLength(10)
        binding.edtReceivePhone.setInputTypeEdittext(InputType.TYPE_CLASS_PHONE)
        binding.edtReceivePhone.setDigitsEdittext("0123456789")
        binding.edtReceivePhone.handleEdittextOnTextChange {
            if (it.isEmpty()) {
                binding.edtReceivePhone.setTextError(getString(com.vht.sdkcore.R.string.notify_gift_relative_enter_phone_number))
                viewModel.validatePhoneLiveData.value = false
            } else {
                val pattern = Pattern.compile("(0[3|5|7|8|9])+([0-9]{8})\\b")
                val matcher = pattern.matcher(it)
                if (!matcher.matches()) {
                    binding.edtReceivePhone.setTextError(getString(com.vht.sdkcore.R.string.wrong_phone_number_format))
                    viewModel.validatePhoneLiveData.value = false
                } else {
                    if (it == rxPreferences.getUserPhoneNumber()) {
                        binding.edtReceivePhone.setTextError(getString(com.vht.sdkcore.R.string.wrong_phone_number_use))
                        viewModel.validatePhoneLiveData.value = false
                    } else {
                        viewModel.validatePhoneLiveData.value = true
                        binding.edtReceivePhone.goneError()
                    }

                }
            }


        }
        binding.edtReceivePhone.handleEdittextOnFocus {
            if (it.isEmpty()) {
                binding.edtReceivePhone.setTextError(getString(com.vht.sdkcore.R.string.notify_gift_relative_enter_phone_number))
                viewModel.validatePhoneLiveData.value = false

            }
        }

        binding.edtReceiveSerialCamera.setMaxLength(30)
        binding.edtReceiveSerialCamera.setImeOption()
        binding.edtReceiveSerialCamera.handleEdittextOnTextChange {
            if (it.isEmpty()) {
                binding.edtReceiveSerialCamera.setTextError(getString(com.vht.sdkcore.R.string.notify_enter_serial))
                viewModel.validateSerialLiveData.value = false
            } else {
                viewModel.validateSerialLiveData.value = true
                binding.edtReceiveSerialCamera.goneError()
            }


        }

        binding.edtReceiveSerialCamera.handleEdittextOnFocus {
            if (it.isEmpty()) {
                binding.edtReceiveSerialCamera.setTextError(getString(com.vht.sdkcore.R.string.notify_enter_serial))
                viewModel.validateSerialLiveData.value = false
            }
        }

    }

}