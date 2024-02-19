package com.vht.sdkcore.utils

import android.text.InputType
import com.vht.sdkcore.R
import com.vht.sdkcore.utils.Define.ACTION_CURTAINS.Companion.CLOSE_CURTAINS
import com.vht.sdkcore.utils.Define.ACTION_CURTAINS.Companion.OPEN_CURTAINS

class Define {

    companion object {
        const val GET_SET_DEV_CONFIG_TIMEOUT = 8000 //默认8秒超时

        /**
         * 和后端设备交互 透传前缀
         */
        const val DEV_CONFIG_PENETRATE_PREFIX = "bypass@"
        const val ACTION_OPEN_MAIN_ACTIVITY = "open.main.activity.intent.action.Launch"
        const val ACTION_CLICK_DEFAULT_EZVIZ_NOTIFICATION = "messageReceiverActivity"
    }

    class BUNDLE_KEY {
        companion object {
            //param
            const val PARAM_EMAIL = "email"
            const val PARAM_PASSWORD = "password"
            const val PARAM_CAPTCHA = "captcha"
            const val PARAM_PASSWORD_CONFIRM = "password_confirm"
            const val PARAM_COUNTRY_CODE = "countryCode"
            const val PARAM_LIST_DEVICE: String = "LIST_DEVICE"
            const val PARAM_DEVICE_ITEM: String = "DEVICE_ITEM"
            const val PARAM_DEVICE_IPC_CAMERA_SORT_ITEM: String =
                "PARAM_DEVICE_IPC_CAMERA_SORT_ITEM"
            const val PARAM_CAMERA_FILTER_ITEM: String = "PARAM_CAMERA_FILTER_ITEM"
            const val PARAM_CAMERA_INFO = "PARAM_CAMERA_INFO"
            const val PARAM_CURTAIN_DIRECTION: String = "CURTAIN_DIRECTION"
            const val PARAM_NAME = "name"
            const val PARAM_SYSTEM_TIME = "PARAM_SYSTEM_TIME"
            const val PARAM_SORT = "sort"
            const val PARAM_ID = "id"
            const val PARAM_FAMILY_ID = "familyid"
            const val PARAM_ROOM_ID = "room_id"
            const val PARAM_ROOM = "room"
            const val PARAM_ROOM_LIST = "roomNameList"
            const val PARAM_FROM = "from"
            const val PARAM_SSID = "ssid"
            const val PARAM_NETWORK_AUTH = "network_auth"
            const val PARAM_IS_LAN_NETWORK = "is_lan_network"
            const val PARAM_BSSID = "bssid"
            const val PARRAM_CAMERA_INFO = "cameraInfo"
            const val PARRAM_VIDEO_PLAYBACK = "videoPlayback"
            const val PARAM_DEVICE_GATEWAY: String = "DEVICE_GATEWAY"
            const val PARAM_ADD_ROOM = "add_room"
            const val PARAM_TYPE = "type"
            const val PARAM_GATE_WAY = "gateway_id"
            const val PARAM_OLD_PASSWORD = "old_password"
            const val PARAM_NEW_PASSWORD = "new_password"
            const val PARAM_ACCESS_TOKEN = "accessToken"
            const val PARAM_CLOUD_VIDEO = "cloud_video"
            const val PARAM_SHOW_BACK = "showBack"
            const val PARAM_FROM_CENTRAL_NOTI = "central_noti"

            const val PARAM_LIST_CAMERA: String = "PARAM_LIST_CAMERA"
            const val PARAM_IDENTIFIER = "identifier"
            const val PARAM_ROLE_ID: String = "role_id"
            const val PARAM_METADATA = "metadata"
            const val PARAM_ADDRESS = "address"
            const val TYPE_HOME = "house"
            const val TYPE_ROOM = "room"
            const val PARAM_ORGANIZATION = "org_id"
            const val SCREEN_ID = "SCREEN_ID"
            const val PARAM_PROJECT_ID = "project_id"
            const val PARAM_DEVICE_TOKEN = "devicetoken"
            const val PARAM_LIST_ROOM = "list_room"
            const val PARAM_DEVICE_ID = "deviceid"
            const val PARAM_METHOD_TYPE_PAY = "PARAM_METHOD_TYPE_PAY"
            const val PARAM_SHARE_ID_JF = "shareidjf"
            const val PARAM_TYPE_CONDITION = "typeCondition"
            const val PARAM_SCENE_ITEM = "SCENE_ITEM"
            const val PARAM_SCENE_ID = "PARAM_SCENE_ID"
            const val PARAM_SMART_SCENE = "PARAM_SMART_SCENE"
            const val PARAM_IS_SHARED_HOME = "PARAM_IS_SHARED_HOME"
            const val PARAM_LIST_DAY = "LIST_DAY"
            const val PARAM_OPEN_TAB_EVENT = "PARAM_OPEN_TAB_EVENT"
            const val PARAM_OPEN_TAB_SCENE = "PARAM_OPEN_TAB_SCENE"
            const val PARAM_OPEN_TAB_DEVICE = "PARAM_OPEN_TAB_DEVICE"
            const val PARAM_ACTION = "action"
            const val PARAM_EVENT = "event"
            const val PARAM_CAMERA = "camera"
            const val PARAM_TYPE_ACTION = "type_action"
            const val PARAM_LIST_TYPE_DEVICE = "type_device"
            const val PARAM_LIST_TYPE_MANAGE_SCREEN = "PARAM_LIST_TYPE_MANAGE_SCREEN"
            const val PARAM_TIME_ITEM: String = "TIME_ITEM"
            const val PARAM_SMS_ITEM: String = "SMS_ITEM"
            const val PARAM_PHONE = "phone"
            const val PARAM_OTP = "otp"
            const val PARAM_PRODUCT_NAME = "productName"
            const val PARAM_PRODUCT = "PARAM_PRODUCT"
            const val PARAM_PAIR_TIME = "pairTime"
            const val PARAM_DEVICE_SCENE: String = "DEVICE_SCENE"
            const val PARAM_SCENE_ACTION_ITEM: String = "SCENE_ACTION_ITEM"
            const val PARAM_INDEX: String = "INDEX"
            const val PARAM_CONDITION_WEATHER: String = "CONDITION_WEATHER"
            const val PARAM_ENPOINT: String = "endpoint"
            const val PARAM_LAT = "latitude"
            const val PARAM_LONG = "longitude"
            const val PARAM_TITLE = "PARAM_TITLE"
            const val PARAM_IP = "ip"
            const val PARAM_ITEM_SETTING = "PARAM_ITEM_SETTING"
            const val PARAM_VENDOR = "vendor"
            const val PARAM_PROTOCOL = "protocol"
            const val PARAM_INFRARED_DEVICE = "PARAM_INFRARED_DEVICE"
            const val PARAM_IR_TYPE = "irtype"
            const val PARAM_IR_CODE = "IRCode"
            const val PARAM_IR_KEY = "PARAM_IR_KEY"
            const val PARAM_IR_CMD = "irCmd"
            const val PARAM_RAW_DATA = "rawData"
            const val PARAM_BACK_UP = "backUp"
            const val PARAM_BACK_CONTROL_IR = "PARAM_BACK_CONTROL_IR"
            const val PARAM_SETTING_NOTIFY = "disableNotify"
            const val PARAM_OPEN_FROM_NOTIFICATION = "PARAM_OPEN_FROM_NOTIFICATION"
            const val PARAM_OPEN_FROM_GUIDE_PAIR = "PARAM_OPEN_FROM_GUIDE_PAIR"
            const val PARAM_TYPE_ONTAP = "PARAM_TYPE_ONTAP"
            const val PARAM_TYPE_LIST_SCENE = "PARAM_TYPE_LIST_SCENE"
            const val PARAM_TIME_ZONE = "PARAM_TIME_ZONE"
            const val PARAM_DEVICE_SERIAL = "PARAM_DEVICE_SERIAL"
            const val PARAM_CHANNEL_ID = "PARAM_CHANNEL_ID"
            const val PARAM_MODEL_CAMERA = "PARAM_MODEL_CAMERA"
            const val PARAM_PERMISSIONS = "PARAM_PERMISSIONS"
            const val PARAM_STATUS_SHARE = "PARAM_STATUS_SHARE"
            const val PARAM_MODEL_SMART_SETTING_MODE = "PARAM_MODEL_SMART_SETTING_MODE"
            const val PARAM_MODEL_FILTER_CAMERA = "PARAM_MODEL_FILTER_CAMERA"
            const val PARAM_LIST_FEATURE = "PARAM_LIST_FEATURE"
            const val PARAM_DAY_FORMAT = "PARAM_DAY_FORMAT"
            const val PARAM_TIME_SCHEDULE = "PARAM_TIME_SCHEDULE"
            const val PARAM_DAY_OF_SCHEDULE = "PARAM_DAY_OF_SCHEDULE"
            const val PARAM_DETAIL_CAMERA = "PARAM_DETAIL_CAMERA"
            const val PARAM_CAPABILITY_CAMERA = "PARAM_CAPABILITY_CAMERA"
            const val PARAM_CAMERA_MESSAGE = "PARAM_CAMERA_MESSAGE"
            const val PARAM_ATTRIBUTES = "attributes"
            const val PARAM_TYPE_MOVE_DEVICE = "PARAM_TYPE_MOVE_DEVICE"
            const val PARAM_TYPE_TIME_SCENE = "PARAM_TYPE_TIME_SCENE"
            const val PARAM_ON_ACTION_LISTENER = "PARAM_ON_ACTION_LISTENER"
            const val PARAM_QUERIES = "queries"
            const val PARAM_KEY = "key"
            const val PARAM_VALUE = "value"
            const val PARAM_SUB_ORG_ID = "id"
            const val PARAM_SUB_ORG = "subs"
            const val PARAM_ORG_ID = "org_id"
            const val MESSAGE_RECORD = "message_record"
            const val PREDICTION = "prediction"
            const val PARAM_OLD_DEVICE = "PARAM_OLD_DEVICE"
            const val PARAM_TARGET_DEVICE = "PARAM_TARGET_DEVICE"
            const val PARAM_LIST_CLOUD = "PARAM_LIST_CLOUD"
            const val PARAM_START_TIME = "PARAM_START_TIME"
            const val PARAM_END_TIME = "PARAM_END_TIME"
            const val PARAM_TIME = "PARAM_TIME"
            const val PARAM_DETAIL_SETTING_CAMERA = "PARAM_DETAIL_SETTING_CAMERA"
            const val PARAM_RECORD_MODE_CAMERA = "PARAM_RECORD_MODE_CAMERA"
            const val PARAM_BOTTOM_CAMERA_INFO_SHEET_CAMERA_SETTING =
                "PARAM_BOTTOM_CAMERA_INFO_SHEET_CAMERA_SETTING"
            const val PARAM_UPDATE_FIRMWARE_SCREEN = "PARAM_UPDATE_FIRMWARE_SCREEN"
            const val PARAM_UPDATE_FIRMWARE_CAMERA_INFOR_SCREEN =
                "PARAM_UPDATE_FIRMWARE_CAMERA_INFOR_SCREEN"
            const val PARAM_IS_UPDATE_FIRMWARE_IMMEDIATELY = "PARAM_IS_UPDATE_FIRMWARE_IMMEDIATELY"
            const val PARAM_CAMERA_INFO_CAMERA_SETTING_SCREEN_TO_SD_CARD_INFO_SCREEN =
                "PARAM_CAMERA_INFO_CAMERA_SETTING_SCREEN_TO_SD_CARD_INFO_SCREEN"
            const val PARAM_CAMERA_INFO_CAMERA_SETTING_SCREEN_TO_REGISTER_CLOUD_SCREEN =
                "PARAM_CAMERA_INFO_CAMERA_SETTING_SCREEN_TO_CLOUD_SCREEN"
            const val PARAM_BOTTOM_DETAIL_CAMERA_RESPONSE_SHEET_CAMERA_SETTING =
                "PARAM_BOTTOM_DETAIL_CAMERA_RESPONSE_SHEET_CAMERA_SETTING"
            const val PARAM_BOTTOM_DEVICE_ITEM_SHEET_CAMERA_SETTING =
                "PARAM_BOTTOM_DEVICE_ITEM_SHEET_CAMERA_SETTING"
            const val PARAM_VOLUME_CAMERA = "PARAM_VOLUME_CAMERA"
            const val PARAM_CONDITION_ACTION_CODE = "PARAM_CONDITION_ACTION_CODE"
            const val PARAM_RECORDING_DURATION = "PARAM_RECORDING_DURATION"
            const val PARAM_RECORDING_STATE = "PARAM_RECORDING_STATE"
            const val PARAM_RECORDING_MODE = "PARAM_RECORDING_MODE"
            const val PARAM_IS_THUMBNAIL = "PARAM_IS_THUMBNAIL"
            const val PARAM_SETTING_RECORD_TIME_VIDEO = "PARAM_SETTING_RECORD_TIME_VIDEO"
            const val PARAM_SETTING_ALARM_STATE = "PARAM_SETTING_ALARM_STATE"
            const val PARAM_SETTING_ALARM_LED_STATE = "PARAM_SETTING_ALARM_LED_STATE"
            const val PARAM_SETTING_ALARM_SOUND_STATE = "PARAM_SETTING_ALARM_SOUND_STATE"
            const val PARAM_SETTING_HUMAN_NOTE = "PARAM_SETTING_HUMAN_NOTE"
            const val PARAM_TYPE_SCHEDULE_ALARM_ADD_OR_EDIT =
                "PARAM_TYPE_SCHEDULE_ALARM_ADD_OR_EDIT"
            const val PARAM_TYPE_SCHEDULE_ALARM_DATA_EDIT = "PARAM_TYPE_SCHEDULE_ALARM_DATA_EDIT"
            const val PARAM_TYPE_TIME_ZONE_CAMERA = "PARAM_TYPE_TIME_ZONE_CAMERA"
            const val PARAM_TYPE_DETECTION_CAMERA_OUT_DOOR_HC22 =
                "PARAM_TYPE_DETECTION_CAMERA_OUT_DOOR_HC22"
            const val PARAM_FROM_MULTIPLE_LIVEVIEW = "PARAM_FROM_MULTIPLE_LIVEVIEW"

            //ftth
            const val PARAM_TITLE_DIALOG = "PARAM_TITLE_DIALOG"
            const val PARAM_TITLE_CANCEL = "PARAM_TITLE_CANCEL"
            const val PARAM_TITLE_SAVE = "PARAM_TITLE_SAVE"

            //camera
            const val PARAM_DEVICE_TYPE = "device_type"
            const val PARAM_CAMERA_SERIAL = "camera_serial"
            const val PARAM_CAMERA_TYPE = "PARAM_CAMERA_TYPE"
            const val PARAM_PACKAGE_ID = "package_id"
            const val PARAM_PRICE = "price"
            const val PARAM_OPERATOR = "operator"
            const val PARAM_VALUE_TYPE = "value_type"
            const val PARAM_ACTION_CALL = "action_call"
            const val PARAM_IS_FROM_LIVEVIEW = "PARAM_IS_FROM_LIVEVIEW"
            const val PARAM_CAMERA_PASSWORD = "PARAM_CAMERA_PASSWORD"

            // Face Management
            const val PARAM_TYPE_SHARE_DEVICE = "PARAM_TYPE_SHARE_DEVICE"
            const val PARAM_TYPE_SHARE_DEVICE_SHARING = "PARAM_TYPE_SHARE_DEVICE_SHARING"
            const val PARAM_TYPE_SHARE_DEVICE_RECEIVED_PENDING =
                "PARAM_TYPE_SHARE_DEVICE_RECEIVED_PENDING"
            const val PARAM_TYPE_SHARE_DEVICE_RECEIVED_ACCEPT =
                "PARAM_TYPE_SHARE_DEVICE_RECEIVED_ACCEPT"

            const val PARAM_TYPE_SHARING_DEVICE = "PARAM_TYPE_SHARING_DEVICE"
            const val PARAM_TYPE_SHARING_FRIEND_DEVICE = "PARAM_TYPE_SHARING_FRIEND_DEVICE"
            const val PARAM_TYPE_SCREEN_OPEN_CREATE_SHARE_CAMERA_EZVIZ =
                "PARAM_TYPE_CREEN_OPEN_CREATE_SHARE_CAMERA_EZVIZ"
            const val PARAM_TYPE_TO_LIVE_VIEW_OPEN_CREATE_SHARE_CAMERA_EZVIZ =
                "PARAM_TYPE_TO_LIVE_VIEW_OPEN_CREATE_SHARE_CAMERA_EZVIZ"
            const val PARAM_TYPE_TO_CHOSSE_DEVICE_OPEN_CREATE_SHARE_CAMERA_EZVIZ =
                "PARAM_TYPE_TO_CHOSSE_DEVICE_OPEN_CREATE_SHARE_CAMERA_EZVIZ"
            const val PARAM_TYPE_TO_CHOOSE_HOME_ORG_ID_SELECT =
                "PARAM_TYPE_TO_CHOOSE_HOME_ORG_ID_SELECT"
            const val PARAM_TYPE_TO_CHOOSE_ROOM_ORG_ID_SELECT =
                "PARAM_TYPE_TO_CHOOSE_ROOM_ORG_ID_SELECT"

            const val PARAM_TYPE_SCREEN_OPEN_CREATE_SHARE_CAMERA_JF =
                "PARAM_TYPE_CREEN_OPEN_CREATE_SHARE_CAMERA_JF"
            const val PARAM_TYPE_SHARE_CAMERA_JF = "PARAM_TYPE_SHARE_CAMERA_JF"
            const val PARAM_IS_SHARE_CAMERA_JF = "PARAM_IS_SHARE_CAMERA_JF"
            const val PARAM_TOTAL_SHARE_CAMERA_JF = "PARAM_TOTAL_SHARE_CAMERA_JF"


            // DỮ LIỆU MỞ MÀN ĐIỀU KHOẢN SỬ DỤNG
            const val PARAM_SCREEN_OPEN_PRIVATE_POLICY = "PARAM_SCREEN_OPEN_PRIVATE_POLICY"
            const val REGISTER_PARAM_SCREEN_OPEN_PRIVATE_POLICY =
                "REGISTER_PARAM_SCREEN_OPEN_PRIVATE_POLICY"
            const val DIALOG_CONFIG_PARAM_SCREEN_OPEN_PRIVATE_POLICY =
                "DIALOG_CONFIG_PARAM_SCREEN_OPEN_PRIVATE_POLICY"
            const val SETTING_PARAM_SCREEN_OPEN_PRIVATE_POLICY =
                "SETTING_PARAM_SCREEN_OPEN_PRIVATE_POLICY"


            // Face Management
            const val PARAM_NUMBER_OF_SELECTABLE_SAMPLES = "NUMBER_OF_SELECTABLE_SAMPLES"
            const val PARAM_FACE_DATA_STATE = "FACE_DATA_STATE"
            const val PARAM_FACE_ID = "FACE_ID"
            const val PARAM_FACE_DETAIL = "FACE_DETAIL"
            const val PARAM_LEFT_IMAGES = "LEFT_IMAGES"
            const val PARAM_RIGHT_IMAGES = "RIGHT_IMAGES"
            const val PARAM_STRAIGHT_IMAGES = "STRAIGHT_IMAGES"
            const val PARAM_FACE_GROUPS = "FACE_GROUPS"
            const val PARAM_FACE_DETAIL_STATE = "FACE_DETAIL_STATE"
            const val PARAM_LIST_FACE_SAMPLE = "LIST_FACE_SAMPLE"
            const val PARAM_NAVIGATE_FROM = "TAKE_NAVIGATE_FROM"
            // Liên kết tài khoản
            const val PARAM_URL_REJECT_PAY_LINK = "PARAM_URL_REJECT_PAY_LINK"
            const val PARAM_URL_REGISTER_PAY_LINK = "PARAM_URL_REGISTER_PAY_LINK"
            const val PARAM_OPEN_SCREEN_PAY_LINK = "PARAM_OPEN_SCREEN_PAY_LINK"
            const val PARAM_CHANGE_LIMIT_PAY_LINK = "PARAM_CHANGE_LIMIT_PAY_LINK"
            const val PARAM_OPEN_SCREEN_REGISTER_PAY_LINK = "PARAM_OPEN_SCREEN_REGISTER_PAY_LINK"
            const val PARAM_OPEN_SCREEN_SETTING_PAY_LINK = "PARAM_OPEN_SCREEN_SETTING_PAY_LINK"
            const val PARAM_DATA_DETAIL_HISTORY_CLOUD = "PARAM_DATA_DETAIL_HISTORY_CLOUD"
            const val PARAM_DATA_ORDER_ID_VT_PAY_WALLET = "PARAM_DATA_ORDER_ID_VT_PAY_WALLET"
            const val PARAM_DATA_OPEN_AUTO_CHARGING_CLOUD = "PARAM_DATA_OPEN_AUTO_CHARGING_CLOUD"
            const val PARAM_DATA_STATUS_PAYLINK = "PARAM_DATA_STATUS_PAYLINK"
            const val PARAM_DATA_REGISTER_CLOUD_PROMOTION = "PARAM_DATA_REGISTER_CLOUD_PROMOTION"
            const val PARAM_DATA_REGISTER_REFER_CODE_CLOUD = "PARAM_DATA_REGISTER_REFER_CODE_CLOUD"

            const val PARAM_TAKE_SAMPLE_FOR = "TAKE_SAMPLE_TO"

            const val PARAM_TIME_PLAN = "TIME_PLAN"

            //Smart home group
            const val PARAM_GROUP = "PARAM_GROUP"
            const val PARAM_GROUP_ID = "PARAM_GROUP_ID"
            const val PARAM_IS_EDIT = "PARAM_IS_EDIT"
            const val PARAM_NOTIFICATION_TYPE = "NOTIFICATION_TYPE"
            const val PARAM_NOTIFICATION_DATA = "NOTIFICATION_DATA"

            const val PARAM_TYPE_SCREEN_HISTORY_CLOUD = "PARAM_TYPE_SCREEN_HISTORY_CLOUD"

            // Service tab
            const val PARAM_SEARCH_PRODUCTS = "PARAM_SEARCH_PRODUCTS"
            const val PARAM_SEARCH_PRODUCTS_TEXT = "PARAM_SEARCH_PRODUCTS_TEXT"
            const val PARAM_IS_FOCUS_SEARCH_PRODUCT = "PARAM_IS_FOCUS_SEARCH_PRODUCT"
            const val PARAM_IS_ALL_PRODUCT = "PARAM_IS_ALL_PRODUCT"
            const val PARAM_URL_PRODUCT_SERVICE = "PARAM_SERVICE_URL_PRODUCT"
            const val PARAM_SERVICE_WARNING = "PARAM_SERVICE_WARNING"
            const val PARAM_SERVICE_WARNING_URL_VIDEO = "PARAM_SERVICE_WARNING_URL_VIDEO"
            const val PARAM_KEY_WEB_VIEW_PRODUCT = "PARAM_KEY_WEB_VIEW_PRODUCT"

            const val PARAM_DETAIL_DEVICE_WARRANTY = "PARAM_DETAIL_DEVICE_WARRANTY"

            // Multi views
            const val PARAM_LIST_HOME_ID = "PARAM_LIST_HOME_ID"
            const val PARAM_LIST_CAMERA_OBJECT = "PARAM_LIST_CAMERA_OBJECT"
            const val PARAM_IS_FROM_MULTIPLE_LIVE = "PARAM_IS_FROM_MULTIPLE_LIVE"


            const val PARAM_OPEN_TO_GIFT_RELATIVES = "PARAM_OPEN_TO_GIFT_RELATIVES"
            const val PARAM_USE_ID_TO_GIFT_RELATIVES = "PARAM_USE_ID_TO_GIFT_RELATIVES"
        }
    }

