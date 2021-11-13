package com.mindfulai.Models.AllOrderHistory;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OrderHistory implements Parcelable {

    public static final Parcelable.Creator<OrderHistory> CREATOR = new Parcelable.Creator<OrderHistory>() {
        @Override
        public OrderHistory createFromParcel(Parcel source) {
            return new OrderHistory(source);
        }

        @Override
        public OrderHistory[] newArray(int size) {
            return new OrderHistory[size];
        }
    };
    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("data")
    @Expose
    private OrderHistoryDataByPage data;
    @SerializedName("errors")
    @Expose
    private Boolean errors;
    @SerializedName("message")
    @Expose
    private String message;

    public OrderHistory() {
    }

    protected OrderHistory(Parcel in) {
        this.status = (Integer) in.readValue(Integer.class.getClassLoader());
        this.data = in.readParcelable(OrderHistoryDataByPage.class.getClassLoader());
        this.errors = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.message = in.readString();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public OrderHistoryDataByPage getData() {
        return data;
    }

    public void setData(OrderHistoryDataByPage data) {
        this.data = data;
    }

    public Boolean getErrors() {
        return errors;
    }

    public void setErrors(Boolean errors) {
        this.errors = errors;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.status);
        dest.writeParcelable(this.data, flags);
        dest.writeValue(this.errors);
        dest.writeString(this.message);
    }
}
