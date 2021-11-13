package com.mindfulai.Models.CartInformation;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mindfulai.Models.varientsByCategory.WholeSalePriceModel;

import java.util.ArrayList;
import java.util.List;

public class Product_ {
    @SerializedName("images")
    @Expose
    private Images images;
    @SerializedName("_id")
    @Expose
    private String id;
    @SerializedName("product")
    @Expose
    private Product__ product;
    @SerializedName("attributes")
    @Expose
    private List<Attribute> attributes = null;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("sku_id")
    @Expose
    private String skuId;
    @SerializedName("price")
    @Expose
    private double price;
    @SerializedName("sellingPrice")
    @Expose
    private double sellingPrice;
    @SerializedName("discountAmt")
    @Expose
    private Float discountAmount;
    @SerializedName("stock")
    @Expose
    private int stock;
    @SerializedName("cod_available")
    @Expose
    private boolean codAvailable;
    @SerializedName("active")
    @Expose
    private boolean active;

    @SerializedName("minOrderQuantity")
    @Expose
    private int minOrderQuantity;

    @SerializedName("maxOrderQuantity")
    @Expose
    private int maxOrderQuantity;
    @SerializedName("memberPrice")
    @Expose
    private double memberPrice;

    @SerializedName("wholeSalePrices")
    @Expose
    private ArrayList<WholeSalePriceModel> wholeSalePrices;


    public double getMemberPrice() {
        return memberPrice;
    }

    public void setMemberPrice(int memberPrice) {
        this.memberPrice = memberPrice;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getMaxOrderQuantity() {
        return maxOrderQuantity;
    }

    public void setMaxOrderQuantity(int maxOrderQuantity) {
        this.maxOrderQuantity = maxOrderQuantity;
    }

    public ArrayList<WholeSalePriceModel> getWholeSalePrices() {
        return wholeSalePrices;
    }

    public void setWholeSalePrices(ArrayList<WholeSalePriceModel> wholeSalePrices) {
        this.wholeSalePrices = wholeSalePrices;
    }

    public Float getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(Float discountAmount) {
        this.discountAmount = discountAmount;
    }

    public boolean isCodAvailable() {
        return codAvailable;
    }

    public void setCodAvailable(boolean codAvailable) {
        this.codAvailable = codAvailable;
    }

    public int getMinOrderQuantity() {
        return minOrderQuantity;
    }

    public void setMinOrderQuantity(int minOrderQuantity) {
        this.minOrderQuantity = minOrderQuantity;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public Images getImages() {
        return images;
    }

    public void setImages(Images images) {
        this.images = images;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Product__ getProduct() {
        return product;
    }

    public void setProduct(Product__ product) {
        this.product = product;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSkuId() {
        return skuId;
    }

    public void setSkuId(String skuId) {
        this.skuId = skuId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

}
