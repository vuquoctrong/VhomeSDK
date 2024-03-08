package com.viettel.vht.sdk.utils

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.databinding.ViewDataBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

abstract class BaseBottomSheetTimePickerFragment<B : ViewDataBinding> :
    BaseBottomSheetFragmentNotModel<B>() {
    private var isTouch = false
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener {
            val bottomSheet =
                (it as BottomSheetDialog).findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
            val behavior = BottomSheetBehavior.from(bottomSheet!!)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED

            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_DRAGGING && isTouch) {
                        behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            })
        }

        // Do something with your dialog like setContentView() or whatever
        return dialog
    }

    /**
     * List time picker
     */
    protected abstract fun listTimePicker(): List<View>

    @SuppressLint("ClickableViewAccessibility")
    override fun initControl() {
        // Disable drag bottom sheet when touch time picker
        listTimePicker().forEach {
            it.setOnTouchListener { _, event ->
                when (event?.action) {
                    MotionEvent.ACTION_UP -> isTouch = false
                    MotionEvent.ACTION_CANCEL -> isTouch = false
                    MotionEvent.ACTION_DOWN -> isTouch = true
                }
                false
            }
        }
    }
}
