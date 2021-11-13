package com.mindfulai.Models.WalletRechargeModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WalletRechargeData {
    @SerializedName("order_id")
    @Expose
    private String orderId;
    @SerializedName("amount")
    @Expose
    private float amount;

    @SerializedName("txnToken")
    @Expose
    private String txnToken;

    public String getTxnToken() {
        return txnToken;
    }

    public void setTxnToken(String txnToken) {
        this.txnToken = txnToken;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
