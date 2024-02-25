package com.viettel.vht.sdk.utils

import android.os.Parcelable

import kotlinx.android.parcel.Parcelize

object AppEnum {
    const val CLOUD_TYPE = 2
    enum class TypeNetwork(val type: Int) {
        WIFI(1),
        MOBILE(2)
    }

    enum class ViewErrorCode(val code: Int) {
        ERROR_NONE(0),
        ERROR_BLANK_TEXT_VIEW(1),
        ERROR_CONTAINS_SPACE(2),
        ERROR_EMAIL_FORMAT(3),
        ERROR_SHORT_PASSWORD(4),
        ERROR_NON_MATCHING_PASSWORD(5),
        ERROR_WRONG_PHONE_FORMAT(6),
        ERROR_BLANK_TEXT_ACCEPT(7),
        ERROR_MIN_TEXT_LENGTH(8),
        ERROR_FORMAT_PASSWORD(9),
        ERROR_MAX_TEXT_LENGTH(10),
        ERROR_EXT_LENGTH(11),
        ERROR_OTP_WRONG(12),
        ERROR_NOT_EQUAL_PASSWORD(13),

    }

    enum class CameraModel(val type: String) {
        SKYLIGHT_LBP1S("VIPC-ZEW-SKL-LBP1S"),
        BOX_VHUB("VHUB-Y04-VVT01"),
        VTT_INDOOR_MEARI("HC22"),
        VTT_MEARI_OUTSIDE("HC32"),
        VTT_C3N_VTT("HC3"),
        VTT_HC2("HC2"),
        VTT_HC3("HC3"),
        EZVIZ_CS_C3N("CS-C3N"),
        EZVIZ_CS_C3W("CS-C3W"),
        EZVIZ_CS_C6N("CS-C6N"),
        ONVIF("ONVIF")
    }

    enum class BundleKey(val key: String) {
        IP_CAMERA_QR("IP_CAMERA_QR"),
        DATA("DATA"),
        PHONE_NUMBER("PHONE_NUMBER"),
        SERIAL_CAMERA("SERIAL_CAMERA"),
        IS_FROM_LIVEVIEW("IS_FROM_LIVEVIEW"),
        JF_ID("JF_ID")

    }

    enum class FragmentRequest(val code: String) {
        IP_CAMERA_QR_REQUEST("IP_CAMERA_QR_REQUEST"),
    }

    enum class Error(val code: String, val resInt: Int) {
        SHARING_LIMIT("100048", com.vht.sdkcore.R.string.sharing_limit),
        SHARED_DEVICE_TO_USER("100047",com.vht.sdkcore.R.string.shared_device_to_user),
        NOT_SHARE_DEVICE("100046", com.vht.sdkcore.R.string.not_share_device),
        NOT_FOUND_SHARED_USER("600083", com.vht.sdkcore.R.string.not_found_shared_user),
        USER_NOT_POSSESS_DEVICE("900006", com.vht.sdkcore.R.string.user_not_possess_device),
        REFUSED_CAMERA("600084", com.vht.sdkcore.R.string.refused_camera_sharing),
        ACCEPTED_CAMERA("100051", com.vht.sdkcore.R.string.accepted_camera_sharing),
    }

    enum class PlaybackState(val value: Int) {
        STOP(0),
        PLAY(1),
        PAUSE(2)
    }


    @Parcelize
    data class IPCameraQR(
        val mnftr: String? = "",
        val model: String? = "",
        val pwd: String? = "",
        val usr: String? = "",
        val serial: String? = ""
    ) : Parcelable

    enum class EventType(val nameId: Int, val type: Int, val ezvizType: Int, val imouType: String) {
        ALL(com.vht.sdkcore.R.string.all, 0, -1,""),
        MOTION_DETECTION(com.vht.sdkcore.R.string.motion_detection, 17, 10002,"motion"),
        HI(com.vht.sdkcore.R.string.human_intrustion, 6, 10079,"human")
    }

    enum class EventJFType(val nameId: Int, val type: String) {
        MOTION_DETECTION(com.vht.sdkcore.R.string.motion_detection, "VideoMotion"),
        HI(com.vht.sdkcore.R.string.human_intrustion, "appEventHumanDetectAlarm")
    }

}