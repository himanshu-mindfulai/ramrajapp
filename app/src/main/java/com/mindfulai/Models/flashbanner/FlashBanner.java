package com.mindfulai.Models.flashbanner;

import java.util.ArrayList;

public class FlashBanner {
    private long status;
    private ArrayList<FlashBannerData> data;
    private boolean errors;
    private String message;

    public long getStatus() { return status; }
    public void setStatus(long value) { this.status = value; }

    public ArrayList<FlashBannerData> getData() { return data; }
    public void setData(ArrayList<FlashBannerData> value) { this.data = value; }

    public boolean getErrors() { return errors; }
    public void setErrors(boolean value) { this.errors = value; }

    public String getMessage() { return message; }
    public void setMessage(String value) { this.message = value; }
}
