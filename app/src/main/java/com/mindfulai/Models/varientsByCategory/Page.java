package com.mindfulai.Models.varientsByCategory;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Page implements Parcelable {
    public static final Creator<Page> CREATOR = new Creator<Page>() {
        @Override
        public Page createFromParcel(Parcel in) {
            return new Page(in);
        }

        @Override
        public Page[] newArray(int size) {
            return new Page[size];
        }
    };
    @SerializedName("limit")
    @Expose
    private double limit;
    @SerializedName("page")
    @Expose
    private int page;
    @SerializedName("totalCount")
    @Expose
    private int totalCount;
    @SerializedName("records")
    @Expose
    private List<Datum> records = null;

    protected Page(Parcel in) {
        limit = in.readDouble();
        page = in.readInt();
        totalCount = in.readInt();
        records = in.createTypedArrayList(Datum.CREATOR);
    }

    public double getLimit() {
        return limit;
    }

    public void setLimit(double limit) {
        this.limit = limit;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public List<Datum> getRecords() {
        return records;
    }

    public void setRecords(List<Datum> records) {
        this.records = records;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeDouble(limit);
        parcel.writeInt(page);
        parcel.writeInt(totalCount);
        parcel.writeTypedList(records);
    }
}
