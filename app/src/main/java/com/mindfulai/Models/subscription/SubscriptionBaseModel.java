package com.mindfulai.Models.subscription;

import java.util.ArrayList;

public class SubscriptionBaseModel {
    private int status;
    private ArrayList<SubscriptionDataModel> data;
    private String message;
    private boolean errors;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public ArrayList<SubscriptionDataModel> getData() {
        return data;
    }

    public void setData(ArrayList<SubscriptionDataModel> data) {
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
