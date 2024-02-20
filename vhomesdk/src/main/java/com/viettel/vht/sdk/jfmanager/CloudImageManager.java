package com.viettel.vht.sdk.jfmanager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.basic.G;
import com.lib.EUIMSG;
import com.lib.FunSDK;
import com.lib.IFunSDKResult;
import com.lib.Mps.MpsClient;
import com.lib.Mps.XPMS_SEARCH_ALARMPIC_REQ;
import com.lib.MsgContent;
import com.lib.SDKCONST;
import com.lib.cloud.CloudDirectory;
import com.lib.sdk.bean.alarm.AlarmInfo;
import com.utils.FileUtils;
import com.utils.XUtils;
import com.vht.sdkcore.utils.Utils;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;


public class CloudImageManager implements IFunSDKResult {
    private static final int TIME_OUT = 1000;
    private int mUserId;
    private boolean mIsDownloading;
    private Queue<DownItemData> mDownloadQueue;
    private HashMap<Integer, DownItemData> mDownloadResultMap;
    private HashMap<String, Bitmap> mBitmapMaps;
    private OnCloudImageListener mListener;
    private Context mContext;

    public CloudImageManager(Context context) {
        mContext = context;
        mUserId = FunSDK.GetId(mUserId, this);
    }

    public String downloadImage(String devId,AlarmInfo alarmInfo, int nType,
                                int nSeq, int imgWidth, int imgHeight) {
        return downloadImage(devId,alarmInfo, nType, nSeq, imgWidth, imgHeight, true);
    }

    public String downloadImage(String devId,AlarmInfo alarmInfo, int nType,
                                int nSeq, int imgWidth, int imgHeight, boolean isDownloadFromDev) {
        if (alarmInfo == null || !alarmInfo.isHavePic()
                || alarmInfo.getId() == null) {
            return null;
        }

        String path;
        path = Utils.Companion.folderThumbnailInternalStoragePath(mContext)
                + File.separator
                + alarmInfo.getId() + "_" + devId + "_thumb.jpg";

        if (FileUtils.isFileExist(path) && new File(path).length() > 0) {
            return path;
        }

        if (isDownloadFromDev) {
            DownItemData down = new DownItemData();
            down.mAlarmInfo = alarmInfo;
            down.mHeight = imgHeight;
            down.mWidth = imgWidth;
            down.mPath = path;
            down.mSeq = nSeq;
            down.mType = nType;
            down.devId= devId;

            if (mDownloadQueue == null) {
                mDownloadQueue = new LinkedBlockingDeque<>();
            }

            if (!mDownloadQueue.contains(down)) {
                mDownloadQueue.add(down);
            }

            downloadImage();
        }

        return null;
    }

    public void downloadImage(String devId,AlarmInfo alarmInfo, int nType, int imgWidth, int imgHeight, int nSeq, OnCloudImageListener listener) {
        if (alarmInfo == null || !alarmInfo.isHavePic()
                || alarmInfo.getId() == null) {
            if (listener != null) {
                listener.onDownloadResult(false, null, null, nType, nSeq);
            }
            return;
        }

        String path;
        path = Utils.Companion.folderThumbnailInternalStoragePath(mContext)
                + File.separator
                + alarmInfo.getId() + "thumb.jpg";
        if (FileUtils.isFileExist(path)) {
            if (mBitmapMaps == null) {
                mBitmapMaps = new HashMap<>();
            }

            Bitmap bitmap;
            if (imgWidth > 0) {
                bitmap = XUtils.createImageThumbnail(path);
            } else {
                bitmap = BitmapFactory.decodeFile(path);
            }

            mBitmapMaps.put(path, bitmap);
            if (listener != null) {
                listener.onDownloadResult(true, path, bitmap, nType, nSeq);
            }
            return;
        }

        Bitmap bitmap = getPicBitmap(path);
        if (bitmap != null) {
            if (listener != null) {
                listener.onDownloadResult(true, path, bitmap, nType, nSeq);
            }
            return;
        }

        DownItemData down = new DownItemData();
        down.mAlarmInfo = alarmInfo;
        down.mHeight = imgHeight;
        down.mWidth = imgWidth;
        down.mPath = path;
        down.mType = nType;
        down.mSeq = nSeq;
        down.mListener = listener;

        if (mDownloadQueue == null) {
            mDownloadQueue = new LinkedBlockingDeque<>();
        }

        if (!mDownloadQueue.contains(down)) {
            mDownloadQueue.add(down);
        }

        downloadImage();
        return;
    }

