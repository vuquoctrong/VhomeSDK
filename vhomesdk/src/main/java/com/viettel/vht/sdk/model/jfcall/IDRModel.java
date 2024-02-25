package com.viettel.vht.sdk.model.jfcall;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Message;
import android.text.TextUtils;

import com.lib.EFUN_ERROR;
import com.lib.FunSDK;
import com.lib.MsgContent;
import com.lib.SDKCONST;
import com.lib.sdk.bean.ElectCapacityBean;
import com.lib.sdk.bean.idr.SDStorage;
import com.manager.device.idr.BatteryStorageResult;
import com.manager.device.idr.IDRManagerCallBack;
import com.manager.device.idr.IDRSleepService;
import com.manager.device.idr.IDRStateResult;
import com.manager.device.idr.IdrDefine;
import com.viettel.vht.sdk.notification.CallBack;
import com.viettel.vht.sdk.ui.jfcameradetail.call.IDRWeakWaitView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import timber.log.Timber;

/**
 * Created by SJ on 2017-10-11.
 */

public final class IDRModel {
    public static final String IS_ACTIVITY_DESTROY_SLEEP_DEV = "is_activity_destroy_sleep_dev";
    //唤醒三次
    private static final int WAKE_UP_COUNT = 2;
    private WeakUp mWeakUp;
    private boolean mIsExecuteIDRFlow = false;
    private boolean mIsIDR = false;
    private String mSN;
    private boolean mResumed = false;
    private IDRCallBack mIDRCallBack;
    private int mUpNum = 0;
    private Disposable mDisposable;
    private WeakReference<Context> mContextWeakReference;
    private boolean mIsRegisterReceiver = false;
    private boolean mIsHomeSendSleep = true;
    private IdrDevBatteryManager mDevBatteryManager;
    private IDRWeakWaitView mIDRWeakWaitView;
    private IDRPowerListener mIDRPowerListener;
    private SDStorage mSDStorage;
    private CallBack mStorageSizeCallBack;
    /**
     * 监听home键的按下
     */
    private BroadcastReceiver mHomeClickReceiver = new BroadcastReceiver() {
        static final String SYSTEM_DIALOG_REASON_KEY = "reason";
        static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
        static final String SYSTEM_DIALOG_REASON_RECENTAPPS = "recentapps";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mWeakUp != null) {
                if (mWeakUp.getState() == WeakUp.IDR_SLEEP) {
                    Timber.d("IDR Already Sleep->" + mSN);
                    return;
                }
            }
            Timber.d("receive home " + mResumed);
            String action = intent.getAction();
            Timber.d("receive home1  " + action);
            switch (action){
                case Intent.ACTION_CLOSE_SYSTEM_DIALOGS:
                    String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                    Timber.d("receive home2  " + reason);
                    if (SYSTEM_DIALOG_REASON_RECENTAPPS.equals(reason)) {
                        break;
                    }

                    if (!SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
                        return;
                    }

                    if (!mResumed) {
                        return;
                    }
                    break;
                default:
                    return;
            }
            Timber.d("handle addToSleepQueue");
            if (mContextWeakReference.get() != null && mIsHomeSendSleep) {
                addToSleepQueue();
            }
        }
    };

    public IDRModel(final Context context, int devType, String sn) {
        this(context, devType, sn, true);
    }

    public IDRModel(final Context context, int devType, String sn, boolean isRegisterHome) {
        if (context == null || !IdrDefine.isIDR(devType) || TextUtils.isEmpty(sn)) {
            return;
        }
        mSN = sn;
        mIsIDR = true;
        WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo info = wifiManager.getConnectionInfo();
            if (info != null && (info.getSSID().toLowerCase().replace("\"", "")
                    .startsWith("xmjp_idr_"))) {
                return;
            }
        }
        IdrDefine.addToPlayDevices(mSN);
        mIsExecuteIDRFlow = true;
        mContextWeakReference = new WeakReference<>(context);
        mIDRCallBack = new IDRCallBack();
        removeFromSleepQueue();
        if (isRegisterHome) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(mHomeClickReceiver, filter, Context.RECEIVER_EXPORTED);
            } else {
                context.registerReceiver(mHomeClickReceiver, filter);
            }
            mIsRegisterReceiver = true;
        }
        EventBus.getDefault().register(this);
    }

    @Subscribe
    public void receiverSleepMsg(IDRStateResult result) {
        try {
            if (!result.getSN().equals(mSN)) {
                return;
            }

            if (mWeakUp !=null){
                mWeakUp.setState(result.getState());
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final void removeFromSleepQueue(Context context, String sn) {
        if (TextUtils.isEmpty(sn)) {
            return;
        }
        IDRSleepService.start(context, sn, IDRSleepService.CANCEL_SLEEP);
    }

    public static final void addToSleepQueue(Context context, String sn) {
        if (TextUtils.isEmpty(sn)) {
            return;
        }
        IDRSleepService.start(context, sn, IDRSleepService.ADD_SLEEP);
    }

    public static final boolean isWeak(String sn) {
        if (TextUtils.isEmpty(sn)) {
            return false;
        }

        int state = FunSDK.GetDevState(sn, SDKCONST.EFunDevStateType.IDR);
        return state == SDKCONST.EFunDevState.LINE;
    }

    /***
     *
     * @param sn
     * @return  是否可唤醒状态
     */
    public static final boolean isUnWeakUp(String sn) {
        if (TextUtils.isEmpty(sn)) {
            return false;
        }

        int state = FunSDK.GetDevState(sn, SDKCONST.EFunDevStateType.IDR);
        Timber.d(sn + "Whether to wake up the state" + state);
        return state == SDKCONST.EFunDevState.SLEEP_UNWEAK;
    }

    public void initWeakUp() {
        if (mWeakUp == null) {
            mWeakUp = new WeakUp(mSN);
        }
        mWeakUp.setState(WeakUp.IDR_WEAK_SUCCESS);
    }

    public void setHomeSendSleep(boolean homeSendSleep) {
        mIsHomeSendSleep = homeSendSleep;
    }

    public String getSN() {
        return mSN;
    }

    public final void showWait(Activity activity) {
        if (mIDRWeakWaitView == null) {
            mIDRWeakWaitView = new IDRWeakWaitView(activity);
        }
        mIDRWeakWaitView.show();
    }

    public final void dismissWait() {
        if (mIDRWeakWaitView == null) {
            return;
        }
        mIDRWeakWaitView.dismiss();
    }

    public int getWeakUpState() {
        if (mWeakUp == null) {
            return WeakUp.IDR_NO_WEAK;
        }
        return mWeakUp.getState();
    }

    public boolean onCreate(CallBack callBack) {
        boolean isSuccess = weakUp(callBack);
        return isSuccess;
    }

    public boolean onRestart(CallBack callBack) {
        if (!mIsIDR || !mIsExecuteIDRFlow) {
            callBack.onSuccess(true);
            return false;
        }

        if (mContextWeakReference.get() != null) {
            removeFromSleepQueue();
        }

        return weakUpDevNeedCheckDevState(callBack);
    }


    private boolean reGetUp(CallBack callBack) {
        if (!mIsIDR || !mIsExecuteIDRFlow) {
            callBack.onSuccess(true);
            return false;
        }

        if (mWeakUp == null) {
            mWeakUp = new WeakUp(mSN);
        }
        mIDRCallBack.setCallBack(callBack);
        return mWeakUp.weakUp(mIDRCallBack);
    }

    private boolean weakUp(CallBack callBack) {

        if (!mIsIDR || !mIsExecuteIDRFlow) {
            callBack.onSuccess(true);
            return false;
        }

        if (mWeakUp == null) {
            mWeakUp = new WeakUp(mSN);
        }

        mIDRCallBack.setCallBack(callBack);
        boolean isSuccess = mWeakUp.weakUp(mIDRCallBack);
        if (isSuccess) {
            mUpNum = 0;
            callBack.onStartWakeUp();
        }
        return isSuccess;
    }

    private boolean weakUpDevNeedCheckDevState(CallBack callBack) {
        if (!mIsIDR || !mIsExecuteIDRFlow) {
            callBack.onSuccess(true);
            return false;
        }

        if (mWeakUp == null) {
            mWeakUp = new WeakUp(mSN);
        }

        mIDRCallBack.setCallBack(callBack);
        boolean isSuccess = mWeakUp.weakUpDevNeedCheckDevState(mIDRCallBack);
        if (isSuccess) {
            mUpNum = 0;
            callBack.onStartWakeUp();
        }
        return isSuccess;
    }

    public void removeSleepDelay() {
        if (!mIsIDR || !mIsExecuteIDRFlow) {
            return;
        }
        if (mContextWeakReference != null && mContextWeakReference.get() != null) {
            removeFromSleepQueue();
        }
    }

    public void cancelBatteryInfo() {
        if (!mIsIDR) {
            return;
        }
        if (mDevBatteryManager == null) {
            return;
        }
        mDevBatteryManager.stopReceive();
    }

    public int getLastStorageStatus() {
        if (mDevBatteryManager == null) {
            Timber.d("sd card status：mDevBatteryManager null");
            return ElectCapacityBean.UNKNOWN_STORAGE;
        }
        return mDevBatteryManager.getLastStorageStatus();
    }

    public void reReceiveBatteryInfo(IdrDevBatteryManager.OnBatteryLevelListener batteryLevelListener) {
        if (!mIsIDR) {
            return;
        }
        if (mDevBatteryManager == null) {
            mDevBatteryManager = new IdrDevBatteryManager(mSN);
        }
        if (mIDRPowerListener == null) {
            mIDRPowerListener = new IDRPowerListener();
        }
        mDevBatteryManager.restartReceive();
        mIDRPowerListener.setOnBatteryLevelListener(batteryLevelListener);
        mDevBatteryManager.setOnBatteryLevelListener(mIDRPowerListener);
    }

    public void receiveBatteryInfo(IdrDevBatteryManager.OnBatteryLevelListener batteryLevelListener) {
        if (!mIsIDR) {
            return;
        }
        if (mDevBatteryManager == null) {
            mDevBatteryManager = new IdrDevBatteryManager(mSN);
        }
        if (mIDRPowerListener == null) {
            mIDRPowerListener = new IDRPowerListener();
        }
        mIDRPowerListener.setOnBatteryLevelListener(batteryLevelListener);
        mDevBatteryManager.setOnBatteryLevelListener(mIDRPowerListener);
        mDevBatteryManager.startReceive();
    }

    public boolean requestStorageSize(CallBack storageSizeCallBack) {
        if (!mIsIDR) {
            return false;
        }

        mStorageSizeCallBack = storageSizeCallBack;
        if(mSDStorage == null){
            mSDStorage = new SDStorage(mSN);
        }
        return mSDStorage.requestGetSD((IDRManagerCallBack) mStorageSizeCallBack);
    }

    private void addToSleepQueue() {
        if (!mIsIDR) {
            return;
        }
        if (mContextWeakReference.get() != null) {
            IDRSleepService.start(mContextWeakReference.get(),
                    mSN,
                    IDRSleepService.ADD_SLEEP);
        }
    }

    private void removeFromSleepQueue() {
        if (!mIsIDR) {
            return;
        }
        if (mContextWeakReference.get() != null) {
            IDRSleepService.start(mContextWeakReference.get(),
                    mSN,
                    IDRSleepService.CANCEL_SLEEP);
        }
    }

    public boolean isResumed() {
        return mResumed;
    }

    public void onResume() {
        mResumed = true;
        IdrDefine.addToPlayDevices(mSN);
    }

    public void onStop() {
        mResumed = false;
    }

    public void clear() {
        EventBus.getDefault().unregister(this);
        if (mSDStorage != null) {
            mSDStorage.onDestroy();
        }
        if (mIDRPowerListener != null) {
            mIDRPowerListener.destroy();
        }
        if (mDevBatteryManager != null) {
            mDevBatteryManager.onDestroy();
        }
        if (mWeakUp != null) {
            mWeakUp.onDestroy();
        }
        if (mIsRegisterReceiver && mContextWeakReference.get() != null) {
            mContextWeakReference.get().unregisterReceiver(mHomeClickReceiver);
        }
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
        if (mIDRCallBack != null) {
            mIDRCallBack.destroy();
            mIDRCallBack = null;
        }
        if (mIDRWeakWaitView != null) {
            mIDRWeakWaitView.destroy();
            mIDRWeakWaitView = null;
        }
    }

    public void onDestroy() {
        clear();
        IdrDefine.exitPlayDevices(mSN);
        if (!mIsExecuteIDRFlow) {
            return;
        }
        addToSleepQueue();
    }

    private final class IDRPowerListener implements IdrDevBatteryManager.OnBatteryLevelListener {
        private IdrDevBatteryManager.OnBatteryLevelListener onBatteryLevelListener;

        public void setOnBatteryLevelListener(IdrDevBatteryManager.OnBatteryLevelListener
                                                      onBatteryLevelListener) {
            this.onBatteryLevelListener = onBatteryLevelListener;
        }

        public void destroy() {
            onBatteryLevelListener = null;
        }

        @Override
        public void onBatteryLevel(int devStorageStatus, int electable, int level, int percent) {
            if (!mIsIDR) {
                return;
            }
            EventBus.getDefault().post(new BatteryStorageResult(devStorageStatus, electable, level));
            if (onBatteryLevelListener != null) {
                onBatteryLevelListener.onBatteryLevel(devStorageStatus, electable, level, percent);
            }
            if (mContextWeakReference == null || mContextWeakReference.get() == null) {
                return;
            }
            if (electable >= 0 && electable < 3) {
                IdrDefine.putPower(mContextWeakReference.get(), mSN, electable, percent);
            }
        }
    }

    private final class IDRCallBack implements CallBack {
        private CallBack callBack;

        public void setCallBack(CallBack callBack) {
            this.callBack = callBack;
        }

        private void destroy() {
            callBack = null;
        }

        @Override
        public void onStartWakeUp() {
            if (callBack != null) {
                callBack.onStartWakeUp();
            }
        }

        @Override
        public void onError(final Message msg, final MsgContent ex) {
            if (callBack == null) {
                return;
            }
            //密码错误
            if (msg.arg1 == EFUN_ERROR.EE_DVR_PASSWORD_NOT_VALID
                    || msg.arg1 == EFUN_ERROR.EE_DVR_PASSWORD_NOT_VALID2) {
                callBack.onError(msg, ex);
                return;
            }

            Timber.d("Failed to wake up: " + mSN + "  " + mUpNum);
            mUpNum++;
            if (mUpNum >= WAKE_UP_COUNT) {
                callBack.onError(msg, ex);
                destroy();
            } else {
                if (mDisposable != null && !mDisposable.isDisposed()) {
                    mDisposable.dispose();
                }
                mDisposable = Observable
                        .just(0)
                        .delay(1, TimeUnit.SECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Integer>() {
                            @Override
                            public void accept(@NonNull Integer integer) throws Exception {
                                if (!reGetUp(callBack)) {
                                    callBack.onError(msg, ex);
                                    destroy();
                                }
                            }
                        });
            }
        }

        @Override
        public void onFail(int status) {
            if (callBack == null) {
                return;
            }
            callBack.onFail(status);
        }

        @Override
        public void onSuccess(Object obj) {
            if (callBack == null) {
                return;
            }
            if (mResumed) {
                callBack.onSuccess(true);
                destroy();
            } else {
                callBack.onSuccess(false);
                destroy();
            }
        }
    }
}
