package com.mindfulai.Models.payments;

import java.time.OffsetDateTime;
import java.util.UUID;

public class PaymentRecords {
    private String id;
    private String purpose;
    private double amountPaid;
    private String  at;
    private boolean failed;
    private String rzpOrderID;
    private double totalAmt;
    private boolean verified;
    private String rzpPaymentID;
    private double paidFromWallet;
    private String remarks;
    private String type;

    public String getType() {
        return type;
    }

    public String getRemarks() {
        return remarks;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getAt() {
        return at;
    }

    public void setAt(String at) {
        this.at = at;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public String getRzpOrderID() {
        return rzpOrderID;
    }

    public void setRzpOrderID(String rzpOrderID) {
        this.rzpOrderID = rzpOrderID;
    }

    public double getTotalAmt() {
        return totalAmt;
    }

    public void setTotalAmt(long totalAmt) {
        this.totalAmt = totalAmt;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getRzpPaymentID() {
        return rzpPaymentID;
    }

    public void setRzpPaymentID(String rzpPaymentID) {
        this.rzpPaymentID = rzpPaymentID;
    }

    public double getPaidFromWallet() {
        return paidFromWallet;
    }

    public void setPaidFromWallet(double paidFromWallet) {
        this.paidFromWallet = paidFromWallet;
    }
}
