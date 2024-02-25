package com.viettel.vht.sdk.model.jfcall;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.manager.device.idr.IdrDefine;

import java.lang.ref.WeakReference;

/**
 * Created by SJ on 2017-11-17.
 */

public class LowPowerPrompt implements IdrDevBatteryManager.OnBatteryLevelListener {
    private WeakReference<Context> mContextWeakReference;
    private boolean mLowPowPrompted = false;
    private String mDevSN;

    public LowPowerPrompt(Context context, String devSN) {
        mContextWeakReference = new WeakReference<>(context.getApplicationContext());
        mDevSN = devSN;
    }

    private Context getContext() {
        if (mContextWeakReference == null)
            return null;
        return mContextWeakReference.get();
    }

    @Override
    public void onBatteryLevel(int devStorageStatus, int electable, int level, int percent) {
        if (electable == 3)
            return;
        if (electable >= 0 && electable < 3) {
            if (!TextUtils.isEmpty(mDevSN))
                IdrDefine.putPower(mContextWeakReference.get(), mDevSN, electable, percent);
        }
        if (getContext() == null)
            return;
        boolean isCharging = electable == 1;
        if (!isCharging &&
                percent <= 20 && percent >= 0 &&
                !mLowPowPrompted) {
            mLowPowPrompted = true;
            Context context = getContext();
            if (context == null)
                return;
            if(IdrDefine.getLowBatteryPush(context,mDevSN)) {
                Toast.makeText(context, "Low quantity of electricity, please charge it or replace the battery in time", Toast.LENGTH_LONG).show();
            }
        }
    }
}
