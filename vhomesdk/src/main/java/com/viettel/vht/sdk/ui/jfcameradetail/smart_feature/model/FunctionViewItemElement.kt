package com.viettel.vht.sdk.ui.jfcameradetail.smart_feature.model

/**
 * Created by zhangyongyong on 2017-05-08-14:41.
 */
class FunctionViewItemElement(
    var normalResourceId: Int,
    var selectedResourceId: Int,
    var name: String,
    var itemType: Int
) {
    var isSelected = false
    var label: String? = null

}