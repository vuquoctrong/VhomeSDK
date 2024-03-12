package com.viettel.vht.sdk.network.repository

import com.vht.sdkcore.pref.RxPreferences
import com.viettel.vht.sdk.model.camera_cloud.CloudStorageRegistered
import com.viettel.vht.sdk.model.camera_cloud.CloudStorageRegisteredResponse
import com.viettel.vht.sdk.network.ApiInterface
import com.viettel.vht.sdk.network.Result
import com.viettel.vht.sdk.utils.DebugConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import kotlin.math.ceil

class CloudRepository @Inject constructor(
    private val apiInterface: ApiInterface,
    private val rxPreferences: RxPreferences,
) {

    fun listDayCloudAlsoSored(listCloudPackage: List<CloudStorageRegistered>?):List<Date>{
        listCloudPackage?.let {  listClouds ->
            if(listClouds.isNotEmpty()){
                //Lấy những gói khác gói chưa kích hoạt
                val datas = listClouds.filter { it.serviceStatus != 2 }
                if(datas.size > 1){
                    val listCurrentDate =  if(datas[0].serviceStatus == 0){
                        getListDatePastDayCloudExpired(datas[0])
                    }else{
                        getListDateCurrentCloudDayWhenAlsoSored(datas[0])
                    }
                    val listFutureDate = if(datas[1].serviceStatus == 0){
                        getListDatePastDayCloudExpired(datas[1])
                    }else{
                        getListDateCurrentCloudDayWhenAlsoSored(datas[1])
                    }

                    return (listCurrentDate+listFutureDate).distinct()
                }else{
                    return if(datas[0].serviceStatus == 0){
                        getListDatePastDayCloudExpired(datas[0])
                    }else{
                        getListDateCurrentCloudDayWhenAlsoSored(datas[0])
                    }
                }

            }
        }

        return listOf()
    }

    private fun getDaySaveCloud(data: String): Int{
        val words = data.split(" ")
        for (word in words) {
            if (word.toIntOrNull() != null) {
                return word.toInt()
            }
        }
        return 0
    }

    private fun getListDatePastDayCloudExpired(data: CloudStorageRegistered):List<Date>{
        val daySave = getDaySaveCloud(data.infoService?.vi?:"")
        var daysDifference = 0.0
        val endTimestamp = data.endDateTime?.times((1000))?:0L   // Thời điểm kết thúc (timestamp)

        val currentDate = Date()
        currentDate.time// Lấy ngày hiện tại
        if(currentDate.time > endTimestamp){
            val timeDifference = currentDate.time - endTimestamp
            daysDifference = timeDifference.toDouble().div(24 * 60 * 60 * 1000)

        }
        return if(daySave <= daysDifference){
            listOf()
        }else{
            val dateList = mutableListOf<Date>()
            val total = (daySave.toDouble() - daysDifference)
            for (i in 0..ceil(total).toInt()) {
                val calendar = Calendar.getInstance()
                calendar.time = Date(endTimestamp)
                calendar.add(Calendar.DATE, -i)
                dateList.add(calendar.time)
            }
            dateList
        }

    }

    private fun getListDateCurrentCloudDayWhenAlsoSored( data: CloudStorageRegistered):List<Date>{
        val daySave = getDaySaveCloud(data.infoService?.vi?:"")
        var daysDifference = 0
        val startTimestamp = data.startDateTime?.times(1000)?:0L // Thời điểm bắt đầu (timestamp)
        val endTimestamp = data.endDateTime?.times((1000))?:0L   // Thời điểm kết thúc (timestamp)

        val currentDate = Date()
        currentDate.time// Lấy ngày hiện tại
        if(currentDate.time > startTimestamp){
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = startTimestamp
            calendar.set(Calendar.HOUR_OF_DAY,0)
            calendar.set(Calendar.MINUTE,0)
            calendar.set(Calendar.SECOND,1)
            val timeDifference = currentDate.time - calendar.timeInMillis
            daysDifference = timeDifference.div(24 * 60 * 60 * 1000).toInt()

        }
        if(daySave >= daysDifference){
            val dateList = mutableListOf<Date>()
            for (i in 0..daysDifference) {
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DATE, -i)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy")
                dateList.add(calendar.time)
                DebugConfig.log("Trong","${dateFormat.format(calendar.time)}")
            }
            return dateList
        }else{
            val dateList = mutableListOf<Date>()
            for (i in 0..daySave) {
                val calendar = Calendar.getInstance()
                // calendar.time = Date(endTimestamp)
                calendar.add(Calendar.DATE, -i)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy")
                dateList.add(calendar.time)
                DebugConfig.log("Trong","${dateFormat.format(calendar.time)}")
            }
            return dateList
        }
    }
    suspend fun checkRegistedCloud(serial: String) = withContext(Dispatchers.IO) {
        try {
            val response = apiInterface.getListCloudStorageRegistered(serial = serial)
            val isRegisted = response.data?.data?.isNotEmpty()
            isRegisted
        } catch (e: Exception) {
            false
        }
    }
    fun getListCloudStorageRegistered(serial: String) = flow {
        try {
            emit(Result.Loading)
            val response = apiInterface.getListCloudStorageRegistered(serial = serial)
            if (response.data != null) {
                emit(Result.Success(response.data))
            } else throw Exception()
        } catch (e: Exception) {
            emit(Result.Error())
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getListCloudStorageRegisteredAccount(
        limit: Int = 100,
        offset: Int = 0,
    ): CloudStorageRegisteredResponse? = withContext(Dispatchers.IO) {
        try {
            val response = apiInterface.getListCloudStorageRegisteredAccount(
                limit = limit,
                offset = offset,
                order = 0,
                userId = rxPreferences.getUserId()
            )
            if (response.data != null) {
                return@withContext  response.data
            } else return@withContext null
        } catch (e: Exception) {
            return@withContext null
        }
    }

    suspend fun getListHistoryBuyCloudStorageRegistered(
        serial: String,
        limit: Int = 100,
        offset: Int = 0,
        skipExpired: Boolean = false
    ): CloudStorageRegisteredResponse? = withContext(Dispatchers.IO) {
        try {
            val response = apiInterface.getListCloudStorageRegistered(
                serial = serial,
                limit = limit,
                offset = offset,
                skipExpired = skipExpired
            )
            if (response.data != null) {
                return@withContext  response.data
            } else return@withContext null
        } catch (e: Exception) {
            return@withContext null
        }
    }

    suspend fun getCloudStorageRegistered(serial: String, offset: Int = 0, limit: Int = 100) =
        withContext(Dispatchers.IO) {
            val response = apiInterface.getListCloudStorageRegistered(
                serial = serial,
                limit = limit,
                offset = offset,
                order = 1,
                skipExpired = true
            )
            response
        }

    fun getListStatusCloudCamera() = flow {
        try {
            emit(Result.Loading)
            val response = apiInterface.getListStatusCloudCamera()
            if (response.code == 200) {
                emit(Result.Success(response.data))
            } else{
                emit(Result.Error())
            }
        } catch (e: Exception) {
            emit(Result.Error(error = e.message?:"".toString()))
        }
    }.flowOn(Dispatchers.IO)

    fun getListCloudStoragePackage(vendor: String) = flow {
        try {
            emit(Result.Loading)
            val response = apiInterface.getListCloudStoragePackage(vendor)
            if (response.data != null) {
                emit(Result.Success(response.data))
            } else throw Exception()
        } catch (e: Exception) {
            emit(Result.Error())
        }
    }.flowOn(Dispatchers.IO)

    fun getStatusAutoChargingCamera(serial: String, method: String ="VTPAY-WALLET") = flow {
        try {
            emit(Result.Loading)
            val response = apiInterface.getStatusAutoChargingCamera(serial = serial)
            if (response.code == 200) {
                emit(Result.Success(response.data))
            } else{
                emit(Result.Error())
            }
        } catch (e: Exception) {
            emit(Result.Error(error = e.message?:"".toString()))
        }
    }.flowOn(Dispatchers.IO)

}