    enum class ListShowType {
        EDIT_HOME,
        ADD_HOME,
        ADD_ROOM,
        EDIT_ROOM
    }

    class LOGIN {
        companion object {
            const val INPUTTYPE_TEXTPASSWORD =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
    }

    class DEVICE {
        companion object {
            const val NAME = "name"
            const val DEVICE_ID = "deviceid"
            const val SETTINGS = "settings"
            const val IF_CODE = "ifrCode"
            const val DIGEST = "digest"
            const val CHIP_ID = "chipid"
            const val FAMILY_ID = "familyid"
            const val ROOM_ID = "roomid"
            const val SORT = "sort"
            var addDeviceResponse = ""
        }
    }

    class REGISTER {
        companion object {
            const val TYPE = "type"
            const val EMAIL = "email"
            const val PHONE_NUMBER = "phoneNumber"
            const val COUNTRY_CODE = "countryCode"
            const val VERIFICATION_CODE = "verificationCode"
            const val PASSWORD = "password"
            const val PHONE = "phone"
            const val CODE = "code"
            const val OTP = "otp"
            const val OTP_TYPE = "otp_type"
            const val OTP_REGISTER = "register"
            const val OTP_FORGOT = "forgot_password_vhome"
            const val OTP_NEW_APP_ID = "new_app_login"
            const val NAME = "name"
        }
    }

