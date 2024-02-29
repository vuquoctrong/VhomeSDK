package com.viettel.vht.sdk.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.manager.account.XMAccountManager
import com.vht.sdkcore.base.BaseActivity
import com.vht.sdkcore.base.OnBackPressedListener
import com.vht.sdkcore.file.AppLogFileManager
import com.vht.sdkcore.pref.RxPreferences
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.convertLongToStringWithFormatter
import com.vht.sdkcore.utils.dialog.CommonAlertDialog
import com.vht.sdkcore.utils.dialog.CommonAlertDialogNotification
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.ActivitySdkVhomeMainBinding
import com.viettel.vht.sdk.funtionsdk.VHomeSDKAddCameraJFListener
import com.viettel.vht.sdk.jfmanager.JFCameraManager
import com.viettel.vht.sdk.model.home.RequestGetAppLogUpLoadLink
import com.viettel.vht.sdk.model.home.RequestUpLoadAppLog
import com.viettel.vht.sdk.navigation.AppNavigation
import com.viettel.vht.sdk.network.ListRequest
import com.viettel.vht.sdk.network.NetworkEvent
import com.viettel.vht.sdk.network.NetworkState
import com.viettel.vht.sdk.ui.jftechaddcamera.JFAddLocalNetworkFragment
import com.viettel.vht.sdk.utils.isNotEmpty
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import java.net.URL
import javax.inject.Inject


@AndroidEntryPoint
class SDKVHomeMainActivity : BaseActivity<ActivitySdkVhomeMainBinding, MainViewModel>() {
    val TAG = SDKVHomeMainActivity::class.simpleName.toString()

    @Inject
    lateinit var appNavigation: AppNavigation


    @Inject
    lateinit var networkEvent: NetworkEvent

    @Inject
    lateinit var rxPreferences: RxPreferences

    @Inject
    lateinit var appLogFileManager: AppLogFileManager

    @Inject
    lateinit var listRequest: ListRequest


    private var upLoadAppLogToLinkJob: Job? = null
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val viewModel: MainViewModel by viewModels()
    private var logID: String? = null
    private var isTheFirstTime: Boolean = true


    override fun getVM() = viewModel

    override val layoutId = R.layout.activity_sdk_vhome_main
    private lateinit var navHostFragment: NavHostFragment

    private val networkCallback by lazy {
        object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                setPublicIPForAppLogFileManager()
            }
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        window.statusBarColor = Color.TRANSPARENT
        super.onCreate(savedInstanceState)
        bindingState()
        setPublicIPForAppLogFileManager()
        window.addFlags(
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host) as NavHostFragment
        appNavigation.bind(navHostFragment.navController)

        navHostFragment.navController.addOnDestinationChangedListener { _, destination, _ ->
            Timber.tag("Navigation").e("destination: $destination")
        }

        (getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).registerDefaultNetworkCallback(
            networkCallback
        )

