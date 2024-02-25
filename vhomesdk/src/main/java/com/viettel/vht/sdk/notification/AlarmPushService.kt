package com.viettel.vht.sdk.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.basic.G
//import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.lib.EUIMSG
import com.lib.FunSDK
import com.lib.IFunSDKResult
import com.lib.Mps.SMCInitInfo
import com.lib.MsgContent
import com.lib.sdk.bean.StringUtils
import com.lib.sdk.bean.alarm.AlarmInfo
import com.lib.sdk.bean.alarm.AlarmInfo.LinkCenterExt
import com.lib.sdk.struct.SDBDeviceInfo
import com.manager.account.XMAccountManager
import com.manager.db.Define.LOGIN_NONE
import com.manager.db.DevDataCenter
import com.manager.device.idr.IDRSleepService
import com.manager.device.idr.IDRSleepService.KEEP_NOTIFICATION_ID
import com.manager.device.idr.IDRStateResult
import com.manager.device.idr.IdrDefine
import com.manager.path.PathManager
import com.manager.push.XMPushManager
import com.manager.push.XMPushManager.LINK_COUNT
import com.manager.push.XMPushManager.PUSH_TYPE_GOOGLE
import com.manager.push.XMPushManager.PUSH_TYPE_XM
import com.manager.push.XMPushManager.TYPE_DOOR_BELL
import com.manager.push.XMPushManager.TYPE_FORCE_DISMANTLE
import com.manager.push.XMPushManager.TYPE_INTERVAL_WAKE
import com.manager.push.XMPushManager.TYPE_LOCAL_ALARM
import com.manager.push.XMPushManager.TYPE_REMOTE_CALL_ALARM
import com.manager.push.XMPushManager.UNINIT_PUSH
import com.manager.push.XMPushManager.getAlarmName
import com.manager.sysability.SysAbilityManager
import com.utils.BaseThreadPool
import com.utils.XUtils
import com.vht.sdkcore.pref.RxPreferences
import com.vht.sdkcore.utils.Constants.EventKey.EVENT_SYSTEM_CHANGE_INFORMATION_CAMERA_JF
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.eventbus.RxEvent
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.eventbus.EventBusPushInfo
import com.viettel.vht.sdk.jfmanager.JFCameraManager
import com.viettel.vht.sdk.model.jfcall.IDRCallResult
import com.viettel.vht.sdk.model.jfcall.WeakUp
import com.viettel.vht.sdk.ui.jfcameradetail.call.IDRVisitorActivity
import com.viettel.vht.sdk.utils.Config
import com.viettel.vht.sdk.utils.Config.EventKey.EVENT_RELOAD_LIST_EVENT
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.io.File
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Queue
import java.util.UUID
import java.util.Vector
import java.util.concurrent.LinkedBlockingQueue
import javax.inject.Inject

@AndroidEntryPoint
class AlarmPushService : Service(), XMPushManager.OnXMPushLinkListener, IFunSDKResult {

    @Inject
    lateinit var rxPreferences: RxPreferences

    private val SHOW_ALARM_NOTIFICATION_INTERVAL = 5 * 60000
    private val IGNORE_ALARM_CALLER_TIMES = 5 * 60000


    //置于前台 每隔1*5分钟刷新一次
    private val PUSH_DEVICE_REFRESH_DEFUALT_TIME = 5 * 60

    //置于后台 每隔10分钟刷新一次
    private val PUSH_DEVICE_REFRESH_BACKGROUND_TIME = 600

    private val DEFAULT_USERNAME_AND_PWD = "xworld"
    val NOTIFICATIN_CHANNEL_ID = "AlarmPush"
    private val NOTIFICATION_BACKGROUND_CHANNEL_ID = "Background"

    var PATH_PHOTO_TEMP: String? = ""
    private var mUserName = ""

    private var mPassWord = ""
    private var userId = -1
    private var mPushDeviceRefreshTime: Int = PUSH_DEVICE_REFRESH_DEFUALT_TIME
    private var isAppInBackground = false
    private var isDestroy = false

    private var isShowToast = false
    private var mDeviceList: ArrayList<SDBDeviceInfo?> = arrayListOf()
    private var baseVector: Vector<SDBDeviceInfo?> = Vector()
    private var mOtherPushInfos = HashMap<String, AlarmInfo>()
    private var mAlarmIdQueue: Queue<String>? = null

    private var mEventBusPushInfo: HashMap<String, EventBusPushInfo>? = null
    private var manager: NotificationManager? = null

    private var builder: NotificationCompat.Builder? = null
    private var mXMPushManager: XMPushManager? = null