    object USER_INFO {
        const val NAME = "name"
        const val EMAIL = "email"
    }

    class TYPE_VERIFICATION {
        companion object {
            const val REGISTER: Int = 0
            const val FORGOT_PASSWORD: Int = 1
            const val CHANGE_PASSWORD: Int = 2
            const val DELETE_ACCOUNT: Int = 3
            const val LOGIN: Int = 4
            const val NEW_APP_ID = 5
        }
    }

    enum class Status {
        PENDING,
        RUNNING,
        FINISHED
    }

    enum class StateShare {
        SHARING,
        SHARED,
        NONE
    }

    class TYPE_DEVICE {
        companion object {
            const val CAMERA = "0"
            const val CAMERA_IPC = "camIpc"
            const val CAMERA_JF = "camJF"
            const val SWITCH = "1"
            const val GATE_WAY = "2"
            const val DOOR = "3"
            const val MOTION = "4"
            const val HUMIDITY = "5"
            const val SMOKE = "6"
            const val AIR_PURIFIER = "7"
            const val ONT_GATEWAY = "gw"

            //            const val ONT_GATEWAY = "8"
            const val SMART_LOCK = "10"
            const val LAMP = "11"

            const val CURTAINS = "14"
            const val EZCAMERA = "cam"
            const val CAMERA_IMOU = "imou"
            const val MAIKA = "maika"
            const val SCENE_SWITCH = "15"

            const val ZIGBEE_REPEATER = "17"
            const val IR_GATEWAY = "gw_ir"
            const val VTTCAMERA = "camVTT"
            const val BLE_LED_DOWNLIGHT_V2 = "ble_led_downlight"
            const val BLE_LED_RGB = "ble_led_rgb"
            const val BLE_LED_RGB_A60 = "ble_led_rgb_a60"
            const val BLE_LED_RGB_STRIP = "ble_led_rgb_strip"
            const val BLE_LED_AT18 = "ble_led_downlight_at18"
            const val BLE_LED_AT20 = "ble_led_downlight_at20"
            const val BLE_LED_AT22 = "ble_led_downlight_at22"
            const val WIFI_BULB_RGB = "wifi_bulb_rgb"

            const val SOCKET_ZIGBEE = "16"
            const val SOCKET_WIFI = "18"

            const val MULTI_SWITCH_ZIGBEE = "12"
            const val MULTI_SWITCH_WIFI = "19"
            const val ROLLING_DOOR = "rolling_door_switch"

            const val LIGHTING_C = "zb_lighting_c"
            const val LIGHTING_CW = "zb_lighting_cw"
            const val LIGHTING_RGB = "zb_lighting_rgb"
            const val LIGHTING_RGBC = "zb_lighting_rgbc"
            const val LIGHTING_RGBCW = "zb_lighting_rgbcw"

            //Tuya device
            const val TUYA_ZIGBEE_GATEWAY = "wfcon"
            const val TUYA_DOOR_SENSOR = "mcs"
            const val TUYA_MOTION_SENSOR = "pir"
            const val TUYA_SWITCH = "kg"
            const val TUYA_SOCKET = "pc"
            const val TUYA_SMART_LOCK = "jtmspro"
            const val TUYA_ROLLING_DOOR = "clkg"
            const val TUYA_ROLLING_DOOR_2 = "ckmkzq"


        }
    }

