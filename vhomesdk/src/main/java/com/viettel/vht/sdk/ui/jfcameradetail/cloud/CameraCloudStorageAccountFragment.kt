package com.viettel.vht.sdk.ui.jfcameradetail.cloud

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.vht.sdkcore.base.BaseFragment
import com.vht.sdkcore.utils.Define
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentAllCameraCloudStorageBinding
import com.viettel.vht.sdk.navigation.AppNavigation
import com.viettel.vht.sdk.network.Result
import com.viettel.vht.sdk.ui.jfcameradetail.cloud.adapter.CameraStatusCloudListAdapter
import com.viettel.vht.sdk.utils.gone
import com.viettel.vht.sdk.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class CameraCloudStorageAccountFragment :
    BaseFragment<FragmentAllCameraCloudStorageBinding, HistoryBuyCloudStorageViewModel>() {


    @Inject
    lateinit var appNavigation: AppNavigation

    private val viewModel: HistoryBuyCloudStorageViewModel by viewModels()

    override val layoutId: Int
        get() = R.layout.fragment_all_camera_cloud_storage

    override fun getVM(): HistoryBuyCloudStorageViewModel = viewModel

    private lateinit var adapter: CameraStatusCloudListAdapter



    override fun initView() {
        super.initView()
        viewModel.getListCameraCloudAccount()
        adapter = CameraStatusCloudListAdapter { data ->
            var camera = data?.camera
            val bundle = Bundle().apply {
                putString(Define.BUNDLE_KEY.PARAM_ID, camera?.id)
                putString(Define.BUNDLE_KEY.PARAM_NAME, camera?.name)
                putString(Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL, camera?.serial)
                when(camera?.model){
                    Define.CAMERA_MODEL.CAMERA_JF_INDOOR,Define.CAMERA_MODEL.CAMERA_JF_OUTDOOR ->{
                        putString(Define.BUNDLE_KEY.PARAM_DEVICE_TYPE, Define.CAMERA_MODEL.CAMERA_JF_VENDOR)
                    }
                    Define.CAMERA_MODEL.CAMERA_EZ_INDOOR,Define.CAMERA_MODEL.CAMERA_EZ_OUTDOOR ->{
                        putString(Define.BUNDLE_KEY.PARAM_DEVICE_TYPE, Define.CAMERA_MODEL.CAMERA_EZ_VENDOR)
                    }
                    Define.CAMERA_MODEL.CAMERA_IPC_INDOOR,Define.CAMERA_MODEL.CAMERA_IPC_OUTDOOR ->{
                        putString(Define.BUNDLE_KEY.PARAM_DEVICE_TYPE, Define.CAMERA_MODEL.CAMERA_IPC_VENDOR)
                    }
                }
            }
            appNavigation.openCloudStorageCamera(bundle)
        }
        binding.rcvPackage.adapter = adapter
        binding.llListCamera.gone()
        binding.toolbar.showButtonRight(true)
    }


    override fun bindingStateView() {
        super.bindingStateView()
        viewModel.responseListCameraCloudAccount.observe(viewLifecycleOwner, observer = {
            when (it) {
                is Result.Loading -> {
                    showHideLoading(true)
                }
                is Result.Error -> {
                    showHideLoading(false)

                }
                is Result.Success -> {
                    showHideLoading(false)
                    adapter.submitList(it.data?.data)
                    if (adapter.itemCount == 0) {
                        binding.llListCamera.gone()
                        binding.tvShowNotDevice.visible()
                    } else {
                        binding.llListCamera.visible()
                        binding.tvShowNotDevice.gone()
                    }

                }
            }
        })

    }

    override fun setOnClick() {
        super.setOnClick()
        binding.toolbar.setOnLeftClickListener {
            appNavigation.navigateUp()
        }

        binding.toolbar.setOnRightClickListener {
            val bundle = Bundle().apply {
                putInt(Define.BUNDLE_KEY.PARAM_TYPE_SCREEN_HISTORY_CLOUD, TYPE_SCREEN_ACCOUNT)
            }
            appNavigation.openHistoryByCloudStorage(bundle)
        }

        binding.tvGiftOpen.setOnClickListener {
          //  appNavigation.openDetailGiftRelativeFragment()
        }

    }


}