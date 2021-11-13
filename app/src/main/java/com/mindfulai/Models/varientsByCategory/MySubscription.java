package com.mindfulai.Models.varientsByCategory;

import android.os.Parcel;
import android.os.Parcelable;

public class MySubscription implements Parcelable {
    private boolean deleted;

    public boolean isDeleted() {
        return deleted;
    }

    protected MySubscription(Parcel in) {
        deleted = in.readByte() != 0;
    }

    public static final Creator<MySubscription> CREATOR = new Creator<MySubscription>() {
        @Override
        public MySubscription createFromParcel(Parcel in) {
            return new MySubscription(in);
        }

        @Override
        public MySubscription[] newArray(int size) {
            return new MySubscription[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (deleted ? 1 : 0));
    }
}
