package com.viettel.vht.sdk.ui.gallery

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.vht.sdkcore.base.BaseViewModel
import com.vht.sdkcore.utils.Utils.Companion.getFileGallery
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(private val application: Application
) : BaseViewModel() {

    var listGalleryLiveData = MutableLiveData<List<ItemGallery>>()

    fun getListGallery() {
        viewModelScope.launch {
            val listFile = withContext(Dispatchers.IO) {
                getFileGallery(application).map {
                    File(it)
                }.filter { it.lastModified()>946659601 }
            }
            Log.d("TAG", "getListGallery listFile: $listFile")
            val data = listFile.groupBy {
                SimpleDateFormat("dd/MM/yyyy").format(Date(it.lastModified()))
            }
            Log.d("TAG", "getListGallery data: $data")
            listGalleryLiveData.value = data.values.map {
                ItemGallery(it.sortedByDescending { it.lastModified() }.map {
                    ItemDetailGallery(it.absolutePath,false,false)
                }, it.first().lastModified())
            }.sortedByDescending {
                it.dateTime
            }
        }
    }
}