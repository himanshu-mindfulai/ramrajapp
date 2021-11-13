package com.mindfulai.Models.WalletRechargeModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WalletRechargeModel {
    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("data")
    @Expose
    private WalletRechargeData data = null;
    @SerializedName("errors")
    @Expose
    private Boolean errors;
    @SerializedName("message")
    @Expose
    private String message;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public WalletRechargeData getData() {
        return data;
    }

    public void setData(WalletRechargeData data) {
        this.data = data;
    }

    public Boolean getErrors() {
        return errors;
    }

    public void setErrors(Boolean errors) {
        this.errors = errors;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
