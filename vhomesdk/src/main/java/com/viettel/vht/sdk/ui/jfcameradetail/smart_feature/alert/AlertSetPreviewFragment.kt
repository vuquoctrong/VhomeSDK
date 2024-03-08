package com.viettel.vht.sdk.ui.jfcameradetail.smart_feature.alert

import android.os.Bundle
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.RelativeLayout
import com.lib.sdk.bean.smartanalyze.Points
import com.vht.sdkcore.base.BaseFragment
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.toastMessage
import com.viettel.vht.sdk.ui.jfcameradetail.smart_feature.view.JFSmartAlertSetFragment
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentAlertSetPreviewBinding
import com.viettel.vht.sdk.utils.DebugConfig
import com.xm.ui.widget.drawgeometry.listener.RevokeStateListener

class AlertSetPreviewFragment :
    BaseFragment<FragmentAlertSetPreviewBinding, AlertSetPreviewViewModel>(),
    RevokeStateListener {

    override val layoutId: Int
        get() = R.layout.fragment_alert_set_preview

    private var viewModel: AlertSetPreviewViewModel = AlertSetPreviewViewModel()

    override fun getVM() = viewModel

    private var devId: String = ""
    private var mDirection = 0

    companion object {
        private val TAG: String = AlertSetPreviewFragment::class.java.simpleName
        fun newInstance(devId: String) = AlertSetPreviewFragment().apply {
            arguments = Bundle().apply {
                putString(Define.BUNDLE_KEY.PARAM_ID, devId)
            }
        }
    }

    override fun initView() {
        super.initView()
        arguments?.let {
            if (it.containsKey(Define.BUNDLE_KEY.PARAM_ID)) {
                devId = it.getString(Define.BUNDLE_KEY.PARAM_ID, "")
            }
        }
        viewModel = AlertSetPreviewViewModel(binding.shapeView)
        initVideoSize()
        viewModel.loginDev(devId,
            onSuccess = {
                viewModel.initMonitor(devId, binding.videoView)
                viewModel.startMonitor()
                val observerScr: ViewTreeObserver = binding.shapeView.viewTreeObserver
                observerScr.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        (parentFragment as? JFSmartAlertSetFragment)?.initAlertView()
                        observerScr.removeOnGlobalLayoutListener(this)
                    }
                })
            },
            onFailed = { errorId ->
                DebugConfig.log(TAG, "requestStream error = $errorId _ $devId")
                toastMessage("Lỗi kết nối camera $errorId")
            })
        binding.shapeView.setOnRevokeStateListener(this)
    }

    private fun initVideoSize() {
        val width = getMinimalScreenWidth()
        val height = getMinimalScreenHeight()

        val layoutParams: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(width, height)
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)

        binding.videoView.layoutParams = layoutParams
        binding.shapeView.layoutParams = layoutParams
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopMonitor()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.destroyMonitor()
    }

    fun setDrawGeometryType(type: Int) {
        binding.shapeView.setGeometryType(type)
    }

    fun revert() {
        binding.shapeView.revertToDefaultPoints()
    }

    fun retreatStep() {
        try {
            binding.shapeView.retreatToPreviousOperationPoints()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun getConvertPoint(): List<Points?> {
        return viewModel.getConvertPoint(binding.shapeView.width, binding.shapeView.height)
    }

    fun initAlertDirection(direction: Int) {
        mDirection = direction
        binding.shapeView.initDirection(direction)
    }

    fun setAlertDirection(direction: Int) {
        mDirection = direction
        binding.shapeView.setDirection(direction)
    }

    fun setConvertPoint(list: List<Points>, size: Int) {
        if (size > 0) {
            val subList = list.subList(0, size)
            viewModel.setConvertPoint(subList, binding.shapeView.width, binding.shapeView.height)
        }
    }

    override fun onRevokeEnable(state: Boolean) {
        (parentFragment as? JFSmartAlertSetFragment)?.changeRevokeState(state)
    }


}