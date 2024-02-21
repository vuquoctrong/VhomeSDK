package com.viettel.vht.sdk.ui.jftechaddcamera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.SupplicantState
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.View
import androidx.navigation.navGraphViewModels
import com.vht.sdkcore.base.BaseFragment
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.isConnectedToWifiBand2400Hz
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentWifiRoomAddCameraJftechBinding
import com.viettel.vht.sdk.navigation.AppNavigation

import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class JFTechWifiRoomAddCameraFragment :
    BaseFragment<FragmentWifiRoomAddCameraJftechBinding, JFTechAddCameraModel>() {

    val TAG = JFTechWifiRoomAddCameraFragment::class.java.simpleName

    private val viewModel: JFTechAddCameraModel by navGraphViewModels(R.id.add_camera_jftech_graph) {
        defaultViewModelProviderFactory
    }

    @Inject
    lateinit var appNavigation: AppNavigation

    override val layoutId = R.layout.fragment_wifi_room_add_camera_jftech


    override fun getVM(): JFTechAddCameraModel = viewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
    }

    override fun onResume() {
        isCheckLocation()
        super.onResume()
    }

    override fun setOnClick() {
        super.setOnClick()
        binding.toolbar.setOnLeftClickListener {
            appNavigation.navigateUp()
        }
        binding.confirmBtn.setOnClickListener {
            requireContext().isConnectedToWifiBand2400Hz { appNavigation.openQuickGide2AddCamera() }
        }

        binding.rlChooseWifi.setOnClickListener {
            gotoApplicationSettingWifi()
        }

        binding.cbRememberPassword.setOnClickListener {
            if (binding.cbRememberPassword.isChecked) {
                viewModel.rememberWifi(viewModel.wifiSSIDLiveData.value.toString().trim(), viewModel.wifiPassLiveData.value.toString().trim())
            } else {
                viewModel.forgotWifi(viewModel.wifiSSIDLiveData.value.toString().trim())
            }
        }
        binding.ivShowPassword.setOnClickListener { v: View? ->
            if (binding.etInputPassword.inputType == Define.LOGIN.INPUTTYPE_TEXTPASSWORD) {
                binding.etInputPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.ivShowPassword.setImageResource(R.drawable.ic_eye_open)
            } else {
                binding.etInputPassword.inputType = Define.LOGIN.INPUTTYPE_TEXTPASSWORD
                binding.ivShowPassword.setImageResource(R.drawable.ic_eye_close)
            }
            binding.etInputPassword.setSelection(
                binding.etInputPassword.text.toString().trim { it <= ' ' }.length
            )
            binding.etInputPassword.requestFocus()
            //When change input type, font reset automatically, so set font again.
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                binding.etInputPassword.typeface =
//                    resources.getFont(com.viettel.vht.main.R.font.gotham_book)
//            }
        }
    }



    private fun initConnectedWifiInfo() {
        val wifiManager =
            requireActivity().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        if (wifiInfo.supplicantState == SupplicantState.COMPLETED) {
            val wifiSSID = wifiInfo.ssid.drop(1).dropLast(1)
            viewModel.wifiSSIDLiveData.value = wifiSSID
            viewModel.dhcpInfo = wifiManager.dhcpInfo
//            Timber.d("ipAddress: ${wifiManager.dhcpInfo.ipAddress}")
            val wifiPassword = viewModel.getWifiPassword(viewModel.wifiSSIDLiveData.value.toString().trim())
            if (wifiPassword.isNotBlank()) {
                binding.cbRememberPassword.isChecked = true
            }
            viewModel.wifiPassLiveData.value = wifiPassword
        }
    }

    private fun gotoApplicationPermissionSetting(context: Context) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val uri =
            Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    fun gotoApplicationSettingWifi() {
        val intent = Intent()
        intent.action = Settings.ACTION_WIFI_SETTINGS
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onPause() {
        super.onPause()
        if (binding.cbRememberPassword.isChecked) {
            viewModel.rememberWifi(viewModel.wifiSSIDLiveData.value.toString().trim(), viewModel.wifiPassLiveData.value.toString().trim())
        } else {
            viewModel.forgotWifi(viewModel.wifiSSIDLiveData.value.toString().trim())
        }
    }

}
