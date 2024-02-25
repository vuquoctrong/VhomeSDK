package com.viettel.vht.sdk.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


import timber.log.Timber;

/**
 * Created by zhangyongyong on 2017-05-23-14:19.
 */

public class NotificationClick extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
//        AlarmRingPlayManager.getInstance(context).stopPlay();
        Timber.d("NotificationClicked");
    }
}
