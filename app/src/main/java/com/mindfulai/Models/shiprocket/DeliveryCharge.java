package com.mindfulai.Models.shiprocket;

public class DeliveryCharge {
    private long courierCompanyID;
    private String courierName;
    private double rate;

    public long getCourierCompanyID() { return courierCompanyID; }
    public void setCourierCompanyID(long value) { this.courierCompanyID = value; }

    public String getCourierName() { return courierName; }
    public void setCourierName(String value) { this.courierName = value; }

    public double getRate() { return rate; }
    public void setRate(double value) { this.rate = value; }
}