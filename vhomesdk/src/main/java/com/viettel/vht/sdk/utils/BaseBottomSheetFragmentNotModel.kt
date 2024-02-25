package com.viettel.vht.sdk.utils

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.viettel.vht.sdk.R

abstract class BaseBottomSheetFragmentNotModel<B : ViewDataBinding> : BottomSheetDialogFragment() {

    protected lateinit var binding: B

    /**
     * Layout ID
     */
    @get:LayoutRes
    protected abstract val layoutId: Int

    /**
     * Setup view
     */
    protected abstract fun initView()

    /**
     * Setup controller
     */
    protected abstract fun initControl()


    /**
     * Dismiss dialog
     */
    protected fun dismissDialog() {
        this.dialog?.dismiss()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initControl()
    }

    fun show(manager: FragmentManager) {
        show(manager, javaClass.name)
    }
}