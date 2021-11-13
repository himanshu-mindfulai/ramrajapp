package com.mindfulai.Models.AllOrderHistory;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Item implements Parcelable {

    public static final Parcelable.Creator<Item> CREATOR = new Parcelable.Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel source) {
            return new Item(source);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };
    @SerializedName("product")
    @Expose
    private Product__ product;
    @SerializedName("mrp")
    @Expose
    private double mrp;
    @SerializedName("sellingPrice")
    @Expose
    private double sellingPrice;
    @SerializedName("quantity")
    @Expose
    private int quantity;
    @SerializedName("stock")
    @Expose
    private int stock;

    public Item() {
    }

    protected Item(Parcel in) {
        this.product = in.readParcelable(Product__.class.getClassLoader());
        this.mrp = in.readDouble();
        this.sellingPrice = in.readDouble();
        this.quantity = in.readInt();
        this.stock = in.readInt();
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(Float sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public Product__ getProduct() {
        return product;
    }

    public void setProduct(Product__ product) {
        this.product = product;
    }

    public double getMrp() {
        return mrp;
    }

    public void setMrp(double price) {
        this.mrp = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.product, flags);
        dest.writeValue(this.mrp);
        dest.writeValue(this.sellingPrice);
        dest.writeValue(this.stock);
        dest.writeValue(this.quantity);
    }
}
