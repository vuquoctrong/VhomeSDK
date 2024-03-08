package com.viettel.vht.sdk.jfmanager

import android.content.Context
import android.graphics.Bitmap
import android.os.Message
import android.util.Log
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONException
import com.basic.G
import com.google.gson.Gson
import com.lib.*
import com.lib.Mps.MpsClient
import com.lib.Mps.SBatchDevAlarmMsgQueryReqParams
import com.lib.Mps.SMCInitInfo
import com.lib.Mps.XPMS_SEARCH_ALARMINFO_REQ
import com.lib.cloud.CloudDirectory
import com.lib.sdk.bean.*
import com.lib.sdk.bean.HandleConfigData.getFullName
import com.lib.sdk.bean.JsonConfig.*
import com.lib.sdk.bean.alarm.AlarmInfo
import com.lib.sdk.bean.cloudmedia.CloudMediaFileInfoBean
import com.lib.sdk.bean.preset.ConfigGetPreset
import com.lib.sdk.bean.tour.PTZTourBean
import com.lib.sdk.struct.H264_DVR_FILE_DATA
import com.lib.sdk.struct.SDBDeviceInfo
import com.manager.XMFunSDKManager
import com.manager.account.BaseAccountManager
import com.manager.account.BaseAccountManager.OnAccountManagerListener
import com.manager.account.XMAccountManager
import com.manager.db.Define
import com.manager.db.DevDataCenter
import com.manager.device.DeviceManager
import com.manager.device.DeviceManager.OnDevManagerListener
import com.manager.device.config.DevConfigInfo
import com.manager.device.media.playback.DevRecordManager
import com.manager.device.media.playback.RecordManager
import com.manager.push.XMPushManager
import com.utils.XUtils
import com.vht.sdkcore.utils.Constants.EventKey.EVENT_SYSTEM_CHANGE_INFORMATION_CAMERA_JF
import com.vht.sdkcore.utils.Utils
import com.vht.sdkcore.utils.eventbus.RxEvent
import com.viettel.vht.sdk.model.DeviceDataResponse
import com.viettel.vht.sdk.utils.DebugConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.json.JSONArray
import org.json.JSONObject
import org.modelmapper.ModelMapper

import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


object JFCameraManager {
    val TAG = JFCameraManager::class.simpleName.toString()
    const val APP_UUID = "6391d1a360b238987546d15b"
    const val APP_KEY = "b6461fefb0424b66f9085623443d0847"
    const val APP_SECRET = "503bc0203f094edcb8a1e88f5365acd7"
    const val APP_MOVEDCARD = 4
    const val TIME_OUT = 5000

    private lateinit var xmFunSDKManager: XMFunSDKManager
    private lateinit var manager: XMAccountManager

    var cameraWatching = ""

    fun initPushGoogle(context: Context, callback: (Boolean) -> Unit) {
//        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { instanceIdResult: InstanceIdResult? ->
//            if (instanceIdResult != null) {
//                val token = instanceIdResult.token
//                Timber.tag(TAG).d("Refreshed token: $token")
//
//                val info = SMCInitInfo()
//                G.SetValue(info.st_0_user, XMAccountManager.getInstance().accountName)
//                G.SetValue(info.st_1_password, XMAccountManager.getInstance().password)
//                G.SetValue(info.st_2_token, token)
//                G.SetValue(
//                    info.st_5_appType, "GoogleV2:" + context.packageName
//                )
//                initFunSDKPush(context, info, PUSH_TYPE_GOOGLE, callback)
//            }
//        }.addOnFailureListener { e: Exception? -> }.addOnCanceledListener {}
//            .addOnCompleteListener { task: Task<InstanceIdResult> ->
//                Log.v(
//                    TAG, "task result : " + task.result.token
//                )
//            }
    }

    fun linkAlarm(context: Context, devId: String, callback: (Boolean) -> Unit) {
        initPushGoogle(context) {
            if (it) {
                val manager = XMPushManager(object : XMPushManager.OnXMPushLinkListener {
                    override fun onPushInit(p0: Int, p1: Int) {
                    }

                    override fun onPushLink(p0: Int, p1: String?, p2: Int, p3: Int) {
                        callback.invoke(p3 >= 0)
                    }

                    override fun onPushUnLink(p0: Int, p1: String?, p2: Int, p3: Int) {
                    }

                    override fun onIsPushLinkedFromServer(p0: Int, p1: String?, p2: Boolean) {

                    }

                    override fun onAlarmInfo(p0: Int, p1: String?, p2: Message?, p3: MsgContent?) {
                        Log.d(TAG, "onAlarmInfo: $p2")
                    }

                    override fun onLinkDisconnect(p0: Int, p1: String?) {
                    }

                    override fun onWeChatState(p0: String?, p1: Int, p2: Int) {
                    }

                    override fun onThirdPushState(p0: String?, p1: Int, p2: Int, p3: Int) {
                    }

                    override fun onAllUnLinkResult() {
                    }

                    override fun onFunSDKResult(p0: Message?, p1: MsgContent?) {
                    }
                })
                manager.linkAlarm(devId, 0)
            }
        }
    }

    fun unLinkAlarm(context: Context, devId: String, callback: (Boolean) -> Unit) {
        initPushGoogle(context) {
            if (it) {
                val manager = XMPushManager(object : XMPushManager.OnXMPushLinkListener {
                    override fun onPushInit(p0: Int, p1: Int) {
                    }

                    override fun onPushLink(p0: Int, p1: String?, p2: Int, p3: Int) {

                    }

                    override fun onPushUnLink(p0: Int, p1: String?, p2: Int, p3: Int) {
                        callback.invoke(p3 >= 0)
                    }

                    override fun onIsPushLinkedFromServer(p0: Int, p1: String?, p2: Boolean) {

                    }

                    override fun onAlarmInfo(p0: Int, p1: String?, p2: Message?, p3: MsgContent?) {
                    }

                    override fun onLinkDisconnect(p0: Int, p1: String?) {
                    }

                    override fun onWeChatState(p0: String?, p1: Int, p2: Int) {
                    }

                    override fun onThirdPushState(p0: String?, p1: Int, p2: Int, p3: Int) {
                    }

                    override fun onAllUnLinkResult() {
                    }

                    override fun onFunSDKResult(p0: Message?, p1: MsgContent?) {
                    }
                })
                manager.unLinkAlarm(devId, 0)
            }
        }
    }

    fun isAlarmLinked(devId: String, callback: (Boolean) -> Unit) {
        callback.invoke(MpsClient.GetLinkState(devId) == SDKCONST.Switch.Open)
    }