    class CAMERA_MODEL {
        companion object {
            const val CAMERA_JF_INDOOR = "HC23" // camera indoor
            const val CAMERA_JF_OUTDOOR = "HC33" // camera outdoor
            const val CAMERA_JF_VENDOR = "camJF"
            const val CAMERA_EZ_INDOOR = "HC2" // camera indoor
            const val CAMERA_EZ_OUTDOOR = "HC3" // camera OUTdoor
            const val CAMERA_EZ_VENDOR = "camVTT"
            const val CAMERA_IPC_INDOOR = "HC22" // camera indoor
            const val CAMERA_IPC_OUTDOOR = "HC32" // camera indoor
            const val CAMERA_IPC_VENDOR = "camIpc"
        }
    }

    class LANGUAGE {
        companion object {
            const val VIETNAMESE = "vi"
            const val ENGLISH = "en"
        }
    }

    class PAIR_DEVICE {
        companion object {
            const val FLAGS = "flags"
            const val COMMAND = "command"
            const val DATA = "data"
            const val MODE = "mode"
            const val ACTION = "action"
            const val DEVICE_ID = "deviceid"
        }
    }

    class STATUS_DEVICE {
        companion object {
            const val POWER = "power"
            const val DOOR = "status_door"
            const val MOTION = "status_motion"
            const val SMOKE = "status_smoke"
            const val TEMPERATURE = "temperature"
            const val HUMIDITY = "humidity"
            const val ONLINE = "online"
            const val OFFLINE = "offline"
            const val AIR_PM1 = "pm1.0"
            const val AIR_IAQ = "iaq"
            const val AIR_PM25 = "pm2.5"
            const val AIR_PM10 = "pm10"
            const val AIR_CO2 = "co2"
            const val AIR_HCHO = "hcho"
            const val DEVICE = "Device"
            const val SEND = "Send"
            const val LOCK = "doorStatus"
            const val POWER_LED = "powerLed"
            const val CURTAINS = "status_curtain"
            const val MAIKA = "action"
            const val VALUE = "value"
            const val BLE_ON_OFF = "light-on-off"
            const val BLE_DIMMER = "light-dim"
            const val BLE_LIGHT_RGB = "light-rgb"
            const val BLE_LIGHT_CTT = "light-cct"
        }
    }

