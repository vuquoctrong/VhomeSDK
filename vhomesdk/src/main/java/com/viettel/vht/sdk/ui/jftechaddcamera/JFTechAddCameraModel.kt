package com.viettel.vht.sdk.ui.jftechaddcamera

import android.app.Application
import android.graphics.Bitmap
import android.net.DhcpInfo
import android.net.wifi.WifiInfo
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import android.text.format.Formatter
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.basic.G
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lib.EFUN_ERROR
import com.lib.EUIMSG
import com.lib.FunSDK
import com.lib.IFunSDKResult
import com.lib.MsgContent
import com.lib.sdk.bean.SystemInfoBean
import com.lib.sdk.struct.SDK_CONFIG_NET_COMMON_V2
import com.manager.account.BaseAccountManager.OnAccountManagerListener
import com.manager.account.XMAccountManager
import com.manager.db.XMDevInfo
import com.manager.device.DeviceManager
import com.manager.device.DeviceManager.OnDevWiFiSetListener
import com.manager.device.DeviceManager.OnSearchLocalDevListener
import com.utils.XMWifiManager
import com.utils.XUtils
import com.vht.sdkcore.base.BaseViewModel
import com.vht.sdkcore.pref.RxPreferences
import com.vht.sdkcore.utils.SingleLiveEvent
import com.viettel.vht.sdk.jfmanager.JFCameraManager
import com.viettel.vht.sdk.model.CheckOwnerResponse
import com.viettel.vht.sdk.model.DeviceDataResponse
import com.viettel.vht.sdk.model.home.OrganizationResponse
import com.viettel.vht.sdk.network.repository.AddDeviceRepository
import com.viettel.vht.sdk.ui.jftechaddcamera.model.DeviceInfoResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class JFTechAddCameraModel @Inject constructor(
    private val application: Application,
    private val rxPreferences: RxPreferences,
    private val addDeviceRepository: AddDeviceRepository,
) : BaseViewModel(), OnSearchLocalDevListener, IFunSDKResult {
    val TAG = JFTechAddCameraModel::class.java.simpleName
    val deviceManager: DeviceManager
    private val accountManager: XMAccountManager
    val listDeviceInfo = MutableLiveData<List<DeviceInfoResponse>>(listOf())
    val localDevList = MediatorLiveData<List<XMDevInfo>>().apply {
//        if (listDeviceInfo != null && listDeviceInfo.value?.isNotEmpty() == true) {
        addSource(listDeviceInfo) { listDevice ->
            if (listDevice == null || listDevice.isEmpty()) {
                return@addSource
            }
            value?.forEach { devInfo ->
                listDevice.forEach { device ->
                    if (devInfo.devId == device.deviceInfo?.sn) {
                        val strArr: List<String> = device.deviceInfo?.version!!.split(".");
                        val deviceType = strArr[3].substring(strArr[3].length - 3)
                        val systemInfoBean: SystemInfoBean = SystemInfoBean()
                        if (deviceType == "7B6") { // indoor
                            systemInfoBean.deviceModel = "R80XV20"
                        } else if (deviceType == "678") { //outdoor
                            systemInfoBean.deviceModel = "80X20"
                        }
                        devInfo.systemInfoBean = systemInfoBean
                        return@forEach
                    }
                }

            }
        }
//        }

    }
    var phoneNumber = ""

    init {
        deviceManager = DeviceManager.getInstance()
        accountManager = XMAccountManager.getInstance()
        phoneNumber = rxPreferences.getUserPhoneNumber()
    }

    val uistate = SingleLiveEvent<UIState>()

    private val exceptionHandler = CoroutineExceptionHandler { _, _ ->
        uistate.postValue(UIState.Error("Có lỗi Coroutine xảy ra"))
    }

    val wifiSSIDLiveData = MutableLiveData<String>()

    val wifiPassLiveData = MutableLiveData<String>()

    var devID = ""

    var devInfo: XMDevInfo? = null

    var cameraType = ""

    var dhcpInfo: DhcpInfo? = null

    var phoneNumberAccountOther: String? = ""

    private var userId = 0

    init {
        userId = FunSDK.GetId(userId, this)
    }


    fun startSetDevToRouterByQrCode(
        successAdd: ((Boolean, Int?) -> Unit),
        failedAdd: ((Boolean, String?) -> Unit),
        onDevWiFiState: ((Boolean) -> Unit)
    ): Bitmap? {
        val xmWifiManager = XMWifiManager.getInstance(application)
        var wifiPwd = wifiPassLiveData.value.toString().trim()
        val wifiInfo: WifiInfo = xmWifiManager.wifiInfo
        var dhcpInfo: DhcpInfo = xmWifiManager.dhcpInfo
        val ssid: String = wifiSSIDLiveData.value.toString().trim()
        val scanResult =
            xmWifiManager.getCurScanResult(ssid) //The ScanResult object obtained through the ssid contains more network information objects
        if (dhcpInfo == null && this.dhcpInfo != null) {
            dhcpInfo = this.dhcpInfo!!
        }
//        if (scanResult == null || dhcpInfo == null) {
//            Timber.d("startSetDevToRouterByQrCode4")
//            return null
//        }
//
//        Timber.d("scanResult: ${scanResult.BSSID}")
//        Timber.d("dhcpInfo: ${dhcpInfo.ipAddress}")
        var pwdType = 1
        if (scanResult != null) {
            var pwdType = XUtils.getEncrypPasswordType(scanResult.capabilities)
            if (pwdType == 3 && (wifiPwd.length == 10 || wifiPwd.length == 26)) {
                wifiPwd = XUtils.asciiToString(wifiPwd)
            }
            if (wifiPwd == null || TextUtils.isEmpty(wifiPwd) || wifiPwd == "null") {
                wifiPwd = ""
                pwdType = 0
            }
        }


        val ipAddress = if(dhcpInfo != null) Formatter.formatIpAddress(dhcpInfo.ipAddress) else ""
//        val macAddress = XMWifiManager.getWiFiMacAddress().replace(":", "")
        val macAddress = XUtils.getWiFiMacAddress().replace(":", "")
//        var macAddress = "020000000"
        Timber.d("ssid: $ssid")
        Timber.d("wifiPwd: $wifiPwd")
        Timber.d("pwdType: $pwdType")
        Timber.d("ipAddress: $ipAddress")
        Timber.d("macAddress: $macAddress")

        val bitmap: Bitmap = deviceManager.initDevToRouterByQrCode(
            ssid,
            wifiPwd,
            pwdType,
            macAddress,
            ipAddress,
            object : OnDevWiFiSetListener {
                override fun onDevWiFiSetState(result: Int) {
                    println("result:$result")
                    //kiểm tra thiết bị đã có wifi chưa
                    if (result < 0) {
                        Handler(Looper.getMainLooper()).postDelayed(
                            { deviceManager.startDevToRouterByQrCode() },
                            2000
                        )
                    }
                }

                override fun onDevWiFiSetResult(xmDevInfo: XMDevInfo) {
                    Timber.d("onDevWiFiSetResult: ${Gson().toJson(xmDevInfo)}")
                    onDevWiFiState.invoke(true)
                    JFCameraManager.setPasswordCamera(xmDevInfo.devId, "")
                    val checkOwnerResponse = checkOwnerCameraJF(xmDevInfo.devId)
                    phoneNumber = rxPreferences.getUserPhoneNumber()
                    Timber.d("phoneNumber: $phoneNumber")
                    if (checkOwnerResponse != null && (TextUtils.isEmpty(checkOwnerResponse.data) || phoneNumber == checkOwnerResponse.data)) {
                        object : Thread() {
                            override fun run() {
                                super.run()
                                val v2 = SDK_CONFIG_NET_COMMON_V2()
                                val data = G.ObjToBytes(v2)
                                //LAN search device,3000 is timeout
                                var iRet = FunSDK.DevLANSearch(xmDevInfo.devId, 3000, data)
                                //1 represents that the device is within the local area network, otherwise it is not
                                if (iRet == 1) {
                                    //Detect TCP services for LAN devices ,15 * 1000 is timeout
                                    iRet = FunSDK.DevIsDetectTCPService(data, 25 * 1000)
                                    //0: Connection failed 1: Connection succeeded
                                    if (iRet == 1) {
                                        // //After successful connection, proceed with device addition
                                        Handler(Looper.getMainLooper()).post {
                                            //If the device name is empty, then the default device serial number
                                            if (XUtils.isEmpty(xmDevInfo.devName)) {
                                                xmDevInfo.devName = xmDevInfo.devId
                                            } else {
                                                val devName = G.ToString(xmDevInfo.sdbDevInfo.st_1_Devname)
                                                if (XUtils.isEmpty(devName)) {
                                                    G.SetValue(xmDevInfo.sdbDevInfo.st_1_Devname,xmDevInfo.devName)
                                                }
                                            }

                                            //Here we can handle some of the processes after the successful distribution network，For example, accessing some configuration information of the device
                                            XMAccountManager.getInstance()
                                                .addDev(xmDevInfo, object : OnAccountManagerListener {
                                                    override fun onSuccess(msgId: Int) {
                                                        devID = xmDevInfo.devId
                                                        devInfo = xmDevInfo
                                                        successAdd.invoke(true, null)
                                                    }

                                                    override fun onFailed(msgId: Int, errorId: Int) {
                                                        if (msgId == EUIMSG.SYS_ADD_DEVICE && errorId != EFUN_ERROR.EE_ACCOUNT_DEVICE_ADD_NOT_UNIQUE) {
                                                            successAdd.invoke(false, errorId)
                                                        }
                                                        Timber.d("onDevWiFiSetResult: $msgId - $errorId")
                                                    }

                                                    override fun onFunSDKResult(
                                                        msg: Message,
                                                        ex: MsgContent
                                                    ) {
                                                        Timber.d("onDevWiFiSetResult1: ${msg.toString()} - ${ex.toString()}")
                                                        if (msg.what == EUIMSG.SYS_ADD_DEVICE && msg.arg1 == EFUN_ERROR.EE_ACCOUNT_DEVICE_ADD_NOT_UNIQUE) {
                                                            phoneNumberAccountOther = ex.str
                                                            successAdd.invoke(false, EFUN_ERROR.EE_ACCOUNT_DEVICE_ADD_NOT_UNIQUE)
                                                        }
                                                    }
                                                })
                                        }
                                    }
                                }
                                Timber.d("onDevWiFiSetResult_iRet: $iRet")
                            }
                        }.start()
                    } else {
                        failedAdd(true, checkOwnerResponse?.data)
                        Timber.e("Thiết bị đã được bindding")
                    }
//                    JFCameraManager.deleteDevice(xmDevInfo.devId)
//                    FunSDK.DevLANSearch(xmDevInfo.devId, 5000, G.ObjToBytes(SDK_CONFIG_NET_COMMON_V2()))

                    // failedAdd.invoke(xmDevInfo)
                }
            })
        deviceManager.startDevToRouterByQrCode()
        return bitmap
    }

    fun searchLanDevice() {
        deviceManager.searchLanDevice(this)
        FunSDK.DevSearchDevice(userId, 0, 0)
    }

    fun addDeviceToAccount(xmDevInfo: XMDevInfo, callback: (Boolean, Int) -> Unit, bindingFail:(String?) -> Unit) {
        FunSDK.DevLANSearch(xmDevInfo.devId, 5000, G.ObjToBytes(SDK_CONFIG_NET_COMMON_V2()))
        //If the device name is empty, then the default device serial number
        if (XUtils.isEmpty(xmDevInfo.devName)) {
            xmDevInfo.devName = xmDevInfo.devId
        } else {
            val devName = G.ToString(xmDevInfo.sdbDevInfo.st_1_Devname)
            if (XUtils.isEmpty(devName)) {
                G.SetValue(xmDevInfo.sdbDevInfo.st_1_Devname,xmDevInfo.devName)
            }
        }
        val checkOwnerResponse = checkOwnerCameraJF(xmDevInfo.devId)
        phoneNumber = rxPreferences.getUserPhoneNumber()
        Timber.d("phoneNumber: $phoneNumber")
        if (checkOwnerResponse != null && (TextUtils.isEmpty(checkOwnerResponse.data) || phoneNumber == checkOwnerResponse.data)) {
            accountManager.addDev(xmDevInfo, object : OnAccountManagerListener {
                override fun onSuccess(msgId: Int) {
                    if (msgId == EUIMSG.SYS_ADD_DEVICE) {
                        if (xmDevInfo.systemInfoBean == null || TextUtils.isEmpty(xmDevInfo.systemInfoBean.deviceModel)) {
                            if (listDeviceInfo.value?.isNotEmpty() == true) {
                                listDeviceInfo.value?.forEach { item ->
                                    if (item.deviceInfo != null && xmDevInfo.devId == item.deviceInfo.sn) {
                                        val strArr: List<String> = item.deviceInfo.version!!.split(".");
                                        val deviceType = strArr[3].substring(strArr[3].length - 3)
                                        val systemInfoBean: SystemInfoBean = SystemInfoBean()
                                        if (deviceType == "7B6") { // indoor
                                            systemInfoBean.deviceModel = "R80XV20"
                                        } else if (deviceType == "678") { //outdoor
                                            systemInfoBean.deviceModel = "80X20"
                                        }
                                        xmDevInfo.systemInfoBean = systemInfoBean
                                    }
                                }
                            }
                        }
                        devID = xmDevInfo.devId
                        devInfo = xmDevInfo
                        callback(true, 0)
                    }
                }

                override fun onFailed(msgId: Int, errorId: Int) {
                    Timber.d("onFailed: $errorId - $msgId")
                    if (msgId == EUIMSG.SYS_ADD_DEVICE && errorId != EFUN_ERROR.EE_ACCOUNT_DEVICE_ADD_NOT_UNIQUE) {
                        callback(false, errorId)
                    }
                }

                override fun onFunSDKResult(msg: Message?, ex: MsgContent?) {
                    Timber.d("addDeviceToAccount_onFunSDKResult: ${msg.toString()} - ${ex?.str}")
                    if (msg?.what == EUIMSG.SYS_ADD_DEVICE && msg.arg1 == EFUN_ERROR.EE_ACCOUNT_DEVICE_ADD_NOT_UNIQUE) {
                        phoneNumberAccountOther = ex?.str
                        callback(false, EFUN_ERROR.EE_ACCOUNT_DEVICE_ADD_NOT_UNIQUE)
                    }
                }
            })
        } else {
            bindingFail(checkOwnerResponse?.data)
            Timber.e("Thiết bị đã được bindding")
        }

    }

    fun createCameraIOTPlatform(serialNumber: String, isInDoor: Boolean) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            addDeviceRepository.createCameraJF(serialNumber, serialNumber, rxPreferences.getOrgIDAccount(), isInDoor)
                .collect { result ->
                    if (result.isSuccess && result.getOrNull() != null) {
                        uistate.postValue(UIState.CreateDeviceIOTPlatformSuccess(result.getOrNull()!!))
                    } else {
                        uistate.postValue(UIState.Error(result.exceptionOrNull()?.message))
                    }
                }
        }
    }

    fun checkOwnerCameraJF(serialNumber: String) : CheckOwnerResponse? = runBlocking {
        addDeviceRepository.checkOwnerCameraJF(serialNumber)
    }

    private fun currentOrgId(): String {
        val org: OrganizationResponse? = try {
            Gson().fromJson(rxPreferences.getCurrentFamilyHome(), OrganizationResponse::class.java)
        } catch (e: Exception) {
            null
        }
        return org?.id ?: ""
    }

    override fun onSearchLocalDevResult(localDevList: MutableList<XMDevInfo>?) {
        if (localDevList != null) {
            if (listDeviceInfo.value?.isNotEmpty() == true) {
                localDevList.forEach { item ->
                    listDeviceInfo.value?.forEach { devInfo ->
                        if (item.devId == devInfo.deviceInfo?.sn) {
                            val strArr: List<String> = devInfo.deviceInfo?.version!!.split(".");
                            val deviceType = strArr[3].substring(strArr[3].length - 3)
                            val systemInfoBean: SystemInfoBean = SystemInfoBean()
                            if (deviceType == "7B6") { // indoor
                                systemInfoBean.deviceModel = "R80XV20"
                            } else if (deviceType == "678") { //outdoor
                                systemInfoBean.deviceModel = "80X20"
                            }
                            item.systemInfoBean = systemInfoBean
                            return@forEach
                        }
                    }
                }
            }
            this.localDevList.postValue(localDevList)
        }
    }


    sealed class UIState {
        class CreateDeviceIOTPlatformSuccess(val device: DeviceDataResponse) : UIState()
        class Error(val message: String?) : UIState()
    }

    override fun OnFunSDKResult(msg: Message?, ex: MsgContent?): Int {
        if (msg?.what == EUIMSG.DEV_SEARCH_DEVICES) {
            if (msg.arg1 < 0) {
                uistate.postValue(UIState.Error("Error search lan device"))
                return 0
            }
            if (msg.arg2 == 0) {
                Timber.d("No_device")
                listDeviceInfo.postValue(listOf())
            } else {
                if (ex != null && !TextUtils.isEmpty(ex.str)) {
                    val jsonObj = JSONObject(ex?.str)
                    val strObj = jsonObj.getString("DevSNList")
                    if (!TextUtils.isEmpty(strObj)) {
                        val dataType = object : TypeToken<List<DeviceInfoResponse>>() {}.type
                        listDeviceInfo.postValue(Gson().fromJson(strObj, dataType))
//                        Timber.d("listDeviceInfo: ${Gson().toJson(listDeviceInfo)}")
                    }
                }
                Timber.d("DevSNListJsonStr: ${ex?.str}")
            }
        }

        return 0
    }

    fun rememberWifi(ssid: String, password: String) {
        rxPreferences.rememberPasswordWifi(ssid, password)
    }

    fun forgotWifi(ssid: String) {
        rxPreferences.rememberPasswordWifi(ssid, "")
    }

    fun getWifiPassword(ssid: String): String {
        return rxPreferences.getRememberPasswordWifi(ssid) ?: ""
    }
}