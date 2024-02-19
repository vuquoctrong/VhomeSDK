package com.vht.sdkcore.base

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
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
import com.vht.sdkcore.utils.dialog.BaseDialog
import com.vht.sdkcore.utils.dialog.CommonAlertDialog
import com.vht.sdkcore.utils.dialog.CommonAlertDialogNotification
import com.vht.sdkcore.utils.dialog.DialogType
import com.vht.sdkcore.utils.dialog.InputDialog
import com.vht.sdkcore.utils.dialog.ItemRadio
import com.vht.sdkcore.utils.dialog.RadioSelectDialog
import com.vht.sdkcore.utils.Constants
import com.vht.sdkcore.utils.Define
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber


abstract class BaseFragment<BD : ViewDataBinding, VM : BaseViewModel> : Fragment() {

    private var _binding: BD? = null
    val binding: BD
        get() {
            return _binding!!
        }
    private lateinit var viewModel: VM
    protected val disposable = CompositeDisposable()

    @get: LayoutRes
    abstract val layoutId: Int
    abstract fun getVM(): VM

    private lateinit var mOnRequestPermissionListener: OnRequestPermissionListener


    @SuppressLint("TimberArgCount")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("BaseFragment", this::class.java.simpleName)
        viewModel = getVM()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
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

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroyView() {
        binding.unbind()
        _binding?.unbind()
        _binding = null
        super.onDestroyView()
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

    fun initFullscreenModeJF() {
        activity?.apply {
            val windowInsetsController =
                WindowCompat.getInsetsController(window, window.decorView)

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
        onPositive: BaseDialog.OnDialogListener? = null,
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
        onRequestPermissionGranted: OnRequestPermissionListener,
    ) {
        mOnRequestPermissionListener = onRequestPermissionGranted
        //request storage permission
        when {
            ContextCompat.checkSelfPermission(
                requireContext(), permission
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
        vararg permissions: String,
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

    fun isNetWorkConnect(): Boolean {
        val connMgr =
            context?.applicationContext?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? = connMgr.activeNetworkInfo
        return networkInfo?.isConnected == true
    }

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

    fun isCheckLocation(): Boolean {
        if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) && PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) && PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CHANGE_WIFI_STATE
            ) && PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_WIFI_STATE
            )
        ) {
            val locationManager =
                requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                locationManager.let {
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        return true
                    } else {
                        CommonAlertDialog.getInstanceCommonAlertdialog(requireContext())
                            .showDialog()
                            .setDialogTitle(R.string.string_tv_open_location_gps)
                            .showPositiveButton()
                            .setOnPositivePressed {
                                requireContext().startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                                it.dismiss()
                            }
                        return false
                    }
                }
            } else {
                return true
            }
        } else {
            requestLocationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_WIFI_STATE
                )
            )
            return false
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

    private val requestLocationPermissionLauncher =
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
                        .setDialogTitleWithString("Viettel Home cần cấp quyền vị trí để tiếp tục")
                        .showCenterImage(DialogType.NOTIFICATION)
                        .setTextPositiveButtonWithString("CẤP QUYỀN")
                        .showPositiveButton()
                        .setOnPositivePressed {
                            it.dismiss()
                            goToSettingPermission()
                        }
                }
            }
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
                remove<T>(key)
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
        onPositiveClick: (() -> Unit)? = null
    ) {
        CommonAlertDialogNotification.getInstanceCommonAlertdialog(requireContext())
            .showDialog()
            .setTextNegativeButton(negativeTitle)
            .setTextPositiveButton(titleBtnConfirm)
            .setContent(message)
            .setCancelBack(false)
            .showCenterImage(type)
            .setDialogTitleWithString(title)
            .setOnNegativePressed {
                it.dismiss()
                onNegativeClick?.invoke()
            }
            .setOnPositivePressed {
                it.dismiss()
                onPositiveClick?.invoke()
            }
    }

    fun dismissDialogNotification() {
        CommonAlertDialogNotification.getInstanceCommonAlertdialog(requireContext()).dismiss()
    }

    protected fun showDialogError(
        title: String,
        type: DialogType = DialogType.ERROR,
        message: String = "",
        titleBtnConfirm: Int = Constants.ERROR_NUMBER,
        onPositiveClick: () -> Unit,
    ) {
        CommonAlertDialogNotification.getInstanceCommonAlertdialog(requireContext())
            .showDialog()
            .setTextPositiveButton(titleBtnConfirm)
            .setContent(message)
            .setCancelBack(false)
            .showCenterImage(type)
            .setDialogTitleWithString(title)
            .setOnPositivePressed {
                it.dismiss()
                onPositiveClick.invoke()
            }
    }

    protected fun showInputDialog(
        title: String,
        inputLabel: String,
        content: String? = null,
        maxLength: Int,
        titleBtnConfirm: Int = Constants.ERROR_NUMBER,
        titleBtnCancel: Int = Constants.ERROR_NUMBER,
        onPositiveClick: ((String) -> Unit)? = null,
        onNegativeClick: (() -> Unit)? = null,
    ) {
        InputDialog.getInstanceInputDialog(requireContext())
            .showDialog()
            .setDialogTitleWithString(title)
            .setInputLabelWithString(inputLabel)
            .setContent(content)
            .setMaxInputLength(maxLength)
            .setTextNegativeButton(titleBtnCancel)
            .setTextPositiveButton(titleBtnConfirm)
            .setOnNegativePressed {
                onNegativeClick?.invoke()
            }
            .setOnPositivePressed { _, input ->
                onPositiveClick?.invoke(input)
            }

    }

    fun openSettingBiometricInDevice() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireActivity().startActivity(Intent(Settings.ACTION_BIOMETRIC_ENROLL))
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                requireActivity().startActivity(Intent(Settings.ACTION_FINGERPRINT_ENROLL))
            } else {
                requireActivity().startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
            }
        }
    }

    protected fun <T : ItemRadio> showRadioDialog(
        title: String,
        titleBtnConfirm: Int = Constants.ERROR_NUMBER,
        titleBtnCancel: Int = Constants.ERROR_NUMBER,
        data: MutableList<T>? = null,
        selectedItem: T? = null,
        @DrawableRes iconSelected: Int? = null,
        @DrawableRes iconUnselected: Int? = null,
        onPositiveClick: (T?) -> Unit,
    ) {
        RadioSelectDialog.getInstanceInputDialog<T>(requireContext())
            .showDialog()
            .setDialogTitleWithString(title)
            .setTextNegativeButton(titleBtnCancel)
            .setTextPositiveButton(titleBtnConfirm)
            .setData(data?.toMutableList())
            .setSelectedItem(selectedItem)
            .setIconRadioSelected(iconSelected)
            .setIconRadioUnselected(iconUnselected)
            .setOnPositivePressed { _, result -> onPositiveClick.invoke(result) }
    }

}