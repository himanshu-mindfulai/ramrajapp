package com.mindfulai.Models.varientsByCategory;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Varient implements Parcelable {

    public static final Creator<Varient> CREATOR = new Creator<Varient>() {
        @Override
        public Varient createFromParcel(Parcel in) {
            return new Varient(in);
        }

        @Override
        public Varient[] newArray(int size) {
            return new Varient[size];
        }
    };
    @SerializedName("_id")
    @Expose
    private String id;
    @SerializedName("attributes")
    @Expose
    private ArrayList<Attribute> attributes = null;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("price")
    @Expose
    private float price;
    @SerializedName("sellingPrice")
    @Expose
    private float sellingPrice;
    @SerializedName("stock")
    @Expose
    private int stock;
    @SerializedName("images")
    @Expose
    private Images images;
    @SerializedName("reviews")
    @Expose
    private Reviews reviews;
    @SerializedName("wishlist")
    @Expose
    private boolean wishlist;
    @SerializedName("inCart")
    @Expose
    private int inCart;
    @SerializedName("minOrderQuantity")
    private int minOrderQuantity;
    @SerializedName("subscribable")
    @Expose
    private boolean subscribable;
    @SerializedName("tag")
    @Expose
    private String tag;
    @SerializedName("maxOrderQuantity")
    @Expose
    private int maxOrderQuantity;

    @SerializedName("wholeSalePrices")
    @Expose
    private ArrayList<WholeSalePriceModel> wholeSalePrices;

    @SerializedName("memberPrice")
    @Expose
    private double memberPrice;

    @SerializedName("isSubscribed")
    @Expose
    private boolean isSubscribed;


    public Varient(String id, ArrayList<Attribute> attributes, String description, float price, float sellingPrice, Integer stock, Images images, Reviews reviews, boolean wishlist, int inCart, Integer minOrderQuantity, boolean subscribable, String tag, ArrayList<WholeSalePriceModel> wholeSalePrices,int maxOrderQuantity,boolean isSubscribed) {
        this.id = id;
        this.attributes = attributes;
        this.description = description;
        this.price = price;
        this.sellingPrice = sellingPrice;
        this.stock = stock;
        this.images = images;
        this.reviews = reviews;
        this.wishlist = wishlist;
        this.inCart = inCart;
        this.minOrderQuantity = minOrderQuantity;
        this.subscribable = subscribable;
        this.tag = tag;
        this.wholeSalePrices = wholeSalePrices;
        this.maxOrderQuantity = maxOrderQuantity;
        this.isSubscribed = isSubscribed;
    }

    public boolean isSubscribed() {
        return isSubscribed;
    }

    protected Varient(Parcel in) {
        id = in.readString();
        attributes = (ArrayList<Attribute>) in.readSerializable();
        description = in.readString();
        price = in.readFloat();
        sellingPrice = in.readFloat();
        stock = in.readInt();
        images = in.readParcelable(Images.class.getClassLoader());
        reviews = in.readParcelable(Reviews.class.getClassLoader());
        wholeSalePrices = (ArrayList<WholeSalePriceModel>) in.readSerializable();
        wishlist = in.readByte() != 0;
        subscribable = in.readByte() != 0;
        inCart = in.readInt();
        tag = in.readString();
        minOrderQuantity = in.readInt();
        maxOrderQuantity = in.readInt();
        memberPrice = in.readDouble();
        isSubscribed =in.readByte() != 0;
    }



    public Varient() {
    }

    public int getMaxOrderQuantity() {
        return maxOrderQuantity;
    }

    public void setMaxOrderQuantity(int maxOrderQuantity) {
        this.maxOrderQuantity = maxOrderQuantity;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public ArrayList<WholeSalePriceModel> getWholeSalePrices() {
        return wholeSalePrices;
    }

    public void setWholeSalePrices(ArrayList<WholeSalePriceModel> wholeSalePrices) {
        this.wholeSalePrices = wholeSalePrices;
    }

    public boolean isSubscribable() {
        return subscribable;
    }

    public void setSubscribable(boolean subscribable) {
        this.subscribable = subscribable;
    }

    public int getMinOrderQuantity() {
        return minOrderQuantity;
    }

    public void setMinOrderQuantity(int minOrderQuantity) {
        this.minOrderQuantity = minOrderQuantity;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public double getMemberPrice() {
        return memberPrice;
    }


    public ArrayList<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(ArrayList<Attribute> attributes) {
        this.attributes = attributes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }


    public Images getImages() {
        return images;
    }

    public void setImages(Images images) {
        this.images = images;
    }

    public Reviews getReviews() {
        return reviews;
    }

    public void setReviews(Reviews reviews) {
        this.reviews = reviews;
    }

    public int getInCart() {
        return inCart;
    }

    public void setInCart(int inCart) {
        this.inCart = inCart;
    }

    public float getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(float sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public boolean getWishlist() {
        return wishlist;
    }

    public void setWishlist(Boolean wishlist) {
        this.wishlist = wishlist;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeSerializable(attributes);
        parcel.writeSerializable(wholeSalePrices);
        parcel.writeString(description);
        parcel.writeFloat(price);
        parcel.writeFloat(sellingPrice);
        parcel.writeInt(stock);
        parcel.writeParcelable(images, i);
        parcel.writeParcelable(reviews, i);
        parcel.writeByte((byte) (wishlist ? 1 : 0));
        parcel.writeByte((byte) (subscribable ? 1 : 0));
        parcel.writeInt(inCart);
        parcel.writeString(tag);
        parcel.writeInt(minOrderQuantity);
        parcel.writeInt(maxOrderQuantity);
        parcel.writeDouble(memberPrice);
        parcel.writeByte((byte) (isSubscribed ? 1 : 0));
    }
}
