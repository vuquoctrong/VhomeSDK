package com.vht.sdkcore.utils

object Constants {
    const val MAX_2_BYTES = 65535
    const val CAMERA_APP_KEY = "fc3868f138cc49b3b919b31469dec4c5"
    const val CAMERA_APP_SECRET = "Zsr7Gj28cKOjJTK3Uhv9ESUm40S5KtRJ"
    const val PREF_FILE_NAME = "Picking.Preferences"
    const val DEFAULT_TIMEOUT = 10
    const val DEFAULT_CALL_TIMEOUT = 15
    const val DEFAULT_TIMEOUT_VERSION_UPDATE = 1
    const val DURATION_TIME_CLICKABLE = 500
    const val READ_TIMEOUT = 60
    const val TCP_ERROR = "10000"

    //splash
    const val DURATION_TIME_SPLASH = 300
    const val DURATION_ANIM_CHANGE_SCREEN = 300L
    const val TYPE_SETTING_TITLE = 1
    const val TYPE_SETTING_CONTENT = 0

    const val TYPE_VIDEO_247 = 3
    const val SPACE = " "
    const val EMPTY = ""
    const val VERTICAL_BAR = "|"
    const val ERROR_NUMBER = -1
    const val TYPE_VIDEO_EVENT = 2

    const val SCREEN_ID = "SCREEN_ID"
    const val ACTION_ID = "ACTION_ID"
    const val SERIAL_CAMERA = "SERIAL_CAMERA"
    const val TRACE_PARENT = "traceparent"

    object NetworkRequestCode {
        const val REQUEST_CODE_200 = 200    //normal
        const val REQUEST_CODE_400 = 400    //parameter error
        const val REQUEST_CODE_401 = 401    //unauthorized error
        const val REQUEST_CODE_402 = 402    //Access token expired.
        const val REQUEST_CODE_403 = 403    //url is wrong
        const val REQUEST_CODE_404 = 404    //No data error
        const val REQUEST_CODE_405 = 405    //resource not found
        const val REQUEST_CODE_406 = 406    //Access denied
        const val REQUEST_CODE_407 = 407    //appid has no permission
        const val REQUEST_CODE_500 = 500    //system error
    }

    object SocketInfo {
        // Action
        const val ACTION_UPDATE = "update"
        const val ACTION_SYS_MSG = "sysmsg"
        const val ACTION_QUERY = "query"
        const val ACTION_USER_ONLINE = "userOnline"
        const val ACTION_SUB_DEVICE = "subDevice"
        const val ACTION_READ_ATTRIBUTE_ACK = "read-attribute-ack"
    }


    //bundle key
    const val CAMERA_INFO = "CAMERA_INFO"
    const val CAMERA_SERIAL = "CAMERA_SERIAL"
    const val CAMERA_VERIFY_CODE = "CAMERA_VERIFY_CODE"
    const val ADD_CAMERA = "ADD_CAMERA"
    const val CAMERA_MODEL = "CAMERA_MODEL"

    //Gallery
    const val PHOTO_FOLDER = "Vhome"
    const val VIDEO_FOLDER = "Vhome"
    const val VIDEO_FOLDER_AVI = "Vhome"
    const val CACHE_THUMBNAIL = "Cache_Thumbnail"

    const val APP_LOG_NAME = "AppLog"
    const val LOG_TCP_HEADER = "APP_TCP"
    const val LOG_DEFAULT = ""
    const val LOG_HTTP_HEADER = "APP_HTTP"
    const val LOG_APP_REP = "APP_REP"
    const val LOG_APP_AGENT = "ANDROID"
    const val LOG_ACTION_START = "START"
    const val LOG_ACTION_END = "END"
    const val FILE_LOCAL_FDF_USER_MANUAL = "user_manual.pdf"
    const val LOCAL_FDF_USER_MANUAL = "manual"

    var inInstruct = false

    object EventKey {
        const val EVENT_SYSTEM_CHANGE_INFORMATION_CAMERA_JF = "EVENT_SYSTEM_CHANGE_INFORMATION_CAMERA_JF"
    }
}
