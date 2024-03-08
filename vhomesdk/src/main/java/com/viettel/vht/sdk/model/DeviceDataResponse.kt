package com.viettel.vht.sdk.model

import android.content.Context
import android.text.format.DateFormat
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.manager.account.XMAccountManager
import com.manager.db.XMDevInfo
import com.vht.sdkcore.R
import com.vht.sdkcore.utils.Define
import com.viettel.vht.sdk.model.camera_cloud.CloudRegistered
import com.viettel.vht.sdk.model.home.AttributeResponse
import com.viettel.vht.sdk.utils.AttributeDataManager
import org.json.JSONObject
import java.io.Serializable
import java.util.Calendar
import java.util.Locale
import kotlin.math.max
import kotlin.random.Random

data class DeviceDataResponse(
    @SerializedName("id") var id: String = "",
    @SerializedName("template_id") val templateId: String = "",
    @SerializedName("name") var name: String = "",
    @SerializedName("group_id") val groupID: String = "",
    @SerializedName("group_name") val groupName: String = "",
    @SerializedName("org_id") var organizationID: String = "",
    @SerializedName("org_name") var organizationName: String = "",
    @SerializedName("type_id") val typeID: String = "",
    @SerializedName("type_name") val typename: String = "",
    @SerializedName("status") var status: String = "",
    @SerializedName("status_share") var statusShare: String? = "",
    @SerializedName("created_by") var createdBy: String = "",
    @SerializedName("permission") var permission: String? = "",
    @SerializedName("attributes") var attributes: List<AttributeResponse>? = arrayListOf(),
    @SerializedName("metadata") var metadata: Metadata? = Metadata(),
    @SerializedName("created_time") var createdTime: Long? = null,
    var features: List<String>? = null,
    var cameraSharingCamera: ItemSharingCamera? = null,
    var stateShare: Define.StateShare = Define.StateShare.NONE,
    var isSelected: Boolean = false,
    var isPrivateMode: Boolean = false,
    var countShare: Int = 0,
    var indexSelect: Int = 0
) : Serializable {

    fun getDeviceType(): String {
        return attributes.getAttribute(AttributeDataManager.AttributeKey.TYPE)
    }

    fun getFireAlarmResponse(): FireAlarmResponse? {
        var fireAlarmResponse: FireAlarmResponse? = null
        if (attributes == null || attributes!!.isEmpty()) {
            return null
        }
        val response = attributes?.filter {
            it.attributeKey == AttributeDataManager.AttributeKey.FIRE_ALARM
                    && it.valueType == AttributeDataManager.ValueType.JSON
        }
        if (!response.isNullOrEmpty()) {
            fireAlarmResponse = try {
                Gson().fromJson(response[0].valueAsString, FireAlarmResponse::class.java)
            } catch (e: Exception) {
                null
            }
        }
        return fireAlarmResponse
    }

    fun getWarrantyActive(): String {
        val timeStamp = attributes.getAttribute(AttributeDataManager.AttributeKey.WARRANTY_ACTIVE)
        return if (timeStamp.isEmpty() || timeStamp == "0") {
            ""
        } else {
            val calendar = Calendar.getInstance(Locale.ENGLISH)
            calendar.timeInMillis = timeStamp.toLong()
            DateFormat.format("dd-MM-yyyy", calendar).toString()
        }
    }

    private fun getLatestFwVersion(): String =
        attributes.getAttribute(AttributeDataManager.AttributeKey.LATEST_FW_VERSION)

    fun isNeedUpdateFw(): Boolean {
        val latestFwVersion = getLatestFwVersion()
        val currentFwVersion = getFirmwareVersion()
        if (latestFwVersion.isNotEmpty() && currentFwVersion.isNotEmpty()) {
            return latestFwVersion != currentFwVersion
        }
        return false
    }
    fun getFirmwareVersion(): String {
        return attributes.getAttribute(AttributeDataManager.AttributeKey.FW_VERSION)
    }

    fun isShared(): Boolean {
        return attributes.getAttribute(AttributeDataManager.AttributeKey.DEVICE_SHARED) == "true"
    }

    fun getIRType(): String {
        return attributes.getAttribute(AttributeDataManager.AttributeKey.IR_TYPE)
    }

    fun getSerialNumber(): String {
        return attributes.getAttribute(AttributeDataManager.AttributeKey.DEVICE_SERIAL)
    }

    // trạng thái thiết bị cho dòng thiết bị JF
    fun updateStateDeviceJF() {
        if (XMAccountManager.getInstance() == null || getDeviceType() != Define.TYPE_DEVICE.CAMERA_JF) return
        val listAttribute =
            attributes?.toMutableList() ?: mutableListOf()
        var isUpdate = false
        if (listAttribute.isNotEmpty()) {
            listAttribute.find { attribute ->
                attribute.attributeKey == AttributeDataManager.AttributeKey.STATUS
            }?.let { attribute ->
                attribute.valueAsString = if (XMAccountManager.getInstance()
                        ?.getDevState(deviceSerial()) == XMDevInfo.ON_LINE
                ) "online" else "offline"
                isUpdate = true
            }
        }
        if (!isUpdate) {
            listAttribute.add(
                AttributeResponse(
                    attributeKey = AttributeDataManager.AttributeKey.STATUS,
                    valueAsString = if (XMAccountManager.getInstance()
                            ?.getDevState(deviceSerial()) == XMDevInfo.ON_LINE
                    ) "online" else "offline"
                )
            )
        }
        attributes = listAttribute
    }

    fun isCamera(): Boolean {
        return getDeviceType() in listOf(
            Define.TYPE_DEVICE.CAMERA,
            Define.TYPE_DEVICE.CAMERA_IPC,
            Define.TYPE_DEVICE.VTTCAMERA,
            Define.TYPE_DEVICE.EZCAMERA,
            Define.TYPE_DEVICE.CAMERA_JF,
            Define.TYPE_DEVICE.CAMERA_IMOU
        )
    }

    // truyền
    fun isDeviceShare(useId: String): Boolean {
        return useId != createdBy
    }

    fun isShareEventCameraJF(): Boolean {
        var result = false
        permission?.let { listPermission ->
            if (listPermission.isNotEmpty()) {
                if (listPermission.contains("DP_AlarmPush")) {
                    result = true
                }
            }
        }
        return result
    }

    fun getStatusDoor(): String {
        return attributes.getAttribute(AttributeDataManager.AttributeKey.STATUS_DOOR)
    }

    private fun getStatusDoorTime(): Long {
        return attributes.getLastUpdateTime(AttributeDataManager.AttributeKey.STATUS_DOOR)
    }

    fun isSmartLockLocking(): Boolean {
        return getStatusSmartLock() in listOf("close", "auto-lock")
    }

    fun getStatusMotion(): String {
        return attributes.getAttribute(AttributeDataManager.AttributeKey.STATUS_MOTION)
    }

    private fun getStatusMotionTime(): Long {
        return attributes.getLastUpdateTime(AttributeDataManager.AttributeKey.STATUS_MOTION)
    }

    fun getTemperature(): String {
        return attributes.getAttribute(AttributeDataManager.AttributeKey.TEMPERATURE)
    }

    fun getTemperatureTime(): Long {
        return attributes.getLastUpdateTime(AttributeDataManager.AttributeKey.TEMPERATURE)
    }

    fun getHumidity(): String {
        return attributes.getAttribute(AttributeDataManager.AttributeKey.HUMIDITY)
    }

    fun getHumidityTime(): Long {
        return attributes.getLastUpdateTime(AttributeDataManager.AttributeKey.HUMIDITY)
    }

    fun getStatusSmoke(): String {
        return attributes.getAttribute(AttributeDataManager.AttributeKey.STATUS_SMOKE)
    }

    fun getStatusSmokeTime(): Long {
        return attributes.getLastUpdateTime(AttributeDataManager.AttributeKey.STATUS_SMOKE)
    }

    fun getIAQ(): String {
        return attributes.getAttribute(AttributeDataManager.AttributeKey.IAQ)
    }

    fun getIAQTime(): Long {
        return attributes.getLastUpdateTime(AttributeDataManager.AttributeKey.IAQ)
    }

    fun getBorderLight(): String {
        return attributes.getAttribute(AttributeDataManager.AttributeKey.BORDER_LIGHT)
            .ifBlank { "1" }
    }

    fun getStatusSmartLock(): String {
        return attributes.getAttribute(AttributeDataManager.AttributeKey.LOCK_HISTORY)
            .ifBlank { "close" }
    }

    fun getStatusSmartLockTime(): Long {
        return attributes.getLastUpdateTime(AttributeDataManager.AttributeKey.LOCK_HISTORY)
    }

    fun getCloudServiceInfo(): CloudRegistered? {
        var cloudRegistered: CloudRegistered? = null
        if (attributes == null || attributes!!.isEmpty()) {
            return null
        }
        val response = attributes?.filter {
            it.attributeKey == AttributeDataManager.AttributeKey.SERVICE_INFO
                    && it.valueType == AttributeDataManager.ValueType.JSON
        }
        if (response != null && response.isNotEmpty()) {
            cloudRegistered = try {
                Gson().fromJson(response[0].valueAsString, CloudRegistered::class.java)
            } catch (e: Exception) {
                null
            }
        }
        return cloudRegistered
    }



    fun getDisplayStatusAndTime(context: Context): Pair<String, Long>? {
        when (getDeviceType()) {
            Define.TYPE_DEVICE.DOOR, Define.TYPE_DEVICE.TUYA_DOOR_SENSOR -> {
                return if (attributes?.find { it.attributeKey == AttributeDataManager.AttributeKey.STATUS_DOOR } != null) {
                    when (getStatusDoor()) {
                        context.getString(R.string.open) -> context.getString(R.string.text_open) to getStatusDoorTime()
                        else -> context.getString(R.string.text_close) to getStatusDoorTime()
                    }
                } else {
                    null
                }
            }

            Define.TYPE_DEVICE.MOTION, Define.TYPE_DEVICE.TUYA_MOTION_SENSOR -> {
                return if (attributes?.find { it.attributeKey == AttributeDataManager.AttributeKey.STATUS_MOTION } != null) {
                    when (getStatusMotion()) {
                        "1" -> context.getString(R.string.motion) to getStatusMotionTime()
                        else -> context.getString(R.string.not_detect) to getStatusMotionTime()
                    }
                } else {
                    null
                }
            }

            Define.TYPE_DEVICE.HUMIDITY -> {
                return if (attributes?.find { it.attributeKey == AttributeDataManager.AttributeKey.TEMPERATURE } != null) {
                    return if (getTemperature().isEmpty() || getHumidity().isEmpty()) {
                        "Chưa xác định" to System.currentTimeMillis()
                    } else {
                        context.getString(
                            R.string.temperature_and_humidity, getTemperature(), getHumidity()
                        ) to maxOf(getTemperatureTime(), getHumidityTime())
                    }
                } else {
                    null
                }
            }

            Define.TYPE_DEVICE.SMOKE -> {
                return if (attributes?.find { it.attributeKey == AttributeDataManager.AttributeKey.STATUS_SMOKE } != null) {
                    when (getStatusSmoke()) {
                        "1" -> context.getString(R.string.smoke) to getStatusSmokeTime()
                        else -> context.getString(R.string.no_smoke) to getStatusSmokeTime()
                    }
                } else {
                    null
                }
            }

            Define.TYPE_DEVICE.AIR_PURIFIER -> {
                return if (attributes?.find { it.attributeKey == AttributeDataManager.AttributeKey.IAQ } != null) {
                    when {
                        getIAQ().isNotEmpty() -> context.getString(
                            R.string.iaq_air,
                            getIAQ()
                        ) to getIAQTime()

                        else -> "Chưa xác định" to getIAQTime()
                    }
                } else {
                    null
                }
            }

            Define.TYPE_DEVICE.TUYA_SMART_LOCK, Define.TYPE_DEVICE.SMART_LOCK -> {
                return if (attributes?.find { it.attributeKey == AttributeDataManager.AttributeKey.LOCK_HISTORY } != null) {
                    when (getStatusSmartLock()) {
                        "OpenByKey", "OpenByApp", "OpenByFinger", "OpenByPinCode", "OpenByRFID" -> context.getString(
                            R.string.text_open
                        ) to getStatusSmartLockTime()

                        "unlocking" -> "Đang mở khóa" to getStatusSmartLockTime()
                        "locking" -> "Đang khóa" to getStatusSmartLockTime()
                        else -> context.getString(R.string.text_close) to getStatusSmartLockTime()
                    }
                } else {
                    null
                }
            }

            else -> {
                return if (isOnline()) "Trực tuyến" to getOnlineTime() else "Mất kết nối" to getOnlineTime()
            }
        }
    }

    fun endpointCount(): Int {
        return try {
            attributes.getAttribute(AttributeDataManager.AttributeKey.ENDPOINT_COUNT).toFloat()
                .toInt()
        } catch (e: Exception) {
            0
        }
    }

    fun isPowerOn(): Boolean {
        val attributeKey = when (getDeviceType()) {
            Define.TYPE_DEVICE.SCENE_SWITCH -> "power_group"

            else -> "power"
        }
        val status = attributes.getAttribute(attributeKey)
        return status == "on"
    }

    fun isPowerOn(endpoint: Int): Boolean {
        val status = attributes.getAttribute("power_$endpoint")
        if (status == "on") return true
        return false
    }

    fun isAllPowerOn(): Boolean {
        for (endpoint in 1..endpointCount()) {
            if (!isPowerOn(endpoint)) {
                return false
            }
        }
        return true
    }

    fun isAllPowerOff(): Boolean {
        for (endpoint in 1..endpointCount()) {
            if (isPowerOn(endpoint)) {
                return false
            }
        }
        return true
    }

    fun countdown(endpoint: Int): Pair<Int, Long> {
        val countdownAttr = attributes?.find { it.attributeKey == "countdown_$endpoint" }
        val duration = countdownAttr?.value?.toString()?.toFloat()?.toInt() ?: 0
        return if (duration == 0) 0 to 0L
        else duration to countdownAttr!!.lastUpdateTs
    }

    fun getPower(): String {
        return attributes.getAttribute("power")
    }

    fun nodeId(): String {
        return attributes.getAttribute(AttributeDataManager.AttributeKey.NODE_ID)
    }

    fun eui64(): String {
        return attributes.getAttribute(AttributeDataManager.AttributeKey.EUI64)
    }

    fun gatewayId(): String {
        return if (!isGateway() && !isCamera()) attributes.getAttribute(AttributeDataManager.AttributeKey.GATEWAY)
            .ifBlank { id } else ""
    }

    fun isGateway(): Boolean {
        return getDeviceType() in listOf(
            Define.TYPE_DEVICE.GATE_WAY,
            Define.TYPE_DEVICE.IR_GATEWAY,
            Define.TYPE_DEVICE.ONT_GATEWAY,
            Define.TYPE_DEVICE.ZIGBEE_REPEATER,
            Define.TYPE_DEVICE.TUYA_ZIGBEE_GATEWAY
        )
    }

    fun getGroupDevice(): String {
        if (protocol() == Define.Protocol.IR) return GroupDevice.IR_DEVICE

        return when (getDeviceType()) {
            Define.TYPE_DEVICE.EZCAMERA, Define.TYPE_DEVICE.CAMERA, Define.TYPE_DEVICE.VTTCAMERA, Define.TYPE_DEVICE.CAMERA_IPC, Define.TYPE_DEVICE.CAMERA_JF, Define.TYPE_DEVICE.CAMERA_IMOU -> GroupDevice.CAMERA


            Define.TYPE_DEVICE.SWITCH, Define.TYPE_DEVICE.MULTI_SWITCH_ZIGBEE, Define.TYPE_DEVICE.MULTI_SWITCH_WIFI, Define.TYPE_DEVICE.SCENE_SWITCH, Define.TYPE_DEVICE.TUYA_SWITCH, Define.TYPE_DEVICE.ROLLING_DOOR, Define.TYPE_DEVICE.TUYA_ROLLING_DOOR, Define.TYPE_DEVICE.TUYA_ROLLING_DOOR_2 -> GroupDevice.SWITCH


            Define.TYPE_DEVICE.SOCKET_ZIGBEE, Define.TYPE_DEVICE.SOCKET_WIFI, Define.TYPE_DEVICE.TUYA_SOCKET -> GroupDevice.SOCKET


            Define.TYPE_DEVICE.DOOR, Define.TYPE_DEVICE.HUMIDITY, Define.TYPE_DEVICE.MOTION, Define.TYPE_DEVICE.SMOKE, Define.TYPE_DEVICE.TUYA_DOOR_SENSOR -> GroupDevice.SENSOR


            Define.TYPE_DEVICE.ONT_GATEWAY, Define.TYPE_DEVICE.IR_GATEWAY, Define.TYPE_DEVICE.GATE_WAY, Define.TYPE_DEVICE.TUYA_ZIGBEE_GATEWAY -> GroupDevice.GATEWAY


            Define.TYPE_DEVICE.LAMP,
            Define.TYPE_DEVICE.BLE_LED_RGB,
            Define.TYPE_DEVICE.BLE_LED_RGB_A60,
            Define.TYPE_DEVICE.BLE_LED_RGB_STRIP,
            Define.TYPE_DEVICE.BLE_LED_DOWNLIGHT_V2,
            Define.TYPE_DEVICE.BLE_LED_AT18,
            Define.TYPE_DEVICE.BLE_LED_AT20,
            Define.TYPE_DEVICE.BLE_LED_AT22,
            Define.TYPE_DEVICE.WIFI_BULB_RGB,
            Define.TYPE_DEVICE.LIGHTING_C,
            Define.TYPE_DEVICE.LIGHTING_CW,
            Define.TYPE_DEVICE.LIGHTING_RGB,
            Define.TYPE_DEVICE.LIGHTING_RGBC,
            Define.TYPE_DEVICE.LIGHTING_RGBCW -> GroupDevice.LAMP

            Define.TYPE_DEVICE.SMART_LOCK,
            Define.TYPE_DEVICE.CURTAINS -> GroupDevice.HOUSEHOLD_APPLIANCES

            else -> GroupDevice.OTHER
        }
    }

    fun protocol(): String {
        val protocol = attributes.getAttribute(AttributeDataManager.AttributeKey.PROTOCOL)
        return protocol.ifEmpty {
            /*
             * Supporting old wifi devices don't have protocol attribute.
             * In order to have protocol attribute, you have to unpair and pair it again
             */
            isWifiDevice()
        }
    }

    private fun isWifiDevice(): String {
        return when (getDeviceType()) {
            Define.TYPE_DEVICE.GATE_WAY,
            Define.TYPE_DEVICE.IR_GATEWAY,
            Define.TYPE_DEVICE.ONT_GATEWAY,
            Define.TYPE_DEVICE.SOCKET_WIFI,
            Define.TYPE_DEVICE.MULTI_SWITCH_WIFI,
            Define.TYPE_DEVICE.WIFI_BULB_RGB -> Define.Protocol.WIFI

            else -> ""
        }
    }

    fun getProductIcon(): Int {
        if (protocol() != Define.Protocol.IR) {
            return when (getDeviceType()) {
                Define.TYPE_DEVICE.CAMERA_JF -> if (getModelCamera() == Define.CAMERA_MODEL.CAMERA_JF_INDOOR) R.drawable.ic_product_camera_hc23 else R.drawable.ic_product_camera_hc33

                else -> R.drawable.ic_product_camera_hc23
            }
        } else {
           return R.drawable.ic_product_camera_hc23
        }
    }

    fun getModelCamera(): String {
        return attributes.getAttribute(AttributeDataManager.AttributeKey.MODEL_CAMERA)
    }


    fun deviceSerial(): String {
        return attributes.getAttribute(AttributeDataManager.AttributeKey.DEVICE_SERIAL)
    }

    fun isOnline(): Boolean {
        return attributes.getAttribute(AttributeDataManager.AttributeKey.STATUS) == "online"
    }

    fun getOnlineTime(): Long {
        return attributes.getLastUpdateTime(AttributeDataManager.AttributeKey.STATUS)
    }

    fun getCustomName(endpoint: Int): String {
        val customName = try {
            Gson().fromJson(
                attributes.getAttribute(AttributeDataManager.AttributeKey.CUSTOM_BUTTON),
                Array<String>::class.java
            ).toList()
        } catch (e: Exception) {
            listOf()
        }
        var endpointName: String? = customName.getOrNull(endpoint - 1)
        if (endpointName == null) {
            endpointName = when (getDeviceType()) {
                Define.TYPE_DEVICE.SWITCH, Define.TYPE_DEVICE.MULTI_SWITCH_ZIGBEE, Define.TYPE_DEVICE.MULTI_SWITCH_WIFI, Define.TYPE_DEVICE.SCENE_SWITCH, Define.TYPE_DEVICE.TUYA_SWITCH -> "Công tắc số $endpoint"

                Define.TYPE_DEVICE.SOCKET_ZIGBEE, Define.TYPE_DEVICE.SOCKET_WIFI, Define.TYPE_DEVICE.TUYA_SOCKET -> "Ổ cắm số $endpoint"

                else -> ""
            }
        }
        return endpointName
    }

    fun getCustomName(): List<String> {
        if (getDeviceType() !in listOf(
                Define.TYPE_DEVICE.SWITCH,
                Define.TYPE_DEVICE.MULTI_SWITCH_ZIGBEE,
                Define.TYPE_DEVICE.MULTI_SWITCH_WIFI,
                Define.TYPE_DEVICE.SOCKET_ZIGBEE,
                Define.TYPE_DEVICE.SOCKET_WIFI,
                Define.TYPE_DEVICE.SCENE_SWITCH
            )
        ) return emptyList()
        try {
            return Gson().fromJson(
                attributes.getAttribute(AttributeDataManager.AttributeKey.CUSTOM_BUTTON),
                Array<String>::class.java
            ).toList()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val names = mutableListOf<String>()
        for (endpoint in 1..endpointCount()) {
            names.add(getCustomName(endpoint))
        }
        return names
    }

    fun getLux(): String {
        return try {
            "${
                attributes.getAttribute(AttributeDataManager.AttributeKey.LUX).toFloat().toInt()
            } LUX"
        } catch (e: Exception) {
            ""
        }
    }

    fun getCCT(): Int {
        when (getDeviceType()) {
            in listOf(
                Define.TYPE_DEVICE.LAMP,
                Define.TYPE_DEVICE.LIGHTING_C,
                Define.TYPE_DEVICE.LIGHTING_CW,
                Define.TYPE_DEVICE.LIGHTING_RGB,
                Define.TYPE_DEVICE.LIGHTING_RGBC,
                Define.TYPE_DEVICE.LIGHTING_RGBCW
            ) -> {
                return try {
                    attributes.getAttribute("${AttributeDataManager.AttributeKey.CCT}_1").toFloat()
                        .toInt()
                } catch (e: Exception) {
                    0
                }
            }

            in listOf(
                Define.TYPE_DEVICE.BLE_LED_RGB,
                Define.TYPE_DEVICE.BLE_LED_RGB_A60,
                Define.TYPE_DEVICE.BLE_LED_RGB_STRIP,
                Define.TYPE_DEVICE.BLE_LED_DOWNLIGHT_V2,
                Define.TYPE_DEVICE.BLE_LED_AT18,
                Define.TYPE_DEVICE.BLE_LED_AT20,
                Define.TYPE_DEVICE.BLE_LED_AT22
            ) -> {
                return try {
                    attributes.getAttribute(AttributeDataManager.AttributeKey.CCT).toFloat().toInt()
                } catch (e: Exception) {
                    0
                }
            }

            in listOf(
                Define.TYPE_DEVICE.SCENE_SWITCH
            ) -> {
                return try {
                    max(
                        attributes.getAttribute(AttributeDataManager.AttributeKey.CCT).toFloat()
                            .toInt(), 800
                    )
                } catch (e: Exception) {
                    800
                }
            }

            in listOf(
                Define.TYPE_DEVICE.WIFI_BULB_RGB
            ) -> {
                return try {
                    attributes.getAttribute(AttributeDataManager.AttributeKey.CCT).toFloat().toInt()
                } catch (e: Exception) {
                    0
                }
            }

            else -> return 0
        }
    }

    fun getTemperatureRange(): Pair<Int, Int> {
        return try {
            val range =
                attributes.getAttribute(AttributeDataManager.AttributeKey.LIGHTING_TEMPERATURE_RANGE)
                    .split("-")
            range[0].toInt() to range[1].toInt()
        } catch (e: Exception) {
            0 to 0
        }
    }

    fun getDim(): Int {
        when (getDeviceType()) {
            in listOf(
                Define.TYPE_DEVICE.LAMP,
                Define.TYPE_DEVICE.LIGHTING_C,
                Define.TYPE_DEVICE.LIGHTING_CW,
                Define.TYPE_DEVICE.LIGHTING_RGB,
                Define.TYPE_DEVICE.LIGHTING_RGBC,
                Define.TYPE_DEVICE.LIGHTING_RGBCW
            ) -> {
                return try {
                    attributes.getAttribute("${AttributeDataManager.AttributeKey.DIM}_1").toFloat()
                        .toInt()
                } catch (e: Exception) {
                    0
                }
            }

            in listOf(
                Define.TYPE_DEVICE.BLE_LED_RGB,
                Define.TYPE_DEVICE.BLE_LED_RGB_A60,
                Define.TYPE_DEVICE.BLE_LED_RGB_STRIP,
                Define.TYPE_DEVICE.BLE_LED_DOWNLIGHT_V2,
                Define.TYPE_DEVICE.BLE_LED_AT18,
                Define.TYPE_DEVICE.BLE_LED_AT20,
                Define.TYPE_DEVICE.BLE_LED_AT22,
                Define.TYPE_DEVICE.SCENE_SWITCH
            ) -> {
                return try {
                    attributes.getAttribute(AttributeDataManager.AttributeKey.DIM).toFloat().toInt()
                } catch (e: Exception) {
                    65535
                }
            }

            in listOf(
                Define.TYPE_DEVICE.WIFI_BULB_RGB
            ) -> {
                return try {
                    attributes.getAttribute(AttributeDataManager.AttributeKey.DIM).toFloat().toInt()
                } catch (e: Exception) {
                    0
                }
            }

            else -> return 0
        }
    }

    fun getLightness(): Int {
        when (getDeviceType()) {
            in listOf(
                Define.TYPE_DEVICE.LAMP,
                Define.TYPE_DEVICE.LIGHTING_C,
                Define.TYPE_DEVICE.LIGHTING_CW,
                Define.TYPE_DEVICE.LIGHTING_RGB,
                Define.TYPE_DEVICE.LIGHTING_RGBC,
                Define.TYPE_DEVICE.LIGHTING_RGBCW
            ) -> {
                return try {
                    attributes.getAttribute("${AttributeDataManager.AttributeKey.DIM}_1").toFloat()
                        .toInt()
                } catch (e: Exception) {
                    0
                }
            }

            in listOf(
                Define.TYPE_DEVICE.BLE_LED_RGB,
                Define.TYPE_DEVICE.BLE_LED_RGB_A60,
                Define.TYPE_DEVICE.BLE_LED_RGB_STRIP,
                Define.TYPE_DEVICE.BLE_LED_DOWNLIGHT_V2,
                Define.TYPE_DEVICE.BLE_LED_AT18,
                Define.TYPE_DEVICE.BLE_LED_AT20,
                Define.TYPE_DEVICE.BLE_LED_AT22,
                Define.TYPE_DEVICE.WIFI_BULB_RGB
            ) -> {
                return try {
                    attributes.getAttribute(AttributeDataManager.AttributeKey.LIGHTNESS).toFloat()
                        .toInt()
                } catch (e: Exception) {
                    65535
                }
            }

            else -> return 0
        }
    }

    fun getHue(): Int {
        when (getDeviceType()) {
            in listOf(
                Define.TYPE_DEVICE.LAMP,
                Define.TYPE_DEVICE.LIGHTING_C,
                Define.TYPE_DEVICE.LIGHTING_CW,
                Define.TYPE_DEVICE.LIGHTING_RGB,
                Define.TYPE_DEVICE.LIGHTING_RGBC,
                Define.TYPE_DEVICE.LIGHTING_RGBCW
            ) -> {
                return try {
                    attributes.getAttribute("${AttributeDataManager.AttributeKey.HUE}_1").toFloat()
                        .toInt()
                } catch (e: Exception) {
                    0
                }
            }

            in listOf(
                Define.TYPE_DEVICE.BLE_LED_RGB,
                Define.TYPE_DEVICE.BLE_LED_RGB_A60,
                Define.TYPE_DEVICE.BLE_LED_RGB_STRIP,
                Define.TYPE_DEVICE.BLE_LED_DOWNLIGHT_V2,
                Define.TYPE_DEVICE.BLE_LED_AT18,
                Define.TYPE_DEVICE.BLE_LED_AT20,
                Define.TYPE_DEVICE.BLE_LED_AT22,
                Define.TYPE_DEVICE.WIFI_BULB_RGB
            ) -> {
                return try {
                    attributes.getAttribute(AttributeDataManager.AttributeKey.HUE).toFloat().toInt()
                } catch (e: Exception) {
                    0
                }
            }

            else -> return 0
        }
    }

    fun getSaturation(): Int {
        when (getDeviceType()) {
            in listOf(
                Define.TYPE_DEVICE.LAMP,
                Define.TYPE_DEVICE.LIGHTING_C,
                Define.TYPE_DEVICE.LIGHTING_CW,
                Define.TYPE_DEVICE.LIGHTING_RGB,
                Define.TYPE_DEVICE.LIGHTING_RGBC,
                Define.TYPE_DEVICE.LIGHTING_RGBCW
            ) -> {
                return try {
                    attributes.getAttribute("${AttributeDataManager.AttributeKey.SATURATION}_1")
                        .toFloat().toInt()
                } catch (e: Exception) {
                    0
                }
            }

            in listOf(
                Define.TYPE_DEVICE.BLE_LED_RGB,
                Define.TYPE_DEVICE.BLE_LED_RGB_A60,
                Define.TYPE_DEVICE.BLE_LED_RGB_STRIP,
                Define.TYPE_DEVICE.BLE_LED_DOWNLIGHT_V2,
                Define.TYPE_DEVICE.BLE_LED_AT18,
                Define.TYPE_DEVICE.BLE_LED_AT20,
                Define.TYPE_DEVICE.BLE_LED_AT22,
                Define.TYPE_DEVICE.WIFI_BULB_RGB
            ) -> {
                return try {
                    attributes.getAttribute(AttributeDataManager.AttributeKey.SATURATION).toFloat()
                        .toInt()
                } catch (e: Exception) {
                    0
                }
            }

            else -> return 0
        }
    }



    fun updateAttribute(key: String, value: Any) {
        val mAttributes = attributes?.toMutableList() ?: mutableListOf()
        val index = mAttributes.indexOfFirst { it.attributeKey == key }
        if (index != -1) {
            mAttributes[index] = mAttributes[index].copy(
                value = value,
                valueAsString = value.toString(),
                lastUpdateTs = System.currentTimeMillis()
            )
        } else {
            mAttributes.add(
                AttributeResponse(
                    attributeKey = key,
                    value = value,
                    valueAsString = value.toString(),
                    lastUpdateTs = System.currentTimeMillis()
                )
            )
        }
        attributes = mAttributes
    }

    private fun List<AttributeResponse>?.getAttribute(key: String): String {
        try {
            this?.forEach {
                if (it.attributeKey == key) return it.valueAsString.ifBlank { it.value.toString() }
            }
            return ""
        } catch (e: Exception) {
            return ""
        }
    }

    private fun List<AttributeResponse>?.getLastUpdateTime(key: String): Long {
        try {
            this?.forEach {
                if (it.attributeKey == key) return it.lastUpdateTs
            }

            return System.currentTimeMillis()
        } catch (e: Exception) {
            return System.currentTimeMillis()
        }
    }

    fun getWifiRSSI(): String? {
        return try {
            attributes.getAttribute(AttributeDataManager.AttributeKey.WIFI_RSSI)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getWifiSSID(): String? {
        return try {
            attributes.getAttribute(AttributeDataManager.AttributeKey.WIFI_SSID)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getBleRSSI(): String? {
        return try {
            attributes.getAttribute(AttributeDataManager.AttributeKey.BLE_RSSI)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getZigbeeRSSI(): String? {
        return try {
            attributes.getAttribute(AttributeDataManager.AttributeKey.ZIGBEE_RSSI)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}