    class MANAGER_TYPE {
        companion object {
            const val HOME = 1
            const val ROOM = 2
        }
    }

    enum class PLAYBACK_RATE(
        val id: Int,
        val value: Int,
        val speed: String
    ) {
        PLAYBACK_RATE_32(
            10,
            32,
            "32 lần"
        ),
        PLAYBACK_RATE_16(
            8,
            16,
            "16 lần"
        ),
        PLAYBACK_RATE_8(
            6,
            8,
            "8 lần"
        ),
        PLAYBACK_RATE_4(
            4,
            4,
            "4 lần"
        ),
        PLAYBACK_RATE_1(
            1,
            1,
            "1 lần"
        )
    }

    class TYPE_DIALOG {
        companion object {
            const val DEFAULT = -1
            const val CONFIRM = 1
            const val UN_AUTHEN = 2
            const val NO_INTERNET = 3
            const val GENERIC = 4
            const val NO_CONNECTED_INTERNET = 5
            const val TYPE_DEVICE_MAIKA = 6
        }
    }

    enum class SCENE_DEVICE(
        val nameDevice: String,
        val status: String,
        val type: String,
        val actionFirst: String,
        val actionSecond: String
    ) {
        SWITCH(
            "Công tắc",
            STATUS_DEVICE.POWER,
            TYPE_DEVICE.SWITCH,
            "on",
            "off"
        ),
        GATE_WAY(
            "Gateway Zigbee",
            "",
            TYPE_DEVICE.GATE_WAY,
            "",
            ""
        ),
        DOOR(
            "Cảm biến cửa",
            STATUS_DEVICE.DOOR,
            TYPE_DEVICE.DOOR,
            "open",
            "close"
        ),
        MOTION(
            "Cảm biến chuyển động",
            STATUS_DEVICE.MOTION,
            TYPE_DEVICE.MOTION,
            "0",
            "1"
        ),
        HUMIDITY(
            "Cảm biến nhiệt độ, độ ẩm",
            STATUS_DEVICE.HUMIDITY,
            TYPE_DEVICE.HUMIDITY,
            "0",
            "1"
        ),
        SMOKE(
            "Cảm biến khói",
            STATUS_DEVICE.SMOKE,
            TYPE_DEVICE.SMOKE,
            "0",
            "1"
        ),
        AIR_PURIFIER(
            "Máy lọc không khí",
            "",
            TYPE_DEVICE.AIR_PURIFIER,
            "0",
            "1"
        ),
        SMART_LOCK(
            "Khóa thông minh",
            STATUS_DEVICE.LOCK,
            TYPE_DEVICE.SMART_LOCK,
            "lock.open",
            "lock.close"
        ),
        CURTAINS(
            "Rèm thông minh",
            STATUS_DEVICE.CURTAINS,
            TYPE_DEVICE.CURTAINS,
            OPEN_CURTAINS,
            CLOSE_CURTAINS
        ),
        MAIKA(
            "Loa Maika",
            STATUS_DEVICE.MAIKA,
            TYPE_DEVICE.MAIKA,
            "1",
            "0"
        ),
        AIR_CON(
            "Điều hòa",
            STATUS_DEVICE.POWER,
            IR_TYPE_DEVICE.AIR_CONDITION,
            "on",
            "off"
        ),
        TV(
            "Ti vi",
            STATUS_DEVICE.POWER,
            IR_TYPE_DEVICE.TV,
            "on",
            "off"
        ),
        CUSTOM(
            "Custom",
            STATUS_DEVICE.POWER,
            IR_TYPE_DEVICE.CUSTOM,
            "on",
            "off"
        ),
        EVENT(
            "Ngữ cảnh",
            "",
            "",
            "on",
            "off"
        )
    }

    class SCENE_TYPE {
        companion object {
            const val SMART_DEVICE = "normal"
            const val TIME = "time"
            const val SEND_SMS = "smsvhome"
            const val SEND_EMAIL = "email"
            const val MQTT = "mqtt"
            const val FCM = "fcm"
            const val CAMERA = "camera"
            const val WEATHER = "weather"
            const val SCENE_SWITCH = "scene_switch"
            const val ON_OFF_SCENE = "event"
            const val ON_TAP = "tap"
            const val ACTIVATE_SCENE = "eventactivate"
            const val DELAY = "delay"
            const val MULTI_ACTION_IR = "multi_action_ir"
        }
    }

    enum class ActionSceneType {
        EDIT_SCENE,
        ADD_SCENE,
        ADD_SCENE_SWITCH
    }

    class ActionTypeMqtt {
        companion object {
            const val EVENT = 1
            const val DATA = 2
            const val DEVICE_ENJOINED = 3
        }
    }

    class LogicalScene {
        companion object {
            const val OR = "or"
            const val AND = "and"
        }
    }

