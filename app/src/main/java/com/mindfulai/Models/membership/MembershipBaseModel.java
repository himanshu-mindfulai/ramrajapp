package com.mindfulai.Models.membership;

import java.util.ArrayList;

public class MembershipBaseModel {
    private int statue;
    private ArrayList<MembershipDataModel> data;
    private String message;
    private boolean errors;

    public int getStatue() {
        return statue;
    }

    public void setStatue(int statue) {
        this.statue = statue;
    }

    public ArrayList<MembershipDataModel> getData() {
        return data;
    }

    public void setData(ArrayList<MembershipDataModel> data) {
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
