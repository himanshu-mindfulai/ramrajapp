package com.mindfulai.Models.orderDetailInfo;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Order implements Parcelable {

    public static final Creator<Order> CREATOR = new Creator<Order>() {
        @Override
        public Order createFromParcel(Parcel source) {
            return new Order(source);
        }

        @Override
        public Order[] newArray(int size) {
            return new Order[size];
        }
    };
    @SerializedName("_id")
    @Expose
    private String id;
    @SerializedName("order_id")
    @Expose
    private String orderId;
    @SerializedName("grandTotal")
    @Expose
    private double grandTotal;
    @SerializedName("order_date")
    @Expose
    private String orderDate;
    @SerializedName("paymentMethod")
    @Expose
    private String paymentMethod;
    @SerializedName("deliveryType")
    @Expose
    private String orderType;

    public Order() {
    }

    protected Order(Parcel in) {
        this.id = in.readString();
        this.orderId = in.readString();
        this.grandTotal = in.readDouble();
        this.orderDate = in.readString();
        this.paymentMethod = in.readString();
        this.orderType = in.readString();
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public double getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(double grandTotal) {
        this.grandTotal = grandTotal;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.orderId);
        dest.writeValue(this.grandTotal);
        dest.writeString(this.orderDate);
        dest.writeString(this.paymentMethod);
        dest.writeString(this.orderType);
    }
}
