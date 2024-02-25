package com.viettel.vht.sdk.ui.jfcameradetail.call

import android.app.Activity
import android.app.KeyguardManager
import android.app.KeyguardManager.KeyguardLock
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.TypedArray
import android.media.AudioManager
import android.media.Ringtone
import android.os.*
import android.view.KeyEvent
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.widget.LinearLayoutCompat
import com.lib.sdk.bean.StringUtils
import com.manager.device.idr.IdrDefine
import com.vht.sdkcore.base.BaseActivity
import com.vht.sdkcore.utils.DeviceUtil
import com.vht.sdkcore.utils.turnScreenOff
import com.vht.sdkcore.utils.turnScreenOn
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.ActivityIdrVisitorBinding
import com.viettel.vht.sdk.jfmanager.JFCameraManager
import com.viettel.vht.sdk.model.jfcall.IDRCallResult
import com.viettel.vht.sdk.model.jfcall.IDRModel
import com.viettel.vht.sdk.model.jfcall.IDRReceiverCall
import com.viettel.vht.sdk.model.jfcall.IDRSleepModel
import com.viettel.vht.sdk.model.jfcall.IdrDevBatteryManager
import com.viettel.vht.sdk.model.jfcall.LowPowerPrompt
import com.viettel.vht.sdk.model.jfcall.PreloadReview
import com.viettel.vht.sdk.ui.jfcameradetail.utils.RingUtils
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import java.io.File
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class IDRVisitorActivity : BaseActivity<ActivityIdrVisitorBinding, IDRVisitorViewModel>(),
    View.OnClickListener {


    companion object {

        private const val PIC_PATH = "PIC_PATH"
        private const val SN = "SN"
        private const val TIME = "TIME"
        private const val ALARM_ID = "ALARM_ID"
        private const val ORIGIN_JSON = "ORIGIN_JSON"
        private const val HAS_TALK_PERMISSION = "HAS_TALK_PERMISSION"
        private const val ALARM_EVENT = "ALARM_EVENT"
        private const val DEV_NAME = "DEV_NAME"

        fun startActivity(
            context: Context,
            sn: String?,
            originJson: String?,
            pic: String?,
            alarmID: String?,
            time: String?,
            hasTalkPermission: Boolean,
            devName: String?,
        ) {
            startActivity(
                context,
                sn,
                originJson,
                pic,
                alarmID,
                time,
                hasTalkPermission,
                "",
                devName
            )
        }

        fun startActivity(
            context: Context,
            sn: String?,
            originJson: String?,
            pic: String?,
            alarmID: String?,
            time: String?,
            hasTalkPermission: Boolean,
            alarmEvent: String?,
            devName: String?,
        ) {
            val intent = Intent(context, IDRVisitorActivity::class.java)
            intent.putExtra(SN, sn)
            intent.putExtra(ORIGIN_JSON, originJson)
            intent.putExtra(PIC_PATH, pic)
            intent.putExtra(TIME, time)
            intent.putExtra(ALARM_ID, alarmID)
            intent.putExtra(HAS_TALK_PERMISSION, hasTalkPermission)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(ALARM_EVENT, alarmEvent)
            intent.putExtra(DEV_NAME, devName)
            context.startActivity(intent)
        }

    }

    private var mPicPath: String? = null
    private var mAlarmTime: String? = null
    private val mVisitorHandler = VisitorHandler(this)
    private var mVibrator: Vibrator? = null
    private var mRingtone: Ringtone? = null
    private var mNeedSleep = false
    private var mDevBatteryManager: IdrDevBatteryManager? = null
    private var mPreloadReview: PreloadReview? = null
    private var mAudioManager: AudioManager? = null
    private var mKeyguardLock: KeyguardLock? = null
    private var mHasTalkPermission = true
    private lateinit var mHardwareKeyWatcher: HardwareKeyWatcher


    private val viewModel: IDRVisitorViewModel by viewModels()

    override val layoutId: Int
        get() = R.layout.activity_idr_visitor

    override fun getVM(): IDRVisitorViewModel = viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O && isTranslucentOrFloating()) {
            fixOrientation()
        }
        super.onCreate(savedInstanceState)
        turnScreenOn()
        val alarmID = intent.getStringExtra(ALARM_ID)
        Timber.d("Call SN %s", intent.getStringExtra(SN))
        Timber.d("Call PIC %s", intent.getStringExtra(PIC_PATH))

        // Listener event press button HOME, RECENTLY, GESTURE navigation
        mHardwareKeyWatcher = HardwareKeyWatcher(this)
        mHardwareKeyWatcher.setOnHardwareKeysPressedListenerListener(object :
            HardwareKeyWatcher.OnHardwareKeysPressedListener {
            override fun onHomePressed() {
                Timber.d("onHomePressed")
                JFCameraManager.alarmId = intent.getStringExtra(ALARM_ID)!!
                finish()
            }

            override fun onRecentAppsPressed() {
                Timber.d("onRecentAppsPressed")
                JFCameraManager.alarmId = intent.getStringExtra(ALARM_ID)!!
                finish()
            }
        })
        mHardwareKeyWatcher.startWatch()

        if (StringUtils.contrastIgnoreCase(
                IdrDefine.getLastCallID(this, intent.getStringExtra(SN)).toString(), alarmID
            ) && JFCameraManager.alarmId == alarmID
        ) {
            EventBus.getDefault().post(
                IDRCallResult(
                    IDRCallResult.RESULT_REOPEN_APP,
                    intent.getStringExtra(SN), -1
                )
            )
            finish()
            return
        }

        removeFromSleepQueue(intent.getStringExtra(SN))
        EventBus.getDefault().post(
            IDRReceiverCall(
                intent.getStringExtra(SN)
            )
        )
        mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        mAudioManager?.mode = AudioManager.MODE_RINGTONE
        initView()
        initData(intent.getStringExtra(SN))
        mDevBatteryManager =
            IdrDevBatteryManager(
                intent.getStringExtra(SN)
            )
        mDevBatteryManager?.setOnBatteryLevelListener(
            LowPowerPrompt(
                this,
                intent.getStringExtra(SN)
            )
        )
        mDevBatteryManager?.startReceive()
        mPreloadReview =
            PreloadReview(
                this,
                intent.getStringExtra(SN)
            )
        mPreloadReview?.preload()
        Timber.d("prebuffer " + mPreloadReview?.previewHandle)

        binding.txtIgnore.setOnClickListener {
            if (mPreloadReview != null) {
                Timber.d("prebuffer stop " + mPreloadReview?.previewHandle)
                mNeedSleep = true
                mPreloadReview?.stop()
                mPreloadReview?.onDestroy()
                mPreloadReview = null
            }
            if (mDevBatteryManager != null) {
                mDevBatteryManager?.onDestroy()
                mDevBatteryManager = null
            }
            if (mKeyguardLock != null) {
                mKeyguardLock?.reenableKeyguard()
            }
            JFCameraManager.alarmId = intent.getStringExtra(ALARM_ID)!!
            finish()
        }

        binding.txtAnswer.setOnClickListener {
            if (mPreloadReview == null) {
                EventBus.getDefault().post(
                    IDRCallResult(
                        IDRCallResult.RESULT_OPEN_REVIEW,
                        intent.getStringExtra(SN),
                        0, intent.getStringExtra(ALARM_EVENT)
                    )
                )
            } else {
                EventBus.getDefault().post(
                    IDRCallResult(
                        IDRCallResult.RESULT_OPEN_REVIEW,
                        intent.getStringExtra(SN),
                        mPreloadReview!!.previewHandle,
                        intent.getStringExtra(ALARM_EVENT)
                    )
                )
                mPreloadReview?.onDestroy()
                mPreloadReview = null
            }
//            val model = IDRCallRecordModel(intent.getStringExtra(SN))
//            model.saveReceivedCalls(
//                this, intent.getStringExtra(ALARM_ID), intent.getStringExtra(TIME)
//            )
            if (mDevBatteryManager != null) {
                mDevBatteryManager?.onDestroy()
                mDevBatteryManager = null
            }
            if (mKeyguardLock != null) {
                mKeyguardLock?.reenableKeyguard()
            }
            finish()
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (event?.keyCode == KeyEvent.KEYCODE_BACK) {
            Timber.d("onBackPressed")
            JFCameraManager.alarmId = intent.getStringExtra(ALARM_ID)!!
            finish()
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    /*
     * Call Notification has notification id which is timestamp of startTime
     */
    private fun removeOnNotificationBar(dateTime: String?) {
        val id = convertDateToTimeStamp(dateTime).toInt()
        Timber.tag("HieuNT").e("Remove Notification: $id")
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(id)
    }

    private fun convertDateToTimeStamp(date: String?): Long {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return try {
            val dateTime = formatter.parse(date)
            dateTime?.time ?: System.currentTimeMillis()
        } catch (ex: Exception) {
            System.currentTimeMillis()
        }
    }

    private fun removeFromSleepQueue(sn: String?) {
        IDRModel.removeFromSleepQueue(this, sn)
    }

    private fun addToSleepQueue(sn: String?) {
        if (IdrDefine.playDevicesContains(sn)) {
            return
        }
        IDRSleepModel.sleep(sn)
    }

    private fun addToSleepQueue(sn: String, delayUpdateDevStateTimes: Int) {
        if (IdrDefine.playDevicesContains(sn)) return
        IDRSleepModel.sleep(sn, delayUpdateDevStateTimes)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
//        if (mVoiceReplyFragment != null && mVoiceReplyFragment.isVisible()) {
//            return
//        }

        val newIntent = intent
        val oldIntent = getIntent()
        Timber.d("Call new SN " + newIntent?.getStringExtra(SN))
        Timber.d("Call new PIC " + newIntent?.getStringExtra(PIC_PATH))
        if (newIntent?.getStringExtra(SN) != oldIntent?.getStringExtra(SN)) {
//            mBtnVoiceReply.setVisibility(View.GONE)
            removeFromSleepQueue(newIntent?.getStringExtra(SN))
            addToSleepQueue(oldIntent?.getStringExtra(SN))
            mDevBatteryManager?.onDestroy()
            mDevBatteryManager =
                IdrDevBatteryManager(
                    newIntent?.getStringExtra(SN)
                )
            mDevBatteryManager?.setOnBatteryLevelListener(
                LowPowerPrompt(
                    this,
                    newIntent?.getStringExtra(SN)
                )
            )
            mDevBatteryManager?.startReceive()
            if (mPreloadReview != null) {
                mPreloadReview?.stop()
                mPreloadReview?.onDestroy()
            }
            mPreloadReview =
                PreloadReview(
                    this,
                    newIntent?.getStringExtra(SN)
                )
            mPreloadReview?.preload()
        }
        setIntent(newIntent)
        initData(newIntent?.getStringExtra(SN))
        if (IdrDefine.getFishFrame(this, intent?.getStringExtra(SN)) != null) {
            binding.imgPushPic.setDefaultBackground(R.drawable.idr_call_default_normal)
        } else {
            binding.imgPushPic.setDefaultBackground(R.drawable.idr_call_default_normal)
        }
    }

    private fun initData(devId: String?) {
        val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        val lockTag = localClassName
        mKeyguardLock = keyguardManager.newKeyguardLock(lockTag)
        if (mKeyguardLock != null) {
            mKeyguardLock?.disableKeyguard()
        }
//        if (mCurLoadFuture != null) {
//            mCurLoadFuture.cancel(true)
//            mCurLoadFuture = null
//        }
        mPicPath = intent.getStringExtra(PIC_PATH)
        mPicPath = mPicPath?.replace(":", "_")
        mAlarmTime = intent.getStringExtra(TIME)
        removeOnNotificationBar(mAlarmTime)
        mHasTalkPermission = intent.getBooleanExtra(HAS_TALK_PERMISSION, true)
//        mRequestImgTime = System.currentTimeMillis()
        mVisitorHandler.removeMessages(VisitorHandler.WHAT_DELAY_LOAD)
        mVisitorHandler.sendEmptyMessageDelayed(VisitorHandler.WHAT_DELAY_LOAD, 100)
        mVisitorHandler.removeMessages(VisitorHandler.WHAT_FINISH)
        mVisitorHandler.sendEmptyMessageDelayed(VisitorHandler.WHAT_FINISH, 16000)
        mVisitorHandler.removeMessages(VisitorHandler.WHAT_SHOW_DELAY_TIME)
        showDelayTime(15)
        Timber.d("Request Image Address " + mPicPath.toString())
        binding.tvSn.text = intent.getStringExtra(DEV_NAME)
        viewModel.devId = devId
    }

    private fun showDelayTime(time: Int) {
        val sec = if (time >= 10) time.toString() else "0$time"
        binding.txtDelayTime.text = "00:$sec"
        if (time > 0) {
            val msg = Message.obtain()
            msg.what = VisitorHandler.WHAT_SHOW_DELAY_TIME
            msg.arg1 = time - 1
            mVisitorHandler.sendMessageDelayed(msg, 1000)
        }
    }

    override fun onClick(v: View?) {
//        mNeedSleep = true
//        if (mPreloadReview != null) {
//            Log.callLog("预缓冲 stop " + mPreloadReview!!.preload())
//            mPreloadReview!!.stop()
//            mPreloadReview!!.onDestroy()
//            mPreloadReview = null
//        }
//        mDevBatteryManager!!.onDestroy()
//        mDevBatteryManager = null
//        EventBus.getDefault()
//            .post(
//                IDRCallResult(
//                    IDRCallResult.RESULT_OPEN_MSG,
//                    intent.getStringExtra(SN)
//                )
//            )
//        finish()
    }


    private fun initView() {

        if (IdrDefine.getFishFrame(this, intent.getStringExtra(SN)) != null) {
            binding.imgPushPic.setDefaultBackground(R.drawable.idr_call_default_normal)
        } else {
            binding.imgPushPic.setDefaultBackground(R.drawable.idr_call_default_normal)
        }
//        binding.imgPushPic.layoutParams.width = Resources.getSystem().displayMetrics.widthPixels
//        binding.imgPushPic.layoutParams.height = binding.imgPushPic.layoutParams.width
//        val width = DeviceUtil.getScreenWidth(this)
//        binding.imgPushPic.layoutParams = LinearLayout.LayoutParams(width, width)
        initRing()
        shock()
    }

    private fun initRing() {
        val pos = IdrDefine.getRing(this, intent.getStringExtra(SN))
        if (pos < 0) {
            mRingtone = RingUtils.getDefaultRingtone(this)
            if (mRingtone == null) {
                mRingtone = RingUtils.getRingtone(this, 0)
            }
        } else {
            mRingtone = RingUtils.getRingtone(this, pos)
            if (mRingtone == null) {
                mRingtone = RingUtils.getDefaultRingtone(this)
                if (mRingtone == null) {
                    mRingtone = RingUtils.getRingtone(this, 0)
                }
                IdrDefine.putRing(this, intent.getStringExtra(SN), -1)
            }
        }
        if (mRingtone == null) {
            return
        }
        if (mRingtone?.isPlaying == true) {
            mRingtone?.stop()
        }
        RingUtils.setRingtoneRepeat(mRingtone, true)
        mRingtone?.play()
    }

    private fun shock() {
        if (IdrDefine.getCallShock(this, intent.getStringExtra(SN))) {
            mVibrator = applicationContext
                .getSystemService(VIBRATOR_SERVICE) as Vibrator
            mVibrator?.vibrate(
                longArrayOf(
                    1000, 800,
                    1000, 800,
                    1000, 800,
                    1000, 400
                ),
                0
            )
        } else {
            //不支持震动取消
            if (null != mVibrator) {
                mVibrator?.cancel()
            }
        }
    }

    private fun loadImg() {
        mPreloadReview!!.getPreviewPic(
            mAlarmTime,
            mPicPath,
            object : PreloadReview.OnPreviewPicListener {
                override fun onPicPath(picPath: String?) {
                    if (picPath != null) {
                        binding.imgPushPic.setImagePath(picPath)
                        val width = DeviceUtil.getScreenWidth(this@IDRVisitorActivity)
                        binding.imgPushPic.layoutParams =
                            LinearLayoutCompat.LayoutParams(width, width)
                        if (mPicPath != null) {
                            val file = File(mPicPath)
                            if (file.exists()) {
                                file.delete()
                            }
                        }
                    }
                }

                override fun onFailed() {
                    mVisitorHandler.sendEmptyMessageDelayed(VisitorHandler.WHAT_DELAY_LOAD, 100)
                }
            })
    }

    private fun delayOver() {
        mNeedSleep = true
        finish()
    }

    override fun onBackPressed() {
        mNeedSleep = true
        mDevBatteryManager!!.onDestroy()
        mDevBatteryManager = null
        super.onBackPressed()
    }

    override fun onResume() {
        super.onResume()
        mVisitorHandler.removeMessages(VisitorHandler.WHAT_DELAY_ANIM)
        mVisitorHandler.sendEmptyMessageDelayed(VisitorHandler.WHAT_DELAY_ANIM, 1000)
        Timber.d("onResume")
    }

    override fun onRestart() {
        super.onRestart()
        Timber.d("onRestart")
//        if (mVoiceReplyFragment == null || !mVoiceReplyFragment.isVisible()) {
//
//        }
        if (mRingtone != null) {
            if (mAudioManager == null) {
                mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            }
            if (mAudioManager?.mode != AudioManager.MODE_RINGTONE) {
                mAudioManager?.mode = AudioManager.MODE_RINGTONE
            }
            mRingtone?.stop()
            mRingtone?.play()
        }
        shock()
    }

    override fun onStop() {
        super.onStop()
        Timber.d("onStop")
        if (mRingtone != null) {
            mRingtone?.stop()
        }
        if (mVibrator != null) {
            mVibrator?.cancel()
        }
        mVisitorHandler.removeMessages(VisitorHandler.WHAT_DELAY_ANIM)
//        if (mAnimationSet != null) {
//            mAnimationSet.cancel()
//        }
    }

    override fun onDestroy() {
        turnScreenOff()
        super.onDestroy()
        mAudioManager = null
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        audioManager.mode = AudioManager.MODE_NORMAL
        if (mDevBatteryManager != null) {
            mDevBatteryManager?.onDestroy()
        }
        mHardwareKeyWatcher.stopWatch();
        IdrDefine.putLastCallID(
            this, intent.getStringExtra(SN),
            intent.getStringExtra(ALARM_ID)!!.toLong()
        )
//        if (mCurLoadFuture != null) {
//            mCurLoadFuture.cancel(true)
//            mCurLoadFuture = null
//        }
        if (mPreloadReview != null) {
            mPreloadReview?.stop()
            mPreloadReview?.onDestroy()
            Timber.d("prebuffer onDestroy stop " + mPreloadReview?.previewHandle)
        }
        if (mVibrator != null && mVibrator!!.hasVibrator()) {
            mVibrator?.cancel()
        }
        if (mVisitorHandler != null) {
            mVisitorHandler.removeMessages(VisitorHandler.WHAT_DELAY_LOAD)
            mVisitorHandler.removeMessages(VisitorHandler.WHAT_FINISH)
            mVisitorHandler.removeMessages(VisitorHandler.WHAT_DELAY_ANIM)
            mVisitorHandler.removeMessages(VisitorHandler.WHAT_SHOW_DELAY_TIME)
            mVisitorHandler.destroy()
        }
//        if (mAnimationSet != null) {
//            mAnimationSet.cancel()
//        }
        if (mRingtone != null) {
            mRingtone?.stop()
        }
//        if (mNeedSleep) {
//            addToSleepQueue(intent.getStringExtra(SN))
//        } else {
//            addToSleepQueue(intent.getStringExtra(SN)!!, 15000)
//        }
        if (mPicPath != null) {
            val file = File(mPicPath)
            if (file.exists()) {
                file.delete()
            }
        }
    }





    private class VisitorHandler(view: IDRVisitorActivity?) : Handler() {
        private val mViewWeak: WeakReference<IDRVisitorActivity?>?

        init {
            mViewWeak = WeakReference(view)
        }

        fun destroy() {
            mViewWeak?.clear()
        }

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (mViewWeak?.get() == null) {
                return
            }
            if (mViewWeak.get()?.isFinishing == true) {
                return
            }
            when (msg.what) {
                WHAT_FINISH -> mViewWeak.get()?.delayOver()
                WHAT_DELAY_LOAD -> mViewWeak.get()?.loadImg()
                WHAT_DELAY_ANIM -> {}
                WHAT_SHOW_DELAY_TIME -> mViewWeak.get()?.showDelayTime(msg.arg1)
                else -> {}
            }
        }

        companion object {
            const val WHAT_DELAY_LOAD = 100
            const val WHAT_FINISH = 101
            const val WHAT_DELAY_ANIM = 102
            const val WHAT_SHOW_DELAY_TIME = 103
        }
    }

    private fun isTranslucentOrFloating(): Boolean {
        var isTranslucentOrFloating = false
        try {
            val styleableRes = Class.forName("com.android.internal.R\$styleable")
                .getField("Window")[null] as IntArray
            val ta = obtainStyledAttributes(styleableRes)
            val m =
                ActivityInfo::class.java.getMethod(
                    "isTranslucentOrFloating",
                    TypedArray::class.java
                )
            m.isAccessible = true
            isTranslucentOrFloating = m.invoke(null, ta) as Boolean
            m.isAccessible = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return isTranslucentOrFloating
    }

    private fun fixOrientation(): Boolean {
        try {
            val field = Activity::class.java.getDeclaredField("mActivityInfo")
            field.isAccessible = true
            val o = field[this] as ActivityInfo
            o.screenOrientation = -1
            field.isAccessible = false
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }


}