    class SMART_LOCK {
        companion object {
            const val PHYSICAL = "00"
            const val ZIGBEE = "01"
            const val CODE = "02"
            const val FINGER = "03"
            const val AUTO = "04"
            const val STATE = "05"
            const val WARNING = "06"
        }
    }

    class STATUS_SMART_LOCK {
        companion object {
            const val LOCK = "01"
            const val OPEN = "02"//unlock
        }
    }

    class SCENE_ACTION_ITEM {
        companion object {
            const val ADD = "add"
            const val EDIT = "edit"
        }
    }

    class SCENE_WEATHER {
        companion object {
            const val TEMPERATURE = "TEMPERATURE"
            const val HUMIDITY = "HUMIDITY"
            const val WEATHER = "WEATHER"
            const val SUN_RISE = "SUN_RISE"
            const val WIND_SPEED = "WIND_SPEED"
        }
    }

    class Intent {
        companion object {
            const val ACTION_CAMERA = "ACTION_CAMERA"
            const val ACTION_NOTIFICATION_SYSTEM = "ACTION_NOTIFICATION_SYSTEM"
            const val ACTION_SHARING_CAMERA = "ACTION_SHARING_CAMERA"
            const val ACTION_INVITE_MEMBER = "InviteMember"
            const val ACTION_REMOVE_MEMBER = "RemoveMember"
            const val ACTION_ROLLING_DOOR_ALARM = "rolling_alarm"
            const val ACTION_OPEN_MAIN = "ACTION_OPEN_MAIN"
            const val ACTION_SMART_SCENE_NOTIFICATION = "ACTION_SMART_SCENE_NOTIFICATION"
            const val ACTION_JF_FIRMWARE_NOTIFY = "ACTION_JF_FIRMWARE_NOTIFY"
            const val BUNDLE = "bundle"
            const val MESSAGE = "message"
            const val THUMBNAIL_URL = "thumbnailUrl"
            const val USER_ID = "userId"
            const val DEVICE_ID = "deviceId"
            const val DEVICE_NAME = "deviceName"
            const val TIME = "time"
            const val EVENT_ID = "eventId"
            const val VIDEO_ID = "videoId"
            const val EVENT_TYPE = "eventType"
            const val ACTION = "ACTION"
            const val DATA_FROM_NOTIFICATION: String = "DATA_FROM_NOTIFICATION"

            //ftth
            const val PARAM_ITEM_PACK = "PARAM_ITEM_PACK"
            const val TYPE_INTERNET_PACK = "TYPE_INTERNET_PACK"
            const val TYPE_REGISTER = "TYPE_REGISTER"
            const val TYPE_CAMERA = "TYPE_CAMERA"
        }
    }

    class ACTION_CURTAINS {
        companion object {
            const val OPEN = "open"
            const val CLOSE = "close"
            const val PAUSE = "pause"
            const val PROGRESS = "progress"
            const val CONVERT = "convert"
            const val OPEN_CURTAINS = "00190104000102"
            const val CLOSE_CURTAINS = "00300104000100"
            const val PAUSE_CURTAINS = "00510104000101"
            const val PROGRESS_CURTAINS = "EF00!00/005D02020004000000"
        }
    }

    enum class DeviceType(
        val deviceType: String,
        val deviceName: String,
        val productName: String,
        val typeCode: String,
        val clusterIn: List<String>,
        val clusterOut: List<String>
    ) {
        AQUARA(
            TYPE_DEVICE.MOTION,
            "Cảm biến chuyển động",
            "Aquara",
            "0x5F01",
            listOf("0x0000", "0x0003", "0xFFFF", "0x0006"),
            listOf("0x0000", "0x0004", "0xFFFF")
        ),
        HEIMAN(
            TYPE_DEVICE.DOOR,
            "Cảm biến cửa",
            "Heiman",
            "0x0402",
            listOf("0x0000", "0x0001", "0x0003", "0x0500", "0x0B05"),
            listOf("0x0019")
        ),
        MOTION_TUYA(
            TYPE_DEVICE.MOTION,
            "Cảm biến chuyển động",
            "Tuya",
            "0x0402",
            listOf("0x0000", "0x0001", "0x0500"),
            listOf("0x0019", "0x000A")
        ),
        CURTAINS_TUYA(
            TYPE_DEVICE.CURTAINS,
            "Rèm thông minh",
            "Tuya",
            "EF00",
            listOf("00"),
            listOf("0203")
        )
    }

    class StatusDevice {
        companion object {
            const val POWER_ON = "on"
            const val POWER_OFF = "off"
            const val ACTION_CLICK = "click"
            const val ACTION_DOUBLE = "double"
            const val ACTION_LONG = "long"
        }
    }

    enum class SceneSWitchAction(
        val typeAction: String,
        val nameAction: String
    ) {
        CLICK(
            "00",
            "Chạm để thực hiện"
        ),
        DOUBLE(
            "01",
            "Chạm hai lần",
        ),
        LONG(
            "02",
            "Chạm và giữ"
        )
    }

    class InfraredKey {
        companion object {
            const val CUSTOM_DEVICE = "CUSTOM_DEVICE"
            const val DEVICE_NORMAL = "DEVICE_NORMAL"
            const val ADD_NEW_KEY = "ADD_NEW_KEY"
            const val CHANGE_KEY_FUNCTION = "CHANGE_KEY_FUNCTION"
            const val UPDATE_KEY_FUNCTION = "UPDATE_KEY_FUNCTION"
            const val BACK = "back"
            const val POWER = "power"
            const val HOME = "home"
            const val MENU = "menu"
            const val MUTE = "mute"
            const val PLAY = "play"
            const val VOL_UP = "volUp"
            const val VOL_DOWN = "volDown"
            const val CHAN_UP = "chanUp"
            const val CHAN_DOWN = "chanDown"
            const val UP = "up"
            const val DOWN = "down"
            const val LEFT = "left"
            const val RIGHT = "right"
            const val OK = "ok"
            const val MAIN_KEYBOARD = "main"
            const val SUB_KEYBOARD = "sub"
            const val ADD_KEY_FUNCTION = "ADD_KEY_FUNCTION"
            const val SCANNER_AIR_CON = "SCANNER_AIR_CON"
            const val SCANNER_DEVICE = "SCANNER_DEVICE"
            const val MODE = "play"
            const val SWING_V = "swingv"
            const val AIRCON_FAN_SPEED = "fanspeed"
            const val DEGREES = "degrees"
            const val FAN_SPEED = "fan_speed"
            const val FAN_POWER = "fan_power"
            const val FAN_TIMER = "fan_timer"
            const val FAN_RHYTHM = "fan_rhythm"
            const val FAN_SWING = "fan_swing"
            const val STB_TV_POWER = "stb_tv_power"

            //fan 2
            const val FAN_SPEED_1 = "fan_speed_1"
            const val FAN_SPEED_2 = "fan_speed_2"
            const val FAN_SPEED_3 = "fan_speed_3"
            const val FAN_OFF_TIMER_1 = "fan_off_timer_1"
            const val FAN_OFF_TIMER_2 = "fan_off_timer_2"
            const val FAN_OFF_TIMER_3 = "fan_off_timer_3"
            const val FAN_CANCEL = "fan_cancel"
            const val FAN_SLEEP = "fan_sleep"
        }
    }

