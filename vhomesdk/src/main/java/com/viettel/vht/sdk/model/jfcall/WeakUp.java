package com.viettel.vht.sdk.model.jfcall;

import android.os.Message;
import android.text.TextUtils;

import com.lib.EFUN_ERROR;
import com.lib.EUIMSG;
import com.lib.FunSDK;
import com.lib.MsgContent;
import com.lib.SDKCONST;
import com.manager.device.idr.IDRStateResult;
import com.viettel.vht.sdk.notification.CallBack;

import org.greenrobot.eventbus.EventBus;

import timber.log.Timber;

/**
 * Created by SJ on 2017-10-11.
 */

public class WeakUp extends BaseRequest {
    public static final int GET_DEV_STATE_FROM_SERVER_ONLY = 1;//仅仅从服务器获取设备状态
    public static final int IDR_AWAKEN = 10000;//使用本地状态 正在唤醒中
    public static final int IDR_WEAK_SUCCESS = 10001;//使用本地状态 唤醒成功
    public static final int IDR_WEAK_FAILURE = 10002;//使用本地状态 唤醒失败
    public static final int IDR_NO_WEAK = 10003;//使用服务器刷新后的状态
    public static final int IDR_SLEEP = 10004;//使用本地状态 休眠中
    private int mState;

    public WeakUp(String sn) {
        super(sn);
        mState = IDR_NO_WEAK;
    }

    public String getSN() {
        return mSN;
    }

    public int getState() {
        return mState;
    }

    public void setState(int state) {
        mState = state;
    }

    public boolean weakUp(CallBack callBack) {
        if (TextUtils.isEmpty(mSN)) {
            return false;
        }
        if (isSending()) {
            return false;
        }
        mState = IDR_AWAKEN;
        mCallBack = callBack;
        FunSDK.DevWakeUp(mID, mSN, 0);
        System.out.println("DevWakeUp");
        Timber.d("send_" + mSN);
        return true;
    }

    public boolean weakUpDevNeedCheckDevState(CallBack callBack) {
        //检测设备状态和发送唤醒命令并发进行
        //从服务器端获取状态，因为回调中只返回在线或者离线，需要获取设备休眠状态需要再调用 GetDevState
        if (TextUtils.isEmpty(mSN)) {
            return false;
        }
        FunSDK.SysGetDevState(mID, mSN, Integer.MAX_VALUE);
        return weakUp(callBack);
    }

    public boolean getDevStateFromServer(CallBack callBack) {
        if (TextUtils.isEmpty(mSN)) {
            return false;
        }
        FunSDK.SysGetDevState(mID, mSN, GET_DEV_STATE_FROM_SERVER_ONLY);
        mCallBack = callBack;
        return true;
    }

    public void cancelWeakUp() {
        mCallBack = null;
        mState = IDR_NO_WEAK;
        Timber.d("cancel_" + mSN);
    }

    @Override
    public int OnFunSDKResult(Message msg, MsgContent ex) {
        if (msg.what == EUIMSG.SYS_GET_DEV_STATE) {
            int state = FunSDK.GetDevState(mSN, SDKCONST.EFunDevStateType.IDR);
            if (ex.seq == GET_DEV_STATE_FROM_SERVER_ONLY) {
                this.mState = IDR_NO_WEAK;
                if (state == SDKCONST.EFunDevState.SLEEP_UNWEAK) {
                    System.out.println("SYS_GET_DEV_STATE:" + state);
                    if (null != mCallBack) {
                        mCallBack.onFail(state);
                    }
                }else {
                    if (null != mCallBack) {
                        mCallBack.onSuccess(state);
                    }
                }
                mCallBack = null;
                return 0;
            }else {
                if (state == SDKCONST.EFunDevState.SLEEP_UNWEAK) {
                    System.out.println("SYS_GET_DEV_STATE:" + state);
                    this.mState = IDR_NO_WEAK;
                    if (null != mCallBack) {
                        mCallBack.onFail(state);
                        EventBus.getDefault().post(new IDRStateResult(mSN, mState));
                    }
                    onDestroy();
                    return 0;
                }
            }
        }else if (msg.what != EUIMSG.DEV_WAKEUP) {
            return 0;
        }
        if (msg.arg1 < 0) {
            if (msg.arg1 == EFUN_ERROR.EE_DVR_PASSWORD_NOT_VALID
                    || msg.arg1 == EFUN_ERROR.EE_DVR_PASSWORD_NOT_VALID2)
                mState = IDR_WEAK_SUCCESS;
            else
                mState = IDR_WEAK_FAILURE;
        } else {
            mState = IDR_WEAK_SUCCESS;
        }
        msg.obj = this;
        Timber.d("result_" + mSN + "_" + msg.arg1 + "_State_" + mState);
        return super.OnFunSDKResult(msg, ex);
    }
}
