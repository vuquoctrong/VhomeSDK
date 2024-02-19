package com.vht.sdkcore.base.ver2.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.*
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
import androidx.navigation.fragment.findNavController
import com.vht.sdkcore.R
import com.vht.sdkcore.base.BaseActivity
import com.vht.sdkcore.utils.Constants
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.dialog.BaseDialog
import com.vht.sdkcore.utils.dialog.CommonAlertDialog
import com.vht.sdkcore.utils.dialog.CommonAlertDialogNotification
import com.vht.sdkcore.utils.dialog.DialogType
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber


abstract class BaseFragmentVer2<BD : ViewDataBinding> : Fragment() {

    private var _binding: BD? = null
    val binding: BD?
        get() {
            return _binding
        }
    private val disposable = CompositeDisposable()

    @get: LayoutRes
    abstract val layoutId: Int

    private lateinit var mOnRequestPermissionListener: OnRequestPermissionListener

    @SuppressLint("TimberArgCount")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.e(
            "Navigation BaseFragmentVer2 ${this::class.java.simpleName}",
            this::class.java.simpleName
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        _binding?.let {
            return it.root
        } ?: throw IllegalArgumentException("Binding variable is null")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding?.let {
            it.lifecycleOwner = viewLifecycleOwner
            initView()

            setOnClick()

            bindingStateView()

            bindingAction()
        }

    }


    open fun setOnClick() {

    }

    open fun initView() {

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
        binding?.unbind()
        _binding?.unbind()
        _binding = null
        super.onDestroyView()
    }

    fun dismissDialogNotification() {
        CommonAlertDialogNotification.getInstanceCommonAlertdialog(requireContext()).dismiss()
    }

    override fun onDestroy() {
        disposable.dispose()
        super.onDestroy()
    }


    fun showHideLoading(isShow: Boolean) {
        if (activity != null && activity is BaseActivity<*, *>) {
            if (activity?.isFinishing == false) {
                if (isShow) {
                    (activity as BaseActivity<*, *>?)!!.showLoading()
                } else {
                    (activity as BaseActivity<*, *>?)!!.hiddenLoading()
                }
            }

        }
    }

    fun exitFullScreenMode() {
        activity?.apply {
            val windowInsetsController =
                WindowCompat.getInsetsController(window, window.decorView)
            window.clearFlags(View.SYSTEM_UI_FLAG_FULLSCREEN)
            window.clearFlags(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            window.clearFlags(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
            window.clearFlags(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
            window.clearFlags(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            windowInsetsController.show(WindowInsetsCompat.Type.statusBars())
            windowInsetsController.show(WindowInsetsCompat.Type.navigationBars())
        }
    }

    fun initFullscreenMode() {
        activity?.apply {
            val windowInsetsController =
                WindowCompat.getInsetsController(window, window.decorView)

            window.addFlags(View.SYSTEM_UI_FLAG_FULLSCREEN)
            window.addFlags(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            window.addFlags(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
            window.addFlags(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
            window.addFlags(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())
            windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())
        }
    }

    fun hideNavigationAndStatus() {
        activity?.apply {
            val windowInsetsController =
                WindowCompat.getInsetsController(window, window.decorView)
            windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())
            windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())
        }
    }

    fun showNavigationAndStatus() {
        activity?.apply {
            val windowInsetsController =
                WindowCompat.getInsetsController(window, window.decorView)
            windowInsetsController.show(WindowInsetsCompat.Type.statusBars())
            windowInsetsController.show(WindowInsetsCompat.Type.navigationBars())
        }
    }

    fun openSetting() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", this.requireContext().packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    fun checkWriteExternalPermission(): Boolean {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        val res = requireContext().checkCallingOrSelfPermission(permission)
        return res == PackageManager.PERMISSION_GRANTED
    }

    fun hideNavigationBarsSystem() {
        activity?.apply {
            val windowInsetsController =
                WindowCompat.getInsetsController(window, window.decorView)
            windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())
        }
    }

    fun showNavigationBar() {
        // set navigation bar status, remember to disable "setNavigationBarTintEnabled"
        val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        // This work only for android 4.4+
        activity?.window?.getDecorView()?.setSystemUiVisibility(flags)

        // Code below is to handle presses of Volume up or Volume down.
        // Without this, after pressing volume buttons, the navigation bar will
        // show up and won't hide
        activity?.window?.getDecorView()?.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN === 0) {
                activity?.window?.decorView?.systemUiVisibility = flags
            }
        }
    }

