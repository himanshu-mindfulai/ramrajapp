package com.mindfulai.Models.LoginData;

import android.os.Parcel;
import android.os.Parcelable;

public class GoogleLoginData implements Parcelable {

    public static final Creator<GoogleLoginData> CREATOR = new Creator<GoogleLoginData>() {
        @Override
        public GoogleLoginData createFromParcel(Parcel in) {
            return new GoogleLoginData(in);
        }

        @Override
        public GoogleLoginData[] newArray(int size) {
            return new GoogleLoginData[size];
        }
    };
    private String id;
    private String email;

    protected GoogleLoginData(Parcel in) {
        id = in.readString();
        email = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(email);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
