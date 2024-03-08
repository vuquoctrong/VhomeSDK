package com.viettel.vht.sdk.ui.informationcamera

import android.graphics.Paint
import android.os.Bundle
import androidx.fragment.app.viewModels
import com.vht.sdkcore.base.BaseFragment
import com.vht.sdkcore.utils.Define
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentInformationCamJfBinding
import com.viettel.vht.sdk.navigation.AppNavigation
import com.viettel.vht.sdk.utils.gone
import com.viettel.vht.sdk.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class InformationCameraJFFragment :
    BaseFragment<FragmentInformationCamJfBinding, InformationCameraJFViewModel>() {

    @Inject
    lateinit var appNavigation: AppNavigation


    private val viewModel: InformationCameraJFViewModel by viewModels()
    private var devId: String = ""
    override val layoutId = R.layout.fragment_information_cam_jf

    override fun getVM(): InformationCameraJFViewModel = viewModel

    override fun initView() {
        super.initView()
        arguments?.let {
            if (it.containsKey(Define.BUNDLE_KEY.PARAM_ID)) {
                devId = it.getString(Define.BUNDLE_KEY.PARAM_ID) ?: ""
                binding.tvSerial.text = devId
            }
            if (it.containsKey(Define.BUNDLE_KEY.PARAM_MODEL_CAMERA)) {
                binding.tvModel.text = it.getString(Define.BUNDLE_KEY.PARAM_MODEL_CAMERA)
            }
        }
        viewModel.devId = devId
        viewModel.getConfig()
        binding.btnSettingWifi.paintFlags = Paint.UNDERLINE_TEXT_FLAG
    }

    override fun bindingStateView() {
        viewModel.getNetworkWifiState.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.clWifi.visible()
                binding.tvWifi.visible()
                try {
                    val buildTime = viewModel.getSystemInfoState.value?.buildTime ?: ""
                    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val buildDateTime = formatter.parse(buildTime)
                    val referenceDateTime = formatter.parse("2024-01-11 00:00:00")
                    if (buildDateTime?.after(referenceDateTime) == true) {
                        binding.btnSettingWifi.visible()
                        binding.ivBackLeftWifi.visible()
                    } else {
                        binding.btnSettingWifi.gone()
                        binding.ivBackLeftWifi.gone()
                    }
                } catch (e: Exception) {
                    binding.btnSettingWifi.gone()
                    binding.ivBackLeftWifi.gone()
                }
                if (viewModel.isLANNetworkWifi) {
                    binding.tvNameWifi.text = getString(com.vht.sdkcore.R.string.wired_network)
                    binding.ivStatusWifi.setImageResource(R.drawable.ic_lan_connection)
                } else {
                    binding.tvNameWifi.text = it.ssid
                    binding.ivStatusWifi.setImageResource(R.drawable.ic_wifi_connection)
                }
            } else {
                binding.tvWifi.gone()
                binding.btnSettingWifi.gone()
                binding.ivBackLeftWifi.gone()
                binding.clWifi.gone()
            }
        }
        viewModel.getSystemInfoState.observe(viewLifecycleOwner) {
            it ?: return@observe
            binding.tvSerial.text = it.serialNo
            binding.tvDeviceVersionValue.text = it.deviceModel
            binding.tvFirmware.text = it.softWareVersion
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val sdfShow = SimpleDateFormat("yyyy/MM/dd - HH:mm:ss", Locale.getDefault())
            try {
                binding.tvReleaseDate.text =
                    sdfShow.format(sdf.parse(it.buildTime.toString()) ?: "") ?: it.buildTime
                            ?: "--:--:-- --/--/----"
            } catch (e: Exception) {
                e.printStackTrace()
                binding.tvReleaseDate.text = it.buildTime ?: "--:--:-- --/--/----"
            }
        }
    }

    override fun setOnClick() {
        binding.toolBar.setOnLeftClickListener {
            appNavigation.navigateUp()
        }

        binding.btnSettingWifi.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(Define.BUNDLE_KEY.PARAM_ID, viewModel.devId)
            bundle.putString(
                Define.BUNDLE_KEY.PARAM_SSID,
                viewModel.getNetworkWifiState.value?.ssid
            )
            bundle.putString(
                Define.BUNDLE_KEY.PARAM_NETWORK_AUTH,
                viewModel.getNetworkWifiState.value?.auth
            )
            bundle.putBoolean(
                Define.BUNDLE_KEY.PARAM_IS_LAN_NETWORK,
                viewModel.isLANNetworkWifi
            )
            appNavigation.openSettingWifiCameraJF(bundle)
        }
    }

    companion object {
        val TAG = InformationCameraJFFragment::class.simpleName.toString()
    }
}