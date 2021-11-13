package com.mindfulai.Models.subscription;

import android.os.Parcel;
import android.os.Parcelable;

public class DaysDataModel implements Parcelable {
    public static final Creator<DaysDataModel> CREATOR = new Creator<DaysDataModel>() {
        @Override
        public DaysDataModel createFromParcel(Parcel in) {
            return new DaysDataModel(in);
        }

        @Override
        public DaysDataModel[] newArray(int size) {
            return new DaysDataModel[size];
        }
    };
    private int sun;
    private int tue;
    private int wed;
    private int thu;
    private int fri;
    private int sat;
    private int mon;

    protected DaysDataModel(Parcel in) {
        sun = in.readInt();
        tue = in.readInt();
        wed = in.readInt();
        thu = in.readInt();
        fri = in.readInt();
        sat = in.readInt();
        mon = in.readInt();
    }

    public DaysDataModel() {
    }

    public int getMon() {
        return mon;
    }

    public void setMon(int mon) {
        this.mon = mon;
    }

    public int getSun() {
        return sun;
    }

    public void setSun(int sun) {
        this.sun = sun;
    }

    public int getTue() {
        return tue;
    }

    public void setTue(int tue) {
        this.tue = tue;
    }

    public int getWed() {
        return wed;
    }

    public void setWed(int wed) {
        this.wed = wed;
    }

    public int getThu() {
        return thu;
    }

    public void setThu(int thu) {
        this.thu = thu;
    }

    public int getFri() {
        return fri;
    }

    public void setFri(int fri) {
        this.fri = fri;
    }

    public int getSat() {
        return sat;
    }

    public void setSat(int sat) {
        this.sat = sat;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(sun);
        parcel.writeInt(tue);
        parcel.writeInt(wed);
        parcel.writeInt(thu);
        parcel.writeInt(fri);
        parcel.writeInt(sat);
        parcel.writeInt(mon);
    }
}
