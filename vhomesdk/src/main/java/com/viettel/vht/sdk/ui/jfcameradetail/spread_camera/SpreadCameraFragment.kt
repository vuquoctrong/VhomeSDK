package com.viettel.vht.sdk.ui.jfcameradetail.spread_camera

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.vht.sdkcore.base.BaseFragment
import com.vht.sdkcore.network.Status
import com.vht.sdkcore.pref.RxPreferences
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.dialog.CommonAlertDialogNotification
import com.vht.sdkcore.utils.dialog.DialogType
import com.vht.sdkcore.utils.isNetworkAvailable
import com.vht.sdkcore.utils.onTextChange
import com.vht.sdkcore.utils.showCustomToast
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentSpreadCameraBinding
import com.viettel.vht.sdk.navigation.AppNavigation
import com.viettel.vht.sdk.utils.gone
import com.viettel.vht.sdk.utils.showCustomNotificationDialog
import com.viettel.vht.sdk.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import java.util.regex.Pattern
import javax.inject.Inject

@AndroidEntryPoint
class SpreadCameraFragment :
    BaseFragment<FragmentSpreadCameraBinding, SpreadCameraViewModel>() {

    @Inject
    lateinit var appNavigation: AppNavigation

    @Inject
    lateinit var rxPreferences: RxPreferences

    override val layoutId: Int
        get() = R.layout.fragment_spread_camera

    private val viewModel: SpreadCameraViewModel by viewModels()
    private var deviceId: String = ""

    override fun getVM(): SpreadCameraViewModel = viewModel


    override fun initView() {
        super.initView()
        arguments?.let {
            if (it.containsKey(Define.BUNDLE_KEY.PARAM_ID)) {
                deviceId = it.getString(Define.BUNDLE_KEY.PARAM_ID) ?: ""
            }
        }

    }

    override fun bindingStateView() {
        super.bindingStateView()
        viewModel.dataResponseSetSpreadCamera.observe(viewLifecycleOwner) {

            when (it.status) {
                Status.LOADING -> showHideLoading(true)
                Status.ERROR -> {
                    showHideLoading(false)

                }
                Status.SUCCESS -> {
                    it.data?.let {  response ->
                        if(response.code == 200){
                            if(response.data?.contains("Cloud") == true){
                                CommonAlertDialogNotification.getInstanceCommonAlertdialog(requireContext())
                                    .showDialog()
                                    .setDialogTitleWithString(getString(com.vht.sdkcore.R.string.string_title_nubmer_spread_dialog))
                                    .showCenterImage(DialogType.NOTIFICATION)
                                    .setContent(com.vht.sdkcore.R.string.string_content_nubmer_spread_dialog)
                                    .setTextPositiveButton(com.vht.sdkcore.R.string.ok)
                                    .showPositiveButton()
                                    .setOnPositivePressed {
                                        it.dismiss()
                                        findNavController().navigateUp()
                                    }

                            }else{
                                showCustomToast(
                                    resTitleString = "Lan tỏa Viettel Home thành công",
                                    onFinish = {
                                        findNavController().navigateUp()
                                    },
                                    showImage = true
                                )
                            }


                        }else{
                            showDialogError(
                                title = "Lỗi xảy ra",
                                type = DialogType.ERROR,
                                message = response.error?:"",
                                titleBtnConfirm = com.vht.sdkcore.R.string.ok,
                                onPositiveClick ={

                                }
                            )
                        }

                    }

                }
            }

        }
    }


    override fun setOnClick() {
        super.setOnClick()

        binding.toolBar.setOnLeftClickListener {
            findNavController().navigateUp()
        }

        binding.btnContinue.setOnClickListener {
            if (isDoubleClick) return@setOnClickListener
            binding.tvError.gone()
            val phone =  binding.edtPhoneNumber.text.toString().trim()
            if(isCheckPhoneNumber(phone )){
                if(!requireContext().isNetworkAvailable()){
                    showCustomNotificationDialog(
                        title = getString(com.vht.sdkcore.R.string.no_connection),
                        type = DialogType.ERROR
                    ) {
                    }
                    return@setOnClickListener
                }
                viewModel.setSpreadCamera(deviceId,phone)
            }
        }

        binding.btnContinue.isEnabled = false
        binding.btnContinue.alpha = 0.5F
        binding.edtPhoneNumber.onTextChange {
            if (it.toString().trim().length == 10) {
                binding.btnContinue.isEnabled = true
                binding.btnContinue.alpha = 1F
            } else {
                binding.btnContinue.isEnabled = false
                binding.btnContinue.alpha = 0.5F
            }
        }

    }


    private fun isCheckPhoneNumber(numberPhone: String): Boolean{
        if(!checkNumberPhone(numberPhone)){
            binding.tvError.visible()
            binding.tvError.text =
                getString(com.vht.sdkcore.R.string.phone_number_is_not_in_the_correct_format)
            return false
        }


        return true
    }


    private fun checkNumberPhone(number: String?): Boolean {
        val NUMBER_PHONE = "(^(\\+84|84|0){1})+([0-9]{9})\$"
        val pattern = Pattern.compile(NUMBER_PHONE)
        val matcher = pattern.matcher(number)
        return matcher.matches()
    }
}