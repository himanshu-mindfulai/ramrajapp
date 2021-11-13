package com.mindfulai.Models.DeliveryMethod;

public class Zones {
    private double firstKg;
    private double afterFirstKg;
    private Zone zone;

    public Zone getZone() {
        return zone;
    }

    public void setZone(Zone zone) {
        this.zone = zone;
    }

    public double getFirstKg() {
        return firstKg;
    }

    public void setFirstKg(double firstKg) {
        this.firstKg = firstKg;
    }

    public double getAfterFirstKg() {
        return afterFirstKg;
    }

    public void setAfterFirstKg(double afterFirstKg) {
        this.afterFirstKg = afterFirstKg;
    }
}
