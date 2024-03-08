package com.viettel.vht.sdk.ui.jfcameradetail.cloud.repository

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.viettel.vht.sdk.model.camera_cloud.CloudStorageRegistered
import com.viettel.vht.sdk.model.camera_cloud.SearchCloudRequest
import com.viettel.vht.sdk.network.repository.CloudRepository
import kotlinx.coroutines.CoroutineScope

class JFCloudEventDataSourceFactory(
    private val request: SearchCloudRequest,
    private val scope: CoroutineScope,
    private val repository: CloudRepository,
) : DataSource.Factory<Int, CloudStorageRegistered>() {

    val source = MutableLiveData<JFCloudEventPageKeyedDataSource>()

    override fun create(): DataSource<Int, CloudStorageRegistered> {
        val source = JFCloudEventPageKeyedDataSource(request, scope, repository)
        this.source.postValue(source)
        return source
    }
}