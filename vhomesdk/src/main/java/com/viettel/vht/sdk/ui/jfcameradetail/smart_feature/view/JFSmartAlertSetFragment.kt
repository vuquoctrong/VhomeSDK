package com.viettel.vht.sdk.ui.jfcameradetail.smart_feature.view

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import com.google.gson.Gson
import com.lib.sdk.bean.ChannelHumanRuleLimitBean
import com.lib.sdk.bean.HumanDetectionBean
import com.lib.sdk.bean.HumanDetectionBean.PedRule
import com.lib.sdk.bean.smartanalyze.Points
import com.manager.db.Define.ALERT_AREA_TYPE
import com.manager.db.Define.ALERT_lINE_TYPE
import com.vht.sdkcore.base.BaseFragment
import com.vht.sdkcore.utils.Define

import com.viettel.vht.sdk.ui.jfcameradetail.smart_feature.alert.AlertSetFunctionFragment
import com.viettel.vht.sdk.ui.jfcameradetail.smart_feature.alert.AlertSetPreviewFragment
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentJfSmartAlertSetBinding
import com.viettel.vht.sdk.navigation.AppNavigation
import com.viettel.vht.sdk.ui.jfcameradetail.view.SettingCameraJFViewModel
import com.xm.ui.widget.drawgeometry.model.DirectionPath
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@Suppress("DEPRECATION")
@AndroidEntryPoint
class JFSmartAlertSetFragment :
    BaseFragment<FragmentJfSmartAlertSetBinding, SettingCameraJFViewModel>() {

    @Inject
    lateinit var appNavigation: AppNavigation

    private val viewModel: SettingCameraJFViewModel by activityViewModels()

    override fun getVM() = viewModel

    override val layoutId: Int
        get() = R.layout.fragment_jf_smart_alert_set


    companion object {
        private val TAG = JFSmartAlertSetFragment::class.java.simpleName

        const val RESULT_ALERT_SET = "RESULT_ALERT_SET"

        fun newInstance(title: String, devId: String) =
            JFSmartAlertSetFragment().apply {
                arguments = Bundle().apply {
                    putString(Define.BUNDLE_KEY.PARAM_ID, devId)
                    putString(Define.BUNDLE_KEY.PARAM_TITLE, title)
                }
            }
    }

    private var mRuleType = 0
    private lateinit var devId: String

    private val mPreviewFragment by lazy { AlertSetPreviewFragment.newInstance(devId) }
    private val mFunctionFragment by lazy { AlertSetFunctionFragment.newInstance() }
    private var mHumanDetection: HumanDetectionBean? = null
    private var mPedRule: ArrayList<PedRule>? = null
    private var channelHumanRuleLimitBean: ChannelHumanRuleLimitBean? = null
    private var direct = HumanDetectionBean.IA_DIRECT_FORWARD //保存最初的方向

    private var size = 0 //保存最初的多边形的边


    override fun initView() {
        super.initView()
        arguments?.let {
            Timber.tag(TAG).d("arguments: ${Gson().toJson(arguments)}")

            if (it.containsKey(Define.BUNDLE_KEY.PARAM_ID)) {
                devId = it.getString(Define.BUNDLE_KEY.PARAM_ID, "")
            }
            if (it.containsKey("RuleType")) {
                mRuleType = it.getInt("RuleType", ALERT_AREA_TYPE)
            }
            if (it.containsKey("HumanDetection")) {
                mHumanDetection = it.getSerializable("HumanDetection") as? HumanDetectionBean
            }
            if (it.containsKey("ChannelHumanRuleLimit")) {
                channelHumanRuleLimitBean =
                    it.getSerializable("ChannelHumanRuleLimit") as? ChannelHumanRuleLimitBean
            }
            mPedRule = mHumanDetection?.pedRules
        }
        when (mRuleType) {
            ALERT_AREA_TYPE -> binding.toolbar.setStringTitle(getString(com.vht.sdkcore.R.string.string_security_zone))
            ALERT_lINE_TYPE -> binding.toolbar.setStringTitle(getString(com.vht.sdkcore.R.string.string_security_fence))
            else -> {}
        }

        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_alert_set_preview, mPreviewFragment).commit()
        val transaction1 = childFragmentManager.beginTransaction()
        transaction1.replace(R.id.fragment_alert_set_function, mFunctionFragment).commit()

    }

    override fun setOnClick() {
        super.setOnClick()
        binding.toolbar.setOnLeftClickListener {
            appNavigation.navigateUp()
        }
        binding.toolbar.setOnRightClickListener {
            saveConfig()
        }
    }

    fun getRuleType(): Int {
        return mRuleType
    }

    fun setShapeType(type: Int) {
        mPreviewFragment.setDrawGeometryType(type)
    }

    private fun saveConfig() {
        Timber.tag(TAG).d("saveConfig")
        dealWithData()
        mHumanDetection?.let { setFragmentResult(RESULT_ALERT_SET, it) }
        appNavigation.navigateUp()
    }

    private fun dealWithData() {
        val points = mPreviewFragment.getConvertPoint()
        when (mRuleType) {
            ALERT_lINE_TYPE -> {
                if (points.size < 2) {
                    return
                }
                mPedRule?.get(0)?.ruleLine?.pts?.apply {
                    points[0]?.let {
                        startX = it.x.toInt()
                        startY = it.y.toInt()
                    }
                    points[1]?.let {
                        stopX = it.x.toInt()
                        stopY = it.y.toInt()
                    }
                }
            }

            ALERT_AREA_TYPE -> {
                mPedRule?.get(0)?.ruleRegion?.apply {
                    ptsNum = points.size
                    setPtsByPoints(points)
                }
            }

            else -> {}
        }
    }

    fun retreatStep() {
        mPreviewFragment.retreatStep()
    }

    fun revert() {
        mPreviewFragment.revert()
        mFunctionFragment.initAlertLineType(direct)
        if (mRuleType == ALERT_lINE_TYPE) {
            mFunctionFragment.initAlertLineType(direct)
        } else if (mRuleType == ALERT_AREA_TYPE) {
            mFunctionFragment.initAlertAreaEdgeCount(size)
        }
    }

    fun setAlertLineDirection(position: Int) {
        val ruleLine = mPedRule?.get(0)?.ruleLine ?: return
        when (position) {
            HumanDetectionBean.IA_DIRECT_FORWARD -> {
                mPreviewFragment.setAlertDirection(DirectionPath.DIRECTION_FORWARD)
                ruleLine.alarmDirect = HumanDetectionBean.IA_DIRECT_FORWARD
            }

            HumanDetectionBean.IA_DIRECT_BACKWARD -> {
                mPreviewFragment.setAlertDirection(DirectionPath.DIRECTION_BACKWARD)
                ruleLine.alarmDirect = HumanDetectionBean.IA_DIRECT_BACKWARD
            }

            HumanDetectionBean.IA_BIDIRECTION -> {
                mPreviewFragment.setAlertDirection(DirectionPath.TWO_WAY)
                ruleLine.alarmDirect = HumanDetectionBean.IA_BIDIRECTION
            }

            else -> mPreviewFragment.setAlertDirection(DirectionPath.NO_DIRECTION)
        }
    }

    fun initAlertView() {
        val pedRule = mPedRule ?: return

        val points: MutableList<Points> = ArrayList()
        when (mRuleType) {
            ALERT_lINE_TYPE -> {
                val linePts = pedRule[0].ruleLine.pts
                if (linePts != null) {
                    points.clear()
                    points.add(Points(linePts.startX.toFloat(), linePts.startY.toFloat()))
                    points.add(Points(linePts.stopX.toFloat(), linePts.stopY.toFloat()))
                    size = 2
                }
                direct = pedRule[0].ruleLine.alarmDirect
                println(
                    "direct:" + direct + "startX:" + linePts?.startX + "startY:" + linePts.startY
                            + "stopX:" + linePts.stopX + "stopY:" + linePts.stopY
                )
                val lineDirect = channelHumanRuleLimitBean?.dwLineDirect
                mFunctionFragment.setDirectionMask(lineDirect)
                mFunctionFragment.initAlertLineType(direct)
                when (direct) {
                    HumanDetectionBean.IA_DIRECT_FORWARD -> mPreviewFragment.initAlertDirection(
                        DirectionPath.DIRECTION_FORWARD
                    )

                    HumanDetectionBean.IA_DIRECT_BACKWARD -> mPreviewFragment.initAlertDirection(
                        DirectionPath.DIRECTION_BACKWARD
                    )

                    HumanDetectionBean.IA_BIDIRECTION -> mPreviewFragment.initAlertDirection(
                        DirectionPath.TWO_WAY
                    )

                    else -> mPreviewFragment.initAlertDirection(DirectionPath.NO_DIRECTION)
                }
            }

            ALERT_AREA_TYPE -> {
                val areaLine = channelHumanRuleLimitBean?.dwAreaLine
                mFunctionFragment.setAreaMask(areaLine)
                val areaDirect = channelHumanRuleLimitBean?.dwAreaDirect
                mFunctionFragment.setDirectionMask(areaDirect)
                val ruleRegion = pedRule[0].ruleRegion
                size = ruleRegion.ptsNum
                points.clear()
                points.addAll(ruleRegion.pointsList)
                direct = ruleRegion.alarmDirect
                mFunctionFragment.initAlertAreaEdgeCount(size)
                when (direct) {
                    HumanDetectionBean.IA_DIRECT_FORWARD -> {
                        mPreviewFragment.initAlertDirection(DirectionPath.DIRECTION_FORWARD)
                    }

                    HumanDetectionBean.IA_DIRECT_BACKWARD -> {
                        mPreviewFragment.initAlertDirection(DirectionPath.DIRECTION_BACKWARD)
                    }

                    HumanDetectionBean.IA_BIDIRECTION -> {
                        mPreviewFragment.initAlertDirection(DirectionPath.TWO_WAY)
                    }

                    else -> {}
                }
            }

            else -> {}
        }
        mPreviewFragment.setConvertPoint(points.toList(), size)
    }

    fun changeRevokeState(state: Boolean) {
        mFunctionFragment.changeRevokeState(state)
    }

}