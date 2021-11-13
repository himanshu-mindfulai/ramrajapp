package com.mindfulai.Models.varientsByCategory;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;


public class DatumForTopDiscount {

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
    private Integer stock;

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

    @SerializedName("maxOrderQuantity")
    private int maxOrderQuantity;

    @SerializedName("subscribable")
    @Expose
    private boolean subscribable;

    @SerializedName("isSubscribed")
    @Expose
    private boolean isSubscribed;

    @SerializedName("tag")
    @Expose
    private String tag;



    @SerializedName("wholeSalePrices")
    @Expose
    private ArrayList<WholeSalePriceModel> wholeSalePrices;
    
    @SerializedName("product")
    @Expose
    private Product product;

    private double discount;


    public boolean isSubscribed() {
        return isSubscribed;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public float getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(float sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
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

    public boolean isWishlist() {
        return wishlist;
    }

    public void setWishlist(boolean wishlist) {
        this.wishlist = wishlist;
    }

    public int getInCart() {
        return inCart;
    }

    public void setInCart(int inCart) {
        this.inCart = inCart;
    }

    public int getMinOrderQuantity() {
        return minOrderQuantity;
    }

    public void setMinOrderQuantity(Integer minOrderQuantity) {
        this.minOrderQuantity = minOrderQuantity;
    }

    public int getMaxOrderQuantity() {
        return maxOrderQuantity;
    }

    public void setMaxOrderQuantity(int maxOrderQuantity) {
        this.maxOrderQuantity = maxOrderQuantity;
    }

    public boolean isSubscribable() {
        return subscribable;
    }

    public void setSubscribable(boolean subscribable) {
        this.subscribable = subscribable;
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

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }
}