    private void downloadImage() {
        if (mDownloadQueue == null || mDownloadQueue.isEmpty()) {
            return;
        }

        if (mDownloadResultMap == null) {
            mDownloadResultMap = new HashMap<>();
        }

        if (mDownloadResultMap.size() < 30) {
            DownItemData down = mDownloadQueue.poll();
            AlarmInfo alarmInfo = down.mAlarmInfo;
            XPMS_SEARCH_ALARMPIC_REQ downInfo = new XPMS_SEARCH_ALARMPIC_REQ();
            G.SetValue(downInfo.st_0_Uuid, down.devId);
            downInfo.st_2_ID = Long.parseLong(alarmInfo.getId());
            downInfo.st_3_Res[0] = (byte) down.mType;
            mDownloadResultMap.put(down.mSeq, down);

            MpsClient.DownloadAlarmImage(mUserId
                    , down.devId
                    , down.mPath
                    , alarmInfo.getOriginJson()
                    , down.mWidth
                    , down.mHeight
                    , down.mSeq);

            downloadImage();
        }
    }

    public class DownItemData implements Serializable {
        public static final int ORIGINAL_IMG = 0;//原图
        public static final int THUMB_IMG = 1;//缩略图
        int mType;
        int mSeq;
        int mWidth = 0; //图片大小，为0则原图大小
        int mHeight = 0;
        String mPath;
        String devId;
        AlarmInfo mAlarmInfo;
        OnCloudImageListener mListener;
    }

    public void release(boolean isRecycleBitmap) {
        try {
            stopDownload();
            if (mBitmapMaps != null) {
                if (isRecycleBitmap) {
                    for (Map.Entry<String, Bitmap> entry : mBitmapMaps.entrySet()) {
                        if (entry != null) {
                            Bitmap bitmap = entry.getValue();
                            if (bitmap != null && !bitmap.isRecycled()) {
                            }
                        }
                    }
                }
                mBitmapMaps.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int OnFunSDKResult(Message message, MsgContent msgContent) {
        switch (message.what) {
            case EUIMSG.MC_SearchAlarmPic:
            case EUIMSG.MC_DownloadMediaThumbnail:
                dealWithDownloadResult(message.arg1 >= 0, msgContent.str, msgContent.seq);
                mIsDownloading = false;
                downloadImage();
                break;
            default:
                break;
        }
        return 0;
    }

    private void dealWithDownloadResult(boolean isSuccess, String imgPath, int seq) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (isSuccess) {
                    long size = new File(imgPath).length();
                    DownItemData downItemData = mDownloadResultMap.get(seq);

                    if (downItemData != null && downItemData.mListener != null && size > 0) {
                        Log.d("TAG", "dealWithDownloadResult: 1");
                        downItemData.mListener.onDownloadResult(true, imgPath, null, downItemData.mType, seq);
                    } else {
                        if (mListener != null && size > 0) {
                            if (downItemData != null) {
                                Log.d("TAG", "dealWithDownloadResult: 2");
                                mListener.onDownloadResult(true, imgPath, null,
                                        downItemData.mType, seq);
                            } else {
                                Log.d("TAG", "dealWithDownloadResult: 3");
                                mListener.onDownloadResult(true, imgPath, null,
                                        SDKCONST.MediaType.PIC, seq);
                            }
                        }
                    }

                    mDownloadResultMap.remove(seq);
                } else {
                    DownItemData downItemData = mDownloadResultMap.get(seq);
                    if (downItemData != null && downItemData.mListener != null) {
                        Log.d("TAG", "dealWithDownloadResult: 4");
                        downItemData.mListener.onDownloadResult(false, null,
                                null, SDKCONST.MediaType.PIC, seq);
                    } else if (mListener != null) {
                        if (downItemData != null) {
                            Log.d("TAG", "dealWithDownloadResult: 5");
                            mListener.onDownloadResult(false, imgPath, null,
                                    downItemData.mType, seq);
                        } else {
                            Log.d("TAG", "dealWithDownloadResult: 6");
                            mListener.onDownloadResult(false, null,
                                    null, SDKCONST.MediaType.PIC, seq);
                        }
                    }
                }
            }
        });
    }

    public Bitmap getPicBitmap(AlarmInfo alarmInfo, boolean isThumb) {
        if (alarmInfo == null) {
            return null;
        }

        String path;
        path = Utils.Companion.folderThumbnailInternalStoragePath(mContext)
                + File.separator
                + alarmInfo.getId() + "thumb.jpg";

        if (mBitmapMaps != null && mBitmapMaps.containsKey(path)) {
            return mBitmapMaps.get(path);
        }

        return null;
    }

    public Bitmap getPicBitmap(String imagePath) {
        if (mBitmapMaps != null && mBitmapMaps.containsKey(imagePath)) {
            return mBitmapMaps.get(imagePath);
        }

        return null;
    }

    public void setOnCloudImageListener(OnCloudImageListener listener) {
        mListener = listener;
    }

    public void stopDownload() {
        if (mDownloadQueue != null && !mDownloadQueue.isEmpty()) {
            MpsClient.StopDownloadAlarmImages(mUserId, 0);
            CloudDirectory.StopDownloadThumbnail();
            mDownloadQueue.clear();
        }
    }

    public interface OnCloudImageListener {
        void onDownloadResult(boolean isSuccess, String imagePath, Bitmap bitmap, int mediaType, int seq);

        void onDeleteResult(boolean isSuccess, int seq);
    }
}
