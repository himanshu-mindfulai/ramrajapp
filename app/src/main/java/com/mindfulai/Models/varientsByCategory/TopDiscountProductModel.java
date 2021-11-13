package com.mindfulai.Models.varientsByCategory;

import java.util.ArrayList;

public class TopDiscountProductModel {

    private int status;
    private ArrayList<DatumForTopDiscount> data;
    private String message;
    private boolean errors;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public ArrayList<DatumForTopDiscount> getData() {
        return data;
    }

    public void setData(ArrayList<DatumForTopDiscount> data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isErrors() {
        return errors;
    }

    public void setErrors(boolean errors) {
        this.errors = errors;
    }
}
