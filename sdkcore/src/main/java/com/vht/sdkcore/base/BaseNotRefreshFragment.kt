package com.vht.sdkcore.base

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.vht.sdkcore.R
import com.vht.sdkcore.utils.dialog.BaseDialog
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

abstract class BaseNotRefreshFragment<BD : ViewDataBinding, VM : BaseViewModel> : Fragment() {

    private var _binding: BD? = null
    val binding get() = _binding
    private lateinit var viewModel: VM
    protected val disposable = CompositeDisposable()
    private var isCreated = false
    @get: LayoutRes
    abstract val layoutId: Int
    abstract fun getVM(): VM

    private lateinit var mOnRequestPermissionListener: OnRequestPermissionListener

    @SuppressLint("TimberArgCount")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("BaseNotRefreshFragment", this::class.java.simpleName)
        viewModel = getVM()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (binding == null){
            _binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
            binding?.lifecycleOwner = viewLifecycleOwner
        }
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!isCreated){
            isCreated = true
            initView()

            setOnClick()

            bindingStateView()

            bindingAction()
        }

    }

    open fun setOnClick() {

    }

    open fun initView() {
        with(viewModel) {

            messageError.observe(viewLifecycleOwner) {
                var message = ""
                if (it is String) {
                    message = it
                } else {
                    if (it is Int) {
                        try {
                            message = getString(it)
                        } catch (e: Exception) {
                            //do nothing
                        }
                    }
                }
                if (!TextUtils.isEmpty(message)) {
                    showAlertDialog(message)
                }
            }

            isLoading.observe(viewLifecycleOwner) {
                showHideLoading(it)
            }

        }
    }

    open fun bindingStateView() {

    }

    open fun bindingAction() {

    }

    protected val isDoubleClick: Boolean
        get() {
            if (activity == null) {
                return false
            }
            return if (activity is BaseActivity<*, *>) {
                (activity as BaseActivity<*, *>?)!!.isDoubleClick
            } else false
        }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDestroy() {
        disposable.dispose()
        super.onDestroy()
    }


    fun showHideLoading(isShow: Boolean) {
        if (activity != null && activity is BaseActivity<*, *>) {
            if (isShow) {
                (activity as BaseActivity<*, *>?)!!.showLoading()
            } else {
                (activity as BaseActivity<*, *>?)!!.hiddenLoading()
            }
        }
    }

    fun showAlertDialog(message: String, onPositive: BaseDialog.OnDialogListener? = null) {
        BaseDialog(requireContext())
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(R.string.ok, onPositive)
            .show()
    }

    fun showAlertDialog(@StringRes message: Int) {
        BaseDialog(requireContext())
            .setMessage(message)
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    fun showAlertDialog(
        message: String,
        @StringRes textPositive: Int,
        onPositive: BaseDialog.OnDialogListener? = null
    ) {
        BaseDialog(requireContext())
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(textPositive, onPositive)
            .show()
    }

    fun showConfirmDialog(
        @StringRes message: Int,
        onPositive: BaseDialog.OnDialogListener? = null,
        onNegative: BaseDialog.OnDialogListener? = null,
    ) {
        BaseDialog(requireContext())
            .setMessage(message)
            .setPositiveButton(R.string.ok, onPositive)
            .setNegativeButton(R.string.cancel, onNegative)
            .show()
    }

    fun showConfirmDialog(
        message: String,
        onPositive: BaseDialog.OnDialogListener? = null,
        onNegative: BaseDialog.OnDialogListener? = null,
    ) {
        BaseDialog(requireContext())
            .setMessage(message)
            .setPositiveButton(R.string.ok, onPositive)
            .setNegativeButton(R.string.cancel, onNegative)
            .show()
    }

    //Request permission start
    private val requestPermissionStorageLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                mOnRequestPermissionListener.onPermissionGranted()
            } else {
                Toast.makeText(
                    requireContext(), "This feature is unavailabe because the feature requires " +
                            "a permission that has denied", Toast.LENGTH_SHORT
                ).show()
            }
        }

    protected fun requestPermisson(
        permission: String,
        onRequestPermissionGranted: OnRequestPermissionListener
    ) {
        mOnRequestPermissionListener = onRequestPermissionGranted
        //request storage permission
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                mOnRequestPermissionListener.onPermissionGranted()
            }
            shouldShowRequestPermissionRationale(permission) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
                //showInContextUI(...)
                requestPermissionStorageLauncher.launch(permission)
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionStorageLauncher.launch(permission)
            }
        }
    }

    protected fun hasPermissions(
        vararg permissions: String
    ): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(
            requireContext(),
            it
        ) == PackageManager.PERMISSION_GRANTED
    }

    protected interface OnRequestPermissionListener {
        fun onPermissionGranted()
    }
    //Request permission end


    fun getFullScreenWidth(): Int {
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

    fun getFullScreenHeight(): Int {
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    fun getMinimalScreenHeight(): Int {
        return getMinimalScreenWidth() * 9 / 16
    }

    fun getMinimalScreenWidth(): Int {
        return getFullScreenWidth()
    }

    fun exitFullScreenModeJF() {
        activity?.apply {
            val windowInsetsController =
                WindowCompat.getInsetsController(window, window.decorView)
            window.clearFlags(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            window.clearFlags(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
            window.clearFlags(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
            window.clearFlags(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            windowInsetsController.show(WindowInsetsCompat.Type.statusBars())
            windowInsetsController.show(WindowInsetsCompat.Type.navigationBars())
        }
    }
}