    fun initFunSDKPush(
        context: Context, info: SMCInitInfo, pushType: Int, callback: (Boolean) -> Unit,
    ) {
        val token: String = XUtils.getPushToken(context)
        if (!StringUtils.isStringNULL(token)) {
            val tokens = StringBuffer(G.ToString(info.st_2_token))
            tokens.append("&&")
            tokens.append(token)
            G.SetValue(info.st_2_token, tokens.toString())
            val appTypes = StringBuffer(G.ToString(info.st_5_appType))
            appTypes.append("&&")
            appTypes.append("Android")
            G.SetValue(info.st_5_appType, appTypes.toString())
        }
        val mXMPushManager = XMPushManager(object : XMPushManager.OnXMPushLinkListener {
            override fun onPushInit(p0: Int, p1: Int) {
                callback.invoke(p1 >= 0)
            }

            override fun onPushLink(p0: Int, p1: String?, p2: Int, p3: Int) {
            }

            override fun onPushUnLink(p0: Int, p1: String?, p2: Int, p3: Int) {
            }

            override fun onIsPushLinkedFromServer(p0: Int, p1: String?, p2: Boolean) {
            }

            override fun onAlarmInfo(p0: Int, p1: String?, p2: Message?, p3: MsgContent?) {
            }

            override fun onLinkDisconnect(p0: Int, p1: String?) {
            }

            override fun onWeChatState(p0: String?, p1: Int, p2: Int) {
            }

            override fun onThirdPushState(p0: String?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onAllUnLinkResult() {
            }

            override fun onFunSDKResult(p0: Message?, p1: MsgContent?) {
            }
        })
        mXMPushManager.initFunSDKPush(context, info, pushType)
    }


    fun init(context: Context) {
        Log.d(TAG, "Init JFCameraManager")
        xmFunSDKManager = XMFunSDKManager.getInstance(0, "", "p2p.viettelcamera.vn", 8765)
        xmFunSDKManager.initXMCloudPlatform(
            context, true
        )

        FunSDK.SysSetServerIPPort("STATUS_DSS_SERVER", "https://status-dss.viettelcamera.vn", 7601)
        FunSDK.SysSetServerIPPort("STATUS_TPS_SERVER", "https://status-tps.viettelcamera.vn", 7602)
        FunSDK.SysSetServerIPPort("STATUS_P2P_SERVER", "https://status-p2p.viettelcamera.vn", 7603)
        FunSDK.SysSetServerIPPort("STATUS_CSS_SERVER", "https://status-css.viettelcamera.vn", 7604)
        FunSDK.SysSetServerIPPort("STATUS_RPS_SERVER", "https://status-rps.viettelcamera.vn", 7605)
        FunSDK.SysSetServerIPPort("CAPS_SERVER", "https://caps.viettelcamera.vn", 443)
        FunSDK.SysSetServerIPPort("CONFIG_SERVER", "https://pub-cfg.viettelcamera.vn", 8186)
        FunSDK.SysSetServerIPPort("UPGRADE_SERVER", "https://upgrade.viettelcamera.vn", 9083)
        FunSDK.SysSetServerIPPort("STATUS_IDR_SERVER", "https://status-wps.viettelcamera.vn", 7606)

        FunSDK.SysSetServerIPPort("PMS_ALM_SERVER", "https://access-pms.viettelcamera.vn", 6502)
        FunSDK.SysSetServerIPPort("ALC_ALM_SERVER", "https://access-alc.viettelcamera.vn", 6503)
        FunSDK.SysSetServerIPPort("ACCESS_CSS_SERVER", "https://access-css.viettelcamera.vn", 6615)

        FunSDK.SysSetServerIPPort("MI_SERVER", "https://rs.viettelcamera.vn", 443)

        if(DebugConfig.SHOW_DEBUG_LOG){
            xmFunSDKManager.initLog()
        }


        /**
         * 低功耗设备：包括 门铃、门锁等，需要调用此方法否则可能无法登录设备，其他设备无需调用
         * Low-power devices: including doorbells, door locks, etc., you need to call this method,
         * otherwise you may not be able to log in to the device, and other devices do not need to call
         */
        // FunSDK.SetFunIntAttr(EFUN_ATTR.SUP_RPS_VIDEO_DEFAULT, SDKCONST.Switch.Open)

    }

    var isLogout = false
    const val EVENT_LOGOUT_CAMERA_JF ="EVENT_LOGOUT_CAMERA_JF"

    fun logout() {
        val mXMPushManager = XMPushManager(object : XMPushManager.OnXMPushLinkListener {
            override fun onPushInit(p0: Int, p1: Int) {

            }

            override fun onPushLink(p0: Int, p1: String?, p2: Int, p3: Int) {
            }

            override fun onPushUnLink(p0: Int, p1: String?, p2: Int, p3: Int) {
            }

            override fun onIsPushLinkedFromServer(p0: Int, p1: String?, p2: Boolean) {
            }

            override fun onAlarmInfo(p0: Int, p1: String?, p2: Message?, p3: MsgContent?) {
            }

            override fun onLinkDisconnect(p0: Int, p1: String?) {
            }

            override fun onWeChatState(p0: String?, p1: Int, p2: Int) {
            }

            override fun onThirdPushState(p0: String?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onAllUnLinkResult() {
                Timber.d("UnLinkAllAlarm Success")
            }

            override fun onFunSDKResult(msg: Message?, ex: MsgContent?) {
                Timber.d("unLinkAllAlarm: ${msg?.what}, ${msg?.arg1}, ${ex?.str}")
            }
        })
        mXMPushManager.unLinkAllAlarm()
        XMAccountManager.getInstance().logout()
        isLoginFailed = true
        isLogout = true
        isUpdateDevStateCompleted = false
        listDeviceShared = null
        EventBus.getDefault().post(RxEvent<String>(EVENT_LOGOUT_CAMERA_JF))
    }

    var alarmId = ""
    var isAdding = false
    var isLoginFailed = true
    var isUpdateDevStateCompleted = false
    var retryLogin = 0
    fun loginAccountJF(user: String, password: String, callback: () -> Unit = {}) {
        Log.d(TAG, "Loading loginAccountJF: user: $user, password: $password")
        XMAccountManager.getInstance()
            ?.login(user, password, Define.LOGIN_BY_INTERNET, object : OnAccountManagerListener {
                override fun onSuccess(p0: Int) {
                    Log.d(TAG, "Login loginAccountJF Success $p0")
                    XMAccountManager.getInstance().setLoginState(true)
                    isLoginFailed = false
                    isLogout = false
                    retryLogin = 0
//                    updateDevState()
                }

                override fun onFailed(p0: Int, p1: Int) {
                    Log.d(TAG, "Login loginAccountJF onFailed  p0:$p0, p1:$p1")
                    XMAccountManager.getInstance().setLoginState(false)
                    if (retryLogin > 2) {
                        isLoginFailed = true
                    } else {
                        Log.d(TAG, "Login loginAccountJF relogin")
                        loginAccountJF(user, password, callback)
                    }
                    retryLogin++
                }

                override fun onFunSDKResult(msg: Message?, ex: MsgContent) {
                    Timber.d("loginAccountJF_onFunSDKResult")
                    // Thực hiện cập nhật danh sách thiết bị cho tài khoản đăng nhập
                    if (msg?.what == EUIMSG.SYS_GET_DEV_INFO_BY_USER) {
                        Timber.d("loginAccountJF_onFunSDKResult_123")
                        if (msg.arg1 < 0) {
                            if (listDeviceShared != null && listDeviceShared?.isNotEmpty() == true) {
                                updateDevState(callback)
                            }
                            isUpdateDevStateCompleted = true
                            return
                        }
                        var pData: ByteArray? = ex.pData
                        if (pData == null || pData.isEmpty()) {
                            if (listDeviceShared != null && listDeviceShared?.isNotEmpty() == true) {
                                updateDevState(callback)
                            }
                            isUpdateDevStateCompleted = true
                            return
                        }
                        val info = SDBDeviceInfo()
                        val nItemLen = G.Sizeof(info)
                        val nCount = pData.size / nItemLen
                        val infos = arrayOfNulls<SDBDeviceInfo>(nCount)
                        for (i in 0 until nCount) {
                            infos[i] = SDBDeviceInfo()
                        }
                        G.BytesToObj(infos, pData)
                        infos.toList()?.forEach {
                            Timber.d("DeviceNameJF: ${it?.devName}")
                        }
                        DevDataCenter.getInstance()?.addDevs(infos.toList())
                        if (listDeviceShared != null && listDeviceShared?.isNotEmpty() == true) {
                            listDeviceShared?.forEach { deviceInfo ->
                                if (!XMAccountManager.getInstance().devList.contains(deviceInfo.getSerialNumber())) {
                                    val sdbDeviceInfo = SDBDeviceInfo()
                                    G.SetValue(
                                        sdbDeviceInfo.st_0_Devmac, deviceInfo.getSerialNumber()
                                    ) //devise serial number

                                    G.SetValue(
                                        sdbDeviceInfo.st_1_Devname, deviceInfo.name
                                    ) //set device name

                                    G.SetValue(
                                        sdbDeviceInfo.st_5_loginPsw, ""
                                    ) //Set device login password

                                    G.SetValue(
                                        sdbDeviceInfo.st_4_loginName, "admin"
                                    ) //Set the device login name, the default is usually admin

                                    DevDataCenter.getInstance().addDev(sdbDeviceInfo)
                                }
                            }
                        }
                        XMAccountManager.getInstance()
                            ?.updateAllDevStateFromServer(XMAccountManager.getInstance().devList,
                                object : BaseAccountManager.OnDevStateListener {
                                    override fun onUpdateDevState(p0: String?) {
                                        Log.d(TAG, "onUpdateDevStateNew: $p0")
                                    }

                                    override fun onUpdateCompleted() {
                                        if (!isUpdateDevStateCompleted) {
                                            isUpdateDevStateCompleted = true
//                                    Handler(Looper.getMainLooper()).postDelayed({
//                                        callback()
//                                    }, 5000)
                                            callback()
                                        }
                                        Log.d(TAG, "onUpdateCompletedNew")

                                    }
                                })
                    }
                }

            })
    }

    var listDeviceShared: List<DeviceDataResponse>? = null

    suspend fun getDevList(): MutableList<HashMap<String, Any>> {
        return withContext(Dispatchers.IO) {
//            updateDevState()
            val devList: MutableList<HashMap<String, Any>> = mutableListOf()
            manager = XMAccountManager.getInstance()
            val listIdDev = mutableListOf<String>()
            listIdDev.addAll(DevDataCenter.getInstance().devList)
            Log.d(TAG, " List Id Dev: ${listIdDev.toString()}")
            for (devId in listIdDev) {
                val xmDevInfo = DevDataCenter.getInstance().getDevInfo(devId)
                Log.d(TAG, "xmDevInfo: ${Gson().toJson(xmDevInfo)}")
                Log.d(TAG, "devState: ${manager.getDevState(devId)}")
                val map = HashMap<String, Any>()
                map["devId"] = devId
                map["isOnline"] = manager.getDevState(devId) == 1 // 0: offline - 1: online
                map["devName"] = xmDevInfo.devName
                devList.add(map)
            }
            Log.d(TAG, "List Dev: ${devList.toString()}")
            devList
        }
    }

    suspend fun updateDevStateSuspend(): Boolean {
        return suspendCancellableCoroutine {
            val accountManager = XMAccountManager.getInstance()
            if (accountManager.isInit) {
                accountManager?.updateAllDevStateFromServer(DevDataCenter.getInstance().devList,
                    object : BaseAccountManager.OnDevStateListener {
                        override fun onUpdateDevState(p0: String?) {
                        }

                        override fun onUpdateCompleted() {
                            if (it.isActive) {
                                Log.d(
                                    "TAG",
                                    "checkUpdateDevState: onUpdateCompleted = ${System.currentTimeMillis()}"
                                )
                                it.resumeWith(Result.success(true))
                            }
                        }
                    })
            }
        }

    }

    suspend fun getInfoWifiSignal(): Double {
        return suspendCancellableCoroutine {
            var userId = 0
            userId = FunSDK.GetId(userId, object : IFunSDKResult {
                override fun OnFunSDKResult(message: Message, msgContent: MsgContent): Int {
                    if (message.arg1 < 0) {
                        it.resumeWithException(Exception("getInfoWifiSignal error ${message.arg1}"))
                        return 0
                    }
                    Log.d("TAG", "OnFunSDKResult: " + msgContent)
                    if (message.what == EUIMSG.SYS_NET_SPEED_TEST) {
                        //arg2 is Download speed   arg3 is Upload speed
                        val value = Utils.formatQualityWifiSignal(msgContent.arg3.toLong())
                        if (it.isActive) {
                            it.resumeWith(Result.success(value))
                        }
                    }
                    return 0
                }
            })
            FunSDK.SysNetSpeedTest(userId, 10, 0)
        }
    }

    suspend fun getLatencyWifi(): String {
        val timeStart = System.currentTimeMillis()
        return suspendCancellableCoroutine {
            var userId = 0
            userId = FunSDK.GetId(userId, object : IFunSDKResult {
                override fun OnFunSDKResult(message: Message, msgContent: MsgContent): Int {
                    if (message.arg1 < 0) {
                        it.resumeWithException(Exception("getLatencyWifiSignal error ${message.arg1}"))
                        return 0
                    }
                    Log.d("TAG", "OnFunSDKResult: " + msgContent)
                    if (message.what == EUIMSG.SYS_NET_PING) {
                        Log.d(
                            TAG, "OnFunSDKResult: time = ${System.currentTimeMillis() - timeStart}"
                        )
                        val averageTime = JSONObject(msgContent.str)["average_time"] as String
                        if (it.isActive) {
                            it.resumeWith(Result.success(averageTime))
                        }
                    }
                    return 0
                }
            })
            FunSDK.Ping(userId, "pub-cfg.viettelcamera.vn", true, 6, 0)
        }
    }

    private var isUpdateCompleted = true
    fun updateDevState(updateCallback: () -> Unit = {}) {
        if (!isUpdateCompleted) {
            return
        }
        isUpdateCompleted = false
        val accountManager = XMAccountManager.getInstance()
        if (accountManager.isInit) {
            accountManager?.updateAllDevStateFromServer(DevDataCenter.getInstance().devList,
                object : BaseAccountManager.OnDevStateListener {
                    override fun onUpdateDevState(p0: String?) {
                        Log.d(TAG, "onUpdateDevState: $p0")
                    }

                    override fun onUpdateCompleted() {
                        Log.d(TAG, "onUpdateCompleted")
                        if (!isUpdateCompleted) {
                            updateCallback()
                            isUpdateCompleted = true
                        }
                    }
                })
        }
    }

    fun setPasswordCamera(devId: String, password: String) {
        val username = FunSDK.DevGetLocalUserName(devId)
        DeviceManager.getInstance()?.setLocalDevPwd(devId, username, password)
//        val devInfo = DevDataCenter.getInstance()?.getDevInfo(devId)
//        if (devInfo != null) {
//            FunSDK.DevSetLocalPwd(
//                G.ToString(devInfo.sdbDevInfo.st_0_Devmac), "",
//                password
//            )
//        }
    }

    fun searchRecordByFile(date: Date, recordManager: RecordManager) {
        if (recordManager is DevRecordManager) {
            val searchTime = Calendar.getInstance()
            searchTime.time = date
            searchTime.set(Calendar.HOUR_OF_DAY, 0)
            searchTime.set(Calendar.MINUTE, 0)
            searchTime.set(Calendar.SECOND, 0)
            val endTime = Calendar.getInstance()
            endTime.time = date
            endTime[Calendar.HOUR_OF_DAY] = 23
            endTime[Calendar.MINUTE] = 59
            endTime[Calendar.SECOND] = 59
            recordManager.searchFileByTime(searchTime, endTime)
        }
    }

    fun searchRecordByTime(date: Date, recordManager: RecordManager) {
        val searchTime = Calendar.getInstance()
        searchTime.time = date
        val times = intArrayOf(
            searchTime.get(Calendar.YEAR),
            searchTime.get(Calendar.MONTH) + 1,
            searchTime.get(Calendar.DATE)
        )
        recordManager.searchFileByTime(times)
    }

    private fun parseJsonVideoCloud(json: String): MutableList<CloudMediaFileInfoBean> {
        val fileList = mutableListOf<CloudMediaFileInfoBean>()
        val tempFileList: MutableList<CloudMediaFileInfoBean>
        try {
            if (StringUtils.isStringNULL(json)) {
                return mutableListOf()
            }
            val jObj = JSON.parseObject(json) ?: return mutableListOf()
            val alarmCenterObj = jObj.getJSONObject("AlarmCenter") ?: return mutableListOf()
            if (alarmCenterObj.containsKey("Body")) {
                val bodyObj = alarmCenterObj.getJSONObject("Body")
                if (bodyObj.containsKey("VideoArray")) {
                    tempFileList = JSON.parseArray(
                        bodyObj.getString("VideoArray"),
                        CloudMediaFileInfoBean::class.java
                    )
                    if (tempFileList.isNotEmpty()) {
                        fileList.addAll(tempFileList)
                        return fileList
                    }
                } else if (bodyObj.containsKey("TimeAxis")) {
                    tempFileList = JSON.parseArray(
                        bodyObj.getString("TimeAxis"),
                        CloudMediaFileInfoBean::class.java
                    )
                    if (tempFileList.isNotEmpty()) {
                        fileList.addAll(tempFileList)
                        return fileList
                    }
                }
            }
        } catch (var5: JSONException) {
            var5.printStackTrace()
        }
        return mutableListOf()
    }

    private suspend fun searchRecordCloudByTime(
        devId: String,
        startTime: IntArray,
        endTime: IntArray,
    ) =
        suspendCancellableCoroutine<MutableList<CloudMediaFileInfoBean>> {
            Log.d(TAG, "searchRecordCloudByTime: startTime = $startTime |||| endTime = $endTime")
            var msgId = 0xff00ff
            msgId = FunSDK.GetId(msgId) { message, msgContent ->
                when (message.what) {
                    EUIMSG.MC_SearchMediaByTime -> {
                        val list = parseJsonVideoCloud(msgContent.str)
                        Log.d(TAG, "searchRecordCloudByTime: list size = ${list.size}")
                        it.resume(list)
                    }
                }
                0
            }
            CloudDirectory.SearchMediaByTime(
                msgId,
                devId,
                0,
                "",
                FunSDK.ToTimeType(startTime),
                FunSDK.ToTimeType(endTime),
                0
            )
        }

    suspend fun searchRecordCloudAllDay(
        devId: String,
        date: Date,
    ): MutableList<H264_DVR_FILE_DATA> {
        val searchTime = Calendar.getInstance()
        searchTime.time = date
        val time = intArrayOf(
            searchTime.get(Calendar.YEAR),
            searchTime.get(Calendar.MONTH) + 1,
            searchTime.get(Calendar.DATE)
        )
        val begin = intArrayOf(time.get(0), time.get(1), time.get(2), 0, 0, 0)
        var end = intArrayOf(time.get(0), time.get(1), time.get(2), 23, 59, 59)
        val result = mutableListOf<H264_DVR_FILE_DATA>()
        while (true) {
            try {
                val list = withContext(Dispatchers.Main) {
                    searchRecordCloudByTime(devId, begin, end)
                }
                if (list.size < 500) {
                    Log.d(TAG, "searchRecordCloudAllDay: end loadmore = ${list.size} data")
                    result.addAll(list.map { it.cloudMediaInfoToH264FileData() })
                    return result
                } else {
                    Log.d(
                        TAG, "searchRecordCloudAllDay: next loadmore = ${list.size} data"
                    )
                    result.addAll(list.map { it.cloudMediaInfoToH264FileData() })
                    val dateEnd = SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss"
                    ).parse(list.last().startTime)
                    val timeMillis = dateEnd.time - 1000
                    val newDateEnd = Date(timeMillis)
                    val newEndCalendar = Calendar.getInstance()
                    newEndCalendar.time = newDateEnd
                    end = intArrayOf(
                        time.get(0),
                        time.get(1),
                        time.get(2),
                        newEndCalendar.get(Calendar.HOUR_OF_DAY),
                        newEndCalendar.get(Calendar.MINUTE),
                        newEndCalendar.get(Calendar.SECOND)
                    )
                }

            } catch (e: Exception) {
                Log.d(
                    TAG, "getAllAlarmInfoByTime: Error = $e"
                )
                return result
            }
        }
    }

    fun CloudMediaFileInfoBean.cloudMediaInfoToH264FileData(): H264_DVR_FILE_DATA {
        val data = H264_DVR_FILE_DATA()
        data.downloadType = 1
        val sDate = startTimeByYear
        val eDate = endTimeByYear
        data.st_3_beginTime.st_0_year = sDate.year + 1900
        data.st_3_beginTime.st_1_month = sDate.month + 1
        data.st_3_beginTime.st_2_day = sDate.date
        data.st_3_beginTime.st_4_hour = sDate.hours
        data.st_3_beginTime.st_5_minute = sDate.minutes
        data.st_3_beginTime.st_6_second = sDate.seconds
        data.st_4_endTime.st_0_year = eDate.year + 1900
        data.st_4_endTime.st_1_month = eDate.month + 1
        data.st_4_endTime.st_2_day = eDate.date
        data.st_4_endTime.st_4_hour = eDate.hours
        data.st_4_endTime.st_5_minute = eDate.minutes
        data.st_4_endTime.st_6_second = eDate.seconds
        data.st_2_fileName = indexFile.toByteArray()
        data.st_0_ch = 0
        data.alarmExFileInfo = JSON.toJSONString(this)
        return data
    }

    fun loginDev(devId: String, callback: () -> Unit) {
        Log.d(TAG, "loginDev: devId = $devId")
        if (DevDataCenter.getInstance().isLowPowerDev(devId)) {
            DeviceManager.getInstance()
                .loginDevByLowPower(devId, object : DeviceManager.OnDevManagerListener<Any?> {
                    override fun onSuccess(s: String, i: Int, abilityKey: Any?) {
                        Log.d(TAG, "loginDev onSuccess: ")
                        callback.invoke()
                    }

                    override fun onFailed(s: String, i: Int, s1: String, errorId: Int) {
                        Log.d(TAG, "loginDev onFailed: $errorId")
//                        setPasswordCamera(devId)
                        loginDev(devId, callback)
                    }
                })
        } else {
            DeviceManager.getInstance()
                .loginDev(devId, object : DeviceManager.OnDevManagerListener<Any?> {
                    override fun onSuccess(s: String, i: Int, abilityKey: Any?) {
                        Log.d(TAG, "loginDev onSuccess: ")
                        callback.invoke()
                    }

                    override fun onFailed(s: String, i: Int, s1: String, errorId: Int) {
                        Log.d(TAG, "loginDev onFailed: $errorId")
//                    setPasswordCamera(devId)
                        loginDev(devId, callback)
                    }
                })
        }
    }

    var retry = 0
    fun deleteDevice(devId: String, callback: (Int) -> Unit = {}) {
        XMAccountManager.getInstance().deleteDev(devId, object : OnAccountManagerListener {
            override fun onSuccess(msgId: Int) {
                Log.d(TAG, " deleteDevice $devId Success")
                setPasswordCamera(devId, "")
                retry = 0
            }

            override fun onFailed(msgId: Int, errorId: Int) {
                Log.d(TAG, " deleteDevice $devId onFailed")
                if (retry > 1) {
                    callback(errorId)
                } else {
                    deleteDevice(devId, callback)
                }
                retry++
            }

            override fun onFunSDKResult(message: Message, msgContent: MsgContent) {

            }
        })
    }

    fun searchAlarmInfoByTime(
        devId: String, msgId: Int, startSearchDay: Calendar, stopSearchDay: Calendar,
    ) {
        val info = XPMS_SEARCH_ALARMINFO_REQ()
        G.SetValue(info.st_00_Uuid, devId)
        info.st_02_StarTime.st_0_year = startSearchDay[Calendar.YEAR]
        info.st_02_StarTime.st_1_month = startSearchDay[Calendar.MONTH] + 1
        info.st_02_StarTime.st_2_day = startSearchDay[Calendar.DATE]
        info.st_02_StarTime.st_4_hour = startSearchDay[Calendar.HOUR_OF_DAY]
        info.st_02_StarTime.st_5_minute = startSearchDay[Calendar.MINUTE]
        info.st_02_StarTime.st_6_second = startSearchDay[Calendar.SECOND]
        info.st_03_EndTime.st_0_year = stopSearchDay[Calendar.YEAR]
        info.st_03_EndTime.st_1_month = stopSearchDay[Calendar.MONTH] + 1
        info.st_03_EndTime.st_2_day = stopSearchDay[Calendar.DATE]
        info.st_03_EndTime.st_4_hour = stopSearchDay[Calendar.HOUR_OF_DAY]
        info.st_03_EndTime.st_5_minute = stopSearchDay[Calendar.MINUTE]
        info.st_03_EndTime.st_6_second = stopSearchDay[Calendar.SECOND]
        info.st_04_Channel = 0
        info.st_06_Number = -1
        info.st_07_Index = 0
        MpsClient.SearchAlarmInfoByTime(msgId, G.ObjToBytes(info), 0) //返回的报警消息按时间倒序
    }

    suspend fun getAllAlarmInfoByTime(
        devId: String, startSearchDay: Calendar, stopSearchDay: Calendar,
    ): MutableList<AlarmInfo> {
        var isLoadMore = true
        val result = mutableListOf<AlarmInfo>()
        while (isLoadMore) {
            try {
                val list = getAlarmInfoByTime(devId, startSearchDay, stopSearchDay)
                if (list.size < 150) {
                    Log.d(TAG, "getAllAlarmInfoByTime: end loadmore = ${list.size} data")
                    result.addAll(list)
                    isLoadMore = false
                } else {
                    Log.d(
                        TAG, "getAllAlarmInfoByTime: next loadmore = ${list.size} data"
                    )
                    result.addAll(list)
                    stopSearchDay.time = SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss"
                    ).parse(list.lastOrNull()?.startTime)
                }

            } catch (e: Exception) {
                Log.d(
                    TAG, "getAllAlarmInfoByTime: Error = $e"
                )
                isLoadMore = false
            }
        }
        Log.d(TAG, "getAllAlarmInfoByTime: result size = ${result.size} data")
        return result
    }

