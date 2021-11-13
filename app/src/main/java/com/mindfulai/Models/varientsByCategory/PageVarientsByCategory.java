package com.mindfulai.Models.varientsByCategory;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PageVarientsByCategory implements Parcelable {

    public static final Creator<PageVarientsByCategory> CREATOR = new Creator<PageVarientsByCategory>() {
        @Override
        public PageVarientsByCategory createFromParcel(Parcel source) {
            return new PageVarientsByCategory(source);
        }

        @Override
        public PageVarientsByCategory[] newArray(int size) {
            return new PageVarientsByCategory[size];
        }
    };
    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("data")
    @Expose
    private Page data = null;
    @SerializedName("errors")
    @Expose
    private Boolean errors;
    @SerializedName("message")
    @Expose
    private String message;

    public PageVarientsByCategory() {
    }

    protected PageVarientsByCategory(Parcel in) {
        this.status = (Integer) in.readValue(Integer.class.getClassLoader());
        this.data = in.readParcelable(Page.class.getClassLoader());
        this.errors = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.message = in.readString();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Page getData() {
        return data;
    }

    public void setData(Page data) {
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
