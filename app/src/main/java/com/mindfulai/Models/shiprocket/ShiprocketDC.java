package com.mindfulai.Models.shiprocket;

import com.mindfulai.Models.shiprocket.DeliveryCharge;

public class ShiprocketDC {
    private int status;
    private ShiprocketData data;
    private String message;
    private boolean errors;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public ShiprocketData getData() {
        return data;
    }

    public void setData(ShiprocketData data) {
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
