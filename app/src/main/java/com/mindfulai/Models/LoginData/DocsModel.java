package com.mindfulai.Models.LoginData;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class DocsModel implements Parcelable {
    public static final Creator<DocsModel> CREATOR = new Creator<DocsModel>() {
        @Override
        public DocsModel createFromParcel(Parcel in) {
            return new DocsModel(in);
        }

        @Override
        public DocsModel[] newArray(int size) {
            return new DocsModel[size];
        }
    };
    @SerializedName("pan")
    private String pan;
    @SerializedName("aadhaar")
    private String aadhar;
    @SerializedName("cheque")
    private String cheque;
    @SerializedName("other")
    private String other;
    @SerializedName("shopInside")
    private String shopInside;
    @SerializedName("shopBusinessCard")
    private String shopBusinessCard;
    @SerializedName("shopMerchant")
    private String shopMerchant;
    @SerializedName("shopOutside")
    private String shopOutside;

    protected DocsModel(Parcel in) {
        pan = in.readString();
        aadhar = in.readString();
        cheque = in.readString();
        other = in.readString();
        shopInside = in.readString();
        shopBusinessCard = in.readString();
        shopMerchant = in.readString();
        shopOutside = in.readString();
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getShopInside() {
        return shopInside;
    }

    public void setShopInside(String shopInside) {
        this.shopInside = shopInside;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }

    public String getShopBusinessCard() {
        return shopBusinessCard;
    }

    public void setShopBusinessCard(String shopBusinessCard) {
        this.shopBusinessCard = shopBusinessCard;
    }

    public String getShopMerchant() {
        return shopMerchant;
    }

    public void setShopMerchant(String shopMerchant) {
        this.shopMerchant = shopMerchant;
    }

    public String getShopOutside() {
        return shopOutside;
    }

    public void setShopOutside(String shopOutside) {
        this.shopOutside = shopOutside;
    }

    public String getAadhar() {
        return aadhar;
    }

    public void setAadhar(String aadhar) {
        this.aadhar = aadhar;
    }

    public String getCheque() {
        return cheque;
    }

    public void setCheque(String cheque) {
        this.cheque = cheque;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(pan);
        parcel.writeString(aadhar);
        parcel.writeString(cheque);
        parcel.writeString(other);
        parcel.writeString(shopInside);
        parcel.writeString(shopOutside);
        parcel.writeString(shopMerchant);
        parcel.writeString(shopBusinessCard);
    }
}
