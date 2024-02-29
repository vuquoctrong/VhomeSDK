package com.viettel.vht.sdk.ui.splash

import android.os.Bundle
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import com.vht.sdkcore.base.BaseFragment
import com.vht.sdkcore.network.Status
import com.vht.sdkcore.pref.AppPreferences.Companion.PREF_ORG_ID
import com.vht.sdkcore.pref.RxPreferences
import com.vht.sdkcore.utils.Define
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentSdkhomeSplashBinding
import com.viettel.vht.sdk.jfmanager.JFCameraManager
import com.viettel.vht.sdk.navigation.AppNavigation
import com.viettel.vht.sdk.utils.Config
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class SDKVHomeLoadingFragment :
    BaseFragment<FragmentSdkhomeSplashBinding, SDKVHomeLoadingViewModel>() {

    @Inject
    lateinit var appNavigation: AppNavigation

    @Inject
    lateinit var rxPreferences: RxPreferences

    override val layoutId = R.layout.fragment_sdkhome_splash

    private val viewModel: SDKVHomeLoadingViewModel by viewModels()



    override fun getVM() = viewModel


    override fun initView() {
        super.initView()
        login()
    }

    override fun onResume() {
        super.onResume()

    }

    override fun setOnClick() {
        super.setOnClick()
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {

                    activity?.finish()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)


    }

    override fun bindingAction() {
        super.bindingAction()
        viewModel.loginResponse.observe(viewLifecycleOwner) { result ->
            when (result.status) {
                Status.LOADING -> {
                    showHideLoading(true)
                }

                Status.SUCCESS -> {
                    showHideLoading(false)
                    result.data?.orgId?.let { rxPreferences.put(PREF_ORG_ID, it) }
                    configCameraJF()
                    openFunctionSDK()
                }

                Status.ERROR -> {
                    showHideLoading(false)
                }
            }
        }

    }

    private fun login() {
        val token = rxPreferences.getFirebaseToken()
        val jsonObject = JSONObject().apply {
            put(Define.BUNDLE_KEY.PARAM_IDENTIFIER, "0986784498")
            put(Define.BUNDLE_KEY.PARAM_PASSWORD, "12345678aA@")
            put(Define.BUNDLE_KEY.PARAM_CAPTCHA, "")
            put("cname", "viettelhome_dev")
            put(
                "pushRegisterJson",
                "[{\"channel\":14,\"channelRegisterJson\":\"{\\\"token\\\":\\\"$token\\\"}\"}]"
            )
        }

        viewModel.login(jsonObject.toString())
    }

    private fun configCameraJF() {
        JFCameraManager.init(context = requireContext())
        rxPreferences.getUserJF().let { phone ->
            if (phone.isNotEmpty()) {
                JFCameraManager.loginAccountJF(
                    user = phone, password = rxPreferences.getTokeJFTech()
                ) {
                    Timber.d("Callback login JF")
                    stopPushServiceCameraJF()
                    startPushServiceCameraJF()
                }
            }
        }
    }

    private fun stopPushServiceCameraJF() {
        //   stopService(Intent(this, AlarmPushService::class.java))
    }
    private fun startPushServiceCameraJF() {
        Timber.d("startPushServiceCameraJF")
//        try {
//            val intent = Intent().apply {
//                addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
//                setClass(applicationContext, AlarmPushService::class.java)
//            }
//            startService(intent)
//        } catch (e: Exception) {
//            Timber.e(e)
//        }
    }

    private fun openFunctionSDK(){
        when((requireActivity().intent.getStringExtra(Config.SDK_DATA_FUNCTION_VHOME))){
            Config.SDK_FUNCTION_OPEN_ADD_CAMERA_JF ->{
                appNavigation.openAddCameraJF()
            }
            Config.SDK_FUNCTION_OPEN_DETAIL_CAMERA_JF ->{
                val bundle = Bundle()
                val idCamera = "68cadfb7-2f64-47be-90c5-5cbd84d5e1af"
                val serialCamera = "7ceeaf9b3e9b6000"
                val nameCamera = "7ceeaf9b3e9b6000"
                bundle.putString(
                    Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL,
                    serialCamera
                )
                bundle.putString(Define.BUNDLE_KEY.PARAM_DEVICE_ID, idCamera)
                bundle.putString(Define.BUNDLE_KEY.PARAM_ID, idCamera)
                bundle.putString(Define.BUNDLE_KEY.PARAM_NAME, nameCamera)

                bundle.putString(Define.BUNDLE_KEY.PARAM_MODEL_CAMERA, "HC23")

                bundle.putString(
                    Define.BUNDLE_KEY.PARAM_STATUS_SHARE, ""
                )

                bundle.putString(
                    Define.BUNDLE_KEY.PARAM_PERMISSIONS, ""
                )

                appNavigation.openDetailCameraJF(bundle)
            }
        }
    }

}