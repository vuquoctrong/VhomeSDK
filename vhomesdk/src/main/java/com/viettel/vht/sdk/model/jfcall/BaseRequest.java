package com.viettel.vht.sdk.model.jfcall;

import android.os.Message;

import com.alibaba.fastjson.annotation.JSONField;
import com.lib.FunSDK;
import com.lib.IFunSDKResult;
import com.lib.MsgContent;
import com.viettel.vht.sdk.notification.CallBack;

/**
 * Created by SJ on 2017-10-11.
 */

public abstract class BaseRequest implements IFunSDKResult {

    @JSONField(serialize = false)
    protected CallBack mCallBack;
    @JSONField(serialize = false)
    protected String mSN;
    @JSONField(serialize = false)
    protected int mID = -1;

    public BaseRequest(String sn) {
        mSN = sn;
        mID = FunSDK.RegUser(this);
    }

    public void onDestroy() {
        if (mID != -1)
            FunSDK.UnRegUser(mID);
        mCallBack = null;
    }

    public boolean isSending() {
        return mCallBack != null;
    }

    @Override
    public int OnFunSDKResult(Message msg, MsgContent ex) {
        if (mCallBack == null)
            return 0;
        if (msg.arg1 < 0) {
            mCallBack.onError(msg, ex);
        } else {
            mCallBack.onSuccess(this);
        }
        mCallBack = null;
        return 0;
    }
}
