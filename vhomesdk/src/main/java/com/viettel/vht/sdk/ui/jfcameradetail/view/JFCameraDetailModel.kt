package com.viettel.vht.sdk.ui.jfcameradetail.view

import android.app.Application
import android.os.SystemClock
import android.util.Log
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lib.FunSDK
import com.lib.SDKCONST
import com.lib.sdk.bean.CameraParamBean
import com.lib.sdk.bean.DetectTrackBean
import com.lib.sdk.bean.HumanDetectionBean
import com.lib.sdk.bean.OPPTZControlBean
import com.lib.sdk.bean.PtzCtrlInfoBean
import com.lib.sdk.bean.preset.ConfigGetPreset
import com.lib.sdk.bean.tour.PTZTourBean
import com.lib.sdk.bean.tour.TourBean
import com.manager.db.DevDataCenter
import com.manager.device.DeviceManager
import com.manager.device.DeviceManager.OnDevManagerListener
import com.manager.device.media.attribute.PlayerAttribute
import com.manager.device.media.monitor.MonitorManager
import com.vht.sdkcore.base.BaseViewModel
import com.vht.sdkcore.pref.RxPreferences
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.SingleLiveEvent
import com.vht.sdkcore.utils.Utils
import com.vht.sdkcore.utils.eventbus.RxEvent
import com.viettel.vht.main.model.ptz_jf.ItemPTZ
import com.viettel.vht.main.model.ptz_jf.UpdateListPTZRequest
import com.viettel.vht.sdk.jfmanager.JFCameraManager
import com.viettel.vht.sdk.model.DeviceResponse
import com.viettel.vht.sdk.model.camera_cloud.PricingCloudResponse
import com.viettel.vht.sdk.network.repository.CloudRepository
import com.viettel.vht.sdk.network.repository.HomeRepository
import com.viettel.vht.sdk.utils.Config
import com.viettel.vht.sdk.utils.DebugConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import javax.inject.Inject

