package com.mindfulai.Models.AllOrderHistory;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OrderItem implements Parcelable {

    public static final Creator<OrderItem> CREATOR = new Creator<OrderItem>() {
        @Override
        public OrderItem createFromParcel(Parcel in) {
            return new OrderItem(in);
        }

        @Override
        public OrderItem[] newArray(int size) {
            return new OrderItem[size];
        }
    };
    @SerializedName("item")
    @Expose
    private Item item;
    @SerializedName("subTotal")
    @Expose
    private double subTotal;
    @SerializedName("discount")
    @Expose
    private double discount;
    @SerializedName("at")
    @Expose
    private String at;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("returnable")
    @Expose
    private boolean returnable = false;

    protected OrderItem(Parcel in) {
        item = in.readParcelable(Item.class.getClassLoader());
        discount = in.readDouble();
        subTotal = in.readDouble();
        at = in.readString();
        status = in.readString();
        returnable = in.readByte() != 0;
    }

    public double getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(double subTotal) {
        this.subTotal = subTotal;
    }

    public boolean getReturnable() {
        return returnable;
    }

    public void setReturnable(boolean returnable) {
        this.returnable = returnable;
    }

    public double getDiscountAmount() {
        return discount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discount = discountAmount;
    }

    public Item getProduct() {
        return item;
    }

    public void setProduct(Item product) {
        this.item = product;
    }


    public String getAt() {
        return at;
    }

    public void setAt(String at) {
        this.at = at;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(item, i);
        parcel.writeDouble(discount);
        parcel.writeString(at);
        parcel.writeString(status);
        parcel.writeByte((byte) (returnable ? 1 : 0));
    }
}
