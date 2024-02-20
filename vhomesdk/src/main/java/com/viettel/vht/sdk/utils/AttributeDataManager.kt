package com.viettel.vht.sdk.utils

import com.google.gson.Gson
import com.viettel.vht.sdk.model.home.AttributeResponse
import com.viettel.vht.sdk.model.home.IrDataResponse
import org.json.JSONObject

class AttributeDataManager {

    class EntityType {
        companion object {
            const val ORGANIZATION = "ORGANIZATION"
            const val DEVICE = "DEVICE"
            const val EVENT = "EVENT"
            const val USER = "USER"
            const val TEMPLATE = "TEMPLATE"
        }
    }

    class ScopeType {
        companion object {
            const val SCOPE_SERVER = "SCOPE_SERVER"
            const val SCOPE_SHARE = "SCOPE_SHARE"
            const val SCOPE_CLIENT = "SCOPE_CLIENT"
        }
    }

    class AttributeKey {
        companion object {
            const val LAT = "latitude"
            const val DEVICE_SHARED = "deviceShared"
            const val LONG = "longitude"
            const val ADDRESS = "address"
            const val TYPE = "type"
            const val PAIR_TIME = "pair_time"
            const val GATEWAY = "gateway_id"
            const val VENDOR = "vendor"
            const val DEVICE_ID = "device_id"
            const val PRODUCT_NAME = "product_name"
            const val END_POINT = "endpoint"
            const val IP = "ip"
            const val DEVICE_SERIAL = "device_serial"
            const val MODEL_CAMERA = "cameraModel"
            const val PROTOCOL = "protocol"
            const val IR_TYPE = "ir_type"
            const val VALUE = "value"
            const val RAW_DATA = "raw_data"
            const val BACK_UP = "backup"
            const val NODE_ID = "node_id"
            const val FW_VERSION = "fwVersion"
            const val FW_PROCESS = "fwProcess"
            const val RECEIVE_NOTIFY = "receive_notify"
            const val IR_CODE = "ir_code"
            const val AIRCON_CMD = "aircon_cmd"
            const val BTN_TYPE = "btn_type"
            const val BTN_COLOR = "btn_color"
            const val EUI64 = "eui64"
            const val ZIGBEE_UUID = "zigbeeUUID"
            const val ZIGBEE_COMMANDS = "zigbee_commands"
            const val BLE_COMMANDS = "ble_commands"
            const val ENDPOINT_COUNT = "endpoint_count"
            const val BATTERY = "battery"
            const val USER_PHONE = "user_phone"
            const val CURTAIN_DIRECTION = "curtain_direction"
            const val STATUS_CURTAIN_V2 = "status_curtain"
            const val CURTAIN_PERCENTAGE = "curtain_percentage"
            const val TEMPERATURE = "temperature"
            const val HUMIDITY = "humidity"
            const val CONDITION_TYPE = "condition_type"
            const val CUSTOM_BUTTON = "custom_button"
            const val BORDER_LIGHT = "border_light"
            const val LINK_BUTTON = "linkButton"
            const val STATUS_DOOR = "status_door"
            const val STATUS_MOTION = "status_motion"
            const val STATUS_SMOKE = "status_smoke"
            const val STATUS_LOCK = "status_lock"
            const val IAQ = "iaq"
            const val LUX = "lux"
            const val STATUS = "status"
            const val DIM = "dim"
            const val CCT = "cct"
            const val TUYA_ID = "tuya_id"
            const val STATUS_PROGRESS = "status_progress"
            const val STATUS_ROLLING_DOOR = "status_rolling_door"
            const val STATUS_LOCK_ROLLING_DOOR = "lock"
            const val ROLLING_SWITCH = "rolling_switch"
            const val ROLLING_DOOR_DURATION = "rolling_door_duration"
            const val ROLLING_DOOR_ALERT = "rolling_door_alert"
            const val TUYA_HOME_ID = "home_id"
            const val LOCK_HISTORY = "lock_history"
            const val FIRE_ALARM = "fire_alarm"
            const val WARRANTY_ACTIVE = "warranty_active"
            const val LATEST_FW_VERSION = "latestFwVersion"
            const val LOCK_WARNING = "lock_warning"
            const val LIGHTNESS = "lightness"
            const val HUE = "hue"
            const val SATURATION = "saturation"
            const val SERVICE_INFO = "service_info"
            const val SMART_LOCK_PASSWORD = "lock_yale_pin"
            const val LIGHTING_TEMPERATURE_RANGE = "temperature_range"
            const val HCL = "HclMode"
            const val GROUP_ADDRESS = "group_on_device"
            const val WIFI_RSSI = "wifi_rssi"
            const val WIFI_SSID = "ssid"
            const val BLE_RSSI = "ble_rssi"
            const val ZIGBEE_RSSI = "zigbee_rssi"
        }
    }

    class ValueType {
        companion object {
            const val STR = "STR"
            const val BOOL = "BOOL"
            const val LONG = "LONG"
            const val DBL = "DBL"
            const val JSON = "JSON"
        }
    }

    companion object {

        fun getIrButtonData(
            attribute: AttributeResponse
        ): IrDataResponse? {
            if (attribute.valueType == ValueType.JSON && attribute.attributeKey != AttributeKey.CUSTOM_BUTTON) {
                val attributeValue = JSONObject(Gson().toJson(attribute.value))
                return IrDataResponse(
                    btnName = attribute.attributeKey,
                    btnType = if (attributeValue.has("btn_type")) attributeValue.getString("btn_type") else null,
                    btnColor = if (attributeValue.has("btn_color")) attributeValue.getString("btn_color") else null,
                    iRCode = if (attributeValue.has("ir_code")) attributeValue.getString("ir_code") else "",
                    backUp = if (attributeValue.has("back_up")) attributeValue.getString("back_up") else null,
                )
            }
            return null
        }

        fun getIrCodeOfButton(
            key: String,
            attribute: AttributeResponse?
        ): String {
            attribute?.let {
                val attributeValue = JSONObject(Gson().toJson(it.value))
                return if (attributeValue.has(key)) attributeValue.getString(key) else ""
            }
            return ""
        }
    }
}