package com.viettel.vht.sdk.ui.jfcameradetail.settingwifi

import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.vht.sdkcore.base.BaseFragment
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.dialog.CommonAlertDialogNotification
import com.vht.sdkcore.utils.dialog.DialogType
import com.vht.sdkcore.utils.tint
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentSettingWifiCameraJfBinding
import com.viettel.vht.sdk.navigation.AppNavigation
import com.viettel.vht.sdk.ui.jfcameradetail.settingwifi.adapter.WifiAPCameraJFAdapter
import com.viettel.vht.sdk.utils.gone
import com.viettel.vht.sdk.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingWifiCameraJFFragment :
    BaseFragment<FragmentSettingWifiCameraJfBinding, SettingWifiCameraJFViewModel>() {

    @Inject
    lateinit var appNavigation: AppNavigation

    private val viewModel: SettingWifiCameraJFViewModel by viewModels()

    override val layoutId = R.layout.fragment_setting_wifi_camera_jf

    private val adapterWifi by lazy {
        WifiAPCameraJFAdapter {
            ConnectWifiCameraJFDialog(it.ssid) { _, password ->
                viewModel.connectWifi(it.ssid, password)
            }.show(childFragmentManager, ConnectWifiCameraJFDialog.TAG)
        }
    }

    override fun getVM() = viewModel

    override fun initView() {
        super.initView()
        if (arguments?.getBoolean(Define.BUNDLE_KEY.PARAM_IS_LAN_NETWORK) == true) {
            binding.tvNameWifi.text = getString(com.vht.sdkcore.R.string.wired_network)
            binding.ivLockWifi.isVisible = false
            binding.ivStatusWifi.setImageResource(R.drawable.ic_lan_connection)
            binding.ivStatusWifi.tint(R.color.color_4E4E4E)
        } else {
            binding.tvNameWifi.text = arguments?.getString(Define.BUNDLE_KEY.PARAM_SSID)
            binding.ivLockWifi.isVisible =
                arguments?.getString(Define.BUNDLE_KEY.PARAM_NETWORK_AUTH) != "OPEN"
        }
        binding.rvWifiAvailable.adapter = adapterWifi
    }

    override fun setOnClick() {
        super.setOnClick()
        binding.toolBar.setOnLeftClickListener {
            appNavigation.navigateUp()
        }

        binding.btnRefresh.setOnClickListener {
            viewModel.refreshWifiList()
        }

        binding.btnAddWifi.setOnClickListener {
            ConnectWifiCameraJFDialog(null) { ssid, password ->
                viewModel.connectWifi(ssid, password)
            }.show(childFragmentManager, ConnectWifiCameraJFDialog.TAG)
        }
    }

    override fun bindingStateView() {
        super.bindingStateView()
        viewModel.isLoadingWifiList.observe(viewLifecycleOwner) {
            if (it) {
                binding.groupWifiAvailable.gone()
                binding.clLoadingSearchWifi.visible()
            } else {
                binding.groupWifiAvailable.visible()
                binding.clLoadingSearchWifi.gone()
            }
        }
        viewModel.wifiRouteSignalLevel.observe(viewLifecycleOwner) {
            if (it.second) {
                binding.tvNameWifi.text = getString(com.vht.sdkcore.R.string.wired_network)
                binding.ivLockWifi.isVisible = false
                binding.ivStatusWifi.setImageResource(R.drawable.ic_lan_connection)
                binding.ivStatusWifi.tint(R.color.color_4E4E4E)
            } else {
                binding.tvNameWifi.text = viewModel.networkWifi?.ssid ?: ""
                binding.ivLockWifi.isVisible = viewModel.networkWifi?.auth != "OPEN"
                binding.ivStatusWifi.setImageResource(
                    when (it.first) {
                        in 1..49 -> R.drawable.ic_wifi_connection_1
                        in 50..79 -> R.drawable.ic_wifi_connection_2
                        in 80..100 -> R.drawable.ic_wifi_connection_3
                        else -> R.drawable.ic_wifi_connection_1
                    }
                )
            }
        }
        viewModel.wifiListLiveData.observe(viewLifecycleOwner) {
            adapterWifi.submitList(it ?: listOf())
            if (it.isNullOrEmpty()) {
                binding.rvWifiAvailable.gone()
                CommonAlertDialogNotification
                    .getInstanceCommonAlertdialog(requireContext())
                    .showDialog()
                    .showCenterImage(DialogType.ERROR)
                    .setDialogTitleWithString(getString(com.vht.sdkcore.R.string.string_error_searching_wifi))
                    .setTextPositiveButtonWithString(getString(com.vht.sdkcore.R.string.ok))
                    .setOnPositivePressed { dialog ->
                        dialog.dismiss()
                    }
                    .hideOptionButton()
                    .showPositiveButton()
            } else {
                binding.rvWifiAvailable.visible()
            }
        }
        viewModel.isConnectingWifi.observe(viewLifecycleOwner) {
            if (it) {
                binding.clLoadingConnectingWifi.visible()
            } else {
                binding.clLoadingConnectingWifi.gone()
            }
        }
        viewModel.isConnectWifiSuccess.observe(viewLifecycleOwner) {
            if (it) {
                CommonAlertDialogNotification
                    .getInstanceCommonAlertdialog(requireContext())
                    .showDialog()
                    .showCenterImage(DialogType.SUCCESS)
                    .setDialogTitleWithString(getString(com.vht.sdkcore.R.string.string_sending_new_wifi_info_successfully))
                    .setContent(getString(com.vht.sdkcore.R.string.string_sending_new_wifi_info_successfully_description))
                    .setTextPositiveButtonWithString(getString(com.vht.sdkcore.R.string.ok))
                    .setOnPositivePressed { dialog ->
                        dialog.dismiss()
                        requireActivity().finish()
                    }
                    .hideOptionButton()
                    .showPositiveButton()
            } else {
                CommonAlertDialogNotification
                    .getInstanceCommonAlertdialog(requireContext())
                    .showDialog()
                    .showCenterImage(DialogType.ERROR)
                    .setDialogTitleWithString(getString(com.vht.sdkcore.R.string.string_sending_new_wifi_info_failed))
                    .setTextPositiveButtonWithString(getString(com.vht.sdkcore.R.string.ok))
                    .setOnPositivePressed { dialog ->
                        dialog.dismiss()
                    }
                    .hideOptionButton()
                    .showPositiveButton()
            }
        }
    }
}
