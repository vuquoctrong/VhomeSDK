package com.viettel.vht.sdk.model.jfcall;

/**
 * Created by SJ on 2017-12-28.
 */

public class IDRReceiverCall {
    private String mSN;

    public IDRReceiverCall(String SN) {
        mSN = SN;
    }

    public String getSN() {
        return mSN;
    }

    public void setSN(String SN) {
        mSN = SN;
    }
}
