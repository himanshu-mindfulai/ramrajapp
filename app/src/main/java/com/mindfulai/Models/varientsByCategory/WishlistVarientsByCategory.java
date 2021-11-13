package com.mindfulai.Models.varientsByCategory;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WishlistVarientsByCategory implements Parcelable {

    public static final Creator<WishlistVarientsByCategory> CREATOR = new Creator<WishlistVarientsByCategory>() {
        @Override
        public WishlistVarientsByCategory createFromParcel(Parcel source) {
            return new WishlistVarientsByCategory(source);
        }

        @Override
        public WishlistVarientsByCategory[] newArray(int size) {
            return new WishlistVarientsByCategory[size];
        }
    };
    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("data")
    @Expose
    private List<Datum> data = null;
    @SerializedName("errors")
    @Expose
    private Boolean errors;
    @SerializedName("message")
    @Expose
    private String message;

    public WishlistVarientsByCategory() {
    }

    protected WishlistVarientsByCategory(Parcel in) {
        this.status = (Integer) in.readValue(Integer.class.getClassLoader());
        this.data = in.createTypedArrayList(Datum.CREATOR);
        this.errors = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.message = in.readString();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public List<Datum> getData() {
        return data;
    }

    public void setData(List<Datum> data) {
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
        dest.writeTypedList(data);
        dest.writeValue(this.errors);
        dest.writeString(this.message);
    }
}