    override fun onCreate() {
        super.onCreate()
        PATH_PHOTO_TEMP = PathManager.getInstance(this)?.tempImages
        Timber.d("PATH_PHOTO_TEMP: $PATH_PHOTO_TEMP")
        backgroundKeepLive()
        val loginType: Int? = DevDataCenter.getInstance()?.loginType
        if (loginType == LOGIN_NONE) {
            stopSelf()
        } else {
            initData()
        }

//        Toast.makeText(this, FunSDK.TS("TR_Start_Push_Service"), Toast.LENGTH_LONG).show()
        Timber.i("Launch push notification service")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            backgroundKeepLive()
            val loginType: Int? = DevDataCenter.getInstance()?.loginType
            if (loginType == LOGIN_NONE) {
                stopSelf()
            } else {
                isAppInBackground = intent.getBooleanExtra("isAppInBackground", false)
                if (isAppInBackground) {
                    //如果APP置于后台 就10分钟检测一次
                    mPushDeviceRefreshTime = PUSH_DEVICE_REFRESH_BACKGROUND_TIME
                    bindDeviceAndUnlinkAbnormalAlarm()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        isDestroy = true
        DevDataCenter.getInstance()?.pushType = UNINIT_PUSH
        BaseThreadPool.getInstance()?.cancelTask()

        //User đã logout
        if (TextUtils.isEmpty(rxPreferences.getUserToken())) {
            XMPushManager(this).unLinkAllAlarm()
        }

        if (userId >= 0) {
            FunSDK.UnRegUser(userId)
            userId = -1
        }
        if (mXMPushManager != null) {
            mXMPushManager?.unInit()
        }

        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    private fun initData() {
        EventBus.getDefault().register(this)
        mAlarmIdQueue = LinkedBlockingQueue(100)
        userId = FunSDK.GetId(userId, this)
        manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATIN_CHANNEL_ID,
                "Push Notification Camera JF", NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.enableLights(true)
            notificationChannel.enableVibration(true)
            manager?.createNotificationChannel(notificationChannel)
        }
        mXMPushManager = XMPushManager(this)
        mUserName = XMAccountManager.getInstance().accountName
        mPassWord = XMAccountManager.getInstance().password
        val listDevId = XMAccountManager.getInstance()?.devList
        if (listDevId != null && listDevId.isNotEmpty()) {
            Timber.d("initData_listDevId: $listDevId")
            listDevId.forEach {
                mDeviceList.add(XMAccountManager.getInstance()?.getDevInfo(it)?.sdbDevInfo)
            }
        }
        if (mDeviceList.isNotEmpty()) {
            initPushList()
//            initVectorData()
        }

      //  initGooglePush()
    }

    private fun initPush() {

    }

    private fun initPushList() {
        Timber.d("initPushList")
        baseVector.removeAllElements()
        try {
            synchronized(mDeviceList) {
                for (info in mDeviceList) {
                    if (info == null || !info.hasPermissionAlarmPush()) {
                        continue
                    }
                    if (!baseVector.contains(info)) {
                        baseVector.addElement(info)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initGooglePush() {
        Timber.d("initGooglePush")
        val info = SMCInitInfo()
        G.SetValue(info.st_0_user, mUserName)
        G.SetValue(info.st_1_password, mPassWord)
//        try {
//            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
//                if (!task.isSuccessful) {
//                    Timber.w("getInstanceId failed ${task.exception}")
//                    return@addOnCompleteListener
//                }
//                // Get new getInstance ID token
//                val pushToken = task.result
//                if (pushToken != null) {
//                    //google could message
//                    Timber.d("google could message ---token: $pushToken")
//                    G.SetValue(info.st_2_token, pushToken)
//                    G.SetValue(info.st_5_appType, "GoogleV2:" + this@AlarmPushService.packageName)
//                    initFunSDKPush(info, PUSH_TYPE_GOOGLE)
//                    return@addOnCompleteListener
//                }
//            }
//        } catch (e: Exception) {
//            Timber.e("initGooglePush_error: ${e.message}")
//        }

    }

    private fun initFunSDKPush(info: SMCInitInfo, pushType: Int) {
        Timber.d("initFunSDKPush")
        var info: SMCInitInfo? = info
        if (null != info) {
            var token: String = rxPreferences.getPushTokeJFTech()
            if (TextUtils.isEmpty(token)) {
                token = XUtils.getPushToken(this)
                if (!TextUtils.isEmpty(token)) {
                    rxPreferences.setPushTokenJFTech(token)
                }
            }
            if (!StringUtils.isStringNULL(token)) {
                val tokens = StringBuffer(G.ToString(info.st_2_token))
                tokens.append("&&")
                tokens.append(token)
                G.SetValue(info.st_2_token, tokens.toString())
                val appTypes = StringBuffer(G.ToString(info.st_5_appType))
                appTypes.append("&&")
                appTypes.append("Android")
                G.SetValue(info.st_5_appType, appTypes.toString())
                Timber.d("tokens:$tokens")
                Timber.d("appTypes:$appTypes")
            }
        } else {
            info = SMCInitInfo()
            G.SetValue(info.st_0_user, mUserName)
            G.SetValue(info.st_1_password, mPassWord)
            var pushToken: String = rxPreferences.getPushTokeJFTech()
            if (TextUtils.isEmpty(pushToken)) {
                pushToken = XUtils.getPushToken(this)
                if (!TextUtils.isEmpty(pushToken)) {
                    rxPreferences.setPushTokenJFTech(pushToken)
                }
            }
            if (!StringUtils.isStringNULL(pushToken)) {
                G.SetValue(info.st_2_token, pushToken)
            }
        }
        mXMPushManager?.initFunSDKPush(this, info, pushType)
        bindDeviceAndUnlinkAbnormalAlarm()
    }

    private fun bindDeviceAndUnlinkAbnormalAlarm() {
        Timber.i("bindDeviceAndUnlinkAbnormalAlarm")
        bindDevice()
    }

    private fun bindDevice() {
        if (isDestroy || DevDataCenter.getInstance()?.pushType == UNINIT_PUSH) {
            return
        }
        Timber.d("bindDevice")
        BaseThreadPool.getInstance().cancelTask()
        BaseThreadPool.getInstance().doTask({
            if (baseVector.isEmpty() || mDeviceList.size != XMAccountManager.getInstance()?.devList?.size) {
                baseVector.clear()
                if (mDeviceList != null) {
                    val listDevId = XMAccountManager.getInstance()?.devList
                    val devList: ArrayList<SDBDeviceInfo?> = arrayListOf()
                    if (listDevId != null && listDevId.isNotEmpty()) {
                        listDevId.forEach {
                            devList.add(XMAccountManager.getInstance()?.getDevInfo(it)?.sdbDevInfo)
                        }
                    }
                    if (mDeviceList != devList) {
                        mDeviceList.clear()
                        mDeviceList.addAll(devList)
                    }
                    initPushList()
                } else {
                    return@doTask
                }
            }
            var info: SDBDeviceInfo? = null
            var linkAllAlarmSns = arrayOfNulls<String>(baseVector.size)
//            var linkAllAlarmDevNames = StringBuffer()
            var bindCount = 0
            for (i in baseVector.indices) {
                if (isDestroy || DevDataCenter.getInstance()?.pushType == UNINIT_PUSH) {
                    Timber.i("AlarmService is destroy or pushType is uninit")
                    return@doTask
                }
//                info = baseVector[i]000000000000000002222222
                info = XMAccountManager.getInstance()?.getDevInfo(baseVector[i]?.sn)?.sdbDevInfo
                val isDevOnline = XMAccountManager.getInstance()?.getDevState(info?.sn) == 1
                Timber.d("deviceName: ${Gson().toJson(info?.devName)}")
                Timber.d("deviceIsOnline: $isDevOnline")

                //如果设备不在线的话就不要去订阅了,分享的设备没有推送权限也不去订阅
//                val sdbDeviceInfo: SDBDeviceInfo = info.getInfo()

                if (info == null || !isDevOnline || !info.hasPermissionAlarmPush()) {
                    System.out.println("is offline:" + info?.sn)
                    Timber.i("The dev[${info?.sn}] is offline")
                    continue
                }
                var isHavePermission = true
                if (JFCameraManager.listDeviceShared != null && JFCameraManager.listDeviceShared?.isNotEmpty() == true) {
                    JFCameraManager.listDeviceShared?.forEach { deviceInfo ->
                        if (info.sn == deviceInfo.getSerialNumber()) {
                            if (deviceInfo.permission?.contains(Config.FEATURE_EVENT_JF) == false) {
                                Timber.i("The dev shared[${info?.sn}] is no permission")
                                isHavePermission = false
                                return@forEach
                            }
                        }
                    }
                }
//                if (!isHavePermission) {
//                    continue
//                }
                if (isHavePermission) {
                    linkAllAlarmSns[i] = info.sn
                }
//                linkAllAlarmSns.append(";")
//                linkAllAlarmDevNames.append(G.ToString(info.st_1_Devname))
//                linkAllAlarmDevNames.append(";")

                //批量订阅超过200条需要分批发送
                if (++bindCount >= LINK_COUNT) {
                    mXMPushManager?.linkAllAlarm(linkAllAlarmSns)
                    linkAllAlarmSns = arrayOfNulls(baseVector.size)
//                    linkAllAlarmDevNames = StringBuffer()
                    bindCount = 0
                }
            }
            Timber.d("linkAllAlarmSns: ${Gson().toJson(linkAllAlarmSns)}")
            if (linkAllAlarmSns.isNotEmpty()) {
                mXMPushManager?.linkAllAlarm(linkAllAlarmSns)
            }
        }, 0, mPushDeviceRefreshTime.toLong())
    }

//    private fun initVectorData() {
//        try {
//            for (i in baseVector.indices) {
//                val info: SDBDeviceInfo = baseVector[i]
//                if (!info.isPushEnable(this, Define.IS_PUSH_DEFAULT)) {
//                    info.setPushState(PushDeviceInfo.STATE_PUSH_NEED_TO_CLOSE)
//                }
//            }
//        } catch (e: java.lang.Exception) {
//            e.printStackTrace()
//        }
//    }

    /**
     * 退到后台 保活
     * 支持第三方推送的话，就不调用此方法
     */
    private fun backgroundKeepLive() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val channel = NotificationChannel(
                    NOTIFICATION_BACKGROUND_CHANNEL_ID,
                    "Background Push Service", NotificationManager.IMPORTANCE_DEFAULT
                )
                val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                if (manager != null) {
                    manager.createNotificationChannel(channel)
                    val builder = NotificationCompat.Builder(
                        application, NOTIFICATION_BACKGROUND_CHANNEL_ID
                    )
                    builder.setStyle(NotificationCompat.BigTextStyle())
                    startPushAliveService(builder.notification)
                    startService(Intent(this, IDRSleepService.InnerService::class.java))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            try {
                val builder = NotificationCompat.Builder(
                    application
                )
                builder.setStyle(NotificationCompat.BigTextStyle())
                startPushAliveService(builder.build())
                startService(Intent(this, IDRSleepService.InnerService::class.java))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startPushAliveService(notification: Notification) {
        startForeground(KEEP_NOTIFICATION_ID, notification)
        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }


    override fun onPushInit(p0: Int, p1: Int) {
        //TODO
    }

    override fun onPushLink(p0: Int, p1: String?, p2: Int, p3: Int) {
        //TODO
    }

    override fun onPushUnLink(p0: Int, p1: String?, p2: Int, p3: Int) {
        //TODO
    }

    override fun onIsPushLinkedFromServer(p0: Int, p1: String?, p2: Boolean) {
        //TODO
    }

    override fun onAlarmInfo(p0: Int, p1: String?, p2: Message?, p3: MsgContent?) {
        //TODO
    }

    override fun onLinkDisconnect(p0: Int, p1: String?) {
        //TODO
    }

    override fun onWeChatState(p0: String?, p1: Int, p2: Int) {
        //TODO
    }

    override fun onThirdPushState(p0: String?, p1: Int, p2: Int, p3: Int) {
        //TODO
    }

    override fun onAllUnLinkResult() {
        clearPush()
    }

    private fun clearPush() {
        Thread { // TODO
//                unBindAllDevices();
            val loginType: Int? = DevDataCenter.getInstance()?.loginType
            if (loginType == LOGIN_NONE) {
                unBindAllDevicesAndDeleteFromDB()
            }
        }.start()
    }

    private fun unBindAllDevicesAndDeleteFromDB() {
        if (baseVector.isEmpty() || null == mXMPushManager) {
            return
        }
        for (i in baseVector.indices) {
            val info: SDBDeviceInfo = baseVector[i] ?: continue
            mXMPushManager?.unLinkAlarmAbnoraml(info.sn, i)
        }
        baseVector.clear()
    }

    override fun onFunSDKResult(msg: Message?, ex: MsgContent?) {
        Timber.d("onFunSDKResult: ${msg?.what} - ${msg?.arg1}")
        Timber.d("onFunSDKResult_MsgContent: ${ex?.str} - ${ex?.toString()}")

        if (JFCameraManager.isLogout) {
            Timber.d("AlarmPushService stop self")
            this.stopSelf()
        }

        var info: SDBDeviceInfo? = null
        when (msg?.what) {
            EUIMSG.MC_ON_PictureCb, EUIMSG.MC_ON_AlarmCb -> {
                val alarmInfo = AlarmInfo()
                alarmInfo.onParse(G.ToString(ex?.pData))
                if (isQueueContains(alarmInfo.id)) {
                    return
                }

                dealWithReceiveAlarm(
                    ex?.str, G.ToString(
                        ex?.pData
                    ), ex?.seq == 1
                )
            }

//            EUIMSG.MC_ON_INIT -> if (ex?.seq != PUSH_TYPE_XM) {
//                if (!JFCameraManager.isUpdateDevStateCompleted) {
//                    return
//                }
//                initPushList()
////                initVectorData()
//                bindDeviceAndUnlinkAbnormalAlarm()
//            }

            EUIMSG.MC_ON_LinkDisCb -> {
                Timber.d("MC_ON_LinkDisCb")
                unBindDevice(ex?.str)
            }

            EUIMSG.MC_UnlinkDev -> {
                Timber.d("MC_UnlinkDev_${ex?.str}")
                dealWithEventBusPushInfo(ex?.str, msg.arg1 >= 0)
                if (msg.arg1 < 0 || ex!!.seq < 0) {
                    return
                }
                if (baseVector.isNotEmpty() && baseVector.size > ex.seq) {
                    info = baseVector[ex.seq]
                    if (info != null) {
                        Timber.d("AlarmPush Dev[${info.sn}] is Close")

//                        info.setPushState(PushDeviceInfo.STATE_PUSH_CLOSE)
                    }
                }
            }

            EUIMSG.MC_LinkDev -> {
                dealWithEventBusPushInfo(ex?.str, msg.arg1 >= 0)
                if (msg.arg1 < 0) {
                    //谷歌推送服务在中国区要切换到本地推送服务
//                    if (msg?.arg1 == EFUN_ERROR.EE_ALARM_NOT_SUPPORTED) {
//                        initLocalPush()
//                    }
                    return
                }
                if (baseVector.isNotEmpty() && baseVector.size > ex!!.seq) {
                    info = baseVector[ex.seq]
                    if (info != null) {
                        Timber.d("AlarmPush Dev[${info.sn}] is Open")
//                        info.setPushState(PushDeviceInfo.STATE_PUSH_OPEN)
//                        if (info.getInfo() != null) {
//                            if (IdrDefine.isIDR(info.getInfo().st_7_nType)) {
//                                SPUtil.getInstance(applicationContext)
//                                    .setSettingParam(Define.DEVICE_PUSH_PREFIX + info.getSn(), true)
//                                SPUtil.getInstance(applicationContext)
//                                    .setSettingParam(
//                                        Define.DEVICE_SUBSCRIBE_STATUS_PREFIX + info.getSn(),
//                                        2
//                                    )
//                            }
//                        }
                    }
                }
            }

            EUIMSG.MC_LinkDevs_Batch -> if (msg?.arg1!! >= 0) {
//                dealWithLinkDevsBatch(ex!!.str)
            }

            EUIMSG.MC_UnLinkDevs_Batch -> if (msg?.arg1!! >= 0) {
//                dealWithUnlinkDevsBatch(ex!!.str)
            }

            EUIMSG.SYS_GET_DEV_INFO_BY_USER -> {
                Timber.d("SYS_GET_DEV_INFO_BY_USER")
                if (ex?.pData == null || ex.pData.isEmpty()) {
                    return
                }
                mDeviceList.clear()
                val info = SDBDeviceInfo()
                val nItemLen = G.Sizeof(info)
                val nCount: Int = ex.pData.size / nItemLen
                val infos = arrayOfNulls<SDBDeviceInfo>(nCount)
                for (i in 0 until nCount) {
                    infos[i] = SDBDeviceInfo()
                }
                G.BytesToObj(infos, ex.pData)
                for (i in 0 until nCount) {
                    // 名称转成正常的UTF-8格式，修复保存密码时，中文名称变乱码的BUG
                    val name = G.ToString(infos[i]?.st_1_Devname)
                    try {
                        G.SetValue(infos[i]?.st_1_Devname, name.toByteArray(charset("UTF-8")))
                    } catch (e: UnsupportedEncodingException) {
                        e.printStackTrace()
                    }
                    mDeviceList.add(infos[i])
                }
                initPushList()
//                initVectorData()
                bindDeviceAndUnlinkAbnormalAlarm()
            }

            else -> {}
        }

    }


    private fun dealWithEventBusPushInfo(devSn: String?, isSuccess: Boolean) {
        Timber.d("dealWithEventBusPushInfo: $devSn")
        if (mEventBusPushInfo != null && mEventBusPushInfo!!.containsKey(devSn)) {
            val eventBusPushInfo = mEventBusPushInfo!![devSn]
            if (eventBusPushInfo != null && eventBusPushInfo.listener != null) {
                eventBusPushInfo.listener.onResult(isSuccess)
            }
            mEventBusPushInfo!!.remove(devSn)
        }
    }

    /**
     * Handle alarm accept data
     *
     * @param devSn       序列号
     * @param jsonData    json数据
     * @param isOtherPush 是否为第三方推送（XM推送和Google推送除外）
     */
    private fun dealWithReceiveAlarm(devSn: String?, jsonData: String, isOtherPush: Boolean) {
        Timber.d("dealWithReceiveAlarm")
        try {
            if (!DevDataCenter.getInstance().isDevExist(devSn)) {
                if (XUtils.isSn(devSn)) {
                    mXMPushManager?.unLinkAlarmAbnoraml(devSn, -1)
                    Timber.d("Alarm No device exists_$devSn")
                } else {
                    dealWithNotifyPushInfo(jsonData)
                }
                return
            }
            /****SPUtil.getInstance(this).getSettingParam(Define.DEVICE_PUSH_PREFIX + sn, false)
             * 如果返回值为true，则为打开消息免打扰，则不显示报警通知
             * 如果返回false，则不进行推送，则免打扰为打开
             */
            val deviceInfo = XMAccountManager.getInstance().getDevInfo(devSn).sdbDevInfo
            if (!deviceInfo.hasPermissionAlarmPush()) {
                Timber.d("Alarm_Do not push$devSn")
                return
            }
//            XMPushManager.setHaveNewPushMsg(this, devSn, true)
            Timber.d("to push = $jsonData")
            val alarmInfo = AlarmInfo()
            alarmInfo.onParse(jsonData)
            if (TextUtils.isEmpty(alarmInfo.id)) {
                return
            }

            //If it is a message push that wakes up at intervals, the device status needs to be updated
            if (IdrDefine.isIDR(deviceInfo.st_7_nType)
                && StringUtils.contrast(alarmInfo.event, TYPE_INTERVAL_WAKE)
            ) {
                dealWithIntervalWakeAlarm(deviceInfo.sn, alarmInfo)
            }
            if (isOtherPush) {
                dealWithOtherPushAlarm(deviceInfo, alarmInfo)
            } else {
                dealWithNoMsgShowTip()
                dealWithPushMsg(alarmInfo, deviceInfo)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Handle notification bar messages (for example: advertisements)
     *
     * @param jsonData
     */
    private fun dealWithNotifyPushInfo(jsonData: String) {
        if (StringUtils.isStringNULL(jsonData)) {
            return
        }
        Timber.d("dealWithNotifyPushInfo: $jsonData")
        val jsonObject: JSONObject? = JSON.parseObject(jsonData)
        if (jsonObject != null && jsonObject.containsKey("CustomInfo")) {
            builder = NotificationCompat.Builder(application, NOTIFICATIN_CHANNEL_ID)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder!!.setChannelId(NOTIFICATIN_CHANNEL_ID)
            }
            val actIntent = Intent(Define.ACTION_OPEN_MAIN_ACTIVITY)
            actIntent.putExtra(Define.Intent.ACTION_NOTIFICATION_SYSTEM, true)
            val customInfoObj = jsonObject.getJSONObject("CustomInfo")
            val titleString = customInfoObj.getString("Title")
            val contentString = URLDecoder.decode(customInfoObj.getString("Content"), "UTF-8")
            val time = customInfoObj.getString("AlarmTime")
            val content = try {
                if (titleString.contains("online")) {
                    getString(
                        com.vht.sdkcore.R.string.noti_jf_camera_connected,
                        contentString,
                        time
                    )
                } else {
                    getString(
                        com.vht.sdkcore.R.string.noti_jf_camera_disconnected,
                        contentString,
                        time
                    )
                }
            } catch (e: Exception) {
                Timber.d("dealWithNotifyPushInfo Error: $e")
                ""
            }
            showAlarmNotice(
                System.currentTimeMillis().toInt(),
                null,
                null,
                null,
                contentString,
                content,
                actIntent
            )
        }
    }

    private fun dealWithIntervalWakeAlarm(devSn: String, alarmInfo: AlarmInfo) {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        try {
            val date = format.parse(alarmInfo.startTime)
            val times = date.time
            val nowTimes = System.currentTimeMillis()
            //If there is a difference of 15 seconds between the time of the message and the current time, the device status will not be refreshed
            if (nowTimes - times <= 15000) {
                EventBus.getDefault().post(IDRStateResult(devSn, WeakUp.IDR_WEAK_SUCCESS))
                Handler(Looper.getMainLooper()).postDelayed({
                    EventBus.getDefault().post(IDRStateResult(devSn, WeakUp.IDR_NO_WEAK))
                }, 15000)
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }

    /**
     * Third-party push message processing (mainly transparent data transmission, processing caller ID information)
     *
     * @param deviceInfo
     * @param alarmInfo
     */
    private fun dealWithOtherPushAlarm(deviceInfo: SDBDeviceInfo?, alarmInfo: AlarmInfo) {
        mOtherPushInfos[alarmInfo.id] = alarmInfo
        if (isQueueContains(alarmInfo.id)) {
            return
        }
        addQueue(alarmInfo.id)
        if (null != deviceInfo && IdrDefine.isIDR(deviceInfo.st_7_nType)) {
            dealWithCallAlarm(deviceInfo.sn, deviceInfo.st_7_nType, alarmInfo)
        }
    }


    private fun isQueueContains(alarmId: String): Boolean {
        synchronized(mAlarmIdQueue!!) { return mAlarmIdQueue!!.contains(alarmId) }
    }

    private fun addQueue(alarmId: String?) {
        if (alarmId == null) {
            return
        }
        synchronized(mAlarmIdQueue!!) {
            if (!mAlarmIdQueue!!.offer(alarmId)) {
                mAlarmIdQueue?.poll()
                mAlarmIdQueue?.add(alarmId)
            }
        }
    }

    private fun dealWithCallAlarm(devSn: String, devType: Int, alarmInfo: AlarmInfo) {
        Timber.d("dealWithCallAlarm")
        val picPath: String = (PATH_PHOTO_TEMP + File.separator + devSn + "_"
                + alarmInfo.id + ".jpg")
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        try {
            val date = format.parse(alarmInfo.startTime)
            val times = date.time
            val nowTimes = System.currentTimeMillis()
            //Incoming calls longer than five minutes are ignored
            if (nowTimes - times <= IGNORE_ALARM_CALLER_TIMES) {
                msgHandle(
                    this, devSn, devType,
                    alarmInfo.event,
                    alarmInfo.originJson,
                    picPath,
                    alarmInfo.id,
                    alarmInfo.startTime,
                    alarmInfo.msg,
                    alarmInfo.msgType,
                    alarmInfo.devName
                )

//                if (!XUtils.isTopActivity(
//                        this,
//                        AddDeviceWithWifiBaseStationActivity::class.java.getName()
//                    )
//                    && !XUtils.isTopActivity(this, QuickConfigResultActivity::class.java.getName())
//                    && !XUtils.isTopActivity(this, RouteRemindActivity::class.java.getName())
//                    && !XUtils.isTopActivity(this, SetDevPsdActivity::class.java.getName())
//                ) {
//
//                }
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }

    private fun dealWithNoMsgShowTip() {
//        try {
//            if (XUtils.isServiceRunning(
//                    this,
//                    XMNotificationListenerService::class.java.getName()
//                )
//            ) {
//                val manager = ShowNotificationTimesManager()
//                if (!manager.isShowNotificationOk(this, packageName)) {
//                    Toast.makeText(
//                        this,
//                        FunSDK.TS("Have_No_Alarm_Notification_Tip"),
//                        Toast.LENGTH_LONG
//                    ).show()
//                } else {
//                    Handler(Looper.getMainLooper()).postDelayed({
//                        val alarmPushTimes = System.currentTimeMillis()
//                        val showNotificationTimes: Long =
//                            manager.getShowNotificationTimes(this@AlarmPushService, packageName)
//                        val times = alarmPushTimes - showNotificationTimes
//                        println("getShowNotificationTimes:$showNotificationTimes")
//                        if (times > AlarmPushService.SHOW_ALARM_NOTIFICATION_INTERVAL) {
//                            Toast.makeText(
//                                this@AlarmPushService,
//                                FunSDK.TS("Have_No_Alarm_Notification_Tip"),
//                                Toast.LENGTH_LONG
//                            ).show()
//                            manager.setShowNotificationTimes(
//                                this@AlarmPushService,
//                                packageName,
//                                System.currentTimeMillis(),
//                                false
//                            )
//                        }
//                    }, 10000)
//                }
//            }
//        } catch (e: java.lang.Exception) {
//            e.printStackTrace()
//        }
    }

    private fun dealWithPushMsg(alarmInfo: AlarmInfo?, deviceInfo: SDBDeviceInfo?) {
        Timber.d("dealWithPushMsg")
        if (null == alarmInfo || null == deviceInfo) {
            return
        }
        if (isQueueContains(alarmInfo.id)) {
            return
        }
        addQueue(alarmInfo.id)
        builder = NotificationCompat.Builder(application, NOTIFICATIN_CHANNEL_ID)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder?.setChannelId(NOTIFICATIN_CHANNEL_ID)
        }
        Timber.d(
            "Call the police_" + deviceInfo + "  " + deviceInfo.st_7_nType + "  " +
                    alarmInfo.event + "  " +
                    alarmInfo.pic
        )
        val sn = deviceInfo.sn
        val picPath: String = (PATH_PHOTO_TEMP + File.separator + sn + "_"
                + alarmInfo.id + ".jpg")
        var result: String? = ""
        //如果在配网界面 就不要弹出来电界面
        if ((deviceInfo.hasPermissionAlarmPush())) {
            if (("IOTAlarm" == alarmInfo.event)) {
                result = alarmInfo.extInfo
            } else {
                result = msgHandle(
                    this,
                    sn,
                    deviceInfo.st_7_nType,
                    alarmInfo.event,
                    alarmInfo.originJson,
                    picPath,
                    alarmInfo.id,
                    alarmInfo.startTime,
                    alarmInfo.msg,
                    alarmInfo.msgType,
                    alarmInfo.devName
                )
            }
        } else {
            result = if (("IOTAlarm" == alarmInfo.event)) {
                alarmInfo.extInfo
            } else {
                alarmType(this, alarmInfo.event, alarmInfo.msg, alarmInfo.msgType)
            }
        }
        dealWithTurnToAlarmMsg(alarmInfo, deviceInfo, result)
    }

    /**
     * The page that handles the message jump
     *
     * @param alarmInfo
     * @param deviceInfo
     * @param alarmResult
     */
    private fun dealWithTurnToAlarmMsg(
        alarmInfo: AlarmInfo?,
        deviceInfo: SDBDeviceInfo?,
        alarmResult: String?
    ) {
        if (deviceInfo == null || !deviceInfo.hasPermissionAlarmPush() || alarmInfo == null) {
            return
        }
        val actIntent = Intent(Define.ACTION_OPEN_MAIN_ACTIVITY)
        when (alarmResult) {
            "Device online" -> {
                actIntent.putExtra(Define.Intent.ACTION_NOTIFICATION_SYSTEM, true)
            }

            "Device offline" -> {
                actIntent.putExtra(Define.Intent.ACTION_NOTIFICATION_SYSTEM, true)
            }
        }
        var noticeTitle: String? = ""
        if (SysAbilityManager.getInstance().isDevChnCloud(this, deviceInfo.sn)) {
            noticeTitle = turnToNewAlarmMsg(alarmInfo, deviceInfo, alarmResult, actIntent)
        } else {
            noticeTitle = turnToOldAlarmMsg(alarmInfo, deviceInfo, alarmResult, actIntent)
        }

        //Notification has notification id which is timestamp of startTime
        val alarmId = convertDateToTimeStamp(alarmInfo.startTime).toInt()

        Timber.tag("HieuNT").e("PushAlarmID: $alarmId $noticeTitle ${alarmInfo.event}")
        showAlarmNotice(
            alarmId,
            deviceInfo.sn, G.ToStringByHtml(deviceInfo.st_1_Devname),
            alarmInfo.event, noticeTitle!!, "", actIntent
        )
    }


    private fun convertContentNotification(
        alarmResult: String?,
        alarmInfo: AlarmInfo,
        deviceInfo: SDBDeviceInfo
    ): Pair<String?, String?> {
        val noticeTitle: String?
        val contentText: String?
        val nameDev = if (G.ToStringByHtml(deviceInfo.st_1_Devname).isNullOrEmpty()) {
            XUtils.getSnByDesensitization(deviceInfo.sn)
        } else {
            G.ToStringByHtml(deviceInfo.st_1_Devname)
        }
        when (alarmResult) {
            "Human Detect", "Human Detection" -> {
                noticeTitle = "Phát hiện người"
                contentText = String.format(
                    "Phát hiện có người tại %s vào %s",
                    nameDev,
                    alarmInfo.startTime
                )
            }

            "Motion Detection" -> {
                noticeTitle = "Phát hiện chuyển động"
                contentText = String.format(
                    "Phát hiện có chuyển động tại %s vào %s",
                    nameDev,
                    alarmInfo.startTime
                )
            }

            "Device Remote Call" -> {
                noticeTitle = "Cuộc gọi từ xa"
                contentText = String.format(
                    "Có cuộc gọi từ %s vào %s",
                    nameDev,
                    alarmInfo.startTime
                )
            }

            "Device online" -> {
                noticeTitle = "Thiết bị đã hoạt động"
                contentText = String.format(
                    "Camera %s đã kết nối trở lại vào %s",
                    nameDev,
                    alarmInfo.startTime
                )
            }

            "Device offline" -> {
                noticeTitle = "Thiết bị ngoại tuyến"
                contentText = String.format(
                    "Camera %s đã mất kết nối vào %s",
                    nameDev,
                    alarmInfo.startTime
                )
            }

            else -> {
                noticeTitle = alarmResult
                contentText = nameDev
            }
        }
        Timber.d("contentText: $contentText")
        return noticeTitle to contentText
    }

    private fun convertDateToTimeStamp(date: String): Long {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return try {
            val dateTime = formatter.parse(date)
            dateTime?.time ?: System.currentTimeMillis()
        } catch (ex: Exception) {
            System.currentTimeMillis()
        }
    }

    /**
     * Jump to the new alarm message page
     *
     * @param alarmInfo
     * @param deviceInfo
     * @param alarmResult
     * @param actIntent
     * @return
     */
    private fun turnToNewAlarmMsg(
        alarmInfo: AlarmInfo,
        deviceInfo: SDBDeviceInfo,
        alarmResult: String?,
        actIntent: Intent
    ): String? {
        var noticeTitle: String? = ""
//        var cName: ComponentName
        var subSn = ""
        val sn = deviceInfo.sn
        actIntent.putExtra("devId", sn)
        actIntent.putExtra("alarmTime", alarmInfo.startTime)
        actIntent.putExtra("devType", deviceInfo.st_7_nType)
        if (!StringUtils.isStringNULL(alarmResult)) {
            if (alarmResult?.equals("Device Remote Call") == true) {
                actIntent.putExtra(Define.Intent.ACTION, Define.Intent.ACTION_CAMERA)
                actIntent.putExtra(Define.Intent.TYPE_CAMERA, Define.TYPE_DEVICE.CAMERA_JF)
                actIntent.putExtra(Define.Intent.DEVICE_ID, sn)
            }
            val (title, contentText) = convertContentNotification(
                alarmResult,
                alarmInfo,
                deviceInfo
            )
            noticeTitle = title
            builder?.setContentText(contentText)
//            cName = if (IdrDefine.isDoor(deviceInfo.st_7_nType)) {
//                // TODO Do you want to handle incoming calls from the door lock device?
//                actIntent.setClass(this, DoorLockMsgActivity::class.java)
//                ComponentName(packageName, DoorLockMsgActivity::class.java.getName())
//            } else {
//                if (Define.isPortraitVideo(this, sn)) {
//                    actIntent.setClass(this, AlarmPicVideoShowPortraitActivity::class.java)
//                    ComponentName(
//                        packageName,
//                        AlarmPicVideoShowPortraitActivity::class.java.getName()
//                    )
//                } else {
//                    actIntent.setClass(this, AlarmPicVideoShowActivity::class.java)
//                    ComponentName(packageName, AlarmPicVideoShowActivity::class.java.getName())
//                }
//            }
        } else if (alarmInfo.linkCenterExt != null) {
            /**
             * Zhilian center push
             */
//            cName = ComponentName(packageName, AlarmDetectionMsg::class.java.getName())
            subSn = alarmInfo.linkCenterExt.subSn
            actIntent.putExtra("subSn", subSn)
            //普通433传感器推送
            if (StringUtils.contrast(alarmInfo.event, "433Alarm")
                || StringUtils.contrast(alarmInfo.event, "ConsSensorAlarm")
            ) {
                noticeTitle = alarmInfo.event
                builder?.setContentText(alarmInfo.pushMsg)
//                actIntent.setClass(this, AlarmDetectionMsg::class.java)
//                cName = ComponentName(packageName, AlarmDetectionMsg::class.java.getName())
//                val msgCount: Int =
//                    SPUtil.getInstance(this).getSettingParam(Define.SENSOR_PUSH + sn + subSn, 0)
//                //用于消息角标
//                SPUtil.getInstance(this)
//                    .setSettingParam(Define.SENSOR_PUSH + sn + subSn, msgCount + 1)
//                val broadcastIntent: Intent = Intent(DevDyncAlarmActivity.ACTION_SENSOR_BROADCAST)
//                sendBroadcast(broadcastIntent)
            } else if (StringUtils.contrast(alarmInfo.event, "DoorLockNotify")
                || StringUtils.contrast(alarmInfo.event, "DoorLockAlarm")
                || StringUtils.contrast(alarmInfo.event, "DoorLockCall")
            ) {
                //门锁报警
                if (StringUtils.contrast(alarmInfo.event, "DoorLockNotify")
                    || StringUtils.contrast(alarmInfo.event, "DoorLockCall")
                ) {
                    noticeTitle = "Notification Event"
                } else if (StringUtils.contrast(alarmInfo.event, "DoorLockAlarm")) {
                    noticeTitle = "Exception Message"
                }
                builder?.setContentText(getTitleByInfo(alarmInfo.linkCenterExt))
//                actIntent.setClass(this, DoorLockMsgActivity::class.java)
                if (StringUtils.contrast(alarmInfo.event, "DoorLockNotify")
                    || StringUtils.contrast(alarmInfo.event, "DoorLockCall")
                ) {
                    actIntent.putExtra("msgType", 0)
                }
                if (StringUtils.contrast(alarmInfo.event, "DoorLockAlarm")) {
                    actIntent.putExtra("msgType", 1)
                }
//                cName = ComponentName(packageName, DoorLockMsgActivity::class.java.getName())
            }
            if (noticeTitle?.equals("Device Remote Call") == true) {
                actIntent.putExtra(Define.Intent.ACTION, Define.Intent.ACTION_CAMERA)
                actIntent.putExtra(Define.Intent.TYPE_CAMERA, Define.TYPE_DEVICE.CAMERA_JF)
                actIntent.putExtra(Define.Intent.DEVICE_ID, sn)
            }
        } else {

            noticeTitle = if (StringUtils.contrast(alarmInfo.event, "VideoMotion")) {
                "Motion Detection"
            } else if (StringUtils.contrast(alarmInfo.event, "VideoAnalyze")) {
                "Intelligent Analysis"
            } else if (StringUtils.contrast(alarmInfo.event, "appEventHumanDetectAlarm")) {
                "Human Detection"
            } else {
                "You have received an event message"
            }
            val (title, contentText) = convertContentNotification(
                noticeTitle,
                alarmInfo,
                deviceInfo
            )
            noticeTitle = title
            builder?.setContentText(contentText)
//            cName = if (Define.isPortraitVideo(this, sn)) {
//                actIntent.setClass(this, AlarmPicVideoShowPortraitActivity::class.java)
//                ComponentName(packageName, AlarmPicVideoShowPortraitActivity::class.java.getName())
//            } else {
//                actIntent.setClass(this, AlarmPicVideoShowActivity::class.java)
//                ComponentName(packageName, AlarmPicVideoShowActivity::class.java.getName())
//            }
        }
//        actIntent.component = cName
        return noticeTitle
    }

    /**
     * Jump to old news page
     *
     * @param alarmInfo
     * @param deviceInfo
     * @param alarmResult
     * @return
     */
    private fun turnToOldAlarmMsg(
        alarmInfo: AlarmInfo,
        deviceInfo: SDBDeviceInfo,
        alarmResult: String?,
        actIntent: Intent
    ): String? {
        var noticeTitle: String? = ""
//        var cName: ComponentName
        var subSn = ""
        val sn = deviceInfo.sn
        if (!StringUtils.isStringNULL(alarmResult)) {
            noticeTitle = alarmResult
            if (StringUtils.contrast(alarmResult, FunSDK.TS("Exception_Message"))) {
                if (alarmInfo.linkCenterExt != null) {
                    noticeTitle =
                        alarmResult + "(" + getTitleByInfo(alarmInfo.linkCenterExt) + ")"
                }
            }
            if (alarmResult?.equals("Device Remote Call") == true) {
                actIntent.putExtra(Define.Intent.ACTION, Define.Intent.ACTION_CAMERA)
                actIntent.putExtra(Define.Intent.TYPE_CAMERA, Define.TYPE_DEVICE.CAMERA_JF)
                actIntent.putExtra(Define.Intent.DEVICE_ID, sn)
            }
            val (title, contentText) =
                convertContentNotification(noticeTitle, alarmInfo, deviceInfo)
            noticeTitle = title
            builder?.setContentText(contentText)
//            cName = if (IdrDefine.isDoor(deviceInfo.st_7_nType)) {
//                actIntent.setClass(this, DoorLockMsgActivity::class.java)
//                ComponentName(packageName, DoorLockMsgActivity::class.java.getName())
//            } else {
//                actIntent.setClass(this, AlarmMessActivity::class.java)
//                ComponentName(packageName, AlarmMessActivity::class.java.getName())
//            }
        } else if (alarmInfo.linkCenterExt != null) {
            /**
             * Zhilian center push
             */
//            cName = ComponentName(packageName, AlarmDetectionMsg::class.java.getName())
            subSn = alarmInfo.linkCenterExt.subSn
            actIntent.putExtra("subSn", subSn)
            if (StringUtils.contrast(alarmInfo.event, "433Alarm")
                || StringUtils.contrast(alarmInfo.event, "ConsSensorAlarm")
            ) {
                //普通433传感器推送
                noticeTitle = alarmInfo.event
                builder?.setContentText(alarmInfo.pushMsg)
//                actIntent.setClass(this, AlarmDetectionMsg::class.java)
//                cName = ComponentName(packageName, AlarmDetectionMsg::class.java.getName())
//                val msgCount: Int =
//                    SPUtil.getInstance(this).getSettingParam(Define.SENSOR_PUSH + sn + subSn, 0)
//                //用于消息角标
//                SPUtil.getInstance(this)
//                    .setSettingParam(Define.SENSOR_PUSH + sn + subSn, msgCount + 1)
//                val broadcastIntent: Intent = Intent(DevDyncAlarmActivity.ACTION_SENSOR_BROADCAST)
//                sendBroadcast(broadcastIntent)
            } else if (StringUtils.contrast(alarmInfo.event, "DoorLockNotify")
                || StringUtils.contrast(alarmInfo.event, "DoorLockAlarm")
                || StringUtils.contrast(alarmInfo.event, "DoorLockCall")
            ) {
                //门锁报警
                if (StringUtils.contrast(alarmInfo.event, "DoorLockNotify")
                    || StringUtils.contrast(alarmInfo.event, "DoorLockCall")
                ) {
                    noticeTitle = "Notification Event"
                } else if (StringUtils.contrast(alarmInfo.event, "DoorLockAlarm")) {
                    noticeTitle = "Exception Message"
                }
                builder?.setContentText(getTitleByInfo(alarmInfo.linkCenterExt))
//                actIntent.setClass(this, DoorLockMsgActivity::class.java)
                if (StringUtils.contrast(alarmInfo.event, "DoorLockNotify")
                    || StringUtils.contrast(alarmInfo.event, "DoorLockCall")
                ) {
                    actIntent.putExtra("msgType", 0)
                }
                if (StringUtils.contrast(alarmInfo.event, "DoorLockAlarm")) {
                    actIntent.putExtra("msgType", 1)
                }
//                cName = ComponentName(packageName, DoorLockMsgActivity::class.java.getName())
            }
            if (noticeTitle?.equals("Device Remote Call") == true) {
                actIntent.putExtra(Define.Intent.ACTION, Define.Intent.ACTION_CAMERA)
                actIntent.putExtra(Define.Intent.TYPE_CAMERA, Define.TYPE_DEVICE.CAMERA_JF)
                actIntent.putExtra(Define.Intent.DEVICE_ID, sn)
            }
        } else {

            noticeTitle = if (StringUtils.contrast(alarmInfo.event, "VideoMotion")) {
                "Motion Detection"
            } else if (StringUtils.contrast(alarmInfo.event, "VideoAnalyze")) {
                "Intelligent Analysis"
            } else {
                "You have received an event message"
            }
            val (title, contentText) =
                convertContentNotification(noticeTitle, alarmInfo, deviceInfo)
            noticeTitle = title

            builder?.setContentText(contentText)
//            actIntent.setClass(this, AlarmMessActivity::class.java)
//            cName = ComponentName(packageName, AlarmMessActivity::class.java.getName())
        }
//        if (XUtils.ApplicationRunningState(application) == ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE
//        ) {
//            actIntent.action = Intent.ACTION_MAIN
//            actIntent.addCategory(Intent.CATEGORY_LAUNCHER)
//            actIntent.addFlags(
//                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
//                        or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
//            )
//        }
        actIntent.putExtra("push_notice", true)
        actIntent.putExtra("sn_val", sn)
        actIntent.putExtra("channel_val", -1)
        actIntent.putExtra("devType", deviceInfo.st_7_nType)
        return noticeTitle
    }

    private fun msgHandle(
        context: Context,
        sn: String?,
        devType: Int,
        event: String?,
        originJson: String?,
        pic: String?,
        alarmID: String?,
        time: String?,
        msg: String?,
        msgType: String?,
        devName: String?
    ): String? {
        if (TextUtils.isEmpty(sn) || TextUtils.isEmpty(event)) {
            return ""
        }
        val type: String? = alarmType(this, event, msg, msgType)
        if (isShowCallUI(event, sn, devType) && !JFCameraManager.isAdding) {
            Timber.d("msgHandle: $sn - ${JFCameraManager.cameraWatching} - ${sn.equals(JFCameraManager.cameraWatching)}")
            if (sn.equals(JFCameraManager.cameraWatching)) {
                EventBus.getDefault().post(
                    EventBusPushInfo(
                        devName,
                        EventBusPushInfo.PUSH_OPERATION.CALL,
                        null
                    )
                )
                return type
            }
            IDRVisitorActivity.startActivity(
                context.applicationContext,
                sn,
                originJson,
                pic,
                alarmID,
                time,
                XMAccountManager.getInstance().getDevInfo(sn).sdbDevInfo.hasPermissionIntercom(),
                event,
                devName
            )
            Handler().postDelayed({
                if (!XUtils.isTopActivity(
                        context,
                        IDRVisitorActivity::class.java.name
                    )
                ) {
                    Toast.makeText(
                        context,
                        "No Caller Interface? Please go to the system setting on the mobile and turn on the permissions of the Background Pop-up.",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }, 1000)
        }
        return type
    }

    private fun isShowCallUI(
        alarmEvent: String?,
        devId: String?,
        devType: Int
    ): Boolean {
        return if (StringUtils.isStringNULL(alarmEvent) || IdrDefine.isNotShowCall(devId)) {
            false
        } else when (alarmEvent) {
            TYPE_LOCAL_ALARM -> IdrDefine.isIDR(devType)
            TYPE_DOOR_BELL, TYPE_FORCE_DISMANTLE, TYPE_REMOTE_CALL_ALARM -> true
            else -> false
        }
    }

    /**
     * Show alarm message notification
     *
     * @param noticeTitle
     * @param actIntent
     */
    private fun showAlarmNotice(
        alarmId: Int,
        devId: String?,
        devName: String?,
        alarmEvent: String?,
        noticeTitle: String,
        noticeContent: String,
        actIntent: Intent?
    ) {
        if (isShowToast && devId != null) {
            Toast.makeText(
                this@AlarmPushService, "Receive a message"
                        + ":"
                        + noticeTitle
                        + "("
                        + devName
                        + ":"
                        + XUtils.getSnByDesensitization(devId)
                        + ")", Toast.LENGTH_LONG
            ).show()
        }

        //如果不是雄迈推送的话 就不要在通知栏上显示雄迈推送过来的消息
        if (DevDataCenter.getInstance()?.pushType != PUSH_TYPE_XM
            && DevDataCenter.getInstance()?.pushType != PUSH_TYPE_GOOGLE
        ) {
            return
        }
        EventBus.getDefault().post(
            RxEvent(
                EVENT_RELOAD_LIST_EVENT,
                devId
            )
        )
        Log.d("TAG", "showAlarmNotice: show alarm camera = $devId")
        builder?.setAutoCancel(true)
        builder?.setContentTitle(noticeTitle)
        //todo icon notification app
//        builder?.setSmallIcon(R.drawable.ic_app_notification)
        builder?.color = getColor(R.color.color_EE0033)
        builder?.setWhen(System.currentTimeMillis())
        builder?.setDefaults(Notification.DEFAULT_VIBRATE or Notification.DEFAULT_LIGHTS)
        builder?.setTicker(noticeTitle)
        builder?.setStyle(NotificationCompat.BigTextStyle())
        if (!StringUtils.isStringNULL(noticeContent)) {
            builder?.setContentText(noticeContent)
        }
        val intent = Intent(
            this@AlarmPushService,
            NotificationClick::class.java
        )
        val fullScreenIntent = PendingIntent.getBroadcast(
            this@AlarmPushService,
            UUID.randomUUID().hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder?.setDeleteIntent(fullScreenIntent)
        if (actIntent != null) {
            actIntent.putExtra(Define.Intent.DEVICE_ID, devId)
            actIntent.putExtra(Define.Intent.TYPE_CAMERA, Define.TYPE_DEVICE.CAMERA_JF)
            val pendingIntent = PendingIntent.getActivity(
                this@AlarmPushService, UUID.randomUUID().hashCode(), actIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder?.setContentIntent(pendingIntent)
        }


        manager?.notify(alarmId, builder?.build())
//        if (StringUtils.contrast(alarmEvent, TYPE_LOCAL_ALARM)
//            && XMPushManager.isOpenCallUI(this@AlarmPushService, devId)
//        ) {
//            AlarmRingPlayManager
//                .getInstance(this@AlarmPushService.applicationContext)
//                .stopPlay()
//        } else {
//            playSound(this@AlarmPushService)
//        }
    }

    private fun openChannel(
        devIds: Array<String>,
        devTypes: IntArray,
        info: SDBDeviceInfo,
        reviewHandle: IntArray
    ) {
        val it = Intent(Define.ACTION_OPEN_MAIN_ACTIVITY)
//        DataCenter.getInstance().setCurDevId(devIds[0])
//        DataCenter.getInstance().setCurrentSDBDeviceInfo(info)
//        it = if (IdrDefine.isDoor(devTypes[0])) {
//            if (Define.isPortraitVideo(this@AlarmPushService, devIds[0])) {
//                Intent(this, DoorLockMonitorPortraitActivity::class.java)
//            } else {
//                Intent(this, DoorLockMonitorActivity::class.java)
//            }
//        } else {
//            Intent(this, MonitorActivity::class.java)
//        }
//        it.component = ComponentName("com.viettel.vht.smarthome.dev.ui.main", MainActivity::class.java.name)
        it.putExtra(Define.Intent.ACTION, Define.Intent.ACTION_CAMERA)
        it.putExtra(Define.Intent.TYPE_CAMERA, Define.TYPE_DEVICE.CAMERA_JF)
        it.putExtra(Define.Intent.DEVICE_ID, devIds[0])
        it.putExtra("devTypes", devTypes)
        it.putExtra("reviewHandles", reviewHandle)
        it.putExtra("fromActivity", AlarmPushService::class.java.simpleName)
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(it)
    }

    private fun bindDevice(devSn: String) {
        Timber.d("bindDevice: ${devSn}")
        if (baseVector.isEmpty()) {
            return
        }
        for (i in baseVector.indices) {
            val info = baseVector[i]
            if (info?.sn.equals(devSn)) {
                mXMPushManager?.linkAlarm(info?.sn, G.ToString(info?.st_1_Devname), i)
                break
            }
        }
    }

    private fun unBindDevice(sn: String?) {
        Timber.d("unBindDevice: ${sn}")
        if (XUtils.isEmpty(sn) || baseVector.isEmpty()) {
            return
        }
        var position = -1
        for (i in baseVector.indices) {
            val info = baseVector[i]
            if (StringUtils.contrast(info?.sn, sn)) {
                position = i
                break
            }
        }
        mXMPushManager?.unLinkAlarm(sn, position)
    }

    private fun dealWithAddDevice(devSn: String) {
        Timber.d("dealWithAddDevice: $devSn")
        val info: SDBDeviceInfo? = XMAccountManager.getInstance()?.getDevInfo(devSn)?.sdbDevInfo
        if (null != info) {
            if (baseVector.isNotEmpty() && !baseVector.contains(info)) {
                baseVector.add(info)
            }
            if (info.hasPermissionAlarmPush()) {
                bindDevice(G.ToString(info.st_0_Devmac))
            }
        }
    }

    private fun dealWithRemoveDevice(devSn: String) {
        Timber.d("dealWithRemoveDevice: $devSn")
        if (XUtils.isEmpty(devSn) || baseVector.isEmpty()) {
            return
        }
        mXMPushManager?.unLinkAlarmAbnoraml(devSn, -1)
        for (i in baseVector.indices) {
            val info = baseVector[i]
            if (StringUtils.contrast(info?.sn, devSn)) {
                baseVector.remove(info)
                break
            }
        }
    }

    @Subscribe
    fun receiverIDRCallResult(result: IDRCallResult) {
        when (result.result) {
            IDRCallResult.RESULT_OPEN_MSG -> {
//                openMsgUI(result.getDevSN())
            }

            IDRCallResult.RESULT_OPEN_REVIEW -> {
                Timber.d("RESULT_OPEN_REVIEW")
                val info: SDBDeviceInfo =
                    XMAccountManager.getInstance()?.getDevInfo(result.devSN)?.sdbDevInfo!!
                DevDataCenter.getInstance()?.setDevType(result.devSN, info.st_7_nType)
//                DevDataCenter.getInstance()?.setIDRAlarmEvent(result.getAlarmEvent())
//                TipManager.getInstance()
//                    .checkAndShowFlowTip(result.getDevSN(), this, object : OnCheckResultListener() {
//                        fun onCheckResult(isOk: Boolean, isNotAllowed: Boolean) {
//                            if (isOk) {
//                                openChannel(
//                                    arrayOf<String>(result.getDevSN()),
//                                    intArrayOf(info.st_7_nType),
//                                    info,
//                                    intArrayOf(result.getPreviewHandle())
//                                )
//                            }
//                        }
//                    })
                openChannel(
                    arrayOf<String>(result.devSN),
                    intArrayOf(info.st_7_nType),
                    info,
                    intArrayOf(result.previewHandle)
                )
            }

            IDRCallResult.RESULT_REOPEN_APP -> {
                Timber.d("RESULT_REOPEN_APP")
                val it = Intent(Define.ACTION_OPEN_MAIN_ACTIVITY)
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                it.putExtra(Define.Intent.ACTION_OPEN_MAIN, true)
                startActivity(it)
            }

            IDRCallResult.RESULT_OPEN_EDIT_CUSTOMIZE_VOICE -> {
//                val intent = Intent(
//                    this,
//                    BellCustomizeActivity::class.java
//                )
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                intent.putExtra("devId", result.getDevSN())
//                intent.putExtra("fileNumber", result.getFileNumber())
//                startActivity(intent)
            }

            else -> {}
        }
    }

    /**
     * After modifying the device name, you need to re-subscribe to the alarm push
     *
     * @param event
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: RxEvent<String>?) {
        if (null == event) {
            return
        }
        if (event.keyId == EVENT_SYSTEM_CHANGE_INFORMATION_CAMERA_JF) {
            val devId: String? = event.value
            if (null != devId) {
                val devInfo = XMAccountManager.getInstance()?.getDevInfo(devId)?.sdbDevInfo
                if (devInfo != null) {
                    mXMPushManager?.linkAlarm(devId, devInfo.devName, 0)
                }
            }
        } else if (event.keyId == JFCameraManager.EVENT_LOGOUT_CAMERA_JF) {
            this.stopSelf()
            stopService(Intent(this, IDRSleepService.InnerService::class.java))
//            stopForeground(true)
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.deleteNotificationChannel(NOTIFICATION_BACKGROUND_CHANNEL_ID)
        }

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun receiveEventBusPushInfo(pushInfo: EventBusPushInfo?) {
        if (pushInfo != null) {
            if (mEventBusPushInfo == null) {
                mEventBusPushInfo = HashMap()
            }
            mEventBusPushInfo!![pushInfo.devId] = pushInfo
            when (pushInfo.operationType) {
                EventBusPushInfo.PUSH_OPERATION.LINK -> bindDevice(pushInfo.devId)
                EventBusPushInfo.PUSH_OPERATION.UNLINK -> unBindDevice(pushInfo.devId)
                EventBusPushInfo.PUSH_OPERATION.ADD_DEV -> dealWithAddDevice(pushInfo.devId)
                EventBusPushInfo.PUSH_OPERATION.REMOVE_DEV -> dealWithRemoveDevice(pushInfo.devId)
                EventBusPushInfo.PUSH_OPERATION.SHARE_ACCEPT -> {
                    initPushList()
//                    initVectorData()
                    bindDeviceAndUnlinkAbnormalAlarm()
                }

                else -> {}
            }
        }
    }

    override fun OnFunSDKResult(p0: Message?, p1: MsgContent?): Int {
        Timber.d("OnFunSDKResult1: ${Gson().toJson(p0)} - ${Gson().toJson(p1)}")
        return 0
    }

    private fun alarmType(
        context: Context,
        event: String?,
        msg: String? = "",
        msgType: String? = ""
    ): String? {
//        return when (event) {
//            TYPE_LOCAL_ALARM -> "Caller"
//            TYPE_PIR_ALARM -> "Human Detect"
//            TYPE_INTERVAL_WAKE -> "Interval Wake"
//            TYPE_RESERVER_WAKE -> "Appointment wake-up"
//            TYPE_FORCE_DISMANTLE -> "Device is pulled out"
//            TYPE_LOW_BATTERY -> "Low Battery"
//            TYPE_REMOTE_CALL_ALARM -> "Device remote call"
//            else -> getAlarmName(context, event, msg, msgType)
//        }
        return getAlarmName(context, event, msg, msgType)
    }

    /**
     * Obtain the prompt text according to the door lock alarm type
     */
    private fun getTitleByInfo(linkCenterExt: LinkCenterExt): String? {
        val msg: String = when (linkCenterExt.msgType) {
            "Card" -> linkCenterExt.msg + "Have gone home"
            "FingerPrint" -> linkCenterExt.msg + "Have gone home"
            "Passwd" -> linkCenterExt.msg + "Have gone home"
            "Temporary" -> "Visitors Visit"
            "Key" -> "Unlocked by key"
            "DoorBell" -> "Someone ringing the doorbell"
            "DoorLockOpenCount" -> "Event Statistics"
            "UnlockFailed" -> "Failed unlock attempt"
            "HasBeenOpen", "HasBeenClose", "HasBeenLocked", "LockedCancel", "DoorLockRestore", "LowBattery", "LowElectAlarm", "ForceDelAlarm", "ForceFingerOpen", "PasswdError", "FingerPrintError", "CardError", "Trespassing", "UnKnowKey" -> FunSDK.TS(
                "DL_" + linkCenterExt.msgType
            )

            else -> ""
        }
        return msg
    }
}