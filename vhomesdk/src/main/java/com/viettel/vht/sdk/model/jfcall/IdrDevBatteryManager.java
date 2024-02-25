package com.viettel.vht.sdk.model.jfcall;

import android.os.Message;
import android.text.TextUtils;

import com.basic.G;
import com.lib.EUIMSG;
import com.lib.FunSDK;
import com.lib.IFunSDKResult;
import com.lib.MsgContent;
import com.lib.SDKCONST;
import com.lib.sdk.bean.ElectCapacityBean;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import timber.log.Timber;

/**
 * Device power display management
 * Created by hws on 2017-10-25.
 */

public class IdrDevBatteryManager implements IFunSDKResult {
    private int mUserId;
    private String mDevId;
    private boolean mIsInit;
    private OnBatteryLevelListener mOnBatteryLevelLs;
    private int seq = 0;
    private ElectCapacityBean electCapacityBean;
    private boolean isSendingStop = false;
    private int mStopType = 0;
    private Disposable mDisposable;

    public IdrDevBatteryManager(String devId) {
        this.mDevId = devId;
        mUserId = FunSDK.GetId(mUserId, this);
    }

    public void startReceive() {
        if (isSendingStop)
            return;
        if (!mIsInit) {
            seq = 0;
            Timber.d("start upload " + mDevId);
            FunSDK.DevStartUploadData(mUserId, mDevId,
                    SDKCONST.UploadDataType.SDK_ELECT_STATE, 3);
        }
    }

    public void restartReceive() {
        mIsInit = false;
        seq = Integer.MAX_VALUE;
        mOnBatteryLevelLs = null;
        Timber.d("stop upload " + mDevId);
        mStopType = 1;
        FunSDK.DevStopUploadData(mUserId, mDevId,
                SDKCONST.UploadDataType.SDK_ELECT_STATE, 1);
    }

    public void stopReceive() {
        seq = Integer.MAX_VALUE;
        mOnBatteryLevelLs = null;
        isSendingStop = true;
        mStopType = 0;
        Timber.d("stop upload " + mDevId);
        FunSDK.DevStopUploadData(mUserId, mDevId,
                SDKCONST.UploadDataType.SDK_ELECT_STATE, 0);
    }

    @Override
    public int OnFunSDKResult(Message msg, MsgContent ex) {
        switch (msg.what) {
            case EUIMSG.EMSG_DEV_START_UPLOAD_DATA:
                Timber.d("start upload result " + seq + "   " + msg.arg1 + " " + ex.seq);
                if (msg.arg1 < 0) {
                    if (seq < 3) {
                        seq++;
                        FunSDK.DevStartUploadData(mUserId, mDevId,
                                SDKCONST.UploadDataType.SDK_ELECT_STATE, 0);
                    }
                } else {
                    mIsInit = true;
                }
                break;
            case EUIMSG.EMSG_DEV_STOP_UPLOAD_DATA:
                Timber.d("stop upload result " + seq + "   " + msg.arg1 + " " + ex.seq);
                seq = 3;
                // 停止接收实时消息
                mIsInit = false;
                if (mStopType == 1) {
                    mDisposable = Flowable
                            .just(0)
                            .delay(1, TimeUnit.SECONDS)
                            .subscribe(new Consumer<Integer>() {
                                @Override
                                public void accept(Integer integer) throws Exception {
                                    isSendingStop = false;
                                    startReceive();
                                    if (mDisposable != null)
                                        mDisposable.dispose();
                                }
                            });
                }
                break;
            case EUIMSG.EMSG_DEV_ON_UPLOAD_DATA:
                parseBatteryState(G.ToString(ex.pData));
                break;
            default:
                break;
        }
        return 0;
    }

    private void parseBatteryState(String jsonStr) {
        if (TextUtils.isEmpty(jsonStr))
            return;
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            jsonObject = jsonObject.optJSONObject(ElectCapacityBean.CLASSNAME);
            electCapacityBean = new ElectCapacityBean();
            Timber.d(mDevId + "_Power_" + jsonStr);
            electCapacityBean.devStorageStatus = jsonObject.optInt("DevStorageStatus", -2);
            electCapacityBean.electable = (jsonObject.optInt("electable", 3));
            electCapacityBean.percent = (jsonObject.optInt("percent", 4));
            if (jsonObject.has("level")) {
                electCapacityBean.level = (jsonObject.optInt("level", -1));
            }
            if (null != mOnBatteryLevelLs) {
                mOnBatteryLevelLs.onBatteryLevel(
                        electCapacityBean.devStorageStatus,
                        electCapacityBean.electable,
                        electCapacityBean.level,
                        electCapacityBean.percent);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getLastStorageStatus() {
        if (electCapacityBean == null) {
            Timber.d("sd card status：electCapacityBean null");
            return ElectCapacityBean.UNKNOWN_STORAGE;
        }
        return electCapacityBean.devStorageStatus;
    }

    public void setOnBatteryLevelListener(OnBatteryLevelListener ls) {
        this.mOnBatteryLevelLs = ls;
    }

    public void destroyNoStop() {
        isSendingStop = true;
        mOnBatteryLevelLs = null;
        FunSDK.UnRegUser(mUserId);
        if (mDisposable != null) {
            mDisposable.dispose();
        }
    }

    public void onDestroy() {
        stopReceive();
        mOnBatteryLevelLs = null;
        FunSDK.UnRegUser(mUserId);
        if (mDisposable != null) {
            mDisposable.dispose();
        }
    }

    public interface OnBatteryLevelListener {
        void onBatteryLevel(int devStorageStatus, int electable, int level, int percent);
    }
}