    fun isScreenOrientationLandscape(): Boolean {
        return activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    fun showAlertDialog(message: String, onPositive: BaseDialog.OnDialogListener? = null) {
        BaseDialog(requireContext())
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(R.string.ok, onPositive)
            .show()
    }

    fun showCameraSettingAlertDialog(
        message: String,
        color: String,
        onPositive: BaseDialog.OnDialogListener? = null,
    ) {
        BaseDialog(requireContext())
            .setCancelable(false)
            .setTitleWithColor(message, color)
            .setPositiveButtonWithColor(R.string.ok, color, context, onPositive)
            .show()
    }

    fun showAlert(@StringRes message: Int) {
        CommonAlertDialog.getInstanceCommonAlertdialog(
            requireContext(),
            Define.TYPE_DIALOG.GENERIC
        )
            .showDialog()
            .setContent(message)
            .showPositiveButton()
            .setOnPositivePressed {
                it.dismiss()
            }
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

    protected fun requestPermission(
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
                onRequestPermissionGranted.onRejectPermissionManyTimes()
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

        fun onRejectPermissionManyTimes()

        fun onDismiss()
    }
    //Request permission end


    fun getFullScreenWidth(): Int {
        val outMetrics = DisplayMetrics()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val display = activity?.display
            display?.getRealMetrics(outMetrics)
        } else {
            @Suppress("DEPRECATION")
            val display = activity?.windowManager?.defaultDisplay
            @Suppress("DEPRECATION")
            display?.getMetrics(outMetrics)
        }
        return outMetrics.widthPixels
    }

    fun getFullScreenHeight(): Int {
        val outMetrics = DisplayMetrics()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val display = activity?.display
            display?.getRealMetrics(outMetrics)
        } else {
            @Suppress("DEPRECATION")
            val display = activity?.windowManager?.defaultDisplay
            @Suppress("DEPRECATION")
            display?.getMetrics(outMetrics)
        }
        return outMetrics.heightPixels
    }

    fun getMinimalScreenHeight(): Int {
        return getMinimalScreenWidth() * 9 / 16
    }

    fun getMinimalScreenWidth(): Int {
        return getFullScreenWidth()
    }

