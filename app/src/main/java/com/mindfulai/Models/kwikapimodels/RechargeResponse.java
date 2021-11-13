package com.mindfulai.Models.kwikapimodels;

public class RechargeResponse {
   private String status;
   private String order_id;
   private String opr_id;
   private String balance;
   private String number;
   private String provider;
   private String amount;
   private String charged_amount;
   private String message;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getOpr_id() {
        return opr_id;
    }

    public void setOpr_id(String opr_id) {
        this.opr_id = opr_id;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCharged_amount() {
        return charged_amount;
    }

    public void setCharged_amount(String charged_amount) {
        this.charged_amount = charged_amount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
