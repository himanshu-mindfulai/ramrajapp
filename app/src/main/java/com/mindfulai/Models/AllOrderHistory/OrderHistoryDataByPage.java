package com.mindfulai.Models.AllOrderHistory;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class OrderHistoryDataByPage implements Parcelable {
    public static final Creator<OrderHistoryDataByPage> CREATOR = new Creator<OrderHistoryDataByPage>() {
        @Override
        public OrderHistoryDataByPage createFromParcel(Parcel in) {
            return new OrderHistoryDataByPage(in);
        }

        @Override
        public OrderHistoryDataByPage[] newArray(int size) {
            return new OrderHistoryDataByPage[size];
        }
    };
    @SerializedName("page")
    private double page;
    @SerializedName("limit")
    private double limit;
    @SerializedName("totalCount")
    private int totalCount;
    @SerializedName("records")
    @Expose
    private ArrayList<Datum> records;

    protected OrderHistoryDataByPage(Parcel in) {
        page = in.readDouble();
        limit = in.readDouble();
        totalCount = in.readInt();
        records = in.createTypedArrayList(Datum.CREATOR);
    }

    public double getPage() {
        return page;
    }

    public void setPage(double page) {
        this.page = page;
    }

    public double getLimit() {
        return limit;
    }

    public void setLimit(double limit) {
        this.limit = limit;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public ArrayList<Datum> getRecords() {
        return records;
    }

    public void setRecords(ArrayList<Datum> records) {
        this.records = records;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeDouble(page);
        parcel.writeDouble(limit);
        parcel.writeDouble(totalCount);
        parcel.writeTypedList(records);
    }
}
