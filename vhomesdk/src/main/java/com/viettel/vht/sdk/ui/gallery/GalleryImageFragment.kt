package com.viettel.vht.sdk.ui.gallery

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.vht.sdkcore.base.BaseFragment
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.dialog.DialogType
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentGalleryImageBinding
import com.viettel.vht.sdk.navigation.AppNavigation
import com.viettel.vht.sdk.utils.DebugConfig
import com.viettel.vht.sdk.utils.invisible
import com.viettel.vht.sdk.utils.showCustomNotificationDialog
import com.viettel.vht.sdk.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class GalleryImageFragment :
    BaseFragment<FragmentGalleryImageBinding, GalleryViewModel>() {
    private val TAG: String = GalleryImageFragment::class.java.simpleName
    private val viewModel: GalleryViewModel by viewModels()
    private val adapter: GalleryAdapter by lazy { GalleryAdapter(mutableListOf()) }
    @Inject
    lateinit var appNavigation: AppNavigation
    override val layoutId: Int
        get() = R.layout.fragment_gallery_image

    override fun getVM(): GalleryViewModel = viewModel

    override fun initView() {
        super.initView()
        binding.toolbar.setOnLeftClickListener {
            findNavController().navigateUp()
        }
        binding.btnUncheck.setOnClickListener {
            binding.btnUncheck.invisible()
            binding.tvDeleteAll.invisible()
            binding.tvDelete.invisible()
            binding.tvGuide.visible()
            adapter.doUnchecked()
        }
        binding.tvDeleteAll.setOnClickListener {
            adapter.selectAll()
        }
        binding.tvDelete.setOnClickListener {
            showCustomNotificationDialog(
                title = getString(com.vht.sdkcore.R.string.confirm_delete),
                type = DialogType.CONFIRM,
                message = com.vht.sdkcore.R.string.confirm_delete_content,
                titleBtnConfirm = com.vht.sdkcore.R.string.dialog_button_ok,
                negativeTitle = com.vht.sdkcore.R.string.dialog_button_cancel,
            ) {
                binding.btnUncheck.invisible()
                binding.tvDeleteAll.invisible()
                binding.tvDelete.invisible()
                binding.tvGuide.visible()
                val list =  adapter.getListChecked()
                DebugConfig.logd(TAG, "initView: $list")
                lifecycleScope.launch {
                    withContext(Dispatchers.IO){
                        list.forEach {
                            File(it).delete()
                        }
                    }
                    viewModel.getListGallery()
                }
            }
        }
        binding.rcvGallery.adapter = adapter
        adapter.actionClickItem= {
            val bundle = Bundle()
            bundle.putString(Define.BUNDLE_KEY.PARAM_VALUE, it)
            appNavigation.openDetailGalleryFragment(bundle)
        }
        adapter.actionCheckedMode= {
            binding.btnUncheck.visible()
            binding.tvDeleteAll.visible()
            binding.tvDelete.visible()
            binding.tvGuide.invisible()
        }
        try {
            viewModel.getListGallery()
        }catch (e:Exception){
            DebugConfig.loge(message = "Error: e: ${e.message}")
        }

        viewModel.listGalleryLiveData.observe(viewLifecycleOwner) {
            adapter.addDatas(it.toMutableList())
        }

    }
}