    class Protocol {
        companion object {
            const val IR = "ir"
            const val ZIGBEE = "zigbee"
            const val BLE = "ble"
            const val WIFI = "wifi"
        }
    }

    class IR_TYPE_DEVICE {
        companion object {
            const val TV = "tv"
            const val AIR_CONDITION = "aircon"
            const val CUSTOM = "custom"
            const val FAN = "fan"
            const val STB = "stb"
        }
    }

    class SETTING_DEVICE {
        companion object {
            const val HUB = "HUB"
            const val WIFI_LIGHTING = "WIFI_LIGHTING"
            const val SUB_DEVICE = "SUB_DEVICE"
            const val SUB_IR = "SUB_IR"
            const val UPGRADE_FIRMWARE = "UPGRADE_FIRMWARE"
            const val ON_OFF_NOTIFY = "ON_OFF_NOTIFY"
            const val SETTING_WIFI = "SETTING_WIFI"
            const val SETTING_KEY = "SETTING_KEY"
            const val CURTAINS = "SETTING_CURTAINS"
            const val ONT_GATEWAY = "ONT_GATEWAY"
            const val MOVE_DEVICE = "MOVE_DEVICE"
            const val SETTING_KEY_NAME = "SETTING_KEY_NAME"
            const val LED = "LED"
            const val ROLLING_DOOR_DURATION = "ROLLING_DOOR_DURATION"
            const val ROLLING_DOOR_ALERT = "ROLLING_DOOR_ALERT"
        }
    }

    enum class ButtonTelevisionData(
        val title: String,
        val key: String,
        val imgEnableResource: Int,
        val imgDisableResource: Int,
    ) {
        BACK(
            "Back",
            InfraredKey.BACK,
            R.drawable.ic_rotate_left,
            R.drawable.ic_rotate_left
        ),
        POWER(
            "Power",
            InfraredKey.POWER,
            R.drawable.ic_enable_power,
            R.drawable.ic_disable_power
        ),
        HOME(
            "Home",
            InfraredKey.HOME,
            R.drawable.ic_enale_home,
            R.drawable.ic_disable_home
        ),
        MUTE(
            "Mute",
            InfraredKey.MUTE,
            R.drawable.ic_enable_volume,
            R.drawable.ic_disable_volume
        ),
        PLAY(
            "Play",
            InfraredKey.PLAY,
            R.drawable.ic_enable_play,
            R.drawable.ic_disable_play
        ),
        MENU(
            "Menu",
            InfraredKey.MENU,
            R.drawable.ic_enable_menu,
            R.drawable.ic_disable_menu
        )
    }

    enum class ButtonSTBData(
        val title: String,
        val key: String,
        val imgEnableResource: Int,
        val imgDisableResource: Int,
    ) {
        TV_POWER(
            "TV Power",
            InfraredKey.STB_TV_POWER,
            R.drawable.ic_enale_tv_power,
            R.drawable.ic_disable_tv_power
        ),
        POWER(
            "Power",
            InfraredKey.POWER,
            R.drawable.ic_enable_power,
            R.drawable.ic_disable_power
        ),
        MENU(
            "Menu",
            InfraredKey.MENU,
            R.drawable.ic_enable_menu,
            R.drawable.ic_disable_menu
        ),
        BACK(
            "Back",
            InfraredKey.BACK,
            R.drawable.ic_rotate_left,
            R.drawable.ic_rotate_left
        ),
        HOME(
            "Home",
            InfraredKey.HOME,
            R.drawable.ic_enale_home,
            R.drawable.ic_disable_home
        ),
        MUTE(
            "Mute",
            InfraredKey.MUTE,
            R.drawable.ic_enable_volume,
            R.drawable.ic_disable_volume
        )
    }

    enum class ButtonFanData(
        val title: String,
        val key: String,
        val imgEnableResource: Int,
        val imgDisableResource: Int,
    ) {
        SPEED(
            "Speed",
            InfraredKey.FAN_SPEED,
            R.drawable.ic_endable_fan_speed_1,
            R.drawable.ic_disable_fan_speed_1
        ),
        POWER(
            "Power",
            InfraredKey.FAN_POWER,
            R.drawable.ic_enable_power,
            R.drawable.ic_disable_power
        ),
        SWING(
            "Swing",
            InfraredKey.FAN_SWING,
            R.drawable.ic_enable_swing,
            R.drawable.ic_disable_swing
        ),
        RHYTHM(
            "Rhythm",
            InfraredKey.FAN_RHYTHM,
            R.drawable.ic_endable_fan_mode,
            R.drawable.ic_disable_fan_mode
        ),
        TIMER(
            "Timer",
            InfraredKey.FAN_TIMER,
            R.drawable.ic_endable_timming,
            R.drawable.ic_disable_timming
        ),
    }

    enum class ButtonFanData2(
        val title: String,
        val key: String,
        val imgEnableResource: Int,
        val imgDisableResource: Int,
    ) {
        POWER(
            "Power",
            InfraredKey.FAN_POWER,
            R.drawable.ic_enable_power,
            R.drawable.ic_disable_power
        ),
        SPEED_1(
            "Speed 1",
            InfraredKey.FAN_SPEED_1,
            R.drawable.ic_endable_fan_speed_2,
            R.drawable.ic_endable_fan_speed_2
        ),
        SPEED_2(
            "Speed 2",
            InfraredKey.FAN_SPEED_2,
            R.drawable.ic_endable_fan_speed_2,
            R.drawable.ic_disable_fan_speed_2
        ),
        SPEED_3(
            "Speed 3",
            InfraredKey.FAN_SPEED_3,
            R.drawable.ic_endable_fan_speed_1,
            R.drawable.ic_disable_fan_speed_1
        ),
        OFF_TIMER_1(
            "Off Timer 1",
            InfraredKey.FAN_OFF_TIMER_1,
            R.drawable.ic_endable_timming,
            R.drawable.ic_disable_timming
        ),
        OFF_TIMER_2(
            "Off Timer 2",
            InfraredKey.FAN_OFF_TIMER_2,
            R.drawable.ic_endable_timming,
            R.drawable.ic_disable_timming
        ),
        OFF_TIMER_3(
            "Off Timer 3",
            InfraredKey.FAN_OFF_TIMER_3,
            R.drawable.ic_endable_timming,
            R.drawable.ic_disable_timming
        ),
        CANCEL(
            "Cancel",
            InfraredKey.FAN_CANCEL,
            R.drawable.ic_rotate_left,
            R.drawable.ic_disable_rotate_left
        ),
        SLEEP(
            "Sleep Mode",
            InfraredKey.FAN_SLEEP,
            R.drawable.ic_endable_timming,
            R.drawable.ic_disable_timming
        ),
    }

