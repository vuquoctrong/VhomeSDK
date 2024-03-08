package com.viettel.vht.sdk.ui.jfcameradetail.cloud.repository

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.executeWithRetry
import com.viettel.vht.sdk.model.camera_cloud.CloudStorageRegistered
import com.viettel.vht.sdk.model.camera_cloud.SearchCloudRequest
import com.viettel.vht.sdk.network.repository.CloudRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class JFCloudEventPageKeyedDataSource(
    private val request: SearchCloudRequest,
    private val scope: CoroutineScope,
    private val repository: CloudRepository,
) : PageKeyedDataSource<Int, CloudStorageRegistered>() {

    var currentPage = 0
    var offset = 0
    val handlerException = CoroutineExceptionHandler { _, throwable ->
        run {
            Timber.d("Error:  $throwable")
        }
    }

    val networkStateLiveData = MutableLiveData<Define.LoadingState>()

    val requestPageLiveData = MutableLiveData<RequestPage>()

    override fun loadAfter(
        params: LoadParams<Int>,
        callback: LoadCallback<Int, CloudStorageRegistered>
    ) {
        currentPage++
        request.offset = offset
        if (request.offset != -1) {
            scope.launch(Dispatchers.IO + handlerException) {
                executeWithRetry {
                    val response = repository.getCloudStorageRegistered(request.serial, request.offset, request.limit)
                    if (response.code != "200") {
                        throw Exception("Search cloud with error message = ${response.msg}")
                    } else {
                        val list = response.data?.data

                        if (list != null && list.isNotEmpty()) {
                            offset += list.size
                            if (response.data?.total!! <= offset) {
                                offset = -1
                            }
                            callback.onResult(list, currentPage)
                        } else {
                            offset = -1
                        }
                        response.code != "200"
                    }
                }

            }
        }
    }

    override fun loadBefore(
        params: LoadParams<Int>,
        callback: LoadCallback<Int, CloudStorageRegistered>
    ) {

    }

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, CloudStorageRegistered>
    ) {
        request.offset = currentPage
        scope.launch(Dispatchers.IO + handlerException) {
            executeWithRetry {
                val response = repository.getCloudStorageRegistered(request.serial, request.offset, request.limit)
                if (response.code != "200") {
                    throw Exception("Search cloud with error message = ${response.msg}")
                } else {
                    val list = response.data?.data
                    if (list != null && list.isNotEmpty()) {
                        offset = response.data?.data?.size ?: 0
                        if (response.data?.total!! <= request.limit) {
                            offset = -1
                        }
                        callback.onResult(
                            list,
                            0,
                            response.data?.total ?: 0,
                            currentPage,
                            currentPage
                        )
                    } else {
                        offset = -1
                    }
                    response.code != "200"
                }
            }

        }
    }
}