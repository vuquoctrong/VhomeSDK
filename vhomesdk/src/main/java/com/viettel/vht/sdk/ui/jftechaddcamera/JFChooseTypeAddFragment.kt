package com.viettel.vht.sdk.ui.jftechaddcamera

import androidx.navigation.navGraphViewModels
import com.vht.sdkcore.base.BaseFragment
import com.vht.sdkcore.pref.RxPreferences
import com.vht.sdkcore.utils.Define
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentJfChooseTypeAddBinding
import com.viettel.vht.sdk.navigation.AppNavigation
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class JFChooseTypeAddFragment : BaseFragment<FragmentJfChooseTypeAddBinding, JFTechAddCameraModel>() {

    @Inject
    lateinit var appNavigation: AppNavigation

    @Inject
    lateinit var rxPreferences: RxPreferences

    private val viewModel: JFTechAddCameraModel by navGraphViewModels(R.id.add_camera_jftech_graph) {
        defaultViewModelProviderFactory
    }

    override val layoutId: Int
        get() = R.layout.fragment_jf_choose_type_add

    override fun getVM(): JFTechAddCameraModel = viewModel

    override fun initView() {
        super.initView()
        arguments.let {
            viewModel.cameraType = it?.getString(Define.BUNDLE_KEY.PARAM_CAMERA_TYPE, "").toString()
        }

    }
    override fun setOnClick() {
        super.setOnClick()
        binding.toolBar.setOnLeftClickListener {
            appNavigation.navigateUp()
        }
        binding.rlAddWifi.setOnClickListener {
            appNavigation.openWifiConnectCameraJF()
        }
        binding.rlAddLan.setOnClickListener {
            appNavigation.openLanConnectCameraJF()
        }
    }

}