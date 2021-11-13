package com.mindfulai.Models.varientsByCategory;

import java.io.Serializable;

public class WholeSalePriceModel implements Serializable {
    private int upto;
    private double pricePerUnit;


    public int getUpto() {
        return upto;
    }

    public void setUpto(int upto) {
        this.upto = upto;
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(double pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }
}
