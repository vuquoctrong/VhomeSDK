package com.viettel.vht.sdk.utils

object Config {
    //Secure Stream Params
    val STREAM_KEEP_ALIVE = 10000L //ms
    const val STREAM_SERVER_KEEP_ALIVE = 10_000L //ms
    val REQUEST_TIMEOUT = 10*1000L //ms
    object EventKey {
        const val EVENT_NOTIFY_STOP_STREAM="EVENT_NOTIFY_STOP_STREAM"
        const val EVENT_UPDATE_FW_CAMERA="EVENT_UPDATE_FW_CAMERA"
        const val EVENT_UPDATE_VOLUME_CAMERA="EVENT_UPDATE_VOLUME_CAMERA"
        const val EVENT_UPDATE_MIC_VOLUME_CAMERA="EVENT_UPDATE_MIC_VOLUME_CAMERA"
        const val EVENT_SETTING_CAMERA_RECORD_STATE_ALL_DAY="EVENT_SETTING_CAMERA_RECORD_STATE_ALL_DAY"
        const val EVENT_SETTING_CAMERA_RECORD_STATE_MOTION="EVENT_SETTING_CAMERA_RECORD_STATE_MOTION"
        const val EVENT_SETTING_CAMERA_FRAME_RATE_5FPS="EVENT_SETTING_CAMERA_FRAME_RATE_5FPS"
        const val EVENT_SETTING_CAMERA_FRAME_RATE_10FPS="EVENT_SETTING_CAMERA_FRAME_RATE_10FPS"
        const val EVENT_SETTING_CAMERA_FRAME_RATE_15FPS="EVENT_SETTING_CAMERA_FRAME_RATE_15FPS"
        const val EVENT_SETTING_CAMERA_FRAME_RATE_20FPS="EVENT_SETTING_CAMERA_FRAME_RATE_20FPS"
        const val EVENT_SETTING_CAMERA_QUALITY_SD="EVENT_SETTING_CAMERA_QUALITY_SD"
        const val EVENT_SETTING_CAMERA_QUALITY_HD="EVENT_SETTING_CAMERA_QUALITY_HD"
        const val EVENT_SETTING_CAMERA_QUALITY_FHD="EVENT_SETTING_CAMERA_QUALITY_FHD"
        const val EVENT_SETTING_CAMERA_VIDEO_ENCODING_H264="EVENT_SETTING_CAMERA_VIDEO_ENCODING_H264"
        const val EVENT_SETTING_CAMERA_VIDEO_ENCODING_H265="EVENT_SETTING_CAMERA_VIDEO_ENCODING_H265"
        const val EVENT_KEY_DELETE_EVENT_SUCCESS="EVENT_KEY_DELETE_EVENT_SUCCESS"
        const val EVENT_SETTING_CAMERA_ENCODING_TIME_VIDEO_30="EVENT_SETTING_CAMERA_ENCODING_TIME_VIDEO_30"
        const val EVENT_SETTING_CAMERA_ENCODING_TIME_VIDEO_60="EVENT_SETTING_CAMERA_ENCODING_TIME_VIDEO_60"
        const val EVENT_SETTING_CAMERA_ENCODING_TIME_VIDEO_90="EVENT_SETTING_CAMERA_ENCODING_TIME_VIDEO_90"
        const val EVENT_SETTING_CAMERA_SETTING_IS_THUMBNAIL_ENABLE="EVENT_SETTING_CAMERA_SETTING_IS_THUMBNAIL_ENABLE"
        const val EVENT_SETTING_CAMERA_SETTING_IS_THUMBNAIL_DISABLE="EVENT_SETTING_CAMERA_SETTING_IS_THUMBNAIL_DISABLE"
        const val EVENT_SETTING_CAMERA_SETTING_ALARM_SOUND_SILENT="EVENT_SETTING_CAMERA_SETTING_ALARM_SOUND_SILENT"
        const val EVENT_SETTING_CAMERA_SETTING_ALARM_SOUND_GENTLE="EVENT_SETTING_CAMERA_SETTING_ALARM_SOUND_GENTLE"
        const val EVENT_SETTING_CAMERA_SETTING_ALARM_SOUND_REPEATED="EVENT_SETTING_CAMERA_SETTING_ALARM_SOUND_REPEATED"
        const val EVENT_SETTING_CAMERA_SETTING_EDIT_NAME="EVENT_SETTING_CAMERA_SETTING_EDIT_NAME"
        const val EVENT_SETTING_CAMERA_SETTING_EDIT_NAME_SUCCESS="EVENT_SETTING_CAMERA_SETTING_EDIT_NAME_SUCCESS"
        const val EVENT_SETTING_CAMERA_OPEN_INFORMATION_CAMERA="EVENT_SETTING_CAMERA_OPEN_INFORMATION_CAMERA"
        const val EVENT_SETTING_CAMERA_REBOOT_DEVICE="EVENT_SETTING_CAMERA_REBOOT_DEVICE"
        const val EVENT_SETTING_CAMERA_RESET_DEFAULT="EVENT_SETTING_CAMERA_RESET_DEFAULT"
        const val EVENT_SETTING_CAMERA_FACTORY_RESET="EVENT_SETTING_CAMERA_FACTORY_RESET"
        const val EVENT_AUTHORIZATION_PROTOCOL_EXPIRED="EVENT_AUTHORIZATION_PROTOCOL_EXPIRED"
        const val EVENT_SETTING_CAMERA_WARNING_HUMAN_HC32="EVENT_SETTING_CAMERA_WARNING_HUMAN_HC32"
        const val EVENT_SETTING_CAMERA_WARNING_DETECTION_HC32="EVENT_SETTING_CAMERA_WARNING_DETECTION_HC32"
        const val EVENT_RELOAD_LIST_EVENT="EVENT_RELOAD_LIST_EVENT"
        const val EVENT_SETTING_CAMERA_SECURITY_FENCE="EVENT_SETTING_CAMERA_SECURITY_FENCE"
        const val EVENT_SETTING_CAMERA_SAFE_AREA="EVENT_SETTING_CAMERA_SAFE_AREA"
        const val EVENT_SETTING_CAMERA_SECURITY_FENCE_DEFAULT="EVENT_SETTING_CAMERA_SECURITY_FENCE_DEFAULT"
        const val EVENT_CHOOSE_HOME_ORG_ID_SHARING_DEVICE_ACCEPTED="EVENT_CHOOSE_HOME_ORG_ID_SHARING_DEVICE_ACCEPTED"
        const val EVENT_CHOOSE_ROOM_ORG_ID_SHARING_DEVICE_ACCEPTED="EVENT_CHOOSE_ROOM_ORG_ID_SHARING_DEVICE_ACCEPTED"

