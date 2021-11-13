package com.mindfulai.Models.varientsByCategory;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Attribute__ implements Parcelable {

    public static final Parcelable.Creator<Attribute__> CREATOR = new Parcelable.Creator<Attribute__>() {
        @Override
        public Attribute__ createFromParcel(Parcel source) {
            return new Attribute__(source);
        }

        @Override
        public Attribute__[] newArray(int size) {
            return new Attribute__[size];
        }
    };
    @SerializedName("attribute")
    @Expose
    private Attribute___ attribute;
    @SerializedName("option")
    @Expose
    private List<Option> option = null;

    public Attribute__() {
    }

    protected Attribute__(Parcel in) {
        this.attribute = in.readParcelable(Attribute___.class.getClassLoader());
        this.option = new ArrayList<Option>();
        in.readList(this.option, Option_.class.getClassLoader());
    }

    public Attribute___ getAttribute() {
        return attribute;
    }

    public void setAttribute(Attribute___ attribute) {
        this.attribute = attribute;
    }

    public List<Option> getOption() {
        return option;
    }

    public void setOption(List<Option> option) {
        this.option = option;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.attribute, flags);
        dest.writeList(this.option);
    }
}
