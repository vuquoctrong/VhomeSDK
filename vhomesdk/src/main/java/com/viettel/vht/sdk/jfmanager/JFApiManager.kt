package com.viettel.vht.sdk.jfmanager

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.lib.FunSDK
import com.utils.MacroUtils
import com.utils.ParseUrlUtils
import com.viettel.vht.sdk.model.share.ApiJFResponse
import com.viettel.vht.sdk.model.share.JFItemReceive
import com.viettel.vht.sdk.model.share.JFItemShare
import okhttp3.ResponseBody
import java.net.URLDecoder
import java.util.HashMap

object JFApiManager {

    val TAG = JFApiManager::class.simpleName.toString()

    private const val API_JF_CODE_SUCCESS = 2000
    var version = "v1"
    var token = ""
    var uuid = ""
    var appKey = ""
    var appSecret = ""
    var strMovedCard = ""
    var movedCard: Int = 0

    fun initJFApiManager(context: Context) {

        //native 方法获取
        val mAccessToken = FunSDK.SysGetCurLoginParams()
        val loginParamsMap: HashMap<String, String> = ParseUrlUtils.parser(mAccessToken)
        if (loginParamsMap.containsKey("accessToken")) {
            token = loginParamsMap["accessToken"] ?: ""
        }

        uuid = MacroUtils.getValue(context, "APP_UUID")
        appKey = MacroUtils.getValue(context, "APP_KEY")
        appSecret = MacroUtils.getValue(context, "APP_SECRET")
        strMovedCard = MacroUtils.getValue(context, "APP_MOVECARD")

        try {
            movedCard = strMovedCard.toInt()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun handleResponseJFCheck(responseBody: ResponseBody): String {
        val jsonResult = URLDecoder.decode(responseBody.string(), "UTF-8")
        // covert json to object
        val searchObj = Gson().fromJson(
            jsonResult,
            ApiJFResponse::class.java
        ) as ApiJFResponse<ArrayList<LinkedTreeMap<String, String>>>?
        Log.d("CameraRepository", "searchObj = " + Gson().toJson(searchObj))

        searchObj?.dataResponse?.let {
            val dataAccount = it[0]
            if (searchObj.code != API_JF_CODE_SUCCESS || it.isEmpty() || dataAccount.get("id")
                    .isNullOrEmpty()
            ) {
                return ""
            } else {
                return dataAccount.get("id") ?: ""
            }
        }
        return ""
    }

    fun handleResponseJFShareCamera(responseBody: ResponseBody): Boolean{
        val jsonResult = URLDecoder.decode(responseBody.string(), "UTF-8")
//        val jsonObject = JSON.parseObject(jsonResult)

        Log.d("ShareCameraJFRequest","jsonResult = $jsonResult")

        // covert json to object
        val shareObj = Gson().fromJson(
            jsonResult,
            ApiJFResponse::class.java
        ) as ApiJFResponse<Any>?
        Log.d("CameraRepository", "searchObj = " + Gson().toJson(shareObj))

        return shareObj != null && shareObj.code == API_JF_CODE_SUCCESS && shareObj.dataResponse != null
    }

    fun handleResponseJFListUsers(responseBody: ResponseBody): ArrayList<JFItemShare> {
        val jsonResult = URLDecoder.decode(responseBody.string(), "UTF-8")
        // covert json to object
        val searchObj = Gson().fromJson(
            jsonResult,
            ApiJFResponse::class.java
        ) as ApiJFResponse<ArrayList<JFItemShare>>?
        Log.d("CameraRepository", "searchObj = " + Gson().toJson(searchObj))

        searchObj?.dataResponse?.let {
            if (searchObj.code != API_JF_CODE_SUCCESS || it.isEmpty()) {
                return arrayListOf()
            } else {
                return it
            }
        }
        return arrayListOf()
    }

    fun handleResponseJFListReceive(responseBody: ResponseBody): ArrayList<JFItemReceive> {
        val jsonResult = URLDecoder.decode(responseBody.string(), "UTF-8")
        // covert json to object
        val searchObj = Gson().fromJson(
            jsonResult,
            ApiJFResponse::class.java
        ) as ApiJFResponse<ArrayList<JFItemReceive>>?
        Log.d("CameraRepository", "searchObj = " + Gson().toJson(searchObj))

        searchObj?.dataResponse?.let {
            if (searchObj.code != API_JF_CODE_SUCCESS || it.isEmpty()) {
                return arrayListOf()
            } else {
                return it
            }
        }
        return arrayListOf()
    }

}