    suspend fun getAlarmInfoByTime(
        devId: String, startSearchDay: Calendar, stopSearchDay: Calendar,
    ) = suspendCancellableCoroutine<MutableList<AlarmInfo>> {
        var msgId = 0xff00ff
        msgId = FunSDK.GetId(msgId, object : IFunSDKResult {
            override fun OnFunSDKResult(message: Message, msgContent: MsgContent): Int {
                when (message.what) {
                    EUIMSG.MC_SearchAlarmInfo -> {
                        if (message.arg1 >= 0) {
                            val listAlarmInfo = mutableListOf<AlarmInfo>()
                            val nNext = IntArray(1)
                            nNext[0] = 0
                            var nStart = 0
                            for (i in 0 until msgContent.arg3) {
                                val ret = G.ArrayToString(msgContent.pData, nStart, nNext)
                                nStart = nNext[0]
                                val info = AlarmInfo()
                                if (!info.onParse(ret)) {
                                    if (!info.onParse("{$ret")) {
                                        continue
                                    }
                                }
                                if (null != info.extInfo) {
                                    val extUserData = info.extUserData
                                    if (null != extUserData && extUserData.isNotEmpty()) {
                                        continue
                                    }
                                }
                                listAlarmInfo.add(info)
                            }
                            it.resume(listAlarmInfo.sortedByDescending { it.startTime }
                                .toMutableList())
                        } else {
                            it.resume(mutableListOf())
                        }
                    }
                }
                return 0
            }
        })
        val info = XPMS_SEARCH_ALARMINFO_REQ()
        G.SetValue(info.st_00_Uuid, devId)
        info.st_02_StarTime.st_0_year = startSearchDay[Calendar.YEAR]
        info.st_02_StarTime.st_1_month = startSearchDay[Calendar.MONTH] + 1
        info.st_02_StarTime.st_2_day = startSearchDay[Calendar.DATE]
        info.st_02_StarTime.st_4_hour = startSearchDay[Calendar.HOUR_OF_DAY]
        info.st_02_StarTime.st_5_minute = startSearchDay[Calendar.MINUTE]
        info.st_02_StarTime.st_6_second = startSearchDay[Calendar.SECOND]
        info.st_03_EndTime.st_0_year = stopSearchDay[Calendar.YEAR]
        info.st_03_EndTime.st_1_month = stopSearchDay[Calendar.MONTH] + 1
        info.st_03_EndTime.st_2_day = stopSearchDay[Calendar.DATE]
        info.st_03_EndTime.st_4_hour = stopSearchDay[Calendar.HOUR_OF_DAY]
        info.st_03_EndTime.st_5_minute = stopSearchDay[Calendar.MINUTE]
        info.st_03_EndTime.st_6_second = stopSearchDay[Calendar.SECOND]
        info.st_04_Channel = 0
        info.st_06_Number = -1
        info.st_07_Index = 0
        MpsClient.SearchAlarmInfoByTime(msgId, G.ObjToBytes(info), 0) //返回的报警消息按时间倒序
    }

