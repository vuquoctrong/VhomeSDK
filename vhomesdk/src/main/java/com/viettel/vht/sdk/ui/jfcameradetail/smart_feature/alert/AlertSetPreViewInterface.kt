package com.viettel.vht.sdk.ui.jfcameradetail.smart_feature.alert

import com.lib.sdk.bean.smartanalyze.Points

interface AlertSetPreViewInterface {
    fun getConvertPoint(width: Int, height: Int): List<Points?>
}