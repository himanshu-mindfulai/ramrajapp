package com.mindfulai.Models.faq;

import java.util.ArrayList;

public class FaqBase {

    private int status;
    private String message;
    private ArrayList<FaqData> data;
    private boolean errors;

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

    public ArrayList<FaqData> getData() {
        return data;
    }

    public void setData(ArrayList<FaqData> data) {
        this.data = data;
    }

    public boolean isErrors() {
        return errors;
    }

    public void setErrors(boolean errors) {
        this.errors = errors;
    }
}
