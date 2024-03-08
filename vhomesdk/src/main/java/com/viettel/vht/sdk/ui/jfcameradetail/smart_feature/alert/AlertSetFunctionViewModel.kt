package com.viettel.vht.sdk.ui.jfcameradetail.smart_feature.alert

import com.basic.G
import com.lib.sdk.bean.HumanDetectionBean.*
import com.manager.db.Define.ALERT_AREA_TYPE
import com.manager.db.Define.ALERT_lINE_TYPE
import com.vht.sdkcore.base.BaseViewModel
import com.viettel.vht.sdk.ui.jfcameradetail.smart_feature.model.FunctionViewItemElement
import com.viettel.vht.sdk.R
import com.xm.ui.widget.drawgeometry.model.GeometryInfo.GEOMETRY_RECTANGLE
import com.xm.ui.widget.drawgeometry.model.GeometryInfo.GEOMETRY_TRIANGLE
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.xm.ui.widget.drawgeometry.model.GeometryInfo.*

@HiltViewModel
class AlertSetFunctionViewModel @Inject constructor(
) : BaseViewModel() {

    private var mSetFunctionInterface: AlertSetFunctionInterface? = null
    private val directEnableMap: HashMap<Int, Boolean> = HashMap()
    private val areaEnableMap: HashMap<Int, Boolean> = HashMap()
    private val itemList: ArrayList<FunctionViewItemElement> = ArrayList()
    private var curSelectItemPos = 0

    constructor(functionInterface: AlertSetFunctionInterface) : this() {
        this.mSetFunctionInterface = functionInterface
        directEnableMap[IA_DIRECT_FORWARD] = false
        directEnableMap[IA_DIRECT_BACKWARD] = false
        directEnableMap[IA_BIDIRECTION] = false

        areaEnableMap[GEOMETRY_TRIANGLE] = false
        areaEnableMap[GEOMETRY_RECTANGLE] = false
        areaEnableMap[GEOMETRY_PENTAGON] = false
        areaEnableMap[GEOMETRY_L] = false
        areaEnableMap[GEOMETRY_AO] = false

    }

    fun showShapeOnCanvas(position: Int, ruleType: Int) {
        if (itemList.size <= position) {
            return
        }
        curSelectItemPos = position
        val itemType: Int = itemList[position].itemType
        when (ruleType) {
            ALERT_lINE_TYPE -> {
                mSetFunctionInterface?.setShapeType(GEOMETRY_LINE)
                mSetFunctionInterface?.setAlertLineType(itemType)
            }
            ALERT_AREA_TYPE -> mSetFunctionInterface?.setShapeType(itemType)
        }
    }

    fun getCurSelectItemPos(): Int {
        return curSelectItemPos
    }

    fun getItemType(position: Int): Int {
        return if (itemList.size <= position) {
            0
        } else itemList[position].itemType
    }

    fun getCurSelItemType(): Int {
        return if ( itemList.size <= curSelectItemPos) {
            0
        } else itemList[curSelectItemPos].itemType
    }

    fun initFunctionViewData(type: Int): List<FunctionViewItemElement> {
        itemList.clear()
        when (type) {
            ALERT_lINE_TYPE -> {
                if (directEnableMap[IA_DIRECT_FORWARD] == true) {
                    itemList.add(
                        FunctionViewItemElement(
                            R.drawable.smart_analyze__line_right_nor,
                            R.drawable.smart_analyze__line_right_sel,
                            "Hướng về phía trước", IA_DIRECT_FORWARD
                        )
                    )
                }
                if (directEnableMap[IA_DIRECT_BACKWARD] == true) {
                    itemList.add(
                        FunctionViewItemElement(
                            R.drawable.smart_analyze__line_left_nor,
                            R.drawable.smart_analyze__line_left_sel,
                            "Hướng ngược lại", IA_DIRECT_BACKWARD
                        )
                    )
                }
                if (directEnableMap[IA_BIDIRECTION] == true) {
                    itemList.add(
                        FunctionViewItemElement(
                            R.drawable.smart_analyze__line_middle_nor,
                            R.drawable.smart_analyze__line_middle_sel,
                            "Hướng hai chiều", IA_BIDIRECTION
                        )
                    )
                }
            }
            ALERT_AREA_TYPE -> {
                if (areaEnableMap[GEOMETRY_TRIANGLE] == true) {
                    itemList.add(
                        FunctionViewItemElement(
                            R.drawable.smart_analyze_shape_triangle_nor,
                            R.drawable.smart_analyze_shape_triangle_sel,
                            "Tam giác", GEOMETRY_TRIANGLE
                        )
                    )
                }
                if (areaEnableMap[GEOMETRY_RECTANGLE] == true) {
                    itemList.add(
                        FunctionViewItemElement(
                            R.drawable.smart_analyze_shape_rectangle_nor,
                            R.drawable.smart_analyze_shape_rectangle_sel,
                            "Hình chữ nhật", GEOMETRY_RECTANGLE
                        )
                    )
                }
                if (areaEnableMap[GEOMETRY_PENTAGON] == true) {
                    itemList.add(
                        FunctionViewItemElement(
                            R.drawable.smart_analyze_shape_pentagram_nor,
                            R.drawable.smart_analyze_shape_pentagram_sel,
                            "Hình thoi", GEOMETRY_PENTAGON
                        )
                    )
                }
                if (areaEnableMap[GEOMETRY_L] == true) {
                    itemList.add(
                        FunctionViewItemElement(
                            R.drawable.smart_analyze_shape_l_nor,
                            R.drawable.smart_analyze_shape_l_sel,
                            "Hình chữ L", GEOMETRY_L
                        )
                    )
                }
                if (areaEnableMap[GEOMETRY_AO] == true) {
                    itemList.add(
                        FunctionViewItemElement(
                            R.drawable.smart_analyze_shape_concave_nor,
                            R.drawable.smart_analyze_shape_concave_sel,
                            "Lõm", GEOMETRY_AO
                        )
                    )
                }
            }
        }
        return itemList
    }

    fun onDestroy() {
        mSetFunctionInterface = null
    }

    fun setDirectionMask(directionMask: String?) {
        var mask = G.getLongFromHex(directionMask)
        var index = 0
        while (mask > 0) {
            if (mask and 0x01 == 1L) {
                directEnableMap[index] = true
            }
            index++
            mask = mask shr 1
        }
    }

    fun setAreaMask(areaMask: String?) {
        var mask = G.getLongFromHex(areaMask)
        var index = 0
        while (mask > 0) {
            if (mask and 0x01 == 1L) {
                areaEnableMap[index + 1] = true
            }
            index++
            mask = mask shr 1
        }
    }

    fun isDirectionDlgShow(): Boolean {
        return (directEnableMap[IA_DIRECT_FORWARD] ?: false
                && directEnableMap[IA_DIRECT_BACKWARD] ?: false
                && directEnableMap[IA_BIDIRECTION] ?: false)
    }


}