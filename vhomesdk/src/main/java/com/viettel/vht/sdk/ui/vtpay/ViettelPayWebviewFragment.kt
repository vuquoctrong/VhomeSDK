package com.viettel.vht.sdk.ui.vtpay

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.viewModels
import com.vht.sdkcore.base.BaseFragment
import com.vht.sdkcore.network.Status
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.dialog.DialogType
import com.vht.sdkcore.utils.showCustomToast
import com.vht.sdkcore.utils.toastMessage
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentViettelPayWebviewBinding
import com.viettel.vht.sdk.model.camera_cloud.BuyCloudVeRequest
import com.viettel.vht.sdk.model.camera_cloud.OnOffAutoChargingCloud
import com.viettel.vht.sdk.model.camera_cloud.PaymentCodeResponse
import com.viettel.vht.sdk.model.camera_cloud.PaymentRequest
import com.viettel.vht.sdk.navigation.AppNavigation
import com.viettel.vht.sdk.utils.DebugConfig

import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class ViettelPayWebviewFragment :
    BaseFragment<FragmentViettelPayWebviewBinding, ViettelPayWebviewViewModel>() {
    @Inject
    lateinit var appNavigation: AppNavigation

    override val layoutId = R.layout.fragment_viettel_pay_webview

    private val viewModel: ViettelPayWebviewViewModel by viewModels()
    var returnUrl = "https://innoway.vn/success"
    var cancelUrl = "https://innoway.vn/cancel"
    var cameraSerial: String = ""
    var methodPay: String = "CTT"
    var packageId = ""
    var price: Int = 0
    var ischeckError = false
    var deviceType = ""
    var deviceId = ""
    private var referCode =""
    private var isExtendAutoCloud = false
    private var isOpenGiftRelative = false
    private var userIdRelative = ""
    override fun getVM() = viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        super.onCreate(savedInstanceState)
    }

    override fun initView() {
        super.initView()
        arguments?.let {
            Timber.d("arguments: ${arguments.toString()}")
            if (it.containsKey(Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL)) {
                cameraSerial = it.getString(Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL, "")
            }
            if (it.containsKey(Define.BUNDLE_KEY.PARAM_PACKAGE_ID)) {
                packageId = it.getString(Define.BUNDLE_KEY.PARAM_PACKAGE_ID, "")
            }
            if (it.containsKey(Define.BUNDLE_KEY.PARAM_PRICE)) {
                price = it.getInt(Define.BUNDLE_KEY.PARAM_PRICE)
            }
            if (it.containsKey(Define.BUNDLE_KEY.PARAM_DEVICE_TYPE)) {
                deviceType = it.getString(Define.BUNDLE_KEY.PARAM_DEVICE_TYPE, "")
            }
            if (it.containsKey(Define.BUNDLE_KEY.PARAM_DEVICE_ID)) {
                deviceId = it.getString(Define.BUNDLE_KEY.PARAM_DEVICE_ID, "")
            }

            if (it.containsKey(Define.BUNDLE_KEY.PARAM_DATA_OPEN_AUTO_CHARGING_CLOUD)) {
                isExtendAutoCloud =
                    it.getBoolean(Define.BUNDLE_KEY.PARAM_DATA_OPEN_AUTO_CHARGING_CLOUD)
            }

            if(it.containsKey(Define.BUNDLE_KEY.PARAM_DATA_REGISTER_REFER_CODE_CLOUD)){
                referCode = it.getString(Define.BUNDLE_KEY.PARAM_DATA_REGISTER_REFER_CODE_CLOUD)?:""
            }

            if (it.containsKey(Define.BUNDLE_KEY.PARAM_OPEN_TO_GIFT_RELATIVES)) {
                isOpenGiftRelative = it.getBoolean(Define.BUNDLE_KEY.PARAM_OPEN_TO_GIFT_RELATIVES)
            }
            if (it.containsKey(Define.BUNDLE_KEY.PARAM_USE_ID_TO_GIFT_RELATIVES)) {
                userIdRelative = it.getString(Define.BUNDLE_KEY.PARAM_USE_ID_TO_GIFT_RELATIVES) ?: ""
            }

//            if(it.containsKey(Define.BUNDLE_KEY.PARAM_METHOD_TYPE_PAY)){
//                methodPay = it.getString(Define.BUNDLE_KEY.PARAM_METHOD_TYPE_PAY,"CTT")
//            }

            if (deviceType == Define.TYPE_DEVICE.CAMERA_JF
                || deviceType == Define.TYPE_DEVICE.VTTCAMERA
                || deviceType == Define.TYPE_DEVICE.CAMERA_IPC) {
                viewModel.getLinkBuyCloudURl(BuyCloudVeRequest(deviceId,packageId,methodPay, invitationCode = referCode,  userIdRelative = userIdRelative))

            } else {
                viewModel.payment(PaymentRequest(cameraSerial, packageId, price.toString()))
            }
        }
    }

    override fun bindingStateView() {
        super.bindingStateView()
        viewModel.paymentResult.observe(viewLifecycleOwner) {
            if (it == null) {
                retryPayment()
            } else {
              if(methodPay == "CTT"){
                  loadDataWebview(it)
              }else{
                  // tiến hành xác thực OTP
              }

            }
        }

        viewModel.responseGetLinkBuyCloudURl.observe(viewLifecycleOwner){
            when (it.status) {
                Status.LOADING -> showHideLoading(true)

                Status.ERROR -> {
                    showHideLoading(false)
                    showDialogError(
                        title = "Lỗi xảy ra",
                        type = DialogType.ERROR,
                        message = "",
                        titleBtnConfirm = com.vht.sdkcore.R.string.ok,
                        onPositiveClick = {
                            appNavigation.navigateUp()
                        }
                    )
                }

                Status.SUCCESS -> {
                    showHideLoading(false)
                    it?.let {
                        if (it.data?.code == 200) {
                            it.data?.let {
                                loadDataWebview(it)
                            }
                        } else {
                            showDialogError(
                                title = "Lỗi xảy ra",
                                type = DialogType.ERROR,
                                message = it?.data?.message ?: "",
                                titleBtnConfirm = com.vht.sdkcore.R.string.ok,
                                onPositiveClick = {
                                    appNavigation.navigateUp()
                                }
                            )
                        }
                    }

                }
            }
        }

        viewModel.responseOnOffAutoChargingCloud.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> showHideLoading(true)

                Status.ERROR -> {
                    showHideLoading(false)
                    registerSuccess()
                }

                Status.SUCCESS -> {
                    showHideLoading(false)

                    it?.data?.let {
                        if (it.code == 200) {
                            registerSuccess()

                        } else {
                            registerSuccess()
                        }
                    }

                }
            }
        }
    }

    private fun registerSuccess(){
        showCustomToast(
            resTitleString = "Đăng ký thành công",
            onFinish = {
                if(isOpenGiftRelative){
                    appNavigation.backToCameraCloudStorageAccountFragment()
                }else{
                    appNavigation.backToCloudStorageJFCamera()
                }
            },
            showImage = true
        )
    }

    private fun retryPayment() {
        if(!ischeckError){
            if (deviceType == Define.TYPE_DEVICE.CAMERA_JF
                || deviceType == Define.TYPE_DEVICE.VTTCAMERA
                || deviceType == Define.TYPE_DEVICE.CAMERA_IPC) {
                viewModel.getLinkBuyCloudURl(BuyCloudVeRequest(deviceId,packageId,methodPay, invitationCode = referCode,  userIdRelative = userIdRelative))
            } else {
                viewModel.payment(PaymentRequest(cameraSerial, packageId, price.toString()))
            }
            ischeckError = true
        }else{
            toastMessage(getString(com.vht.sdkcore.R.string.something_wrong))
        }

    }

    private fun loadDataWebview(paymentResponse: PaymentCodeResponse) {
        val uri = Uri.parse(paymentResponse.data)
        binding.webview.webChromeClient = WebChromeClient()
        binding.webview.settings.javaScriptEnabled = true
        cancelUrl = uri.getQueryParameter("cancel_url") ?: "https://innoway.vn/cancel"
        returnUrl = uri.getQueryParameter("return_url") ?: "https://innoway.vn/success"
        Timber.d("cancelUrl: $cancelUrl")
        Timber.d("returnUrl: $returnUrl")

        binding.webview.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Timber.d("onPageStarted: $url")
                DebugConfig.log(message = "onPageStarted: $url")
                if (url?.contains(cancelUrl) == true) {
                    showHideLoading(false)
                    appNavigation.navigateUp()
                    return
                }
                if (url?.contains(returnUrl) == true && url.contains("error_code=00")) {
                    showHideLoading(false)
                    if (isExtendAutoCloud) {
                        viewModel.setOnOffAutoChargingCloud(
                            OnOffAutoChargingCloud(
                                serial = cameraSerial,
                                status = "activate",
                                method = "VTPAY-WALLET",
                                otp = "system-run",
                                orderId = ""
                            )
                        )
                    } else {
                        registerSuccess()
                    }
                    return
                }
                showHideLoading(true)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                showHideLoading(false)
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

            override fun onLoadResource(view: WebView?, url: String?) {
                super.onLoadResource(view, url)
                Timber.d("urlLoad: $url")
            }
        }
        binding.webview.loadUrl(paymentResponse.data)
    }

    override fun setOnClick() {
        super.setOnClick()
        binding.ivLeft.setOnClickListener {
            appNavigation.navigateUp()
        }
    }
}