@HiltViewModel
class JFCameraDetailModel @Inject constructor(
    private val application: Application,
    private val rxPreferences: RxPreferences,
    private val cloudRepository: CloudRepository,
    private val homeRepository: HomeRepository
) : BaseViewModel() {
    lateinit var devId: String
    lateinit var deviceId: String
    var currentCameraParamBean: CameraParamBean? = null
    private val deviceManager: DeviceManager
    lateinit var mediaManager: MonitorManager
    var isDisableVolume = MutableLiveData(false)
    var currentHumanDetectionBean: HumanDetectionBean? = null
    var currentDetectTrackBean: DetectTrackBean? = null
    var actionReloadEvent = SingleLiveEvent<Boolean>()
    val openOptionLiveEvent = SingleLiveEvent<RxEvent<ItemPreset>>()
    var presets = mutableListOf<ConfigGetPreset>()
    var listTour = mutableListOf<PTZTourBean>()
    var tour: PTZTourBean = PTZTourBean()
    var selectedPresetTour = mutableListOf<Int>()

    val responseSystemFunction = SingleLiveEvent<Int>()

    init {
        deviceManager = DeviceManager.getInstance()
        registerEvent()
    }

    private val TAG = JFCameraDetailModel::class.simpleName.toString()

    fun isSupportPtz(callback: (Boolean) -> Unit) {
        DevDataCenter.getInstance()
            ?.isSupportSupportPTZTour(devId, object : OnDevManagerListener<Any?> {
                override fun onSuccess(devId: String, operationType: Int, result: Any?) {
                    DebugConfig.log(
                        TAG,
                        "isSupportSupportPTZTour onSucces $devId $operationType $result"
                    )
                    callback(result as Boolean)
                }

                override fun onFailed(devId: String, msgId: Int, jsonName: String, errorId: Int) {
                    DebugConfig.log(TAG, "isSupportSupportPTZTour onFailed $errorId")
                    callback(false)
                }
            })
    }

    fun initMonitor(devId: String, viewGroup: ViewGroup) {
        mediaManager = deviceManager.createMonitorPlayer(viewGroup, devId)
        mediaManager.setHardDecode(false)
        mediaManager.setChnId(0)
    }

    fun loginDev(devId: String, onSuccess: (() -> Unit), onFailed: ((Int) -> Unit)) {
        deviceManager.loginDev(devId, object : OnDevManagerListener<Any?> {
            override fun onSuccess(devId: String, operationType: Int, result: Any?) {
                DebugConfig.log(TAG, "loginDev success")
                onSuccess.invoke()
            }

            override fun onFailed(devId: String, msgId: Int, jsonName: String, errorId: Int) {
                DebugConfig.log(TAG, "loginDev onFailed $errorId")
                onFailed.invoke(errorId)
            }
        })

    }

    fun isPlaying() = mediaManager.playState == PlayerAttribute.E_STATE_PlAY

    fun startMonitor() {
        mediaManager.streamType = SDKCONST.StreamType.Main
        mediaManager.setRealTimeEnable(true)
        mediaManager.startMonitor() //Start Monitoring
        openVoice()
    }

    fun stopMonitor() {
        mediaManager.stopPlay()
        mediaManager.destroyTalk()
    }

    fun destroyMonitor() {
        mediaManager.destroyPlay()
        mediaManager.release()
    }

    fun devicePTZControl(currentActionPTZ: ActionControlPTZ) {
        val nPTZCommand = when (currentActionPTZ.action) {
            ActionControlPTZ.UP.action -> {
                0
            }

            ActionControlPTZ.DOWN.action -> {
                1
            }

            ActionControlPTZ.RIGHT.action -> {
                2
            }

            ActionControlPTZ.LEFT.action -> {
                3
            }

            else -> {
                0
            }
        }
        val bStop = currentActionPTZ.action == ActionControlPTZ.STOP.action
        val ptzCtrlInfoBean = PtzCtrlInfoBean()
        ptzCtrlInfoBean.devId = mediaManager.devId
        ptzCtrlInfoBean.ptzCommandId = nPTZCommand
        ptzCtrlInfoBean.isStop = bStop
        Log.e("Trong", "data${ptzCtrlInfoBean.ptzCommandId}")
        deviceManager.devPTZControl(ptzCtrlInfoBean, null)
    }

    fun openVoice(isTalking: Boolean = false) {
        if (mediaManager != null) {
            if (isTalking) {
                // đang talk + bật âm thanh
                mediaManager.doubleDirectionSound(application, true)
                mediaManager.closeVoiceBySound()
            } else {
                mediaManager.openVoiceBySound()
            }
        }
    }

    fun closeVoice(isTalking: Boolean = false) {
        if (mediaManager != null) {
            if (isTalking) {
                // đang talk + tắt âm thanh
                mediaManager.doubleDirectionSound(application, false)
            }
            mediaManager.closeVoiceBySound()
        }
    }

    fun startDoubleIntercom() {
        mediaManager.startTalkByDoubleDirection(application, true)
        openVoice(true)
        isDisableVolume.value = false
    }

    fun stopIntercom() {
        mediaManager.destroyTalk()
        if (isDisableVolume.value == false) {
            mediaManager.openVoiceBySound()
        } else {
            mediaManager.closeVoiceBySound()
        }
    }

    fun saveImageToGallery(path: String, devId: String) {
        io.launch {
            Utils.saveImageToGalleryJFCamera(application.applicationContext, path, devId)
        }
    }

    fun saveVideoToGallery(path: String, devId: String) {
        io.launch {
            Utils.saveVideoToGalleryJFCamera(application.applicationContext, path, devId)
        }
    }

    fun flipCamera(callback: (Int) -> Unit) {
        JFCameraManager.flipCamera(devId, currentCameraParamBean, callback)
    }

    suspend fun checkCloudStorageRegistered(serial: String) =
        cloudRepository.checkRegistedCloud(serial)

    fun setHumanDetection(newState: Boolean, callback: (Boolean) -> Unit) {
        currentHumanDetectionBean?.isEnable = newState
        JFCameraManager.setHumanDetection(devId, currentHumanDetectionBean) {
            if (!it) {
                currentHumanDetectionBean?.isEnable = !newState
            }
            callback.invoke(it)
        }
    }

    fun setDetectTrack(newState: Boolean, callback: (Boolean) -> Unit) {
        currentDetectTrackBean?.enable = if (newState) {
            1
        } else {
            0
        }
        JFCameraManager.setDetectTrack(devId, currentDetectTrackBean, callback)
    }

    fun getInfoSettingCameraJF(devId: String, callback: (String) -> Unit) {
        JFCameraManager.getDevInfo(devId, callback)
    }

    private val _listPricingCloud =
        SingleLiveEvent<com.vht.sdkcore.network.Result<PricingCloudResponse>>()
    val dataListPricingCloud: SingleLiveEvent<com.vht.sdkcore.network.Result<PricingCloudResponse>> get() = _listPricingCloud

    fun getListPricingPaymentCloud() {
        viewModelScope.launch {
            homeRepository.getListPricingPaymentCloud().collect {
                _listPricingCloud.value = it
            }
        }
    }


    private val _listCameraLiveData =
        SingleLiveEvent<com.vht.sdkcore.network.Result<DeviceResponse>>()
    val listCameraLiveData: SingleLiveEvent<com.vht.sdkcore.network.Result<DeviceResponse>> get() = _listCameraLiveData

    fun getListCamera() {
        viewModelScope.launch {
            homeRepository.getDeviceByOrg(rxPreferences.getOrgIDAccount()).collect { it ->
                _listCameraLiveData.value = it
            }
        }
    }

    fun getTourAndPresets(callback: (MutableList<ConfigGetPreset>) -> Unit) {
        JFCameraManager.getTour(devId) { tours ->
            val listTour = tours.toMutableList()
            this.listTour = listTour
            if (listTour.isNotEmpty()) {
                tour = listTour[0]
            }
            JFCameraManager.getPresets(devId) { presets ->
                val listPresets = presets.toMutableList()
                callback.invoke(listPresets)
                this.presets = listPresets
                syncPTZ()
            }
        }
    }

    fun addPreset(
        presetName: String,
        callback: (Boolean, OPPTZControlBean?) -> Unit
    ) {
        if (!isPlaying()) {
            callback.invoke(false, null)
            return
        }
        JFCameraManager.addPreset(devId, getSuitablePresetPosition(), presetName, callback)
    }

    fun editNamePreset(
        presetId: Int,
        presetName: String,
        callback: (Boolean, OPPTZControlBean?) -> Unit
    ) {
        if (!isPlaying()) {
            callback.invoke(false, null)
            return
        }
        JFCameraManager.editPreset(devId, presetId, presetName, callback)
    }

    fun updatePreset(
        presetId: Int,
        presetName: String,
        callback: (Boolean, OPPTZControlBean?) -> Unit
    ) {
        if (!isPlaying()) {
            callback.invoke(false, null)
            return
        }
        JFCameraManager.addPreset(devId, presetId, presetName, callback)
    }

    fun syncPTZ() {
        viewModelScope.launch {
            val listPreset = presets.map {
                ItemPTZ(it.Id.toString(), it.PresetName)
            }
            val isSuccess = homeRepository.updateListPTZ(UpdateListPTZRequest(devId, listPreset))
            Log.d(TAG, "syncPTZ: isSuccess = $isSuccess")
        }
    }

    fun turnPreset(
        presetId: Int,
        callback: (Boolean) -> Unit
    ) {
        JFCameraManager.turnPreset(devId, presetId, callback)
    }

    fun selectItemPatrol(
        presetId: Int,
        callback: (MutableList<TourBean>) -> Unit
    ) {
        if (tour.Tour.isNullOrEmpty()) {
            Log.d("TOUR_DEBUG", "selectItemPatrol: empty tour")
            JFCameraManager.addItemTour(devId, presetId, tour.Id, 0) {
                Log.d("TOUR_DEBUG", "selectItemPatrol: addItemTour = $it")
                JFCameraManager.getTour(devId) { presets ->
                    val listTour = presets.toMutableList()
                    this.listTour = listTour
                    Log.d("TOUR_DEBUG", "selectItemPatrol: addItemTour listTour = ${listTour.size}")
                    if (listTour.isNotEmpty()) {
                        tour = listTour.get(0)
                        Log.d(
                            "TOUR_DEBUG",
                            "selectItemPatrol: addItemTour tour = ${tour.Tour.size}"
                        )
                    }
                    callback.invoke(tour.Tour)
                }
            }
        } else {
            if (tour.Tour.any { it.Id == presetId }) {
                JFCameraManager.deleteItemTour(devId, presetId, tour.Id) {
                    Log.d("TOUR_DEBUG", "selectItemPatrol: deleteItemTour = $it")
                    JFCameraManager.getTour(devId) { presets ->
                        val listTour = presets.toMutableList()
                        this.listTour = listTour
                        Log.d(
                            "TOUR_DEBUG",
                            "selectItemPatrol: deleteItemTour listTour = ${listTour.size}"
                        )
                        if (listTour.isNotEmpty()) {
                            tour = listTour.get(0)
                            Log.d(
                                "TOUR_DEBUG",
                                "selectItemPatrol: deleteItemTour tour = ${tour.Tour.size}"
                            )
                        }
                        callback.invoke(tour.Tour)
                    }
                }
            } else {
                JFCameraManager.addItemTour(devId, presetId, tour.Id, tour.Tour.size) {
                    Log.d("TOUR_DEBUG", "selectItemPatrol: addItemTour = $it")
                    JFCameraManager.getTour(devId) { presets ->
                        val listTour = presets.toMutableList()
                        this.listTour = listTour
                        Log.d(
                            "TOUR_DEBUG",
                            "selectItemPatrol: addItemTour listTour = ${listTour.size}"
                        )
                        if (listTour.isNotEmpty()) {
                            tour = listTour.get(0)
                            Log.d(
                                "TOUR_DEBUG",
                                "selectItemPatrol: addItemTour tour = ${tour.Tour.size}"
                            )
                        }
                        callback.invoke(tour.Tour)
                    }
                }
            }
        }
    }

    var isTouring = false

    fun startOrStopTour(
        callback: (Boolean) -> Unit
    ) {
        if (isTouring) {
            JFCameraManager.stopTour(devId) {
                isTouring = it
                callback.invoke(isTouring)
            }
        } else {
            JFCameraManager.startTour(devId, tour.Id) {
                isTouring = it
                callback.invoke(isTouring)
            }
        }
    }

    fun checkExistPreset(presetName: String) =
        presets.any { it.PresetName.trim() == presetName.trim() }

    suspend fun deletePresets(listId: List<Int>, callback: (MutableList<Int>) -> Unit) {
        val listDeleteSuccess = mutableListOf<Int>()
        listId.forEach { presetId ->
            JFCameraManager.deleteItemTour(devId, presetId, tour.Id) {
                Log.d(TAG, "deletePresets: deleteItemTour result = $it")
            }
            val result = JFCameraManager.deletePreset(devId, presetId)
            if (result.first) {
                listDeleteSuccess.add(result.second)
            }
        }
        callback.invoke(listDeleteSuccess)
    }

    fun saveImgPreset(id: Int) {
        val dir = File(application.filesDir, "screenshot_preset")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val path = dir.path
        val imgPath = mediaManager.capture(path)
        val f = File(imgPath)
        waitImgCreated(f)
        rxPreferences.setImagePreset("$devId-$id", imgPath)
    }

    fun updateImgPreset(id: Int) {
        rxPreferences.deleteImagePreset("$devId-$id")
        val dir = File(application.filesDir, "screenshot_preset")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val path = dir.path
        val imgPath = mediaManager.capture(path)
        val f = File(imgPath)
        waitImgCreated(f)
        rxPreferences.setImagePreset("$devId-$id", imgPath)
    }

    fun deleteImgPreset(id: Int) {
        rxPreferences.deleteImagePreset("$devId-$id")
    }

    fun updateWhenAddPresets(mOPPTZControlBean: OPPTZControlBean) {
        presets.add(ConfigGetPreset().apply {
            Id = mOPPTZControlBean.Parameter.Preset
            PresetName = mOPPTZControlBean.Parameter.PresetName
        })
    }

    fun updateWhendeletePresets(id: Int) {
        presets.removeIf { it.Id == id }
    }

    fun updateItemPresetInList(id: Int, newName: String) {
        presets.find {
            it.Id == id
        }?.apply {
            PresetName = newName
        }
    }

    private fun waitImgCreated(img: File) {
        val timeout = 2000
        var time = 0
        while (!img.exists() && time < timeout) {
            time += 200
            SystemClock.sleep(200)
        }
    }


    private fun getSuitablePresetPosition(): Int {
        val uniqueInt = HashSet<Int>()
        for (i in presets.indices) {
            uniqueInt.add(presets.get(i).Id)
        }
        var uniquePosition = 1
        while (!uniqueInt.add(uniquePosition)) {
            uniquePosition++
        }
        return uniquePosition
    }

    fun registerEvent() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    fun getSystemFunction() {
        if (!this::devId.isInitialized) return
        viewModelScope.launch {
            var userIdGetSystemFunction = 0
            userIdGetSystemFunction = FunSDK.GetId(userIdGetSystemFunction) { message, _ ->
                responseSystemFunction.value = message.arg1
                0
            }

            FunSDK.DevGetConfigByJson(
                userIdGetSystemFunction,
                devId,
                "SystemFunction",
                1024,
                -1,
                Define.GET_SET_DEV_CONFIG_TIMEOUT,
                0
            )
        }
    }

    override fun onCleared() {
        EventBus.getDefault().unregister(this)
        super.onCleared()
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun onMessageEvent(event: RxEvent<String>) {
        if (event.keyId == Config.EventKey.EVENT_RELOAD_LIST_EVENT && event.value == devId) {
            Log.d(TAG, "onMessageEvent: reload event")
            actionReloadEvent.value = true
        }
    }
}