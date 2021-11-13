package com.mindfulai.Models.categoryData;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Datum implements Parcelable {

    @SerializedName("_id")
    @Expose
    private String id;
    @SerializedName("is_active")
    @Expose
    private Boolean isActive;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("subcategory")
    @Expose
    private List<Subcategory> subcategory = null;
    @SerializedName("image")
    @Expose
    private String image;
    private boolean selected;


    protected Datum(Parcel in) {
        id = in.readString();
        byte tmpIsActive = in.readByte();
        isActive = tmpIsActive == 0 ? null : tmpIsActive == 1;
        name = in.readString();
        image = in.readString();
        selected = in.readByte() != 0;
    }

    public static final Creator<Datum> CREATOR = new Creator<Datum>() {
        @Override
        public Datum createFromParcel(Parcel in) {
            return new Datum(in);
        }

        @Override
        public Datum[] newArray(int size) {
            return new Datum[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Subcategory> getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(List<Subcategory> subcategory) {
        this.subcategory = subcategory;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeByte((byte) (isActive == null ? 0 : isActive ? 1 : 2));
        dest.writeString(name);
        dest.writeString(image);
        dest.writeByte((byte) (selected ? 1 : 0));
    }
}
