package com.mindfulai.Models.orderDetailInfo;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Attribute implements Parcelable {

    public static final Parcelable.Creator<Attribute> CREATOR = new Parcelable.Creator<Attribute>() {
        @Override
        public Attribute createFromParcel(Parcel source) {
            return new Attribute(source);
        }

        @Override
        public Attribute[] newArray(int size) {
            return new Attribute[size];
        }
    };
    @SerializedName("attribute")
    @Expose
    private Attribute_ attribute;
    @SerializedName("option")
    @Expose
    private Option option;

    public Attribute() {
    }

    protected Attribute(Parcel in) {
        this.attribute = in.readParcelable(Attribute_.class.getClassLoader());
        this.option = in.readParcelable(Option.class.getClassLoader());
    }

    public Attribute_ getAttribute() {
        return attribute;
    }

    public void setAttribute(Attribute_ attribute) {
        this.attribute = attribute;
    }

    public Option getOption() {
        return option;
    }

    public void setOption(Option option) {
        this.option = option;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.attribute, flags);
        dest.writeParcelable(this.option, flags);
    }
}
