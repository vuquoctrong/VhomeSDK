package com.viettel.vht.sdk.model.jfcall;

import android.content.Context;
import android.os.Message;
import android.util.Base64;

import androidx.annotation.NonNull;

import com.basic.G;
import com.lib.EUIMSG;
import com.lib.FunSDK;
import com.lib.Mps.MpsClient;
import com.lib.Mps.XPMS_SEARCH_ALARMINFO_REQ;
import com.lib.MsgContent;
import com.manager.device.idr.IdrDefine;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import timber.log.Timber;

/**
 * Created by SJ on 2017-11-20.
 */

public class PreloadReview extends BaseRequest implements Comparable {
    private int mPreviewHandle = 0;
    private long mStartPreloadTime = -1;
    private String mPicPath;
    private XPMS_SEARCH_ALARMINFO_REQ mInfo;
    private Context mContext;
    public PreloadReview(Context context, String sn) {
        super(sn);
        mContext = context;
    }

    public static final byte[] getFishFrame(String frameStr) {
        byte[] fishFrame = Base64.decode(frameStr.getBytes(), Base64.DEFAULT);
        return fishFrame;
    }

    public String getSN() {
        return mSN;
    }

    public long getStartPreloadTime() {
        return mStartPreloadTime;
    }

    public int getPreviewHandle() {
        return mPreviewHandle;
    }

    public boolean preload() {
//        if (isSending()) {
//            return false;
//        }
//        mStartPreloadTime = System.currentTimeMillis();
//        mPreviewHandle = FunSDK.MediaRealPlay(mID, mSN, 0, 0, null,
//                0);
//        com.xm.device.idr.define.Log.preloadLog(mSN + "_start_" + mPreviewHandle);
//        FunSDK.SetIntAttr(mPreviewHandle, EFUN_ATTR.EOA_SET_MEDIA_VIEW_VISUAL, 0);
//        FunSDK.SetIntAttr(mPreviewHandle,EFUN_ATTR.EOA_MEDIA_YUV_USER, 0);
//
//        if (mPreviewHandle == 0)
//            return false;
//        return true;
        return false;
    }

    public void stop() {
        IdrDefine.clearBufferEndState(mContext, mSN);
        if (mPreviewHandle != 0) {
            Timber.d(mSN + "_stop_" + mPreviewHandle);
            FunSDK.MediaStop(mPreviewHandle);
            mPreviewHandle = 0;
            mStartPreloadTime = -1;
        }
    }

    public void getPreviewPic(String alarmTime,String picPth,OnPreviewPicListener onPreviewPicListener) {
        try {
            this.mPicPath = picPth;
            this.mOnPreviewPicLs = onPreviewPicListener;
            mInfo = new XPMS_SEARCH_ALARMINFO_REQ();
            G.SetValue(mInfo.st_00_Uuid, mSN);

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            calendar.setTime(format.parse(alarmTime));
            mInfo.st_02_StarTime.st_0_year = calendar.get(Calendar.YEAR);
            mInfo.st_02_StarTime.st_1_month = calendar.get(Calendar.MONTH) + 1;
            mInfo.st_02_StarTime.st_2_day = calendar.get(Calendar.DATE);
            mInfo.st_02_StarTime.st_4_hour = calendar.get(Calendar.HOUR_OF_DAY);
            mInfo.st_02_StarTime.st_5_minute = calendar.get(Calendar.MINUTE);
            mInfo.st_02_StarTime.st_6_second = calendar.get(Calendar.SECOND);
            mInfo.st_03_EndTime.st_0_year = calendar.get(Calendar.YEAR);
            mInfo.st_03_EndTime.st_1_month = calendar.get(Calendar.MONTH) + 1;
            mInfo.st_03_EndTime.st_2_day = calendar.get(Calendar.DATE);
            mInfo.st_03_EndTime.st_4_hour = calendar.get(Calendar.HOUR_OF_DAY);
            mInfo.st_03_EndTime.st_5_minute = calendar.get(Calendar.MINUTE);
            mInfo.st_03_EndTime.st_6_second = calendar.get(Calendar.SECOND);
            mInfo.st_04_Channel = 0;
            mInfo.st_06_Number = 0;
            MpsClient.SearchAlarmInfoByTime(mID, G.ObjToBytes(mInfo), 0);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        Timber.d(mSN + "_destroy_" + mPreviewHandle);
        super.onDestroy();
    }

    @Override
    public int OnFunSDKResult(Message msg, MsgContent ex) {
        switch (msg.what) {
            case EUIMSG.START_PLAY:
                if (msg.arg1 < 0) {
                    Timber.d(mSN + "_result_" + msg.arg1 + "_" + mPreviewHandle);
                    stop();
                }
                break;
            case EUIMSG.ON_FRAME_USR_DATA:
                handleFrame(msg, ex);
                break;
            case EUIMSG.ON_PLAY_BUFFER_END:
                Timber.d(mSN + "_ON_PLAY_BUFFER_END_" + msg.arg1 + "_" + mPreviewHandle);
                IdrDefine.putBufferEndState(mContext, mSN, mPreviewHandle);
                break;
            case EUIMSG.MC_SearchAlarmPic:
                if (msg.arg1 >= 0) {
                     mOnPreviewPicLs.onPicPath(ex.str);
                }else {
                    mOnPreviewPicLs.onFailed();
                }
                break;
            case EUIMSG.MC_SearchAlarmInfo:
                if (msg.arg1 >= 0) {
                   String json = G.ToString(ex.pData);
                   if (json != null) {
                       if(json.contains("StorageBucket") || json.contains("\"Pic\":")) {
                           MpsClient.DownloadAlarmImage(mID
                                   , mSN
                                   , mPicPath
                                   , json
                                   , 0
                                   , 0
                                   , 0);
                       }else {
                           mOnPreviewPicLs.onFailed();
                       }
                   }
                }else {
                    mOnPreviewPicLs.onFailed();
                }
                break;
        }
        return super.OnFunSDKResult(msg, ex);
    }

    private void handleFrame(Message msg, MsgContent ex) {
        if (ex.pData != null && ex.pData.length > 8) {
            saveFishFrame(ex.pData, msg.arg2);
        }
    }

    private void saveFishFrame(byte[] pFishParam, int arg2) {
        String frameStr =
                new String(Base64.encodeToString(pFishParam, Base64.DEFAULT));
        IdrDefine.putFishFrame(mContext, mSN, arg2 + "___" + frameStr);
    }

    @Override
    public int compareTo(@NonNull Object o) {
        if (o == null)
            return 1;
        PreloadReview preloadReview = (PreloadReview) o;
        if (preloadReview.getStartPreloadTime() > getStartPreloadTime()) {
            return -1;
        } else if (preloadReview.getStartPreloadTime() == getStartPreloadTime()) {
            return 0;
        }
        return 1;
    }

    private OnPreviewPicListener mOnPreviewPicLs;
    public interface OnPreviewPicListener {
        void onPicPath(String picPath);
        void onFailed();
    }
}