    private fun dealWithSearchAlarmResult(
        alarmCount: Int, alarmInfos: ByteArray?,
    ): MutableList<AlarmInfo> {
        val list = mutableListOf<AlarmInfo>()
        var info: AlarmInfo? = null
        val nNext = IntArray(1)
        nNext[0] = 0
        var nStart = 0
        for (i in 0 until alarmCount) {
            val ret = G.ArrayToString(alarmInfos, nStart, nNext)
            nStart = nNext[0]
            info = AlarmInfo()
            if (!info.onParse(ret)) {
                if (!info.onParse("{$ret")) {
                    continue
                }
            }
            list.add(info)
        }
        return list
    }

    suspend fun searchAlarmSystem(
        pageSize: Int = 10, startSearchDay: Calendar, stopSearchDay: Calendar,
    ) = suspendCancellableCoroutine<MutableList<AlarmInfo>> {
        var msgId = 0xff00ff
        msgId = FunSDK.GetId(msgId, object : IFunSDKResult {
            override fun OnFunSDKResult(message: Message, msgContent: MsgContent): Int {
                when (message.what) {
                    EUIMSG.MC_BATCH_DEV_ALARM_MSG_QUERY -> {
                        try {
                            if (message.arg1 >= 0) {
                                val list = msgContent.pData?.let {
                                    dealWithSearchAlarmResult(message.arg2, it)
                                } ?: mutableListOf()
                                Log.d(TAG, "listAlarmSystem = ${list.size}")
                                Log.d(TAG, "Search notification: list JF size = ${list.size}")
                                it.resume(list)
                            } else {
                                Log.d(TAG, "Error listAlarmSystem")
                                it.resume(mutableListOf())
                            }
                        } catch (e: Exception) {
                            Timber.d("onFunSDKResult ${e.message}")
                        }
                    }

                    else -> {}
                }
                return 0
            }
        })
        Log.d(TAG, "Search notification: startTime = ${startSearchDay.time}")
        Log.d(TAG, "Search notification: endTime = ${stopSearchDay.time}")
        val info = SBatchDevAlarmMsgQueryReqParams()
        G.SetValue(info.st_5_szAlarmType, "DevStatusAlarm")
        info.st_2_nChannel = -1
        info.st_0_nStartTime = (startSearchDay.timeInMillis / 1000).toInt()
        info.st_1_nEndTime = (stopSearchDay.timeInMillis / 1000).toInt()
        info.st_3_nMaxNumber = pageSize
        info.st_4_nPageIndex = 1
        MpsClient.BatchDevAlarmMsgQuery(msgId, "", G.ObjToBytes(info), 0)
    }

    suspend fun setReadAlarmSystem(
        list: List<Pair<String?, List<String?>>>,
    ) = suspendCancellableCoroutine<Boolean> {
        var msgId = 0xff00ff
        msgId = FunSDK.GetId(msgId, object : IFunSDKResult {
            override fun OnFunSDKResult(message: Message, msgContent: MsgContent): Int {
                when (message.what) {
                    EUIMSG.MC_BATCH_SET_ALARM_MSG_READ_FLAG -> {
                        Log.d(TAG, "MC_BATCH_SET_ALARM_MSG_READ_FLAG : message = ${message}")
                        it.resume(message.arg1 >= 0)
                    }

                    else -> {}
                }
                return 0
            }
        })

        val sendObj = JSONObject()
        sendObj.put("msg", "msgstatus_record")
        val statusListArr = JSONArray()
        list.forEach { item ->
            val obj = JSONObject()
            obj.put("sn", item.first)
            val idsArray = JSONArray()
            item.second.forEach { id ->
                idsArray.put(id)
            }
            obj.put("ids", idsArray)
            statusListArr.put(obj)
        }
        sendObj.put("statuslist", statusListArr)
        Log.d(TAG, "setReadAlarmSystem: sendObj = $sendObj")
        MpsClient.BatchSetAlarmMsgReadFlag(msgId, sendObj.toString(), 0)
    }

    suspend fun deleteAlarmSystem(
        list: List<Pair<String?, List<String?>>>,
    ) = suspendCancellableCoroutine<Boolean> {
        var msgId = 0xff00ff
        msgId = FunSDK.GetId(msgId, object : IFunSDKResult {
            override fun OnFunSDKResult(message: Message, msgContent: MsgContent): Int {
                when (message.what) {
                    EUIMSG.MC_BATCH_DELETE_ALARM -> {
                        Log.d(TAG, "MC_BATCH_DELETE_ALARM : arg1 = ${message.arg1}")
                        it.resume(message.arg1 >= 0)
                    }

                    else -> {}
                }
                return 0
            }
        })

        val sendObj = JSONObject()
        sendObj.put("msg", "delete_alarm")
        sendObj.put("deltype", "MSG")
        val statusListArr = JSONArray()
        list.forEach { item ->
            val obj = JSONObject()
            obj.put("sn", item.first)
            val idsArray = JSONArray()
            item.second.forEach { id ->
                idsArray.put(id)
            }
            obj.put("ids", idsArray)
            statusListArr.put(obj)
        }
        sendObj.put("dellist", statusListArr)
        Log.d(TAG, "deleteAlarmSystem: sendObj = $sendObj")
        MpsClient.BatchDeleteAlarm(msgId, sendObj.toString(), 0)
    }

    suspend fun downloadImageAlrmInfo(alarmInfoJsonObj: JSONObject, context: Context) =
        suspendCancellableCoroutine<Bitmap> {
            val alarmInfo = AlarmInfo()
            alarmInfo.onParse(alarmInfoJsonObj)
            val mCloudImageManager = CloudImageManager(context)
            mCloudImageManager.setOnCloudImageListener(object :
                CloudImageManager.OnCloudImageListener {
                override fun onDownloadResult(
                    isSuccess: Boolean,
                    imagePath: String?,
                    bitmap: Bitmap?,
                    mediaType: Int,
                    seq: Int,
                ) {
                    if (isSuccess) {
                        bitmap?.let { result ->
                            it.resumeWith(Result.success(result))
                        }
                        Log.d(
                            TAG, "onDownloadResult: devId = ${alarmInfo.sn} --- path = $imagePath"
                        )
                    }
                }

                override fun onDeleteResult(isSuccess: Boolean, seq: Int) {

                }

            })
//            mCloudImageManager.downloadImage(
//                alarmInfo,
//                SDKCONST.MediaType.VIDEO, 0, 0, 0, true
//            )
        }

