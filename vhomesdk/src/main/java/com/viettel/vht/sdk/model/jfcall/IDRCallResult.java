package com.viettel.vht.sdk.model.jfcall;

/**
 * Created by SJ on 2017-10-14.
 */

public class IDRCallResult {
    public static final String RESULT_OPEN_MSG = "RESULT_OPEN_MSG";
    public static final String RESULT_OPEN_REVIEW = "RESULT_OPEN_REVIEW";
    public static final String RESULT_REOPEN_APP = "RESULT_REOPEN_APP";
    public static final String RESULT_OPEN_VISITOR = "RESULT_OPEN_VISITOR";
    public static final String RESULT_OPEN_EDIT_CUSTOMIZE_VOICE = "RESULT_OPEN_EDIT_CUSTOMIZE_VOICE";
    public static final String RESET = "RESET";
    private String mResult;
    private String mDevSN;
    private String mPic;
    private int mPreviewHandle;
    private int fileNumber;//自定义语音对应设备端的ID号
    private String mAlarmEvent;
    public IDRCallResult() {
    }

    public IDRCallResult(String result, String devSN) {
        mResult = result;
        mDevSN = devSN;
    }

    public IDRCallResult(String result, String devSN, String pic) {
        mResult = result;
        mDevSN = devSN;
        mPic = pic;
    }

    public IDRCallResult(String result, String devSN, int previewHandle) {
        mResult = result;
        mDevSN = devSN;
        mPreviewHandle = previewHandle;
    }

    public IDRCallResult(String result, String devSN, int previewHandle, String alarmEvent) {
        mResult = result;
        mDevSN = devSN;
        mPreviewHandle = previewHandle;
        mAlarmEvent = alarmEvent;
    }

    public String getDevSN() {
        return mDevSN;
    }

    public void setDevSN(String devSN) {
        mDevSN = devSN;
    }

    public String getResult() {
        return mResult;
    }

    public void setResult(String result) {
        mResult = result;
    }

    public String getPic() {
        return mPic;
    }

    public void setPic(String pic) {
        mPic = pic;
    }

    public int getPreviewHandle() {
        return mPreviewHandle;
    }

    public void setPreviewHandle(int previewHandle) {
        mPreviewHandle = previewHandle;
    }

    public int getFileNumber() {
        return fileNumber;
    }

    public void setFileNumber(int fileNumber) {
        this.fileNumber = fileNumber;
    }

    public void setAlarmEvent(String alarmEvent) {
        this.mAlarmEvent = alarmEvent;
    }

    public String getAlarmEvent() {
        return mAlarmEvent;
    }
}
