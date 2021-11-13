package com.mindfulai.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

public class CustomOrderData implements Parcelable {
    private int quantity;
    private String item;

    public CustomOrderData() {
    }

    protected CustomOrderData(Parcel in) {
        quantity = in.readInt();
        item = in.readString();
    }

    public static final Creator<CustomOrderData> CREATOR = new Creator<CustomOrderData>() {
        @Override
        public CustomOrderData createFromParcel(Parcel in) {
            return new CustomOrderData(in);
        }

        @Override
        public CustomOrderData[] newArray(int size) {
            return new CustomOrderData[size];
        }
    };

    public int getQty() {
        return quantity;
    }

    public void setQty(int qty) {
        this.quantity = qty;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public CustomOrderData(int qty, String item) {
        this.quantity = qty;
        this.item = item;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(quantity);
        dest.writeString(item);
    }
}