    fun modifyDeviceName(devId: String, newName: String, callback: (Boolean) -> Unit) {
        val accountManager = XMAccountManager.getInstance()
        val oldDevName = DevDataCenter.getInstance().getDevInfo(devId)?.devName
        accountManager.modifyDevName(devId, newName, object : OnAccountManagerListener {
            override fun onSuccess(p0: Int) {
                callback.invoke(true)
                EventBus.getDefault()
                    .post(RxEvent(EVENT_SYSTEM_CHANGE_INFORMATION_CAMERA_JF, devId))
            }

            override fun onFailed(p0: Int, p1: Int) {
                callback.invoke(false)
                DevDataCenter.getInstance().getDevInfo(devId)?.devName = oldDevName
            }

            override fun onFunSDKResult(p0: Message?, p1: MsgContent?) {

            }

        })
    }

    var retryDevInfo = 0
    fun getDevInfo(devId: String, callback: (String) -> Unit) {
        val devConfigInfo = DevConfigInfo.create(object : OnDevManagerListener<Any?> {
            override fun onSuccess(devId: String?, msgId: Int, result: Any?) {
                retryDevInfo = 0
                if (result is String) {
                    callback.invoke(result)
                } else {
                    callback.invoke(JSON.toJSONString(result))
                }
            }

            override fun onFailed(devId: String, msgId: Int, s1: String, errorId: Int) {
                if (retryDevInfo > 2) {
                    callback.invoke("")
                } else {
                    Log.d(TAG, "getDevInfo retry")
                    getDevInfo(devId, callback)
                }
                retryDevInfo++
            }
        })
        devConfigInfo.jsonName = JsonConfig.SYSTEM_INFO
        devConfigInfo.chnId = -1
        val devConfigManager = DeviceManager.getInstance().getDevConfigManager(devId)
        devConfigManager.getDevConfig(devConfigInfo)
    }

    fun getStorageInfo(devId: String, callback: (String) -> Unit) {
        val devConfigInfo = DevConfigInfo.create(object : OnDevManagerListener<Any?> {
            override fun onSuccess(devId: String?, msgId: Int, result: Any?) {
                if (result is String) {
                    callback.invoke(result)
                } else {
                    callback.invoke(JSON.toJSONString(result))
                }
            }

            override fun onFailed(devId: String, msgId: Int, s1: String, errorId: Int) {
                callback.invoke("")
            }
        })

        devConfigInfo.jsonName = JsonConfig.STORAGE_INFO
        devConfigInfo.chnId = -1
        val devConfigManager = DeviceManager.getInstance().getDevConfigManager(devId)
        devConfigManager.getDevConfig(devConfigInfo)
    }

    fun formatStorage(devId: String, callback: (Boolean) -> Unit) {
        val devConfigInfo = DevConfigInfo.create(object : OnDevManagerListener<Any?> {
            override fun onSuccess(devId: String?, msgId: Int, result: Any?) {
                callback.invoke(true)
            }

            override fun onFailed(devId: String, msgId: Int, s1: String, errorId: Int) {
                callback.invoke(false)
            }
        })
        devConfigInfo.jsonName = JsonConfig.OPSTORAGE_MANAGER
        devConfigInfo.timeOut = 10000
        devConfigInfo.chnId = -1
        val opStorageManagerBean = OPStorageManagerBean()
        opStorageManagerBean.action = "Clear"
        opStorageManagerBean.serialNo = 0
        opStorageManagerBean.type = "Data"
        opStorageManagerBean.partNo = 0
        val jsonData =
            HandleConfigData.getSendData(JsonConfig.OPSTORAGE_MANAGER, "0x08", opStorageManagerBean)
        devConfigInfo.jsonData = jsonData
        val devConfigManager = DeviceManager.getInstance().getDevConfigManager(devId)
        devConfigManager.setDevConfig(devConfigInfo)
    }

