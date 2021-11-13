package com.mindfulai.Models.DeliveryMethod;

import java.util.ArrayList;

public class DeliveryMethodBase {
    private int status;
    private String message;
    private boolean errors;
    private ArrayList<DeliveryMethodData> data;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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

    public ArrayList<DeliveryMethodData> getData() {
        return data;
    }

    public void setData(ArrayList<DeliveryMethodData> data) {
        this.data = data;
    }
}
