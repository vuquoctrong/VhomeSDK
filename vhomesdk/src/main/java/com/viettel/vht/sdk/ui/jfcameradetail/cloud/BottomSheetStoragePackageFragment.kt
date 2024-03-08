package com.viettel.vht.sdk.ui.jfcameradetail.cloud

import com.vht.sdkcore.base.BaseBottomSheetFragment
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentBottomSheetStoragePackageBinding
import com.viettel.vht.sdk.ui.jfcameradetail.cloud.adapter.StoragePackageAdapter
import com.viettel.vht.sdk.utils.BaseBottomSheetFragmentNotModel

class BottomSheetStoragePackageFragment constructor(
    val listData: List<StoragePackageAdapter.Data>,
    val callBack: (Int) -> Unit
) : BaseBottomSheetFragmentNotModel<FragmentBottomSheetStoragePackageBinding>() {
    override val layoutId: Int
        get() = R.layout.fragment_bottom_sheet_storage_package

    private lateinit var storagePackageAdapter: StoragePackageAdapter

    override fun initView() {
        storagePackageAdapter = StoragePackageAdapter {
            callBack(it)
            dismiss()
        }
        storagePackageAdapter.submitList(listData)
        binding.rcvData.adapter = storagePackageAdapter
    }

    override fun initControl() {
        //TODO
    }
}