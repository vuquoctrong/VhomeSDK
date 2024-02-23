package com.viettel.vht.sdk.ui.splash

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
                    appNavigation.openAddCameraJF()
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

}