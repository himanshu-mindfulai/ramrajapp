package com.mindfulai.Models.payments;

public class PaymentBase {
    private long status;
    private PaymentChild data;
    private boolean errors;
    private String message;

    public long getStatus() { return status; }
    public void setStatus(long value) { this.status = value; }

    public PaymentChild getData() { return data; }
    public void setData(PaymentChild value) { this.data = value; }

    public boolean getErrors() { return errors; }
    public void setErrors(boolean value) { this.errors = value; }

    public String getMessage() { return message; }
    public void setMessage(String value) { this.message = value; }
}
