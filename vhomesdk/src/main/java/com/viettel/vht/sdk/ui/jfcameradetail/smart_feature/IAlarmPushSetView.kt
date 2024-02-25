package com.viettel.vht.sdk.ui.jfcameradetail.smart_feature

interface IAlarmPushSetView {
    fun onGetMotionDetectResult(isSuccess: Boolean)
    fun onSaveConfigResult(isSuccess: Boolean)
    fun onGetConfigFailed()
}