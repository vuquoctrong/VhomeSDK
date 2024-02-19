package com.vht.sdkcore.utils

object AppCoreEnum {

    enum class SDCardStatus(val value: String, val data: String, val title: String) {
        NORMAL("0", "Bình Thường", "Đang hoạt động"),
        WRONG_STORAGE_MEDIUM("1", "Phương tiện lưu trữ sai", "Không xác định"),
        UN_FORMATTED("2", "Không định dạng", "Không xác định"),
        FORMATTING("3", "Đang xóa", "Đang xóa"),
        NO_SDCARD("4", "Chưa có thẻ nhớ", "Chưa có thẻ nhớ"),
    }

    enum class SDCardConfigFormat(
        val diskIndex: String,
        val clientType: String,
        val netType: String,
        val featureCode: String,
        val osVersion: String,
        val sdkVersion: String,
    ) {
        FormatSDCardData(
            "1",
            "0",
            "UNKNOWN",
            "f24940e782454a0ca7cbf7c2a292a6c7",
            "4.4.2",
            "v.5.1.5.30"
        )
    }

    enum class CameraType(val type: Int, val cameraModel: String, cameraType: String) {
        HC2Camera(0, "HC2", "EzvizCamera"),
        HC3Camera(1, "HC3", "EzvizCamera"),
        HC22Camera(2, "HC22", "VHTCamera"),
        HC32Camera(3, "HC32", "VHTCamera"),
        HC23Camera(5, "HC23", "JFCamera"),
        HC33Camera(5, "HC33", "JFCamera"),
    }

}