        appLogFileManager.initFolder(this@SDKVHomeMainActivity)
        appLogFileManager.initAppLogFile(rxPreferences.getUserId())
        sendAndDeleteLocalFileInToServer()
        lifecycleScope.launchWhenResumed {
            startUpLoadAppLogToLink()
        }
//        configCameraJF()
    }

    private fun sendAndDeleteLocalFileInToServer() {
        try {
            val listFiles = appLogFileManager.getFolder().listFiles()
            isTheFirstTime = true
            if (listFiles.size > 1) {
                logID = rxPreferences.getUserId() + "_" + System.currentTimeMillis()
                    .convertLongToStringWithFormatter()
                Timber.tag(TAG).d("sendAndDeleteLocalFileInToServer: $logID")
                viewModel.getAppLogUpLoadLink(
                    RequestGetAppLogUpLoadLink(
                        endpoint = "/v2/api/home-system/app-log/upload-link",
                        userId = rxPreferences.getUserId(),
                        logId = logID,
                        logType = "SCHEDULE_LOG",
                        startTime = appLogFileManager.getCurrentFile().lastModified(),
                        stopTime = System.currentTimeMillis()
                    )
                )
            }
        } catch (e: Exception) {
            hiddenLoading()
        }
    }

    private fun bindingState() {
        viewModel.appLogUpLoadLinkResponse.observe(this) {
            if (it.data?.data != null) {
                when (it.status) {
                    com.vht.sdkcore.network.Status.SUCCESS -> {
                        val currentFolderFiles = appLogFileManager.getFolder().listFiles()
                        Timber.tag(TAG)
                            .d("appLogUpLoadLinkResponse size ${currentFolderFiles.size}")
                        currentFolderFiles.forEach { file ->
                            if (isTheFirstTime) {
                                if (file.absolutePath != appLogFileManager.getCurrentFile().absolutePath) {
                                    if (file.isNotEmpty()) {
                                        Timber.tag(TAG)
                                            .d("appLogUpLoadLinkResponse size file.isNotEmpty()")
                                        viewModel.uploadFileToServer(
                                            file,
                                            it.data?.data ?: ""
                                        )
                                    } else {
                                        Timber.tag(TAG)
                                            .d("appLogUpLoadLinkResponse size file.is Empty()")
                                        file.delete()
                                    }
                                }
                            } else {
                                if (file.isNotEmpty()) {
                                    viewModel.uploadFileToServer(
                                        file,
                                        it.data?.data ?: ""
                                    )
                                } else {
                                    Timber.tag(TAG)
                                        .d("appLogUpLoadLinkResponse size file.is Empty()")
                                    file.delete()
                                }
                            }
                        }
                        if (!isTheFirstTime) {
                            appLogFileManager.initAppLogFile(rxPreferences.getUserId())
                        }
                    }

                    com.vht.sdkcore.network.Status.LOADING -> {
                        Timber.tag(TAG)
                            .e("appLogUpLoadLinkResponse loading")
                    }

                    com.vht.sdkcore.network.Status.ERROR -> {
                        Timber.tag(TAG)
                            .d("appLogUpLoadLinkResponse error ${it.exception}")
                    }
                }

            }
        }

        viewModel.uploadFileToServerStatus.observe(this) {
            if (it == true) {
                try {
                    viewModel.getAppLogStatus(
                        RequestUpLoadAppLog(
                            endpoint = "/v2/api/home-system/app-log/upload-status-update",
                            userId = rxPreferences.getUserId(),
                            logId = logID
                        )
                    )
                } catch (e: java.lang.Exception) {

                }
            }
        }

        viewModel.appLogStatus.observe(this) {
            if (it.data != null) {
                if (it.data?.errorName == "Ok") {

                }
            }
        }



    }

    private suspend fun retrievePublicIp(): String = withContext(Dispatchers.IO) {
        return@withContext try {
            val url = URL("https://api.ipify.org")
            url.readText()
        } catch (e: Exception) {
            hiddenLoading()
            e.printStackTrace()
            ""
        }
    }

    private fun setPublicIPForAppLogFileManager() {
        lifecycleScope.launchWhenResumed {
            val publicIP = retrievePublicIp()
            appLogFileManager.setPublicIP(publicIP)
        }
    }

    private suspend fun startUpLoadAppLogToLink() {
        upLoadAppLogToLinkJob?.cancel()
        upLoadAppLogToLinkJob = applicationScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(UP_LOAD_APP_LOG_TIME)
                isTheFirstTime = false
                logID = rxPreferences.getUserId() + "_" + System.currentTimeMillis()
                    .convertLongToStringWithFormatter()
                try {
                    viewModel.getAppLogUpLoadLink(
                        RequestGetAppLogUpLoadLink(
                            endpoint = "/v2/api/home-system/app-log/upload-link",
                            userId = rxPreferences.getUserId(),
                            logId = logID,
                            logType = "SCHEDULE_LOG",
                            startTime = appLogFileManager.getCurrentFile().lastModified(),
                            stopTime = System.currentTimeMillis()
                        )
                    )
                } catch (e: Exception) {

                }
            }
        }
    }

    private fun cancelAllJob() {
        upLoadAppLogToLinkJob?.cancel()
        upLoadAppLogToLinkJob = null
    }


    private fun showMessage(message: String) {
        CommonAlertDialog.getInstanceCommonAlertdialog(this, Define.TYPE_DIALOG.GENERIC)
            .showDialog()
            .setDialogTitleWithString(message)
            .showPositiveButton()
            .setOnPositivePressed { it.dismiss() }
    }



    private fun configCameraJF() {
        JFCameraManager.init(context = application)
        rxPreferences.getUserJF().let { phone ->
            if (phone.isNotEmpty()) {
                JFCameraManager.loginAccountJF(
                    user = phone, password = rxPreferences.getTokeJFTech()
                ) {
                    Timber.d("Callback login JF")
                    stopPushServiceCameraJF()
                    startPushServiceCameraJF()
                }
            }
        }
    }

    private fun startPushServiceCameraJF() {
        Timber.d("startPushServiceCameraJF")
//        try {
//            val intent = Intent().apply {
//                addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
//                setClass(applicationContext, AlarmPushService::class.java)
//            }
//            startService(intent)
//        } catch (e: Exception) {
//            Timber.e(e)
//        }
    }

    private fun stopPushServiceCameraJF() {
     //   stopService(Intent(this, AlarmPushService::class.java))
    }

    override fun onRestart() {
        super.onRestart()
//        if (!XUtils.isServiceRunning(this, AlarmPushService::class.simpleName)) {
//            startPushServiceCameraJF()
//        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.clear()
    }

    override fun onDestroy() {
        (getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).unregisterNetworkCallback(
            networkCallback
        )
        dismissDialog()
        cancelAllJob()
        JFCameraManager.isUpdateDevStateCompleted = false
        JFCameraManager.isLoginFailed = true
        listRequest.listNetworkRequest = mutableListOf()
        XMAccountManager.getInstance().setLoginState(false)
        super.onDestroy()
    }

    private fun dismissDialog() {
        CommonAlertDialogNotification.instance?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
        CommonAlertDialogNotification.instance = null

        CommonAlertDialog.instance?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
        CommonAlertDialog.instance = null
    }


    override fun onBackPressed() {
        val currentFragment: Fragment =
            navHostFragment.childFragmentManager.fragments[0]

        if (currentFragment is OnBackPressedListener) (currentFragment as OnBackPressedListener).onBackPressed()
        else super.onBackPressed()
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launchWhenResumed {
            networkEvent.observableNetworkState
                .flowOn(Dispatchers.Main)
                .collectLatest { status ->

                    val currentFragment: Fragment =
                        navHostFragment.childFragmentManager.fragments[0]
                    when (status) {
                        is NetworkState.UNAUTHORIZED -> {
                            Timber.d("Main_NetworkState.UNAUTHORIZED")
                            CommonAlertDialog.getInstanceCommonAlertdialog(
                                this@SDKVHomeMainActivity,
                                Define.TYPE_DIALOG.UN_AUTHEN
                            )
                                .showDialog()
                                .setDialogTitle(com.vht.sdkcore.R.string.authentication_error)
                                .showPositiveButton()
                                .setOnPositivePressed {
                                    it.dismiss()
                                }
                            networkEvent.publish(NetworkState.INITIALIZE)
                        }

                        is NetworkState.NO_INTERNET -> {
                            if (currentFragment is JFAddLocalNetworkFragment)
                                return@collectLatest
                            CommonAlertDialog.getInstanceCommonAlertdialog(
                                this@SDKVHomeMainActivity,
                                Define.TYPE_DIALOG.NO_INTERNET
                            )
                                .showDialog()
                                .showImageDescription(R.drawable.ic_no_internet)
                                .setDialogTitle(com.vht.sdkcore.R.string.network_error)
                                .showPositiveButton()
                                .setOnPositivePressed {
                                    it.dismiss()
                                }
                            hiddenLoading()
                            networkEvent.publish(NetworkState.INITIALIZE)
                        }

                        is NetworkState.CONNECTED_INTERNET -> {
                            if (rxPreferences.isShowingPopupLocalMode()) {
                                CommonAlertDialog.instance?.dismiss()
                                rxPreferences.setIsShowingPopupLocalMode(false)
                            }
                        }

                        is NetworkState.GENERIC -> {
                            Timber.tag(TAG).d("Error code = ${status.exception.code}")
                            val fragment: Fragment =
                                navHostFragment.childFragmentManager.fragments[0]
                            when {
                                status.exception.code in 1100..1100 -> {
                                    handleErrorCode(status.exception.code)
                                }

                                (status.exception.code == 2028 || status.exception.code == 2030
                                        || status.exception.code == 2010 || status.exception.code == 2003 || status.exception.code == 2013)
                                -> {
                                    handleErrorCode(status.exception.code)
                                }

                                status.exception.code == 2009-> {
                                    handleErrorCode(status.exception.code)
                                }

                                status.exception.code == 2013 -> {
                                    handleErrorCode(status.exception.code)
                                }

                                status.exception.code in 2001..2025 -> {
                                    handleErrorCode(status.exception.code)
                                }

                                status.exception.code in 3003..3005 -> {
                                    handleErrorCode(status.exception.code)
                                }

                                status.exception.code in 1401..1402 -> {
                                    handleEventErrorCode(status.exception.code)
                                }

                                status.exception.code in 2201..2201 -> {
                                    handleAssistantErrorCode(status.exception.code)
                                }

                                status.exception.errorCode in 800033..800033 -> {
                                    handleCameraErrorCode(status.exception.errorCode)
                                }

                                status.exception.code in 10013..10013 -> {
                                    showAlert(status.exception.message)
                                }

                                status.exception.code in 100013..100013 -> {
                                    showAlert(
                                        status.exception.message,
                                        getString(com.vht.sdkcore.R.string.string_notification)
                                    )
                                }
                            }
                            networkEvent.publish(NetworkState.INITIALIZE)
                        }

                        else -> {}
                    }
                }
        }
    }

    override fun onPause() {
        super.onPause()
    }

    private fun handleErrorCode(errorCode: Int) {
        val messageError = when (errorCode) {
            1100 -> getString(com.vht.sdkcore.R.string.non_existent_entity)

            2006 -> getString(com.vht.sdkcore.R.string.wrong_identifier_exists)

            2003 -> getString(com.vht.sdkcore.R.string.accout_not_exit)

            2008 -> getString(com.vht.sdkcore.R.string.OTP_is_expired)

            2009 -> getString(com.vht.sdkcore.R.string.OTP_is_incorrect)

            2010 -> getString(com.vht.sdkcore.R.string.identifier_password_incorrect)

            2011 -> getString(com.vht.sdkcore.R.string.account_not_activated)

            2013 -> getString(com.vht.sdkcore.R.string.password_is_incorrect)

            2015 -> getString(com.vht.sdkcore.R.string.password_different)

            2016 -> getString(com.vht.sdkcore.R.string.generate_otp_failed)

            2020 -> getString(com.vht.sdkcore.R.string.password_not_like_ezviz)

            2023 -> getString(com.vht.sdkcore.R.string.phone_lock_down)

            2024 -> getString(com.vht.sdkcore.R.string.otp_lock_down)

            2025 -> getString(com.vht.sdkcore.R.string.fotgot_password_wrong_phone_number)

            3003 -> {
                getString(com.vht.sdkcore.R.string.captcha_wrong)
            }

            3005 -> {
                getString(com.vht.sdkcore.R.string.captcha_please)
            }

            //2006
            else -> getString(com.vht.sdkcore.R.string.something_wrong)
        }

        val fragment: Fragment =
            navHostFragment.childFragmentManager.fragments[0]

        var title = ""

        when (errorCode) {
            2008, 2010, 2011, 2013, 2015, 2020 -> {
                title = "Đăng nhập thất bại"
            }

            2025 -> title = "Tài khoản chưa đăng ký"

            else -> title = ""
        }
        showAlert(messageError, title)
    }

    private fun handleAssistantErrorCode(errorCode: Int) {
        val messageError = when (errorCode) {
            //2201
            else -> getString(com.vht.sdkcore.R.string.dataset_is_existed)
        }
        showAlert(messageError)
    }

    private fun handleEventErrorCode(errorCode: Int) {
        val messageError = when (errorCode) {
            1401 -> {
                getString(com.vht.sdkcore.R.string.active_scene_on_tab_fail)
            }

            //1402
            else -> getString(com.vht.sdkcore.R.string.active_scene_outside)
        }

        showAlert(messageError)
    }

    private fun handleCameraErrorCode(errorCode: Int) {
        when (errorCode) {
            800033 -> showAlert("Gói cước đang được sử dụng")
        }
    }


    private fun showAlert(message: String, title: String? = null) {
        CommonAlertDialog.getInstanceCommonAlertdialog(
            this@SDKVHomeMainActivity,
            Define.TYPE_DIALOG.GENERIC
        )
            .showDialog()
            .setDialogTitleWithString(title)
            .showImageDescription(com.vht.sdkcore.R.drawable.ic_dialog_error)
            .setContent(message)
            .showPositiveButton()
            .setOnPositivePressed {
                it.dismiss()
                hiddenLoading()
            }
    }


    companion object {
        private const val UP_LOAD_APP_LOG_TIME = (5 * 60 * 1000L) //ms
    }

}
