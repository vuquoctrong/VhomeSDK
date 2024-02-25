package com.viettel.vht.sdk.notification;

import android.os.Message;

import com.lib.MsgContent;

/**
 * 唤醒回调
 * Created by SJ on 2017-10-11.
 */

public interface CallBack<T>{
    /**
     * 开始唤醒
     */
    void onStartWakeUp();
    /**
     * 唤醒报错回调
     * @param msg
     * @param ex
     */
    void onError(Message msg, MsgContent ex);

    /**
     * 唤醒失败回调
     * @param status 当前的设备状态
     */
    void onFail(int status);

    /**
     * 唤醒成功
     * @param obj 返回的数据
     */
    void onSuccess(T obj);
}