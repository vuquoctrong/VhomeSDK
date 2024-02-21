package com.viettel.vht.sdk.ui.jftechaddcamera

import android.os.Bundle
import android.view.View
import androidx.navigation.navGraphViewModels
import com.vht.sdkcore.base.BaseFragment
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentTwoQuickGideAddCameraIftechBinding
import com.viettel.vht.sdk.navigation.AppNavigation
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class JFTechQuickGide2AddCameraFragment :
    BaseFragment<FragmentTwoQuickGideAddCameraIftechBinding, JFTechAddCameraModel>() {
    val TAG = JFTechQuickGide2AddCameraFragment::class.java.simpleName

    @Inject
    lateinit var appNavigation: AppNavigation

    private val viewModel: JFTechAddCameraModel by navGraphViewModels(R.id.add_camera_jftech_graph){
        defaultViewModelProviderFactory
    }

    override val layoutId = R.layout.fragment_two_quick_gide_add_camera_iftech

    override fun getVM(): JFTechAddCameraModel = viewModel

    override fun initView() {
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun setOnClick() {
        super.setOnClick()
        binding.btnSubmit.setOnClickListener {
            appNavigation.openShowQRCodeAddCamera()
        }

        binding.toolbar.setOnLeftClickListener {
            appNavigation.navigateUp()
        }
    }


}


