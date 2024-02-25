package com.viettel.vht.sdk.model.jfcall;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class NoDisturbingAlarmTimeInfoBean implements Cloneable {

    private String msg;
    private String sn;

    private int type; //0: Globally disabled 1: Globally enabled 2: Time period enabled (the content of the "ts" field is only valid at this time)

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    private List<List<AlarmTimeBean>> ts;


    public List<List<AlarmTimeBean>> getTs() {
        return ts == null ? new ArrayList<>() : ts;
    }

    public void setTs(List<List<AlarmTimeBean>> ts) {
        this.ts = ts;
    }

    @NonNull
    @Override
    public NoDisturbingAlarmTimeInfoBean clone() {
        NoDisturbingAlarmTimeInfoBean clone = new NoDisturbingAlarmTimeInfoBean();
        clone.setMsg(msg);
        clone.setSn(sn);
        clone.setType(type);
        clone.setTs(new ArrayList<>());
        if (ts != null) {
            for (List<AlarmTimeBean> list : ts) {
                List<AlarmTimeBean> cloneList = null;
                if (list != null) {
                    cloneList = new ArrayList<>();
                    for (AlarmTimeBean bean : list) {
                        cloneList.add(bean.clone());
                    }
                }
                clone.getTs().add(cloneList);
            }
        }
        return clone;
    }

    public static class AlarmTimeBean implements Cloneable {

        private int en;
        private String st;
        private String et;

        public int getEn() {
            return en == 1 ? 1 : 0;
        }

        public void setEn(int en) {
            this.en = en;
        }

        public String getSt() {
            return st == null ? "" : st;
        }

        public void setSt(String st) {
            this.st = st;
        }

        public String getEt() {
            return et == null ? "" : et;
        }

        public void setEt(String et) {
            this.et = et;
        }

        @NonNull
        @Override
        public AlarmTimeBean clone() {
            AlarmTimeBean clone = new AlarmTimeBean();
            clone.setEn(en);
            clone.setSt(st);
            clone.setEt(et);
            return clone;
        }
    }
}
