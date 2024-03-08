package com.viettel.vht.sdk.ui.jfcameradetail.cloud

import androidx.fragment.app.activityViewModels
import com.vht.sdkcore.base.BaseFragment
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentJfCloudPostpaidBinding
import com.viettel.vht.sdk.model.camera_cloud.CloudStoragePackage
import com.viettel.vht.sdk.navigation.AppNavigation
import com.viettel.vht.sdk.ui.jfcameradetail.cloud.adapter.StorageTimeAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class JFCloudPostpaidFragment : BaseFragment<FragmentJfCloudPostpaidBinding, JFCloudStorageViewModel>() {


    @Inject
    lateinit var appNavigation: AppNavigation

    private val viewModel: JFCloudStorageViewModel by activityViewModels()

    override val layoutId: Int
        get() = R.layout.fragment_jf_cloud_postpaid

    override fun getVM(): JFCloudStorageViewModel = viewModel

    private var listData: ArrayList<CloudStoragePackage> = arrayListOf()

    private lateinit var storageTimeAdapter: StorageTimeAdapter

    override fun initView() {
        super.initView()
        binding.tvDeviceName.text = viewModel.deviceName
        listData.add(CloudStoragePackage(expired = 259200, price = 30000))
        listData.add(CloudStoragePackage(expired = 604800, price = 50000))
        listData.add(CloudStoragePackage(expired = 2592000, price = 100000))
        storageTimeAdapter = StorageTimeAdapter(false) {}
        storageTimeAdapter.submitList(listData)
        binding.rcvStorageTime.adapter = storageTimeAdapter
    }

    override fun setOnClick() {
        super.setOnClick()
        binding.toolbar.setOnLeftClickListener {
            appNavigation.navigateUp()
        }
    }


}