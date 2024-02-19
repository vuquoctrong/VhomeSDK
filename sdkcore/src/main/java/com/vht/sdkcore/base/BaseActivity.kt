package com.vht.sdkcore.base

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.vht.sdkcore.R
import com.vht.sdkcore.utils.Constants
import com.vht.sdkcore.utils.dialog.BaseDialog
import com.vht.sdkcore.utils.dialog.LoadingDialog
import timber.log.Timber
import java.lang.ref.WeakReference

abstract class BaseActivity<BD : ViewDataBinding, VM : BaseViewModel> : AppCompatActivity() {

    protected lateinit var binding: BD
    private lateinit var viewModel: VM

    private var lastTimeClick: Long = 0

    @get: LayoutRes
    abstract val layoutId: Int
    abstract fun getVM(): VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(WeakReference(this).get()!!, layoutId)
        binding.lifecycleOwner = this

        viewModel = getVM()
    }

    override fun onDestroy() {
        this?.let {
            LoadingDialog.getInstance(it)?.destroyLoadingDialog()
        }
        binding.unbind()
        super.onDestroy()
    }

    fun showLoading() {
        Handler(Looper.getMainLooper()).post {
            this?.let {
                LoadingDialog.getInstance(it)?.show()
            }
        }
    }

    fun hiddenLoading() {
        Handler(Looper.getMainLooper()).post {
            this?.let {
                LoadingDialog.getInstance(it)?.hidden()
            }
        }
    }

    //click able
    val isDoubleClick: Boolean
        get() {
            val timeNow = System.currentTimeMillis()
            if (timeNow - lastTimeClick >= Constants.DURATION_TIME_CLICKABLE) {
                //click able
                lastTimeClick = timeNow
                return false
            }
            return true
        }


    /**
     * Close SoftKeyboard when user click out of EditText
     */
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {

        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }


    override fun onBackPressed() {
        super.onBackPressed()
        Timber.d("onBackPressed in activity")
    }

    private fun showAlertDialog(message: String) {
        BaseDialog(this)
            .setMessage(message)
            .setPositiveButton(R.string.ok, null)
            .show()
    }
}