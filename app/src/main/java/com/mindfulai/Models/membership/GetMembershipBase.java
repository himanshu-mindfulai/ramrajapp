package com.mindfulai.Models.membership;

import java.util.ArrayList;

public class GetMembershipBase {
    private int status;
    private ArrayList<GetMembershipResponse> data;
    private String message;
    private boolean errors;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public ArrayList<GetMembershipResponse> getData() {
        return data;
    }

    public void setData(ArrayList<GetMembershipResponse> data) {
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