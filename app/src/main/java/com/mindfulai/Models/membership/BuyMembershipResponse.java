package com.mindfulai.Models.membership;

public class BuyMembershipResponse {
    private String order_id;
    private double amount;
    private String txnToken;
    private String action;

    public void setTxnToken(String txnToken) {
        this.txnToken = txnToken;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
