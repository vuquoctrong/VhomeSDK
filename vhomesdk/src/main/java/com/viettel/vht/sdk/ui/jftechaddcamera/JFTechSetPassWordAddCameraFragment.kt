package com.viettel.vht.sdk.ui.jftechaddcamera

import android.os.Bundle
import android.os.Message
import android.text.InputType
import android.text.TextUtils
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.navigation.navGraphViewModels
import com.alibaba.fastjson.JSON
import com.basic.G
import com.google.gson.Gson
import com.lib.*
import com.lib.EFUN_ERROR.EE_DVR_PASSWORD_NOT_VALID
import com.lib.sdk.bean.HandleConfigData
import com.lib.sdk.bean.JsonConfig
import com.lib.sdk.bean.StringUtils
import com.lib.sdk.bean.TimeZoneBean
import com.manager.db.DevDataCenter
import com.manager.device.DeviceManager
import com.manager.device.DeviceManager.OnDevManagerListener
import com.utils.XUtils
import com.vht.sdkcore.base.BaseFragment
import com.vht.sdkcore.pref.RxPreferences
import com.vht.sdkcore.utils.Constants.EventKey.EVENT_SYSTEM_CHANGE_INFORMATION_CAMERA_JF
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.Define.Companion.GET_SET_DEV_CONFIG_TIMEOUT
import com.vht.sdkcore.utils.EdtState
import com.vht.sdkcore.utils.StringUtils.validatepassword
import com.vht.sdkcore.utils.eventbus.RxEvent
import com.vht.sdkcore.utils.showCustomToast
import com.vht.sdkcore.utils.toastMessage
import com.viettel.vht.sdk.jfmanager.JFCameraManager
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentSetPasswordAddCameraJftechBinding
import com.viettel.vht.sdk.funtionsdk.VHomeSDKManager
import com.viettel.vht.sdk.model.DeviceDataResponse
import com.viettel.vht.sdk.navigation.AppNavigation
import com.viettel.vht.sdk.utils.getArgSerializable
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class JFTechSetPassWordAddCameraFragment :
    BaseFragment<FragmentSetPasswordAddCameraJftechBinding, JFTechAddCameraModel>(),
    IFunSDKResult {

    val TAG = JFTechSetPassWordAddCameraFragment::class.java.simpleName

    val CHANGE_RANDOM_PASSWORD_TIMEOUT = 15000 //修改随机用户名密码默认给15s超时


    private val viewModel: JFTechAddCameraModel by navGraphViewModels(R.id.add_camera_jftech_graph) {
        defaultViewModelProviderFactory
    }

    @Inject
    lateinit var rxPreferences: RxPreferences

    @Inject
    lateinit var appNavigation: AppNavigation
    @Inject
    lateinit var sdkManager: VHomeSDKManager


    private var _msgId = 0xff00ff

    private var mTempUser: String? = null
    private var mTempPwd: String? = null
    private var devToken: String? = null
    private val jsonToken = com.alibaba.fastjson.JSONObject()

    override val layoutId = R.layout.fragment_set_password_add_camera_jftech


    override fun getVM(): JFTechAddCameraModel = viewModel

    private val device: DeviceDataResponse
        get() = getArgSerializable(
            Define.BUNDLE_KEY.PARAM_DEVICE_ITEM,
            DeviceDataResponse::class.java
        )


    override fun initView() {
        super.initView()
        binding.tvIdCamera.text = viewModel.devID
        _msgId = FunSDK.GetId(_msgId, this)
        binding.toolbar.showHiddenBack(false)
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Timber.d("handleOnBackPressed")
                    return
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
        // Dev của đối tác, thông báo không cần gọi hàm này
//        FunSDK.DevConfigJsonNotLogin(
//            _msgId, viewModel.devID,
//            JsonConfig.GET_RANDOM_USER, null, 1660, -1, 0, GET_SET_DEV_CONFIG_TIMEOUT, 0
//        )
    }


    override fun setOnClick() {
        super.setOnClick()
//        binding.toolbar.setOnLeftClickListener {
//            cameraNavigation.navigateUp()
//        }
        binding.confirmBtn.setOnClickListener {
            if(isDoubleClick)return@setOnClickListener
            setPassWordCamera()
        }
        binding.edtPassWord.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        binding.ivShowPassword.setImageResource(R.drawable.ic_eye_open)
        binding.edtConfirmPassWord.inputType =
            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        binding.ivShowConfirmPassword.setImageResource(R.drawable.ic_eye_open)

        binding.ivShowPassword.setOnClickListener { v: View? ->
            if (binding.edtPassWord.inputType == Define.LOGIN.INPUTTYPE_TEXTPASSWORD) {
                binding.edtPassWord.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.ivShowPassword.setImageResource(R.drawable.ic_eye_open)
            } else {
                binding.edtPassWord.inputType = Define.LOGIN.INPUTTYPE_TEXTPASSWORD
                binding.ivShowPassword.setImageResource(R.drawable.ic_eye_close)
            }
            binding.edtPassWord.setSelection(
                binding.edtPassWord.text.toString().trim { it <= ' ' }.length
            )
            binding.edtPassWord.requestFocus()
            //When change input type, font reset automatically, so set font again.
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                binding.edtPassWord.typeface =
//                    resources.getFont(com.viettel.vht.main.R.font.gotham_book)
//            }
        }

        binding.ivShowConfirmPassword.setOnClickListener { v: View? ->
            if (binding.edtConfirmPassWord.inputType == Define.LOGIN.INPUTTYPE_TEXTPASSWORD) {
                binding.edtConfirmPassWord.inputType =
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.ivShowConfirmPassword.setImageResource(R.drawable.ic_eye_open)
            } else {
                binding.edtConfirmPassWord.inputType = Define.LOGIN.INPUTTYPE_TEXTPASSWORD
                binding.ivShowConfirmPassword.setImageResource(R.drawable.ic_eye_close)
            }
            binding.edtConfirmPassWord.setSelection(
                binding.edtConfirmPassWord.text.toString().trim { it <= ' ' }.length
            )
            binding.edtConfirmPassWord.requestFocus()
            //When change input type, font reset automatically, so set font again.
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                binding.edtConfirmPassWord.typeface =
//                    resources.getFont(com.viettel.vht.main.R.font.gotham_book)
//            }
        }

    }

    override fun bindingStateView() {
        super.bindingStateView()


    }


    private fun setPassWordCamera() {
        if (validatePassword(binding.edtPassWord.text.toString(), binding.edtConfirmPassWord.text.toString())) {
            setDataPassWordDevice()
        }
    /*else {
            showCustomNotificationDialog(
                title = getString(R.string.string_set_password_camera_iftech_error),
                type = DialogType.ERROR
            ) {

            }
        }*/

    }


    private fun isCheckPassword(): Boolean {
        val pwText = binding.edtPassWord.text.toString()
        val confirmPWText = binding.edtConfirmPassWord.text.toString()

        if (pwText.length < 8 || confirmPWText.length < 8) {
            return false
        }
        if (pwText.isEmpty()) {
            return false
        }
        if (confirmPWText.isEmpty()) {
            return false
        }
        if (!XUtils.contrast(pwText, confirmPWText)) {
            return false
        }
        return true
    }

    private fun validatePassword(
        password: String,
        passwordConfirm: String
    ): Boolean {
        when (password.validatepassword()) {
            EdtState.EDT_EMPTY -> {
                showAlert(com.vht.sdkcore.R.string.string_wrong_new_password_empty)
                return false
            }
            EdtState.EDT_LENGTH_INVALID -> {
                showAlert(com.vht.sdkcore.R.string.wrong_password_length)
                return false
            }
            EdtState.EDT_NOT_HALFWIDTH_OR_DIGIT -> {
                showAlert(com.vht.sdkcore.R.string.wrong_password_length)
                return false
            }
        }

        when (passwordConfirm.validatepassword()) {
            EdtState.EDT_EMPTY -> {
                showAlert(com.vht.sdkcore.R.string.wrong_password_confirm_empty)
                return false
            }
            EdtState.EDT_LENGTH_INVALID -> {
                showAlert(com.vht.sdkcore.R.string.wrong_password_length)
                return false
            }
            EdtState.EDT_NOT_HALFWIDTH_OR_DIGIT -> {
                showAlert(com.vht.sdkcore.R.string.wrong_password_length)
                return false
            }
        }

        if (password != passwordConfirm) {
            showAlert(com.vht.sdkcore.R.string.string_wrong_verification_code_empty)
            return false
        }

        return true
    }

    override fun OnFunSDKResult(msg: Message?, ex: MsgContent?): Int {
        msg?.let {
            showHideLoading(false)
            when (it.what) {
                EUIMSG.DEV_SET_JSON -> {
                    showHideLoading(false)
                    if (ex?.arg3 == 1660 || "ModifyPassword" == ex?.str) {
                        if (msg.arg1 < 0) {
                            toastMessage("Có lỗi xảy ra: ${msg.what}, ${msg.arg1}, ${ex.str}")
                            return 0
                        }
                        devToken = FunSDK.DevGetLocalEncToken(viewModel.devID)
                        if (!StringUtils.isStringNULL(devToken)) {
                            jsonToken["Admin"] = devToken
                        }
                        JFCameraManager.setPasswordCamera(viewModel.devID, binding.edtPassWord.text.toString())
                        toNextScreen()
                    }
                }

                EUIMSG.SYS_ADD_DEVICE -> {
                    if (viewModel.devID.isNotEmpty()) {
                        if (msg.arg1 < 0 && msg.arg1 != EFUN_ERROR.EE_ACCOUNT_DEVICE_ALREADY_EXSIT) {
                            // 如果已经存在了了，重置密码，登出   -99992  为本地登录时已存在设备时的错误码
                            if (EFUN_ERROR.EE_USER_NO_DEV == msg.arg1 || msg.arg1 == EFUN_ERROR.EE_OBJ_EXIST) {
                                FunSDK.SysSetDevMasterAccount(
                                    _msgId,
                                    viewModel.devID,
                                    FunSDK.GetFunStrAttr(EFUN_ATTR.LOGIN_USER_ID),
                                    0
                                )
                            } else {
                                //添加设备失败 尝试3次
                                if (ex!!.seq < 3) {
                                    if (DevDataCenter.getInstance()?.isLoginByAccount == true) {
                                        FunSDK.SysAddDevice(
                                            _msgId,
                                            G.ObjToBytes(viewModel.devInfo?.sdbDevInfo),
                                            "ma=true&delOth=true",
                                            if (StringUtils.isStringNULL(devToken)) "" else jsonToken.toJSONString(),
                                            ex.seq + 1
                                        )
                                    } else {
                                        FunSDK.SysAddDevice(
                                            _msgId,
                                            G.ObjToBytes(viewModel.devInfo?.sdbDevInfo),
                                            "",
                                            if (StringUtils.isStringNULL(devToken)) "" else jsonToken.toJSONString(),
                                            ex.seq + 1
                                        )
                                    }
                                } else {
                                    toastMessage("Có lỗi xảy ra: ${msg.what}, ${msg.arg1}, ${ex?.str}")
                                    Timber.d("Có lỗi xảy ra: ${msg.what}, ${msg.arg1}, ${ex?.str}")
                                    DevDataCenter.getInstance()?.removeDev(viewModel.devID)
                                }
                                return 0
                            }
                        } else if (msg.arg1 == EFUN_ERROR.EE_ACCOUNT_DEVICE_ALREADY_EXSIT
                            || viewModel.devInfo?.sdbDevInfo != null) {
                            FunSDK.SysSetDevMasterAccount(
                                _msgId,
                                viewModel.devID,
                                FunSDK.GetFunStrAttr(EFUN_ATTR.LOGIN_USER_ID),
                                0
                            )
                            if (msg.arg1 != EFUN_ERROR.EE_ACCOUNT_DEVICE_ALREADY_EXSIT) {
                                FunSDK.SysModifyCacheDevInfo(
                                    viewModel.devID,
                                    System.currentTimeMillis() / 1000,
                                    0,
                                    0,
                                    ""
                                )
                            }
                        } else {
                            FunSDK.SysModifyCacheDevInfo(
                                viewModel.devID,
                                System.currentTimeMillis() / 1000,
                                0,
                                0,
                                ""
                            )
                        }
//                        DataCenter.getInstance().setCurDevId(devId);
//                        IdrDefine.setNotShowCall(devId);
//                        EventBus.getDefault().post(
//                            MessageEvent(
//                                MessageEvent.ADD_DEVICE,
//                                devId,
//                                mDeviceInfo.st_7_nType
//                            )
//                        )
//                        EventBus.getDefault().post(
//                            MessageEvent(
//                                MessageEvent.REFRESH_DEVICE,
//                                devId,
//                                mDeviceInfo.st_7_nType
//                            )
//                        )
                        syncDevTimeZone(_msgId, viewModel.devID)
                        syncDevTime(_msgId, viewModel.devID)
                        toNextScreen()
                    }

                }

                EUIMSG.DEV_CONFIG_JSON_NOT_LOGIN -> {

                    if(msg.arg1 < 0) {
                        toastMessage("Có lỗi xảy ra: ${msg.what}, ${msg.arg1}, ${ex?.str}")
                        Timber.d("Có lỗi xảy ra: ${msg.what}, ${msg.arg1}, ${ex?.str}")
                        return 0
                    }
                    if (StringUtils.contrast(ex?.str, "ChangeRandomUser")) {


                        // 用户名修改好之后，跳转到下个页面
                        JFCameraManager.setPasswordCamera(
                            viewModel.devID,
                            binding.edtPassWord.text.toString()
                        )
                        FunSDK.DevLogout(
                            _msgId,
                            viewModel.devID,
                            0
                        ) //设置完随机用户名密码之后登出设备

                        FunSDK.DevGetConfigByJson(
                            _msgId,
                            viewModel.devID,
                            JsonConfig.SYSTEM_INFO,  //登出之后在登入设备获取为了获取设备token;
                            1024,
                            -1,
                            GET_SET_DEV_CONFIG_TIMEOUT,
                            0
                        )
                        return 0
                    }

                    val json = G.ToString(ex?.pData)

                    if (!XUtils.isEmpty(json)) {
                        try {
                            val jsonObject = JSON.parseObject(json)
                            if (jsonObject != null) {
                                val randomUser = jsonObject.getJSONObject("GetRandomUser")
                                if (randomUser != null) {
                                    val info = randomUser.getString("Info")
                                    if (!XUtils.isSn(viewModel.devID)) {
                                        return 0
                                    }
                                    // 解密
                                    val tempUserPwd =
                                        FunSDK.DecDevRandomUserInfo(viewModel.devID, info)
                                    if (!XUtils.isEmpty(tempUserPwd)) {
                                        val split = tempUserPwd.split(":".toRegex()).toTypedArray()
                                        if (split.size >= 3) {
                                            mTempUser = split[1].substring(0, 4)
                                            mTempPwd = split[2].substring(0, 6)
                                        }
                                        if (!TextUtils.isEmpty(mTempUser) && !TextUtils.isEmpty(mTempPwd)) {
                                            JFCameraManager.setPasswordCamera(
                                                viewModel.devID,
                                                mTempPwd!!
                                            )
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                }
                EUIMSG.DEV_GET_JSON -> {
                    if (JsonConfig.SYSTEM_INFO == ex?.str) {
                        if (!XUtils.isSn(viewModel.devID)) {
                            return 0
                        }
                        if (msg.arg1 >= 0) {
                            devToken = FunSDK.DevGetLocalEncToken(viewModel.devID)
                            if (!TextUtils.isEmpty(mTempUser) && !TextUtils.isEmpty(mTempPwd)) {
                                addDevice()
                            } else {
                                EventBus.getDefault().post(RxEvent(EVENT_SYSTEM_CHANGE_INFORMATION_CAMERA_JF, viewModel.devID))
                                toNextScreen()
                            }
                        } else if (msg.arg1 == EE_DVR_PASSWORD_NOT_VALID
                            || msg.arg1 == EFUN_ERROR.EE_DVR_PASSWORD_NOT_VALID2
                        ) {
                            //随机用户名和密码的设备正常情况下不会返回密码错误，这里不用判断
//                            val intent: Intent = Intent(this, InputDevicePsdActivity::class.java)
//                            intent.putExtra("devId", this.devId)
//                            startActivity(intent)
                            toastMessage("Có lỗi xảy ra: ${msg.what}, ${msg.arg1}, ${ex?.str}")
                            Timber.d("Có lỗi xảy ra: ${msg.what}, ${msg.arg1}, ${ex?.str}")
                        } else {

                            //随机用户名密码时，必须设置用户名和密码后才能添加设备, 获取配置失败直接提示错误
                            toastMessage("Có lỗi xảy ra: ${msg.what}, ${msg.arg1}, ${ex?.str}")
                            Timber.d("Có lỗi xảy ra: ${msg.what}, ${msg.arg1}, ${ex?.str}")
//                            ErrorManager.Instance().ShowError(msg.what, msg.arg1, ex!!.str, true)
                        }
                    }
                }
                else -> {

                }
            }
        }
        return 0
    }


    // Đồng bộ hóa thời gian thiết bị
    private fun syncDevTime(userID: Int, devSN: String?) {
        val c = Calendar.getInstance(Locale.getDefault())
        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(c.time)
        FunSDK.DevSetConfigByJson(
            userID,
            devSN,
            JsonConfig.OPTIME_SET,
            HandleConfigData.getSendData(JsonConfig.OPTIME_SET, "0x00000001", time),
            -1,
            GET_SET_DEV_CONFIG_TIMEOUT,
            0
        )
    }

    /*
     * cài đặt múi giờ
     */
    private fun setTimeZone(): Int {
        val cal = Calendar.getInstance(Locale.getDefault())
        val zoneOffset = cal[Calendar.ZONE_OFFSET].toFloat()
        val zone = (zoneOffset / 60.0 / 60.0 / 1000.0).toFloat() // 时区，东时区数字为正，西时区为负
        return (-zone * 60).toInt()
    }

    private fun syncDevTimeZone(userID: Int, devSN: String?) {
        val timeZoneBean = TimeZoneBean()
        timeZoneBean.timeMin = setTimeZone()
        timeZoneBean.FirstUserTimeZone = 0
        FunSDK.DevSetConfigByJson(
            userID,
            devSN,
            JsonConfig.SYSTEM_TIMEZONE,
            HandleConfigData.getSendData(JsonConfig.SYSTEM_TIMEZONE, "0x1", timeZoneBean),
            -1,
            GET_SET_DEV_CONFIG_TIMEOUT,
            0
        )
    }

    private fun addDevice() {
        G.SetValue(viewModel.devInfo?.sdbDevInfo?.st_4_loginName, binding.tvIdCamera.text.toString())
        G.SetValue(viewModel.devInfo?.sdbDevInfo?.st_5_loginPsw, binding.edtPassWord.text.toString())
        if (DevDataCenter.getInstance()?.isLoginByAccount == true) {
            FunSDK.SysAddDevice(
                _msgId,
                G.ObjToBytes(viewModel.devInfo?.sdbDevInfo),
                "ma=true&delOth=true",
                if (StringUtils.isStringNULL(devToken)) "" else jsonToken.toJSONString(),
                0
            )
        } else {
            FunSDK.SysAddDevice(
                _msgId,
                G.ObjToBytes(viewModel.devInfo?.sdbDevInfo),
                "",
                if (StringUtils.isStringNULL(devToken)) "" else jsonToken.toJSONString(),
                0
            )
        }
    }

    private fun toNextScreen() {
        showCustomToast(
            resTitle = com.vht.sdkcore.R.string.string_set_password_camera_iftech,
            onFinish = {
                val bundle = Bundle().apply {
                    putSerializable(Define.BUNDLE_KEY.PARAM_DEVICE_ITEM, device)
                }
                sdkManager.vHomeSDKAddCameraJFListener?.onSuccess(device)
              //  appNavigation.openSetDeviceNameCameraJF(bundle)
                requireActivity().finish()
            },
            showImage = true
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        FunSDK.UnRegUser(_msgId)
    }

    private fun setDataPassWordDevice() {
        showHideLoading(true)
        val xmDevInfo = DevDataCenter.getInstance()?.getDevInfo(viewModel.devID)
        if (xmDevInfo != null) {
            Timber.d("devPassword: ${xmDevInfo.devName}")
            Timber.d("devPassword: ${xmDevInfo.devPassword}")
            val deviceInfo = xmDevInfo.sdbDevInfo
            Timber.d("hasPermissionModifyPwd: ${deviceInfo.hasPermissionModifyPwd()}")
            if (deviceInfo != null) {
                var loginName = G.ToString(deviceInfo.st_4_loginName)
                val oldPass = xmDevInfo.devPassword
                val newPass = binding.edtPassWord.text.toString()
                Timber.d("xmDevInfo: ${Gson().toJson(xmDevInfo)}")
                Timber.d("loginName: $loginName")
                Timber.d("oldPass: $oldPass")
                Timber.d("newPass: $newPass")
                if (TextUtils.isEmpty(loginName)) {
                    loginName = "admin"
                }
                if (!TextUtils.isEmpty(mTempUser) && !TextUtils.isEmpty(mTempPwd)) {

                    // If it is displayed, it means that the process of random username and password is required
                    // modify username and password
                    val json = JSONObject()
                    try {

                        val innerJson = JSONObject()
                        innerJson.put("RandomName",loginName)
                        innerJson.put("RandomPwd", FunSDK.DevGetLocalPwd(viewModel.devID))
                        innerJson.put("NewName", binding.tvIdCamera.text)
                        innerJson.put("NewPwd", newPass)
                        json.put("ChangeRandomUser", innerJson)
                        json.put("Name", "ChangeRandomUser")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    FunSDK.DevConfigJsonNotLoginPtl(
                        _msgId,
                        viewModel.devID,
                        "ChangeRandomUser",
                        json.toString(),
                        1660,
                        -1,
                        0,
                        CHANGE_RANDOM_PASSWORD_TIMEOUT,
                        0,
                        SDKCONST.Switch.Open
                    )
                } else {
                    DeviceManager.getInstance()?.modifyDevPwd(
                        viewModel.devID,
                        loginName,
                        oldPass,
                        newPass,
                        object : OnDevManagerListener<Any?> {
                            override fun onSuccess(devId: String, operationType: Int, result: Any?) {
                                showHideLoading(false)
                                JFCameraManager.setPasswordCamera(
                                    viewModel.devID,
                                    binding.edtPassWord.text.toString()
                                )
                                if (!TextUtils.isEmpty(mTempUser) && !TextUtils.isEmpty(mTempPwd)) {
                                    addDevice()
                                } else {
                                    EventBus.getDefault().post(RxEvent(EVENT_SYSTEM_CHANGE_INFORMATION_CAMERA_JF, viewModel.devID))
                                    toNextScreen()
                                }
                            }

                            override fun onFailed(devId: String, msgId: Int, s1: String, errorId: Int) {
                                showHideLoading(false)
                                Timber.e("Error: $devId - $msgId - $s1 - $errorId")
                                if (errorId == EE_DVR_PASSWORD_NOT_VALID) {
//                            JFCameraManager.setPasswordCamera(viewModel.devID, binding.edtPassWord.text.toString())
                                    showCustomToast("Mật khẩu của bạn nhập không đúng. Hãy sử dụng mật khẩu đã được thiết lập.") {
                                        val bundle = Bundle().apply {
                                            putSerializable(Define.BUNDLE_KEY.PARAM_DEVICE_ITEM, device)
                                        }
                                      //  appNavigation.openSetDeviceNameCameraJF(bundle)
                                        sdkManager.vHomeSDKAddCameraJFListener?.onSuccess(device)
                                        requireActivity().finish()
                                    }
//                            setNewPasswordDevice()
//                            verifyCodeDevice()
                                    return
                                }
                                toastMessage("Có lỗi xảy ra: ${msgId}, ${s1}, ${errorId}")
                            }

                        })
                }
            }

        }
    }

}