    enum class ModeAirCondition(
        val title: String,
        val key: Int,
        val imgEnableResource: Int,
        val imgDisableResource: Int,
    ) {
        OFF(
            "Off",
            -1,
            R.drawable.ic_rotate_left,
            R.drawable.ic_rotate_left
        ),
        AUTO(
            "Auto",
            0,
            R.drawable.ic_rotate_left,
            R.drawable.ic_rotate_left
        ),
        COOL(
            "Cool",
            1,
            R.drawable.ic_rotate_left,
            R.drawable.ic_rotate_left
        ),
        HEAT(
            "Heat",
            2,
            R.drawable.ic_rotate_left,
            R.drawable.ic_rotate_left
        ),
        DRY(
            "Dry",
            3,
            R.drawable.ic_rotate_left,
            R.drawable.ic_rotate_left
        ),
        FAN(
            "Fan",
            4,
            R.drawable.ic_rotate_left,
            R.drawable.ic_rotate_left
        )
    }

    enum class FanAirCondition(
        val title: String,
        val key: Int,
        val imgEnableResource: Int,
        val imgDisableResource: Int,
    ) {
        AUTO(
            "Auto",
            0,
            R.drawable.ic_rotate_left,
            R.drawable.ic_rotate_left
        ),
        MIN(
            "Min",
            1,
            R.drawable.ic_rotate_left,
            R.drawable.ic_rotate_left
        ),
        LOW(
            "Low",
            2,
            R.drawable.ic_rotate_left,
            R.drawable.ic_rotate_left
        ),
        MEDIUM(
            "Medium",
            3,
            R.drawable.ic_rotate_left,
            R.drawable.ic_rotate_left
        ),
        HIGH(
            "High",
            4,
            R.drawable.ic_rotate_left,
            R.drawable.ic_rotate_left
        ),
        MAX(
            "Max",
            5,
            R.drawable.ic_rotate_left,
            R.drawable.ic_rotate_left
        )
    }

    enum class SwingAirCondition(
        val title: String,
        val key: Int,
        val imgEnableResource: Int,
        val imgDisableResource: Int,
    ) {
        OFF(
            "Off",
            -1,
            R.drawable.ic_rotate_left,
            R.drawable.ic_rotate_left
        ),
        AUTO(
            "Auto",
            0,
            R.drawable.ic_rotate_left,
            R.drawable.ic_rotate_left
        ),
        HIGHEST(
            "Highest",
            1,
            R.drawable.ic_rotate_left,
            R.drawable.ic_rotate_left
        ),
        HIGH(
            "High",
            2,
            R.drawable.ic_rotate_left,
            R.drawable.ic_rotate_left
        ),
        MIDDLE(
            "Middle",
            3,
            R.drawable.ic_rotate_left,
            R.drawable.ic_rotate_left
        ),
        LOW(
            "Low",
            4,
            R.drawable.ic_rotate_left,
            R.drawable.ic_rotate_left
        ),
        LOWEST(
            "Lowest",
            5,
            R.drawable.ic_rotate_left,
            R.drawable.ic_rotate_left
        )
    }

    enum class PowerCondition(
        val title: String,
        val status: Boolean,
        val imgEnableResource: Int,
        val imgDisableResource: Int,
    ) {
        POWER_ON(
            "Power",
            true,
            R.drawable.ic_enable_power,
            R.drawable.ic_disable_power
        ),
        POWER_OFF(
            "Power",
            false,
            R.drawable.ic_enable_power,
            R.drawable.ic_disable_power
        )
    }

    class StatusCamera {
        companion object {
            const val DEVICE_LIGHT = 3
            const val PRIVACY_MODE = 7
            const val IR_LIGHT = 10
            const val SLEEP_MODE = 21
            const val SOUND = 22
            const val AUTO_TRACKING = 650
        }
    }

    enum class TimeFormatCamera(
        val id: Int,
        val title: String
    ) {
        TIME_0(
            0,
            "YYYY-MM-DD"
        ),
        TIME_1(
            1,
            "MM-DD-YYYY"
        ),
        TIME_2(
            2,
            "DD-MM-YYYY"
        )
    }

    class FragmentResultKey {
        companion object {
            const val ORG_ID = "ORG_ID"
            const val BORDER_LIGHT = "BORDER_LIGHT"
            const val FROM_DESTINATION = "FROM_DESTINATION"
        }
    }

    enum class FirebaseEvent(val value: String) {
        SELECT_DEVICE("select_device"),
        CLICK_AI("android_click_ai"),
        WAKE_AI("android_wake_ai"),
        CLICK_TAB_SCENE("android_click_tab_scene")
    }

    enum class FirebaseEventParam(val value: String) {
        DEVICE("device")
    }

    class SceneDefaultType {
        companion object {
            const val ECOSYSTEM = "ecosystem"
            const val SECURITY = "security"
            const val CONVENIENT = "convenient"
        }
    }

    enum class TypeScheduleAlarm(val type: Int) {
        ADD(0),
        EDIT(1),
        DELETE(2)
    }

    enum class Instruction(val value: Int) {
        FIRST_APP(0),
        FIRST_DEVICE(1),
        FIRST_FAVORITE(2),
        FIRST_ADD_FAVORITE(3),
        EDIT_FAVORITE(4)
    }

    enum class LoadingState {
        LOADING,
        SUCCESS,
        FAIL,
    }

    enum class FaceDirection(val direction: String) {
        STRAIGHT("STRAIGHT"),
        LEFT("LEFT"),
        NEAR_LEFT("NEAR_LEFT"),
        OVER_LEFT("OVER_LEFT"),
        RIGHT("RIGHT"),
        NEAR_RIGHT("NEAR_RIGHT"),
        OVER_RIGHT("OVER_RIGHT"),
        INVALID("INVALID")
    }

    class EzvizNotification {
        companion object {
            const val ID = "id"
            const val USERNAME = "username"
            const val TIMESTAMP = "t"
            const val Extension = "ext"
            const val SOUND = "SOUND"
            const val Message = "message"
        }

        enum class Ext(val pos: Int) {
            AlarmType(0), AlarmTime(1), SerialNumber(2), Channel(3),
            VideoURL(6), HasSmallVideo(9), AlarmEventID(10), PictureEncryptionType(11),
            PicturePassword(12), AIType(13), AlarmRecordId(15), PictureURL(16),
            DeviceName(17),
        }
    }

    enum class PaymentMethodsCloud(val type: String){
        // tài khoản gốc
        TKG ("TKG"),
        //Viettel Money
        VTT("VTT"),
        // Thẻ nội địa
        NAPAS("NAPAS"),
        // Thẻ quốc tế
        CYBER("CYBER"),
        // Hình thức khác
        OTHER("OTHER"),
    }
}

const val SWITCH_ALL_DELAY = 500L
const val ACTION_WAKE_UP_AI_ON = "ACTION_WAKE_UP_AI_ON"
const val ACTION_WAKE_UP_AI_OFF = "ACTION_WAKE_UP_AI_OFF"
const val REGISTER_NEW = 0
const val REGISTER_BONUS = 1
const val MAX_PHONE_NUMBER_FIRE_ALERT_SERVICE = 3
const val MAX_LENGTH_SET_LOCATION_FIRE_ALERT_SERVICE = 50
const val MAX_LENGTH_SET_NAME_FIRE_ALERT_SERVICE = 30
const val MAX_LENGTH_PHONE_NUMBER = 10
const val MAX_LENGTH_EDIT_SWITCH_BUTTON_NAME = 30
const val MAX_LENGTH_EDIT_DEVICE_NAME = 30