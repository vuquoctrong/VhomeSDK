package com.viettel.vht.sdk.ui.jftechaddcamera

import android.os.Bundle
import android.view.View
import androidx.navigation.navGraphViewModels
import com.vht.sdkcore.base.BaseFragment
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentOneQuickGideAddCameraIftechBinding
import com.viettel.vht.sdk.navigation.AppNavigation
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class JFTechQuickGide1AddCameraFragment :
    BaseFragment<FragmentOneQuickGideAddCameraIftechBinding, JFTechAddCameraModel>() {
    val TAG = JFTechQuickGide1AddCameraFragment::class.java.simpleName

    @Inject
    lateinit var appNavigation: AppNavigation

    private val viewModel: JFTechAddCameraModel by navGraphViewModels(R.id.add_camera_jftech_graph) {
        defaultViewModelProviderFactory
    }

    override val layoutId = R.layout.fragment_one_quick_gide_add_camera_iftech

    override fun getVM(): JFTechAddCameraModel = viewModel

    override fun initView() {
        if (viewModel.cameraType == "HC23") {
            binding.imCamera1.setImageResource(R.drawable.img_reset_hc23)
        } else {
            binding.imCamera1.setImageResource(R.drawable.img_reset_hc33)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun setOnClick() {
        super.setOnClick()
        binding.btnSubmit.setOnClickListener {
            if (isCheckLocation()) {
                appNavigation.openWifiRoomAddCamera()
            }

        }

        binding.toolbar.setOnLeftClickListener {
            appNavigation.navigateUp()
        }
    }


}