    fun rebootOrResetIPC(devId: String, command: String?, callback: (Boolean) -> Unit) {
        var userId = 0
        userId = FunSDK.GetId(userId, object : IFunSDKResult {
            override fun OnFunSDKResult(msg: Message?, ex: MsgContent?): Int {
                if (JsonConfig.OP_IPC_CONTROL == ex?.str) {
                    if (msg?.arg1 ?: -1 < 0) {
                        if (msg?.arg1 == EFUN_ERROR.EE_DVR_NEED_REBOOT) {
                            //恢复默认返回70150，设备会自动重启
                            callback.invoke(true)
                        } else {
                            callback.invoke(false)
                        }
                    } else {
                        var command: String? = null
                        try {
                            val jsonObject = JSON.parseObject(G.ToString(ex.pData))
                            if (jsonObject.getString("Name") != null) {
                                command = jsonObject.getString("Name")
                            }
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                        callback.invoke(true)
                    }
                }
                return 0
            }

        })
        val obj = com.alibaba.fastjson.JSONObject()
        obj["Command"] = command
        FunSDK.DevCmdGeneral(
            userId,
            devId,
            4010,
            JsonConfig.OP_IPC_CONTROL,
            -1,
            TIME_OUT,
            HandleConfigData.getSendData(
                getFullName(JsonConfig.OP_IPC_CONTROL, 0), "0x01", obj
            ).toByteArray(),
            -1,
            0
        )
    }

    /**
     * @param mode: 0,1
     * @param callback
     */
    fun flipCamera(
        devId: String, cameraParamBean: CameraParamBean?, callback: (Int) -> Unit,
    ) {
        val currentMode = G.getIntFromHex(cameraParamBean?.PictureFlip)
        val newFlip = G.getHexFromInt(if (currentMode == 0) 1 else 0)
        val currentModeMirror = G.getIntFromHex(cameraParamBean?.PictureMirror)
        val newMirror = G.getHexFromInt(if (currentModeMirror == 0) 1 else 0)
        val devConfigInfo = DevConfigInfo.create(object : OnDevManagerListener<Any?> {
            override fun onSuccess(devId: String?, msgId: Int, result: Any?) {
                Timber.tag(TAG).d("flipCamera onSuccess")
                cameraParamBean?.PictureFlip = newFlip
                cameraParamBean?.PictureMirror = newMirror
                callback.invoke(currentMode)
            }

            override fun onFailed(devId: String, msgId: Int, s1: String, errorId: Int) {
                Timber.tag(TAG).d("flipCamera failed")
                callback.invoke(-1)
            }
        })
        devConfigInfo.jsonName = JSONCONFIG.CAMERA_PARAM
        val newCameraParamBean = ModelMapper().map(cameraParamBean, CameraParamBean::class.java)
        newCameraParamBean.PictureFlip = newFlip
        newCameraParamBean.PictureMirror = newMirror
        val jsonData = HandleConfigData.getSendData(
            getFullName(JSONCONFIG.CAMERA_PARAM, 0), "0x08", newCameraParamBean
        )
        devConfigInfo.chnId = 0
        devConfigInfo.jsonData = jsonData
        val devConfigManager = DeviceManager.getInstance().getDevConfigManager(devId)
        devConfigManager.setDevConfig(devConfigInfo)
    }

    /**
     * @param mode: 0,1
     * @param callback
     */
    fun flipCameraLeftRight(
        devId: String, cameraParamBean: CameraParamBean?, callback: (Boolean) -> Unit,
    ) {
        val currentMode = G.getIntFromHex(cameraParamBean?.PictureMirror)
        val newMirror = G.getHexFromInt(if (currentMode == 0) 1 else 0)
        val devConfigInfo = DevConfigInfo.create(object : OnDevManagerListener<Any?> {
            override fun onSuccess(devId: String?, msgId: Int, result: Any?) {
                Timber.tag(TAG).d("flipCameraLeftRight onSuccess")
                cameraParamBean?.PictureMirror = newMirror
                callback.invoke(true)
            }

            override fun onFailed(devId: String, msgId: Int, s1: String, errorId: Int) {
                Timber.tag(TAG).d("flipCameraLeftRight failed")
                callback.invoke(false)
            }
        })
        devConfigInfo.jsonName = JSONCONFIG.CAMERA_PARAM
        val newCameraParamBean = ModelMapper().map(cameraParamBean, CameraParamBean::class.java)
        newCameraParamBean.PictureMirror = newMirror
        val jsonData = HandleConfigData.getSendData(
            getFullName(JSONCONFIG.CAMERA_PARAM, 0), "0x08", newCameraParamBean
        )
        devConfigInfo.chnId = 0
        devConfigInfo.jsonData = jsonData
        val devConfigManager = DeviceManager.getInstance().getDevConfigManager(devId)
        devConfigManager.setDevConfig(devConfigInfo)
    }

    fun getCameraParamex(devId: String, callback: (CameraParamBean?) -> Unit) {
        val devConfigInfo = DevConfigInfo.create(object : OnDevManagerListener<CameraParamBean?> {
            override fun onSuccess(devId: String?, msgId: Int, result: CameraParamBean?) {
                callback.invoke(result)
            }

            override fun onFailed(devId: String, msgId: Int, s1: String, errorId: Int) {
                Log.d(TAG, "getCameraParamex onFailed")
                callback.invoke(null)
            }
        })

        devConfigInfo.jsonName = JSONCONFIG.CAMERA_PARAM
        devConfigInfo.chnId = 0
        val devConfigManager = DeviceManager.getInstance().getDevConfigManager(devId)
        devConfigManager.getDevConfig(devConfigInfo)
    }

    fun setVolume(
        devId: String, volume: Int, volumeBean: DevVolumeBean?, callback: (Boolean) -> Unit,
    ) {
        val devConfigInfo = DevConfigInfo.create(object : OnDevManagerListener<Any?> {
            override fun onSuccess(devId: String?, msgId: Int, result: Any?) {
                Timber.tag(TAG).d("setVolume onSuccess")
                callback.invoke(true)
            }

            override fun onFailed(devId: String, msgId: Int, s1: String, errorId: Int) {
                Timber.tag(TAG).d("setVolume failed")
                callback.invoke(false)
            }
        })
        devConfigInfo.jsonName = CFG_DEV_HORN_VOLUME
        val newVolumeBean = DevVolumeBean()
        newVolumeBean.audioMode = volumeBean?.audioMode
        newVolumeBean.leftVolume = volume
        newVolumeBean.rightVolume = volume
        val jsonData = HandleConfigData.getSendData(
            getFullName(CFG_DEV_HORN_VOLUME, 0), "0x08", newVolumeBean
        )
        devConfigInfo.chnId = 0
        devConfigInfo.jsonData = jsonData
        val devConfigManager = DeviceManager.getInstance().getDevConfigManager(devId)
        devConfigManager.setDevConfig(devConfigInfo)
    }

    fun setMicVolume(
        devId: String, volume: Int, micVolumeBean: DevVolumeBean?, callback: (Boolean) -> Unit,
    ) {
        val devConfigInfo = DevConfigInfo.create(object : OnDevManagerListener<Any?> {
            override fun onSuccess(devId: String?, msgId: Int, result: Any?) {
                Timber.tag(TAG).d("setMicVolume onSuccess")
                callback.invoke(true)
            }

            override fun onFailed(devId: String, msgId: Int, s1: String, errorId: Int) {
                Timber.tag(TAG).d("setMicVolume failed")
                callback.invoke(false)
            }
        })
        devConfigInfo.jsonName = "fVideo.VolumeIn"
        val newMicVolumeBean = DevVolumeBean()
        newMicVolumeBean.audioMode = micVolumeBean?.audioMode
        newMicVolumeBean.leftVolume = volume
        newMicVolumeBean.rightVolume = volume
        val jsonData = HandleConfigData.getSendData(
            getFullName("fVideo.VolumeIn", 0), "0x08", newMicVolumeBean
        )
        devConfigInfo.chnId = 0
        devConfigInfo.jsonData = jsonData
        val devConfigManager = DeviceManager.getInstance().getDevConfigManager(devId)
        devConfigManager.setDevConfig(devConfigInfo)
    }

    fun getVolumeCamera(devId: String, callback: (DevVolumeBean?) -> Unit) {
        val devConfigInfo = DevConfigInfo.create(object : OnDevManagerListener<DevVolumeBean?> {
            override fun onSuccess(devId: String?, msgId: Int, result: DevVolumeBean?) {
                callback.invoke(result)
            }

            override fun onFailed(devId: String, msgId: Int, s1: String, errorId: Int) {
                Timber.tag(TAG).d("getVolumeCamera onFailed")
                callback.invoke(null)
            }
        })

        devConfigInfo.jsonName = CFG_DEV_HORN_VOLUME
        devConfigInfo.chnId = 0
        val devConfigManager = DeviceManager.getInstance().getDevConfigManager(devId)
        devConfigManager.getDevConfig(devConfigInfo)
    }

    fun getMicVolumeCamera(devId: String, callback: (DevVolumeBean?) -> Unit) {
        var userId = 0
        userId = FunSDK.GetId(userId) { _, ex ->
            if ("fVideo.VolumeIn" == ex?.str) {
                val data = HandleConfigData<DevVolumeBean?>()
                if (data.getDataObj(G.ToString(ex.pData), DevVolumeBean::class.java)) {
                    callback.invoke(data.obj)
                }
            }
            0
        }
        FunSDK.DevGetConfigByJson(
            userId, devId, "fVideo.VolumeIn", 1024, 0, TIME_OUT, 0
        )
    }

    fun isSupportStatusLed(devId: String): Boolean {
        return FunSDK.GetDevAbility(devId, "OtherFunction/SupportStatusLed") > 0
    }

    fun getStatusLED(devId: String, callback: (FbExtraStateCtrlBean?) -> Unit) {
        var userId = 0xff00ff
        userId = FunSDK.GetId(userId) { _, ex ->
            if (CFG_FbExtraStateCtrl == ex?.str) {
                HandleConfigData<FbExtraStateCtrlBean?>().takeIf {
                    it.getDataObj(G.ToString(ex.pData), FbExtraStateCtrlBean::class.java)
                }?.apply {
                    callback.invoke(obj)
                }
            }
            0
        }
        FunSDK.DevGetConfigByJson(userId, devId, CFG_FbExtraStateCtrl, 1024, -1, TIME_OUT, 0)

    }

    fun setStatusLED(
        devId: String,
        newState: Boolean,
        fbExtraStateInfo: FbExtraStateCtrlBean?,
        callback: (Boolean) -> Unit,
    ) {
        val devConfigInfo = DevConfigInfo.create(object : OnDevManagerListener<Any?> {
            override fun onSuccess(devId: String?, msgId: Int, result: Any?) {
                Timber.tag(TAG).d("setStatusLED onSuccess")
                callback.invoke(true)
            }

            override fun onFailed(devId: String, msgId: Int, s1: String, errorId: Int) {
                Timber.tag(TAG).d("setStatusLED onFailed")
                callback.invoke(false)
            }
        })
        devConfigInfo.jsonName = CFG_FbExtraStateCtrl
        fbExtraStateInfo?.ison = if (newState) SDKCONST.Switch.Open else SDKCONST.Switch.Close
        devConfigInfo.jsonData = HandleConfigData.getSendData(
            getFullName(CFG_FbExtraStateCtrl, -1), "0x08", fbExtraStateInfo
        )
        devConfigInfo.chnId = -1
        devConfigInfo.timeOut = TIME_OUT
        devConfigInfo.seq = 0
        DeviceManager.getInstance().getDevConfigManager(devId).setDevConfig(devConfigInfo)
    }

    fun isSupportVoiceLightAlarm(devId: String): Boolean {
        return FunSDK.GetDevAbility(devId, "OtherFunction/SupportVoiceLightAlarm") > 0
    }

    fun setStatusDefence(devId: String, action: String, callback: (Boolean) -> Unit) {
        var userId = 0
        userId = FunSDK.GetId(userId) { msg, msgContent ->
            if (StringUtils.contrast(msgContent.str, "OPVoiceLightAlarm")) {
                callback.invoke(msg.arg1 >= 0)
            }
            0
        }
        val obj = JSONObject().apply {
            put("Action", action)
            put("VoiceType", 520)
            put("LightFreq", 500)
        }
        val sendObj = JSONObject().apply {
            put("Name", "OPVoiceLightAlarm")
            put("SessionID", "0x0000000001")
            put("OPVoiceLightAlarm", obj)
        }
        FunSDK.DevCmdGeneral(
            userId,
            devId,
            1450,
            "OPVoiceLightAlarm",
            -1,
            TIME_OUT,
            sendObj.toString().toByteArray(),
            0,
            0
        )
    }

    fun isSupportOneKeyMaskVideo(devId: String): Boolean {
        return FunSDK.GetDevAbility(devId, "OtherFunction/SupportOneKeyMaskVideo") > 0
    }

    fun getStatusMask(devId: String, callback: (Boolean) -> Unit) {
        var userId = 0
        userId = FunSDK.GetId(userId, object : IFunSDKResult {
            override fun OnFunSDKResult(msg: Message?, ex: MsgContent?): Int {
                if (ex?.str == JsonConfig.CFG_ONE_KEY_MASK_VIDEO) {
                    val data: HandleConfigData<OPOneKeyMaskVideoBean?> =
                        HandleConfigData<OPOneKeyMaskVideoBean?>()
                    data.getDataObj(G.ToString(ex.pData), OPOneKeyMaskVideoBean::class.java)
                    callback.invoke(data.obj?.isEnable ?: false)
                    Log.d(TAG, "getStatusMask: isEnable = ${data.obj?.isEnable}")
                }
                return 0
            }
        })
        FunSDK.DevGetConfigByJson(
            userId, devId, JsonConfig.CFG_ONE_KEY_MASK_VIDEO, 1024, 0, TIME_OUT, 0
        )
    }

    fun setStatusMask(devId: String, newState: Boolean, callback: (Boolean) -> Unit) {
        var userId = 0
        userId = FunSDK.GetId(userId, object : IFunSDKResult {
            override fun OnFunSDKResult(msg: Message?, ex: MsgContent?): Int {
                if (msg?.arg1 ?: -1 >= 0 && ex?.str == JsonConfig.CFG_ONE_KEY_MASK_VIDEO) {
                    callback.invoke(true)
                } else {
                    callback.invoke(false)
                }
                return 0
            }
        })
        val bean = OPOneKeyMaskVideoBean()
        bean.isEnable = newState
        val jsonData = HandleConfigData.getSendData(
            getFullName(JsonConfig.CFG_ONE_KEY_MASK_VIDEO, 0), "0x08", bean
        )
        FunSDK.DevSetConfigByJson(
            userId, devId, JsonConfig.CFG_ONE_KEY_MASK_VIDEO, jsonData, 0, TIME_OUT, 0
        )
    }

    fun getHumanDetection(devId: String, callback: (HumanDetectionBean?) -> Unit) {
        var userId = 0
        userId = FunSDK.GetId(userId, object : IFunSDKResult {
            override fun OnFunSDKResult(msg: Message?, ex: MsgContent?): Int {
                if (ex?.str == JsonConfig.DETECT_HUMAN_DETECTION) {
                    val data: HandleConfigData<HumanDetectionBean?> =
                        HandleConfigData<HumanDetectionBean?>()
                    data.getDataObj(G.ToString(ex.pData), HumanDetectionBean::class.java)
                    callback.invoke(data.obj)
                    Log.d(TAG, "getHumanDetection: isEnable = ${data.obj?.isEnable}")
                }
                return 0
            }
        })
        FunSDK.DevGetConfigByJson(
            userId, devId, JsonConfig.DETECT_HUMAN_DETECTION, 1024, 0, TIME_OUT, 0
        )
    }

    fun setHumanDetection(devId: String, bean: HumanDetectionBean?, callback: (Boolean) -> Unit) {
        var userId = 0
        userId = FunSDK.GetId(userId, object : IFunSDKResult {
            override fun OnFunSDKResult(msg: Message?, ex: MsgContent?): Int {
                if (msg?.arg1 ?: -1 >= 0 && ex?.str == JsonConfig.DETECT_HUMAN_DETECTION) {
                    callback.invoke(true)
                } else {
                    callback.invoke(false)
                }
                return 0
            }
        })
        val jsonData = HandleConfigData.getSendData(
            getFullName(JsonConfig.DETECT_HUMAN_DETECTION, 0), "0x08", bean
        )
        FunSDK.DevSetConfigByJson(
            userId, devId, JsonConfig.DETECT_HUMAN_DETECTION, jsonData, 0, TIME_OUT, 0
        )
    }

    fun getDetectTrack(devId: String, callback: (DetectTrackBean?) -> Unit) {
        var userId = 0
        userId = FunSDK.GetId(userId, object : IFunSDKResult {
            override fun OnFunSDKResult(msg: Message?, ex: MsgContent?): Int {
                if (ex?.str == JsonConfig.CFG_DETECT_TRACK) {
                    val data: HandleConfigData<DetectTrackBean?> =
                        HandleConfigData<DetectTrackBean?>()
                    data.getDataObj(G.ToString(ex.pData), DetectTrackBean::class.java)
                    if (data.obj !is ArrayList<*>) {
                        callback.invoke(data.obj)
                        Log.d(TAG, "getDetectTrack: Enable = ${data.obj?.enable}")
                    }
                }
                return 0
            }
        })
        FunSDK.DevGetConfigByJson(
            userId, devId, JsonConfig.CFG_DETECT_TRACK, 1024, -1, TIME_OUT, 0
        )
    }

    fun setDetectTrack(devId: String, bean: DetectTrackBean?, callback: (Boolean) -> Unit) {
        var userId = 0
        userId = FunSDK.GetId(userId, object : IFunSDKResult {
            override fun OnFunSDKResult(msg: Message?, ex: MsgContent?): Int {
                if (msg?.arg1 ?: -1 >= 0 && ex?.str == JsonConfig.CFG_DETECT_TRACK) {
                    callback.invoke(true)
                } else {
                    callback.invoke(false)
                }
                return 0
            }
        })
        val jsonData = HandleConfigData.getSendData(
            getFullName(JsonConfig.CFG_DETECT_TRACK, -1), "0x08", bean
        )
        FunSDK.DevSetConfigByJson(
            userId, devId, JsonConfig.CFG_DETECT_TRACK, jsonData, -1, TIME_OUT, 0
        )
    }

    fun getMotionDetect(devId: String, callback: (String) -> Unit) {
        val motionInfo = DevConfigInfo.create(object : OnDevManagerListener<Any?> {
            override fun onFailed(devId: String, msgId: Int, s1: String, errorId: Int) {
                callback.invoke("")
            }

            override fun onSuccess(p0: String?, p1: Int, result: Any?) {
                if (result is String) {
                    callback.invoke(result)
                } else {
                    callback.invoke(JSON.toJSONString(result))
                }
            }
        })
        motionInfo.jsonName = JsonConfig.DETECT_MOTIONDETECT
        motionInfo.chnId = 0
        val devConfigManager = DeviceManager.getInstance().getDevConfigManager(devId)
        devConfigManager.getDevConfig(motionInfo)
    }

    fun setMotionDetect(devId: String, jsonData: String?, callback: (Int) -> Unit) {
        if (jsonData == null) {
            return
        }
        val devConfigInfo = DevConfigInfo.create(object : OnDevManagerListener<Any?> {
            override fun onFailed(devId: String, msgId: Int, s1: String, errorId: Int) {
                callback.invoke(0)
            }

            override fun onSuccess(p0: String?, p1: Int, p2: Any?) {
                callback.invoke(1)
            }
        })
        devConfigInfo.jsonName = JsonConfig.DETECT_MOTIONDETECT
        devConfigInfo.chnId = 0
        devConfigInfo.jsonData = jsonData
        val devConfigManager = DeviceManager.getInstance().getDevConfigManager(devId)
        devConfigManager.setDevConfig(devConfigInfo)
    }

    fun rebootDev(devId: String, callback: (Boolean) -> Unit) {
        val devConfigInfo = DevConfigInfo.create(object : OnDevManagerListener<Any?> {
            override fun onFailed(devId: String, msgId: Int, s1: String, errorId: Int) {
                callback.invoke(false)
            }

            override fun onSuccess(p0: String?, p1: Int, p2: Any?) {
                callback.invoke(true)
            }
        })

        // 0表示重启 1表示关闭
        /*0 means restart and 1 means close*/
        val obj = com.alibaba.fastjson.JSONObject()
        obj.put("Action", "Reboot")
        devConfigInfo.jsonName = JsonConfig.OPERATION_OPMACHINE
        devConfigInfo.jsonData =
            HandleConfigData.getSendData(JsonConfig.OPERATION_OPMACHINE, "0x01", obj)
        devConfigInfo.cmdId = EDEV_JSON_ID.OPMACHINE
        devConfigInfo.chnId = -1
        val devConfigManager = DeviceManager.getInstance().getDevConfigManager(devId)
        devConfigManager.setDevCmd(devConfigInfo)
    }

    private fun dealWithSystemInfo(dataJson: String?) {
        try {

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getCardInfo(
        devId: String,
    ) = suspendCancellableCoroutine<Pair<Boolean, Int>> {
        var msgId = 0xff00ff
        msgId = FunSDK.GetId(msgId) { message, msgContent ->
            when (message.what) {
                EUIMSG.DEV_GET_JSON -> {
                    val jsonData = G.ToString(msgContent.pData)
                    if (StringUtils.contrast(msgContent.str, STORAGE_INFO)) {
                        if (jsonData == null) {
                            it.resumeWith(Result.success(false to message.arg1))
                        } else {
                            val handleConfigData: HandleConfigData<*> = HandleConfigData<Any?>()
                            if (handleConfigData.getDataObj(
                                    jsonData,
                                    StorageInfoBean::class.java
                                )
                            ) {
                                val data = handleConfigData.obj as? List<StorageInfoBean>
                                if (data?.isNotEmpty() == true) {
                                    val storageInfoBeans: List<StorageInfoBean> = data
                                    var sumRemainSize: Long = 0
                                    var sumTotalSize: Long = 0
                                    for (storageInfo in storageInfoBeans) {
                                        if (storageInfo.Partition.isNullOrEmpty()) {
                                            continue
                                        }
                                        for (partition in storageInfo.Partition) {
                                            if (null == partition) {
                                                continue
                                            }
                                            val remainSize = G.getLongFromHex(partition.RemainSpace)
                                            val totalSize = G.getLongFromHex(partition.TotalSpace)
                                            if (partition.DirverType == SDKCONST.SDK_FileSystemDriverTypes.SDK_DRIVER_READ_WRITE.toLong() || partition.DirverType == SDKCONST.SDK_FileSystemDriverTypes.SDK_DRIVER_IMPRCD.toLong()) {
                                                sumRemainSize += remainSize
                                                sumTotalSize += totalSize
                                            } else if (partition.DirverType == SDKCONST.SDK_FileSystemDriverTypes.SDK_DRIVER_SNAPSHOT.toLong()) {
                                                sumRemainSize += remainSize
                                                sumTotalSize += totalSize
                                            }
                                        }
                                    }
                                    if (sumTotalSize > 0) {
                                        it.resumeWith(Result.success(true to message.arg1))
                                    } else {
                                        it.resumeWith(Result.success(false to message.arg1))
                                    }
                                } else {
                                    it.resumeWith(Result.success(false to message.arg1))
                                }
                            } else {
                                it.resumeWith(Result.success(false to message.arg1))
                            }
                        }
                    }
                }
            }
            0
        }
        FunSDK.DevGetConfigByJson(
            msgId, devId, STORAGE_INFO, 1024, -1, TIME_OUT, 0
        )
    }

    private fun isSupportWifiRouteSignalLevel(devId: String) =
        FunSDK.GetDevAbility(devId, "NetServerFunction/WifiRouteSignalLevel") > 0

    fun getWifiRouteSignalLevel(devId: String, callback: (Int, Boolean) -> Unit) {
        if (!isSupportWifiRouteSignalLevel(devId)) return
        var userId = 0
        userId = FunSDK.GetId(userId) { msg, msgContent ->
            if (msg.what == EUIMSG.DEV_CMD_EN && WIFI_ROUTE_INFO == msgContent.str) {
                if (msgContent.pData != null) {
                    val handleConfigData = HandleConfigData<WifiRouteInfo>()
                    if (handleConfigData.getDataObj(
                            G.ToString(msgContent.pData), WifiRouteInfo::class.java
                        )
                    ) {
                        val wifiRouteInfo = handleConfigData.obj
                        if (wifiRouteInfo.isWlanStatus) {
                            callback.invoke(wifiRouteInfo.signalLevel, false)
                        } else if (wifiRouteInfo.isEth0Status) {
                            callback.invoke(-1, true)
                        }
                    }
                }
            }
            0
        }
        //如果支持设备WiFi信号强度获取的话，进行获取
        FunSDK.DevCmdGeneral(userId, devId, 1020, WIFI_ROUTE_INFO, -1, TIME_OUT, null, 0, 0)
    }

    fun getPresets(devId: String, callback: (List<ConfigGetPreset>) -> Unit) {
        val disablePreset = intArrayOf(99, 128, 100, 250)
        var userId = 0
        userId = FunSDK.GetId(userId) { msg, msgContent ->
            Log.d(TAG, "getPresets: msgContent = ${msgContent}")
            Log.d(TAG, "getPresets:msg = $msg")
            if (msg.what == EUIMSG.DEV_GET_JSON) {
                val data = HandleConfigData<List<ConfigGetPreset>>()
                val json = G.ToString(msgContent.pData)
                if (data.getDataObj(json, ConfigGetPreset::class.java)) {
                    Log.d(TAG, "getPresets: ${data.obj.size}")
                    val list = data.obj.toMutableList()
                    list.removeIf { disablePreset.contains(it.Id) }
                    callback.invoke(list)
                } else {
                    callback.invoke(listOf())
                }
            }
            0
        }
        FunSDK.DevGetConfigByJson(
            userId, devId, ConfigGetPreset.JSON_NAME, 1024, -1, TIME_OUT, 0
        )
    }

    fun getPresets(devId: String): Flow<List<ConfigGetPreset>> = flow {
        val disablePreset = intArrayOf(99, 128, 100, 250)
        var userId = 0
        userId = FunSDK.GetId(userId) { msg, msgContent ->
            CoroutineScope(Dispatchers.IO).launch {
                if (msg.what == EUIMSG.DEV_GET_JSON) {
                    val data = HandleConfigData<List<ConfigGetPreset>>()
                    val json = G.ToString(msgContent.pData)
                    if (data.getDataObj(json, ConfigGetPreset::class.java)) {
                        val list = data.obj.toMutableList()
                        list.removeIf { disablePreset.contains(it.Id) }
                        emit(list)
                    } else {
                        emit(mutableListOf<ConfigGetPreset>())
                    }
                } else {
                    emit(mutableListOf<ConfigGetPreset>())
                }
            }
            0
        }
        FunSDK.DevGetConfigByJson(
            userId, devId, ConfigGetPreset.JSON_NAME, 1024, -1, TIME_OUT, 0
        )
    }.flowOn(Dispatchers.IO)

    fun turnPreset(
        devId: String,
        presetId: Int,
        callback: (Boolean) -> Unit,
    ) {
        val mOPPTZControlBean = OPPTZControlBean()
        mOPPTZControlBean.Command = OPPTZControlBean.TURN_PRESET
        mOPPTZControlBean.Parameter.Channel = 0
        mOPPTZControlBean.Parameter.Preset = presetId
        var userId = 0
        userId = FunSDK.GetId(userId) { msg, msgContent ->
            if (msg.what == EUIMSG.DEV_CMD_EN) {
                callback.invoke(msg.arg1 > 0)
            }
            0
        }
        val data = HandleConfigData.getSendData(
            OPPTZControlBean.OPPTZCONTROL_JSONNAME, "0x08", mOPPTZControlBean
        )
        FunSDK.DevCmdGeneral(
            userId,
            devId,
            OPPTZControlBean.OPPTZCONTROL_ID,
            OPPTZControlBean.TURN_PRESET,
            1024,
            TIME_OUT,
            data.toByteArray(),
            -1,
            0
        )
    }


    fun addPreset(
        devId: String,
        presetId: Int,
        presetName: String,
        callback: (Boolean, OPPTZControlBean?) -> Unit,
    ) {
        val mOPPTZControlBean = OPPTZControlBean()
        mOPPTZControlBean.Command = OPPTZControlBean.SET_PRESET
        mOPPTZControlBean.Parameter.Channel = 0
        mOPPTZControlBean.Parameter.Preset = presetId
        mOPPTZControlBean.Parameter.PresetName = presetName
        var userId = 0
        userId = FunSDK.GetId(userId) { msg, msgContent ->
            if (msg.what == EUIMSG.DEV_CMD_EN) {
                callback.invoke(msg.arg1 > 0, mOPPTZControlBean)
            }
            0
        }
        val data = HandleConfigData.getSendData(
            OPPTZControlBean.OPPTZCONTROL_JSONNAME, "0x08", mOPPTZControlBean
        )
        FunSDK.DevCmdGeneral(
            userId,
            devId,
            OPPTZControlBean.OPPTZCONTROL_ID,
            OPPTZControlBean.SET_PRESET,
            1024,
            TIME_OUT,
            data.toByteArray(),
            -1,
            0
        )
    }

    fun editPreset(
        devId: String,
        presetId: Int,
        presetName: String,
        callback: (Boolean, OPPTZControlBean?) -> Unit,
    ) {
        val mOPPTZControlBean = OPPTZControlBean()
        mOPPTZControlBean.Command = OPPTZControlBean.EDIT_NAME
        mOPPTZControlBean.Parameter.Channel = 0
        mOPPTZControlBean.Parameter.Preset = presetId
        mOPPTZControlBean.Parameter.PresetName = presetName.trim()
        var userId = 0
        userId = FunSDK.GetId(userId) { msg, msgContent ->
            if (msg.what == EUIMSG.DEV_CMD_EN) {
                callback.invoke(msg.arg1 > 0, mOPPTZControlBean)
            }
            0
        }
        val data = HandleConfigData.getSendData(
            OPPTZControlBean.OPPTZCONTROL_JSONNAME, "0x08", mOPPTZControlBean
        )
        FunSDK.DevCmdGeneral(
            userId,
            devId,
            OPPTZControlBean.OPPTZCONTROL_ID,
            OPPTZControlBean.EDIT_NAME,
            1024,
            TIME_OUT,
            data.toByteArray(),
            -1,
            0
        )
    }

    suspend fun deletePreset(
        devId: String, presetId: Int,
    ) = suspendCancellableCoroutine<Pair<Boolean, Int>> {
        val mOPPTZControlBean = OPPTZControlBean()
        mOPPTZControlBean.Command = OPPTZControlBean.REMOVE_PRESET
        mOPPTZControlBean.Parameter.Channel = 0
        mOPPTZControlBean.Parameter.Preset = presetId
        var userId = 0
        userId = FunSDK.GetId(userId) { msg, msgContent ->
            if (msg.what == EUIMSG.DEV_CMD_EN) {
                it.resume(Pair(msg.arg1 > 0, mOPPTZControlBean.Parameter.Preset))
            }
            0
        }
        val data = HandleConfigData.getSendData(
            OPPTZControlBean.OPPTZCONTROL_JSONNAME, "0x08", mOPPTZControlBean
        )
        FunSDK.DevCmdGeneral(
            userId,
            devId,
            OPPTZControlBean.OPPTZCONTROL_ID,
            OPPTZControlBean.REMOVE_PRESET,
            1024,
            TIME_OUT,
            data.toByteArray(),
            -1,
            0
        )
    }

    fun getTour(devId: String, callback: (List<PTZTourBean>) -> Unit) {
        var userId = 0
        userId = FunSDK.GetId(userId) { msg, msgContent ->
            if (msg.what == EUIMSG.DEV_GET_JSON) {
                if (msg.arg1 > 0) {
                    val handleConfigData = HandleConfigData<List<PTZTourBean>>()
                    if (handleConfigData.getDataObj(
                            G.ToString(msgContent.pData),
                            PTZTourBean::class.java
                        )
                    ) {
                        val list = handleConfigData.obj.toMutableList()
                        list.first().apply {
                            Tour = Tour.distinctBy {
                                it.Id
                            }
                        }
                        callback.invoke(list)
                    } else {
                        callback.invoke(mutableListOf())
                    }
                } else {
                    callback.invoke(mutableListOf())
                }
            }
            0
        }
        FunSDK.DevGetConfigByJson(
            userId,
            devId,
            PTZTourBean.JSON_NAME,
            2048,
            -1,
            TIME_OUT,
            0
        )
    }

    fun addItemTour(
        devId: String,
        presetId: Int,
        tourId: Int,
        presetIndex: Int,
        callback: (Boolean) -> Unit,
    ) {
        val bean = com.lib.sdk.bean.tour.OPPTZControlBean()
        bean.Command = com.lib.sdk.bean.tour.OPPTZControlBean.ADD_TOUR
        bean.Parameter.Preset = presetId
        bean.Parameter.Tour = tourId
        bean.Parameter.PresetIndex = presetIndex
        bean.Parameter.Step = 3 // mặc định 3s
        bean.Parameter.TourTimes = 1 // mặc định 1 lần
        var userId = 0
        userId = FunSDK.GetId(userId) { msg, msgContent ->
            if (msg.what == EUIMSG.DEV_CMD_EN) {
                callback.invoke(msg.arg1 > 0)
            }
            0
        }
        FunSDK.DevCmdGeneral(
            userId,
            devId,
            com.lib.sdk.bean.tour.OPPTZControlBean.OPPTZCONTROL_ID,
            com.lib.sdk.bean.tour.OPPTZControlBean.ADD_TOUR,
            -1,
            TIME_OUT,
            HandleConfigData.getSendData(
                com.lib.sdk.bean.tour.OPPTZControlBean.OPPTZCONTROL_JSONNAME,
                "0x08",
                bean
            ).toByteArray(),
            -1,
            0
        )
    }

    fun deleteItemTour(
        devId: String,
        presetId: Int,
        tourId: Int,
        callback: (Boolean) -> Unit,
    ) {
        val bean = com.lib.sdk.bean.tour.OPPTZControlBean()
        bean.Command = com.lib.sdk.bean.tour.OPPTZControlBean.DELETE_TOUR
        bean.Parameter.Preset = presetId
        bean.Parameter.Tour = tourId
        bean.Parameter.Step = 3 // mặc định 3s
        bean.Parameter.TourTimes = 1 // mặc định 1 lần
        var userId = 0
        userId = FunSDK.GetId(userId) { msg, msgContent ->
            if (msg.what == EUIMSG.DEV_CMD_EN) {
                if (msg.what == EUIMSG.DEV_CMD_EN) {
                    callback.invoke(msg.arg1 > 0)
                }
            }
            0
        }
        FunSDK.DevCmdGeneral(
            userId,
            devId,
            com.lib.sdk.bean.tour.OPPTZControlBean.OPPTZCONTROL_ID,
            com.lib.sdk.bean.tour.OPPTZControlBean.DELETE_TOUR,
            -1,
            TIME_OUT,
            HandleConfigData.getSendData(
                com.lib.sdk.bean.tour.OPPTZControlBean.OPPTZCONTROL_JSONNAME,
                "0x08",
                bean
            ).toByteArray(),
            -1,
            0
        )
    }

    fun startTour(
        devId: String,
        tourId: Int,
        callback: (isTouring: Boolean) -> Unit,
    ) {
        val bean = com.lib.sdk.bean.tour.OPPTZControlBean()
        bean.Command = com.lib.sdk.bean.tour.OPPTZControlBean.START_TOUR
        bean.Parameter.Preset = 0
        bean.Parameter.Tour = tourId
        bean.Parameter.Step = 3 // mặc định 3s
        bean.Parameter.TourTimes = 1 // mặc định 1 lần
        var userId = 0
        userId = FunSDK.GetId(userId) { msg, msgContent ->
            if (msg.what == EUIMSG.DEV_CMD_EN) {
                callback.invoke(msg.arg1 > 0)
            } else if (msg.what == EUIMSG.DEV_PTZ_CONTROL) {
                if (msg.arg1 == com.lib.sdk.bean.tour.OPPTZControlBean.PTZ_TOUR_END_RSP_ID) {
                    callback.invoke(false)
                }
            }
            0
        }
        FunSDK.DevCmdGeneral(
            userId,
            devId,
            com.lib.sdk.bean.tour.OPPTZControlBean.OPPTZCONTROL_ID,
            com.lib.sdk.bean.tour.OPPTZControlBean.START_TOUR,
            -1,
            TIME_OUT,
            HandleConfigData.getSendData(
                com.lib.sdk.bean.tour.OPPTZControlBean.OPPTZCONTROL_JSONNAME,
                "0x08",
                bean
            ).toByteArray(),
            -1,
            0
        )
    }

    fun stopTour(
        devId: String,
        callback: (isTouring: Boolean) -> Unit,
    ) {
        val bean = com.lib.sdk.bean.tour.OPPTZControlBean()
        bean.Command = com.lib.sdk.bean.tour.OPPTZControlBean.STOP_TOUR
        bean.Parameter.Preset = 0
        bean.Parameter.Tour = 0
        bean.Parameter.Step = 3 // mặc định 3s
        bean.Parameter.TourTimes = 1 // mặc định 1 lần
        var userId = 0
        userId = FunSDK.GetId(userId) { msg, msgContent ->
            if (msg.what == EUIMSG.DEV_CMD_EN) {
                callback.invoke(msg.arg1 <= 0)
            }
            0
        }
        FunSDK.DevCmdGeneral(
            userId,
            devId,
            com.lib.sdk.bean.tour.OPPTZControlBean.OPPTZCONTROL_ID,
            com.lib.sdk.bean.tour.OPPTZControlBean.STOP_TOUR,
            -1,
            TIME_OUT,
            HandleConfigData.getSendData(
                com.lib.sdk.bean.tour.OPPTZControlBean.OPPTZCONTROL_JSONNAME,
                "0x08",
                bean
            ).toByteArray(),
            -1,
            0
        )
    }
}

enum class CommandJF(val cmd: String) {
    REBOOT("RebootDev"), RESET("ResetConfig")
}



