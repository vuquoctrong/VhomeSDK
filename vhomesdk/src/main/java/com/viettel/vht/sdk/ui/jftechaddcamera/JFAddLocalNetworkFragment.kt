package com.viettel.vht.sdk.ui.jftechaddcamera

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navGraphViewModels
import com.vht.sdkcore.base.BaseFragment
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.dialog.CommonAlertDialog
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentJfAddLocalNetworkBinding
import com.viettel.vht.sdk.navigation.AppNavigation
import com.viettel.vht.sdk.network.NetworkEvent
import com.viettel.vht.sdk.network.NetworkState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

@AndroidEntryPoint
class JFAddLocalNetworkFragment : BaseFragment<FragmentJfAddLocalNetworkBinding, JFTechAddCameraModel>() {

    @Inject
    lateinit var networkEvent: NetworkEvent

    @Inject
    lateinit var appNavigation: AppNavigation

    private val viewModel: JFTechAddCameraModel by navGraphViewModels(R.id.add_camera_jftech_graph) {
        defaultViewModelProviderFactory
    }

    private val settingWifiLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    override val layoutId: Int
        get() = R.layout.fragment_jf_add_local_network

    override fun getVM(): JFTechAddCameraModel = viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }


    override fun setOnClick() {
        super.setOnClick()
        binding.toolBar.setOnLeftClickListener {
            appNavigation.navigateUp()
        }
        binding.btnNext.setOnClickListener {
            appNavigation.openLanDeviceCameraJF()
        }
    }


    override fun bindingStateView() {
        super.bindingStateView()
        lifecycleScope.launchWhenResumed {
            networkEvent.observableNetworkState
                .flowOn(Dispatchers.Main)
                .collectLatest { status ->
                    if (status == NetworkState.NO_INTERNET || status == NetworkState.NO_CONNECT_INTERNET) {
                        CommonAlertDialog.getInstanceCommonAlertdialog(requireContext(), Define.TYPE_DIALOG.CONFIRM)
                            .showDialog()
                            .setDialogTitleWithString("Nhắc nhở")
                            .setContent("Vui lòng Bật Wifi trên điện thoại của bạn để quét các thiết bị trong mạng cục bộ")
                            .showNegativeAndPositiveButton()
                            .setTextPositiveButton(com.vht.sdkcore.R.string.string_open_wifi)
                            .setTextNegativeButton(com.vht.sdkcore.R.string.text_close)
                            .setOnNegativePressed { it.dismiss() }
                            .setOnPositivePressed {
                                settingWifiLauncher.launch(Intent(Settings.ACTION_WIFI_SETTINGS))
                                it.dismiss()
                            }
                    }
                }
        }
    }




}