package com.mindfulai.Models.kwikapimodels;

public class PlanItem {
    private int rs;
    private String validity;
    private String desc;
    private String Type;

    public void setRs(int rs) {
        this.rs = rs;
    }

    public int getRs() {
        return rs;
    }

    public String getValidity() {
        return validity;
    }

    public String getDesc() {
        return desc;
    }

    public String getType() {
        return Type;
    }

    public void setValidity(String validity) {
        this.validity = validity;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setType(String type) {
        Type = type;
    }
}
