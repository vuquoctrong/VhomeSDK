package com.viettel.vht.sdk.ui.jfcameradetail.cloud.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.vht.sdkcore.utils.Define

data class NetworkListing<T : Any>(
    val pagedList: LiveData<PagedList<T>>,
    val requestPage: LiveData<RequestPage>,
    val networkState: LiveData<Define.LoadingState>,
    val logState: LiveData<String> = MutableLiveData(),
)

data class RequestPage(val currentPage: Int = 0, val totalItem: Int = 0, val pageSize: Int = 0)

