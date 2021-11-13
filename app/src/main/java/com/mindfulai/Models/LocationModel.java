package com.mindfulai.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class LocationModel implements Parcelable {

    public static final Creator<LocationModel> CREATOR = new Creator<LocationModel>() {
        @Override
        public LocationModel createFromParcel(Parcel in) {
            return new LocationModel(in);
        }

        @Override
        public LocationModel[] newArray(int size) {
            return new LocationModel[size];
        }
    };
    private double[] coordinates;

    protected LocationModel(Parcel in) {
        coordinates = in.createDoubleArray();
    }

    public double[] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(double[] coordinates) {
        this.coordinates = coordinates;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDoubleArray(coordinates);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