    fun checkReadStoragePermission(checkPermanentlyDenied: Boolean = false): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES
                ) && PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_VIDEO
                )
            ) {
                return true
            } else {
                if (checkPermanentlyDenied) {
                    requestPermissionCheckPermanentlyDeniedLauncher.launch(
                        arrayOf(
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.READ_MEDIA_VIDEO
                        )
                    )
                } else {
                    requestPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.READ_MEDIA_VIDEO
                        )
                    )
                }
                return false
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) && PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) && PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_MEDIA_LOCATION
                    )
                ) {
                    return true
                } else {
                    if (checkPermanentlyDenied) {
                        requestPermissionCheckPermanentlyDeniedLauncher.launch(
                            arrayOf(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.ACCESS_MEDIA_LOCATION
                            )
                        )
                    } else {
                        requestPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.ACCESS_MEDIA_LOCATION
                            )
                        )
                    }
                    return false
                }

            } else {
                if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) && PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ) {
                    return true
                } else {
                    if (checkPermanentlyDenied) {
                        requestPermissionCheckPermanentlyDeniedLauncher.launch(
                            arrayOf(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            )
                        )
                    } else {
                        requestPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            )
                        )
                    }
                    return false
                }
            }
        }

    }

    fun checkAudioPermission(checkPermanentlyDenied: Boolean = false): Boolean {
        if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.MODIFY_AUDIO_SETTINGS
            ) && PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            )
        ) {
            return true
        } else {
            if (checkPermanentlyDenied) {
                requestPermissionCheckPermanentlyDeniedLauncher.launch(
                    arrayOf(
                        Manifest.permission.MODIFY_AUDIO_SETTINGS,
                        Manifest.permission.RECORD_AUDIO
                    )
                )
            } else {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.MODIFY_AUDIO_SETTINGS,
                        Manifest.permission.RECORD_AUDIO
                    )
                )
            }
            return false
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { data ->


        }

    private val requestPermissionCheckPermanentlyDeniedLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { data ->
            if (data.isEmpty()) {
                return@registerForActivityResult
            }
            val deniedList: List<String> = data.filter {
                it.value.not()
            }.map {
                it.key
            }
            if (deniedList.isNotEmpty()) {
                val permanentlyMappedList = deniedList.map {
                    activity?.let { activity ->
                        ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
                    }
                }
                if (permanentlyMappedList.contains(false)) {
                    CommonAlertDialogNotification.getInstanceCommonAlertdialog(requireContext())
                        .showDialog()
                        .setTextNegativeButton(R.string.refuse)
                        .setTextPositiveButton(R.string.setting)
                        .setContent("Vui lòng cho phép VHome Ghi âm và truy cập vào Album trên thiết bị của bạn để lưu ảnh chụp màn hình và video")
                        .showCenterImage(DialogType.CONFIRM)
                        .setDialogTitleWithString("Không có quyền truy cập")
                        .setOnNegativePressed {
                            it.dismiss()
                        }
                        .setOnPositivePressed {
                            it.dismiss()
                            goToSettingPermission()
                        }
                }
            }
        }

    fun goToSettingPermission() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireContext().packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    fun openSystemWifiSetting() {
        val wifiSettingsIntent = Intent(Settings.ACTION_WIFI_SETTINGS)
        startActivity(wifiSettingsIntent)
    }

    fun <T> doOnFragmentResult(key: String, action: (T) -> Unit) {
        findNavController().currentBackStackEntry?.savedStateHandle?.run {
            getLiveData<T>(key).observe(viewLifecycleOwner) {
                action.invoke(it)
                clearSavedStateProvider(key)
            }
        }
    }

    fun setFragmentResult(key: String, value: Any) {
        findNavController().previousBackStackEntry?.savedStateHandle?.set(key, value)
    }

    protected fun showDialogNotification(
        title: String,
        type: DialogType,
        message: Int = Constants.ERROR_NUMBER,
        titleBtnConfirm: Int = Constants.ERROR_NUMBER,
        negativeTitle: Int = Constants.ERROR_NUMBER,
        onNegativeClick: (() -> Unit)? = null,
        onPositiveClick: () -> Unit
    ) {
        CommonAlertDialogNotification.getInstanceCommonAlertdialog(requireContext())
            .showDialog()
            .setTextNegativeButton(negativeTitle)
            .setTextPositiveButton(titleBtnConfirm)
            .setContent(message)
            .showCenterImage(type)
            .setDialogTitleWithString(title)
            .setOnNegativePressed {
                it.dismiss()
                onNegativeClick?.invoke()
            }
            .setOnPositivePressed {
                it.dismiss()
                onPositiveClick.invoke()
            }
    }

    fun openSettingBiometricInDevice() {
        requireActivity().startActivity(Intent(Settings.ACTION_SETTINGS))
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            requireActivity().startActivity(Intent(Settings.ACTION_BIOMETRIC_ENROLL))
//        } else {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                requireActivity().startActivity(Intent(Settings.ACTION_FINGERPRINT_ENROLL))
//            } else {
//                requireActivity().startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
//            }
//        }
    }

}
