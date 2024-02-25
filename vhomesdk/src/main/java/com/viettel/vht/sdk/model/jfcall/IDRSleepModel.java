package com.viettel.vht.sdk.model.jfcall;

import android.text.TextUtils;

import com.manager.device.idr.IDRSleepService;
import com.manager.device.idr.Sleep;

import org.greenrobot.eventbus.EventBus;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import timber.log.Timber;

/**
 * Created by SJ on 2017-05-18.
 */

public class IDRSleepModel {
    private static final int SLEEP_DELAY_TIME = 15;
    private Map<String, Sleep> mIDRModelMap = new ConcurrentHashMap<>();
    private ExecutorService mExecutor;
    private SleepRunnable mSleepRunnable;

    public IDRSleepModel() {

    }

    public static final void sleep(String sn) {
        Sleep sleep = new Sleep(sn);
        sleep.sleep(null);
    }

    public static final void sleep(String sn,int delayUpdateDevStateTimes) {
        Sleep sleep = new Sleep(sn);
        sleep.sleep(null,delayUpdateDevStateTimes);
    }

    public void addSleepIDR(String sn) {
        if (TextUtils.isEmpty(sn)) {
            return;
        }
        synchronized (mIDRModelMap) {
            if (mIDRModelMap.containsKey(sn)) {
                Sleep sleep = mIDRModelMap.get(sn);
                sleep.setSleepCountDown(SLEEP_DELAY_TIME);
                Timber.d("exist_" + sn + "_" + sleep.getSleepCountDown());
            } else {
                Timber.d("add_" + sn);
                Sleep sleep = new Sleep(sn);
                sleep.setSleepCountDown(SLEEP_DELAY_TIME);
                mIDRModelMap.put(sn, sleep);
            }
            startSleepRunnable();
        }
    }

    public void removeSleepIDR(String sn) {
        if (TextUtils.isEmpty(sn)) {
            return;
        }
        synchronized (mIDRModelMap) {
            if (mIDRModelMap.containsKey(sn)) {
                mIDRModelMap.remove(sn);
                Timber.d("remove_" + sn);
                if (mIDRModelMap.isEmpty()) {
                    EventBus.getDefault().post(IDRSleepService.SLEEP_QUEUE_EMPTY);
                }
            } else {
                if (mIDRModelMap.isEmpty()) {
                    EventBus.getDefault().post(IDRSleepService.SLEEP_QUEUE_EMPTY);
                }
            }
        }
    }

    public void startSleepRunnable() {
        if (mExecutor == null) {
            mExecutor = Executors.newSingleThreadExecutor();
        }
        if (mSleepRunnable == null) {
            mSleepRunnable = new SleepRunnable();
        }
        Timber.d("startSleepRunnable_" + mSleepRunnable.running);
        if (!mSleepRunnable.running) {
            mSleepRunnable.restart();
            mExecutor.execute(mSleepRunnable);
        }
    }

    public void stopSleepRunnable() {
        if (mSleepRunnable != null) {
            mSleepRunnable.running = false;
        }
    }

    public void onDestroy() {
        stopSleepRunnable();
        synchronized (mIDRModelMap) {
            Iterator iter = mIDRModelMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, Sleep> entry = (Map.Entry) iter.next();
                entry.getValue().onDestroy();
                iter.remove();
            }
        }
        if (mExecutor != null && !mExecutor.isShutdown()) {
            mExecutor.shutdownNow();
        }

    }

    public class SleepRunnable implements Runnable {
        public volatile boolean running;
        private volatile int mEmptyTime;

        public void restart(){
            running = true;
            mEmptyTime = 0;
        }

        @Override
        public void run() {
            while (running) {
                try {
                    synchronized (mIDRModelMap) {
                        if (mIDRModelMap.isEmpty()) {
                            if (mEmptyTime >= 30) {
                                running = false;
                                EventBus.getDefault().post(IDRSleepService.SLEEP_QUEUE_EMPTY);
                                return;
                            }
                        } else {
                            mEmptyTime = 0;
                        }
                        Iterator iter = mIDRModelMap.entrySet().iterator();
                        while (iter.hasNext()) {
                            final Map.Entry<String, Sleep> entry = (Map.Entry) iter.next();
                            int sleepCount = entry.getValue().getSleepCountDown();
                            if (sleepCount <= 1) {
                                entry.getValue().sleep(null);
                                        /**/
//                                entry.getValue().onDestroy();
                                        /**/
                                iter.remove();
                            } else {
                                entry.getValue().setSleepCountDown(sleepCount - 5);
                            }
                        }
                        if (mIDRModelMap.isEmpty()) {
                            if (mEmptyTime >= 30) {
                                running = false;
                                EventBus.getDefault().post(IDRSleepService.SLEEP_QUEUE_EMPTY);
                                return;
                            }
                        } else {
                            mEmptyTime = 0;
                        }
                    }
                    mEmptyTime += 5;
                    Timber.d("EmptyTime  " + mEmptyTime);
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            running = false;
            EventBus.getDefault().post(IDRSleepService.SLEEP_QUEUE_EMPTY);
        }
    }
}
