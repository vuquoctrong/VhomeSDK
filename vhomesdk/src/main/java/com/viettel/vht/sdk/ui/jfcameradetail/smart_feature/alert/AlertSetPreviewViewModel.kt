package com.viettel.vht.sdk.ui.jfcameradetail.smart_feature.alert

import android.view.ViewGroup
import com.lib.SDKCONST
import com.lib.sdk.bean.smartanalyze.Points
import com.manager.device.DeviceManager
import com.manager.device.media.monitor.MonitorManager
import com.vht.sdkcore.base.BaseViewModel
import com.xm.ui.widget.drawgeometry.listener.GeometryInterface
import com.xm.ui.widget.drawgeometry.model.GeometryPoints
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AlertSetPreviewViewModel @Inject constructor() : BaseViewModel(), AlertSetPreViewInterface {
    private lateinit var mDrawGeometry: GeometryInterface

    //JF camera
    private val deviceManager: DeviceManager by lazy { DeviceManager.getInstance() }
    private var mediaManager: MonitorManager? = null

    constructor(geometry: GeometryInterface) : this() {
        mDrawGeometry = geometry
    }

    override fun getConvertPoint(width: Int, height: Int): List<Points?> {
        val list = getVertex()
        for (points in list) {
            points.x =
                (points.x * CONVERT_PARAMETER / width).toInt()
                    .toFloat()
            points.y =
                (points.y * CONVERT_PARAMETER / height).toInt()
                    .toFloat()
        }
        return list
    }


    fun setConvertPoint(list: List<Points>, width: Int, height: Int) {
        val points = arrayOfNulls<GeometryPoints>(list.size)
        for (i in list.indices) {
            points[i] = GeometryPoints(
                list[i].x * width / CONVERT_PARAMETER,
                list[i].y * height / CONVERT_PARAMETER
            )
        }
        mDrawGeometry.setGeometryPoints(points)
    }

    private fun getVertex(): List<Points> {
        val list: MutableList<Points> = ArrayList()
        for (points in mDrawGeometry.vertex) {
            list.add(Points(points.x, points.y))
        }
        return list
    }

    //JF camera
    fun loginDev(devId: String, onSuccess: (() -> Unit), onFailed: ((Int) -> Unit)) {
        deviceManager.loginDev(devId, object : DeviceManager.OnDevManagerListener<Any?> {
            override fun onSuccess(devId: String, operationType: Int, result: Any?) {
                onSuccess.invoke()
            }

            override fun onFailed(devId: String, msgId: Int, jsonName: String, errorId: Int) {
                onFailed.invoke(errorId)
            }
        })

    }

    fun initMonitor(devId: String, viewGroup: ViewGroup) {
        mediaManager = deviceManager.createMonitorPlayer(viewGroup, devId)
        mediaManager?.setHardDecode(false)
        mediaManager?.setChnId(0)
    }


    fun startMonitor() {
        mediaManager?.streamType = SDKCONST.StreamType.Main
        mediaManager?.setRealTimeEnable(true)
        mediaManager?.startMonitor() //Start Monitoring
    }

    fun stopMonitor() {
        mediaManager?.stopPlay()
    }

    fun destroyMonitor() {
        mediaManager?.destroyPlay()
    }

    companion object {
        private const val CONVERT_PARAMETER = 8192
    }
}