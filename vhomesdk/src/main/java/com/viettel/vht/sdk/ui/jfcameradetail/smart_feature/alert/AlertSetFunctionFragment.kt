package com.viettel.vht.sdk.ui.jfcameradetail.smart_feature.alert

import android.annotation.SuppressLint
import android.view.View
import com.manager.db.Define.*
import com.vht.sdkcore.base.BaseFragment
import com.vht.sdkcore.utils.getColorCompat
import com.viettel.vht.sdk.ui.jfcameradetail.smart_feature.view.JFSmartAlertSetFragment
import com.viettel.vht.sdk.ui.jfcameradetail.smart_feature.model.FunctionViewItemElement
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentAlertSetFunctionBinding
import com.viettel.vht.sdk.ui.jfcameradetail.smart_feature.view.SmartAnalyzeFunctionView
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class AlertSetFunctionFragment :
    BaseFragment<FragmentAlertSetFunctionBinding, AlertSetFunctionViewModel>(),
    SmartAnalyzeFunctionView.OnItemClickListener, AlertSetFunctionInterface, View.OnClickListener {

    companion object {
        fun newInstance() = AlertSetFunctionFragment()
    }

    override val layoutId: Int
        get() = R.layout.fragment_alert_set_function

    private val viewModel: AlertSetFunctionViewModel = AlertSetFunctionViewModel(this)

    override fun getVM() = viewModel

    private var mRuleType = 0
    private var itemPos = -1
    private var edgeCount = 0
    private val shapeStack: Stack<Int> = Stack()
    private var actionCount = 0

    override fun initView() {
        super.initView()
//        binding.boundaryAlertDirection.setOnClickListener(this)
//        binding.smartAnalyzeSave.setOnClickListener(this)
        binding.alertSetFunctionSmartLayout.setOnItemClickListener(this)
        binding.smartAnalyzeRevert.setOnClickListener(this)
        binding.smartAnalyzeRevoke.setOnClickListener(this)

        mRuleType = (parentFragment as? JFSmartAlertSetFragment)?.getRuleType() ?: 0
        initFunctionView()
    }

    @SuppressLint("SetTextI18n")
    private fun initFunctionView() {
        val functionList: List<FunctionViewItemElement> = viewModel.initFunctionViewData(mRuleType)

        if (functionList.isNotEmpty()) {
            binding.alertSetFunctionTipsLayout.visibility = View.VISIBLE
        }
        when (mRuleType) {
            ALERT_lINE_TYPE -> {
//                binding.alertLineTriggerDirection.setVisibility(View.VISIBLE)
                binding.tvTitle.text = "Hướng của hàng rào an ninh"
                binding.alertSetFunctionTips.text =
                    "Bạn sẽ nhận được thông báo khi có người đi qua đường hàng rào an ninh theo hướng mũi tên."
            }

            ALERT_AREA_TYPE -> {
//                binding.alertAreaSetting.setVisibility(View.VISIBLE)
                binding.tvTitle.text = "Hình dạng vùng an ninh"
                binding.alertSetFunctionTips.text =
                    "Bạn sẽ nhận được thông báo khi có người đi vào khu vực vùng an ninh đã thiết lập."
            }

            GOODS_RETENTION_TYPE -> {
//                binding.goodsApplicationScenarios.setVisibility(View.VISIBLE)
                binding.alertSetFunctionTipsLayout.visibility = View.GONE
            }

            STOLEN_GOODS_TYPE -> {
//                binding.goodsApplicationScenarios.setVisibility(View.VISIBLE)
                binding.alertSetFunctionTipsLayout.visibility = View.GONE
            }

            else -> {}
        }
        binding.alertSetFunctionSmartLayout.setData(functionList)
        initData()
    }

    private fun initData() {
//        if (itemPos == -1) {
//            itemPos = 0;
//        }
//        mFunctionView.setItemSelected(itemPos);
//        if (viewModel.isDirectionDlgShow()) {
//            binding.boundaryAlertDirection.setVisibility(View.VISIBLE)
//        }
    }


    override fun setShapeType(type: Int) {
        (parentFragment as? JFSmartAlertSetFragment)?.setShapeType(type)
    }

    override fun initAlertLineType(lineType: Int) {
        itemPos = lineType
        binding.alertSetFunctionSmartLayout.setItemSelected(itemPos)
    }

    override fun setAlertLineType(lineType: Int) {
        try {
            (parentFragment as? JFSmartAlertSetFragment)?.setAlertLineDirection(lineType)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun initAlertAreaEdgeCount(edgeCount: Int) {
        this.edgeCount = edgeCount
        itemPos = if (edgeCount <= 6) {
            edgeCount - 3
        } else if (edgeCount == 8) {
            4
        } else {
            5
        }
        binding.alertSetFunctionSmartLayout.setItemSelected(itemPos)
    }

    override fun setDirectionMask(directionMask: String?) {
        viewModel.setDirectionMask(directionMask)
        initFunctionView()
    }

    override fun setAreaMask(areaMask: String?) {
        viewModel.setAreaMask(areaMask)
        initFunctionView()
    }

    override fun onItemClick(view: View?, position: Int, label: String?) {
        if (!shapeStack.empty()) {
            if (shapeStack.peek() == position) {
                shapeStack.pop()
            }
        }
        viewModel.showShapeOnCanvas(position, mRuleType)
        shapeStack.push(position)
        actionCount = 0
        changeStateButtonRevoke(true)
        changeStateButtonRevert(true)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.smart_analyze_revoke -> {
                if (actionCount > 0) {
                    (parentFragment as? JFSmartAlertSetFragment)?.retreatStep()
                    actionCount--
                } else {
                    itemRetreatStep()
                }
            }

            R.id.smart_analyze_revert -> {
                revert()
            }

            else -> {}
        }
    }

    private fun itemRetreatStep() {
        if (shapeStack.size > 1) {
            shapeStack.pop()
            val position = shapeStack.peek()
            binding.alertSetFunctionSmartLayout.setItemSelected(position)
            viewModel.showShapeOnCanvas(position, mRuleType)
        } else {
            revert()
        }
    }

    fun changeRevokeState(state: Boolean) {
        if (state) {
            changeStateButtonRevoke(true)
            changeStateButtonRevert(true)
            actionCount++
        } else {
            actionCount = 0
        }
    }

    private fun revert() {
        (parentFragment as? JFSmartAlertSetFragment)?.revert()
        shapeStack.clear()
        actionCount = 0
        changeStateButtonRevoke(false)
        changeStateButtonRevert(false)
    }

    private fun changeStateButtonRevoke(state: Boolean) {
        binding.smartAnalyzeRevoke.isClickable = state
        if (state) {
            binding.smartAnalyzeRevoke.setColorFilter(requireContext().getColorCompat(R.color.color_F8214B))
        } else {
            binding.smartAnalyzeRevoke.setColorFilter(requireContext().getColorCompat(R.color.color_4E4E4E))
        }
    }

    private fun changeStateButtonRevert(state: Boolean) {
        binding.smartAnalyzeRevert.isClickable = state
        if (state) {
            binding.smartAnalyzeRevert.setColorFilter(requireContext().getColorCompat(R.color.color_F8214B))
        } else {
            binding.smartAnalyzeRevert.setColorFilter(requireContext().getColorCompat(R.color.color_4E4E4E))
        }
    }

//    /**
//     * 设置提示是否显示
//     * @param state
//     */
//    fun setTipsView(state: Int) {
//        binding.alertSetFunctionTipsLayout.setVisibility(state)
//    }
}