package com.viettel.vht.sdk.ui.jfcameradetail.cloud.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.viettel.vht.sdk.model.camera_cloud.CloudStorageRegistered
import com.viettel.vht.sdk.network.repository.CloudRepository

class CloudAccountPagingSource (
    private val cloudRepository: CloudRepository,
    val serial: String
) : PagingSource<Int, CloudStorageRegistered>() {
    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, CloudStorageRegistered> {
        return try {
            val nextPageNumber = params.key ?: 0
            val response = cloudRepository.getListCloudStorageRegisteredAccount( limit = 10, offset =  nextPageNumber*10)
            val dataResponse = response?.data?: listOf()
            LoadResult.Page(
                data = dataResponse,
                prevKey = null,
                nextKey = if(dataResponse.isEmpty()) null else nextPageNumber + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, CloudStorageRegistered>): Int? {

        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}