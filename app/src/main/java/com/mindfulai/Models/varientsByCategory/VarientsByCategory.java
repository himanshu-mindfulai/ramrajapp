package com.mindfulai.Models.varientsByCategory;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class VarientsByCategory implements Parcelable {

    public static final Creator<VarientsByCategory> CREATOR = new Creator<VarientsByCategory>() {
        @Override
        public VarientsByCategory createFromParcel(Parcel in) {
            return new VarientsByCategory(in);
        }

        @Override
        public VarientsByCategory[] newArray(int size) {
            return new VarientsByCategory[size];
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

    protected VarientsByCategory(Parcel in) {
        if (in.readByte() == 0) {
            status = null;
        } else {
            status = in.readInt();
        }
        data = in.createTypedArrayList(Datum.CREATOR);
        byte tmpErrors = in.readByte();
        errors = tmpErrors == 0 ? null : tmpErrors == 1;
        message = in.readString();
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
    public void writeToParcel(Parcel parcel, int i) {
        if (status == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(status);
        }
        parcel.writeTypedList(data);
        parcel.writeByte((byte) (errors == null ? 0 : errors ? 1 : 2));
        parcel.writeString(message);
    }
}
