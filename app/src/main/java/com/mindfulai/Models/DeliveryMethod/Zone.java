package com.mindfulai.Models.DeliveryMethod;

public class Zone {
    private String id;
    private String name;
    private String[] pincodes;
    private Days days;

    public String getID() { return id; }
    public void setID(String value) { this.id = value; }

    public String getName() { return name; }
    public void setName(String value) { this.name = value; }

    public String[] getPincodes() { return pincodes; }
    public void setPincodes(String[] value) { this.pincodes = value; }

    public Days getDays() { return days; }
    public void setDays(Days value) { this.days = value; }
}