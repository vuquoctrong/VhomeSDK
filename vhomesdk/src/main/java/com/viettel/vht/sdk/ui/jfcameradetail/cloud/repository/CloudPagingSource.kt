package com.viettel.vht.sdk.ui.jfcameradetail.cloud.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.viettel.vht.sdk.model.camera_cloud.CloudStorageRegistered
import com.viettel.vht.sdk.network.repository.CloudRepository

class CloudPagingSource (
    private val cloudRepository: CloudRepository,
    val serial: String
) : PagingSource<Int, CloudStorageRegistered>() {
    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, CloudStorageRegistered> {
        try {
            // Start refresh at page 1 if undefined.
            val nextPageNumber = params.key ?: 0
            val response = cloudRepository.getListHistoryBuyCloudStorageRegistered(serial = serial, limit = 10, offset =  nextPageNumber*10, skipExpired = false)
            val dataResponse = response?.data?: listOf()
            return LoadResult.Page(
                data = dataResponse,
                prevKey = null, // Only paging forward.
                nextKey = if(dataResponse.isEmpty()) null else nextPageNumber + 1
            )
        } catch (e: Exception) {
            // Handle errors in this block and return LoadResult.Error for
            // expected errors (such as a network failure).
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, CloudStorageRegistered>): Int? {

        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}