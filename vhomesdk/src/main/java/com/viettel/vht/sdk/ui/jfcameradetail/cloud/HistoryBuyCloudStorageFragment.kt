package com.viettel.vht.sdk.ui.jfcameradetail.cloud

import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import com.vht.sdkcore.base.BaseFragment
import com.vht.sdkcore.pref.RxPreferences
import com.vht.sdkcore.utils.Define
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentHistoryBuyCloudStorageBinding
import com.viettel.vht.sdk.navigation.AppNavigation
import com.viettel.vht.sdk.ui.jfcameradetail.cloud.adapter.HistoryBuyCloudAdapter
import com.viettel.vht.sdk.utils.gone
import com.viettel.vht.sdk.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

const val TYPE_SCREEN_CAMERA = 1
const val TYPE_SCREEN_ACCOUNT = 2

@AndroidEntryPoint
class HistoryBuyCloudStorageFragment :
    BaseFragment<FragmentHistoryBuyCloudStorageBinding, HistoryBuyCloudStorageViewModel>() {

    @Inject
    lateinit var appNavigation: AppNavigation

    @Inject
    lateinit var rxPreferences: RxPreferences

    private val viewModel: HistoryBuyCloudStorageViewModel by viewModels()

    override val layoutId: Int
        get() = R.layout.fragment_history_buy_cloud_storage

    override fun getVM(): HistoryBuyCloudStorageViewModel = viewModel

    private lateinit var adapter: HistoryBuyCloudAdapter


    private var typeScreen = 1

    override fun initView() {
        super.initView()

        arguments?.let {

            if (it.containsKey(Define.BUNDLE_KEY.PARAM_TYPE_SCREEN_HISTORY_CLOUD)) {
                typeScreen = it.getInt(Define.BUNDLE_KEY.PARAM_TYPE_SCREEN_HISTORY_CLOUD)
            }
            when (typeScreen) {
                TYPE_SCREEN_CAMERA -> {
                    if (it.containsKey(Define.BUNDLE_KEY.PARAM_ID)) {
                        viewModel.deviceId = it.getString(Define.BUNDLE_KEY.PARAM_ID) ?: ""
                    }
                    if (it.containsKey(Define.BUNDLE_KEY.PARAM_NAME)) {
                        viewModel.deviceName = it.getString(Define.BUNDLE_KEY.PARAM_NAME) ?: ""
                    }
                    if (it.containsKey(Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL)) {
                        viewModel.deviceSerial =
                            it.getString(Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL) ?: ""
                    }
                }
            }

        }

        adapter = HistoryBuyCloudAdapter(rxPreferences.getUserId(), clickItem = {
            val bundle = bundleOf().apply {
                putParcelable(Define.BUNDLE_KEY.PARAM_DATA_DETAIL_HISTORY_CLOUD, it)
                putString(Define.BUNDLE_KEY.PARAM_ID, viewModel.deviceId)
            }
            appNavigation.openDetailHistoryCloudStorage(bundle)
        })
        binding.rcvPackage.adapter = adapter
        adapter.addLoadStateListener { loadState ->
            if (loadState.refresh is LoadState.Loading) {
                // Hiển thị trạng thái loading (đang tải dữ liệu)
                showHideLoading(true)

            } else if (loadState.refresh is LoadState.Error) {
                // Hiển thị trạng thái lỗi
                showHideLoading(false)
                if (adapter.itemCount < 1) {
                    binding.imvNotData.visible()
                    binding.tvNotData.visible()
                    binding.rcvPackage.gone()
                } else {
                    binding.imvNotData.gone()
                    binding.tvNotData.gone()
                    binding.rcvPackage.visible()
                }
            } else if (loadState.refresh is LoadState.NotLoading) {
                // Hiển thị trạng thái thành công (hoàn thành tải dữ liệu)
                showHideLoading(false)
                if (adapter.itemCount < 1) {
                    binding.imvNotData.visible()
                    binding.tvNotData.visible()
                    binding.rcvPackage.gone()
                } else {
                    binding.imvNotData.gone()
                    binding.tvNotData.gone()
                    binding.rcvPackage.visible()
                }
            }
        }
    }


    override fun bindingStateView() {
        super.bindingStateView()
        when (typeScreen) {
            TYPE_SCREEN_CAMERA -> {
                lifecycleScope.launch {
                    viewModel.pagedListCloudHistory.collect { pagingData ->
                        adapter.submitData(pagingData)

                    }
                }
            }

            TYPE_SCREEN_ACCOUNT -> {
                lifecycleScope.launch {
                    viewModel.pagedListCloudAccountHistory.collect { pagingData ->
                        adapter.submitData(pagingData)

                    }
                }
            }
        }


    }

    override fun setOnClick() {
        super.setOnClick()
        binding.toolbar.setOnLeftClickListener {
            appNavigation.navigateUp()
        }

    }


}