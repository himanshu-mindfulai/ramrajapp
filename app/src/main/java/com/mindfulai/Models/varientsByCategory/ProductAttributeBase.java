package com.mindfulai.Models.varientsByCategory;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ProductAttributeBase implements Parcelable {

    @SerializedName("status")
    @Expose
    int status;

    @SerializedName("data")
    @Expose
    ArrayList<ProductAttribute> data;

    @SerializedName("errors")
    @Expose
    boolean errors;

    @SerializedName("message")
    @Expose
    String message;


    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public ArrayList<ProductAttribute> getData() {
        return data;
    }

    public void setData(ArrayList<ProductAttribute> data) {
        this.data = data;
    }

    public boolean isErrors() {
        return errors;
    }

    public void setErrors(boolean errors) {
        this.errors = errors;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    protected ProductAttributeBase(Parcel in) {
        status = in.readInt();
        data = in.createTypedArrayList(ProductAttribute.CREATOR);
        errors = in.readByte() != 0;
        message = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(status);
        dest.writeTypedList(data);
        dest.writeByte((byte) (errors ? 1 : 0));
        dest.writeString(message);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ProductAttributeBase> CREATOR = new Creator<ProductAttributeBase>() {
        @Override
        public ProductAttributeBase createFromParcel(Parcel in) {
            return new ProductAttributeBase(in);
        }

        @Override
        public ProductAttributeBase[] newArray(int size) {
            return new ProductAttributeBase[size];
        }
    };
}
