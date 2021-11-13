package com.mindfulai.Models.varientsByCategory;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ProductAttribute implements Parcelable {
    @SerializedName("attribute")
    @Expose
    private Attribute_ attribute;

    @SerializedName("options")
    @Expose
    private ArrayList<Option> options;

    public Attribute_ getAttribute() {
        return attribute;
    }

    public void setAttribute(Attribute_ attribute) {
        this.attribute = attribute;
    }

    public ArrayList<Option> getOptions() {
        return options;
    }

    public void setOptions(ArrayList<Option> options) {
        this.options = options;
    }

    protected ProductAttribute(Parcel in) {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ProductAttribute> CREATOR = new Creator<ProductAttribute>() {
        @Override
        public ProductAttribute createFromParcel(Parcel in) {
            return new ProductAttribute(in);
        }

        @Override
        public ProductAttribute[] newArray(int size) {
            return new ProductAttribute[size];
        }
    };
}
