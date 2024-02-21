package com.viettel.vht.sdk.eventbus;

/**
 * @author hws
 * @class 报警消息操作传递
 * @time 2019-08-22 11:03
 */
public class EventBusPushInfo {
    private String devId;
    private PUSH_OPERATION operationType;
    private OnOperationResultListener listener;
    public enum PUSH_OPERATION {
        LINK,
        UNLINK,
        ADD_DEV,
        REMOVE_DEV,
        LOGOUT,
        SHARE_ACCEPT,
        CALL,
    }
    public EventBusPushInfo(String devId, PUSH_OPERATION operationType, OnOperationResultListener listener) {
        this.devId = devId;
        this.operationType = operationType;
        this.listener = listener;
    }

    public PUSH_OPERATION getOperationType() {
        return this.operationType;
    }

    public void setOperationType(PUSH_OPERATION operationType) {
        this.operationType = operationType;
    }

    public String getDevId() {
        return devId;
    }

    public void setDevId(String devId) {
        this.devId = devId;
    }

    public OnOperationResultListener getListener() {
        return listener;
    }

    public interface OnOperationResultListener {
        void onResult(boolean isSuccess);
    }

}