        const val EVENT_UPDATE_PRESET="EVENT_UPDATE_PRESET"
        const val EVENT_EDIT_PRESET="EVENT_EDIT_PRESET"
        const val EVENT_DELETE_PRESET="EVENT_DELETE_PRESET"
        const val EVENT_MULTIPLE_PRESET="EVENT_MULTIPLE_PRESET"




    }

    const val SDK_DATA_FUNCTION_VHOME="SDK_DATA_FUNCTION_VHOME"
    const val SDK_FUNCTION_OPEN_ADD_CAMERA_JF="SDK_FUNCTION_OPEN_ADD_CAMERA_JF"
    const val SDK_FUNCTION_OPEN_DETAIL_CAMERA_JF="SDK_FUNCTION_OPEN_DETAIL_CAMERA_JF"

    const val SIZE_QR_CODE_VTT_EZVIZ = 4
    const val SCAN_QR_CAMERA_EZVIZ_VIETTEL = "Viettel"
    const val SCAN_QR_CAMERA_VTT = "VIETTEL"


    const val FEATURE_LIVEVIEW = "LIVEVIEW"
    const val FEATURE_PLAYBACK = "PLAYBACK"
    const val FEATURE_EVENT = "EVENT"
    const val FEATURE_AUDIO = "VOICETALK"
    const val FEATURE_PTZ = "PTZCONTROL"

    const val ACCEPTED = "ACCEPTED"
    const val SHARING = "SHARING"
    const val REJECTED = "REJECTED"

    const val COUNT_RETRY_THUMBNAIL_JF = 1

    const val FEATURE_LIVEVIEW_JF = ""
    const val FEATURE_PLAYBACK_JF = "DP_LocalStorage,DP_ViewCloudVideo"
    const val FEATURE_EVENT_JF = "DP_AlarmPush"
    const val FEATURE_AUDIO_JF = "DP_Intercom"
    const val FEATURE_PTZ_JF = "DP_PTZ"

    const val POWERS_DEV_INFO_KEY = "devInfo"

    var sdkAPP_KEY = "nlaDOC8uS6Xn7L0JIcPD"
    var sdkAPP_SECRET = "yKeMoImiHp9DUXxoGpERza31xSyCWunW"
    var sdkBASE_URL = "https://stg-iotapi.viettel.io"
    var sdkBASE_URL_CAMERA_JF = "https://rs.viettelcamera.vn"
    var sdkAppName = "Vhome"


}