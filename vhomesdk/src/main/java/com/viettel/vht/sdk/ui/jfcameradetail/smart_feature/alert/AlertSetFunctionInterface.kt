package com.viettel.vht.sdk.ui.jfcameradetail.smart_feature.alert

interface AlertSetFunctionInterface {
    fun setShapeType(type: Int)
    fun initAlertLineType(lineType: Int)
    fun setAlertLineType(lineType: Int)
    fun initAlertAreaEdgeCount(edgeCount: Int)

    //设置方向掩码
    fun setDirectionMask(directionMask: String?)

    //设置支持警戒区域种类掩码
    fun setAreaMask(areaMask: String?)
}