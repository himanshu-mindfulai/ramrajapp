package com.mindfulai.Models.society;

import java.util.ArrayList;

public class SocietyBase {
    private long status;
    private String message;
    private boolean errors;
    private ArrayList<SocietyData> data;

    public long getStatus() { return status; }
    public void setStatus(long value) { this.status = value; }

    public String getMessage() { return message; }
    public void setMessage(String value) { this.message = value; }

    public boolean getErrors() { return errors; }
    public void setErrors(boolean value) { this.errors = value; }

    public ArrayList<SocietyData> getData() { return data; }
    public void setData(ArrayList<SocietyData> value) { this.data = value; }
}
