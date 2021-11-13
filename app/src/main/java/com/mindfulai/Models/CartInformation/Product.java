package com.mindfulai.Models.CartInformation;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Product {

    @SerializedName("product")
    @Expose
    private Product_ product;
    @SerializedName("quantity")
    @Expose
    private Integer quantity;
    @SerializedName("discountAmt")
    @Expose
    private double discountAmount;
    @SerializedName("zoneValid")
    @Expose
    private boolean zoneValid;

    @SerializedName("weight")
    @Expose
    private double weight;

    @SerializedName("sellingPrice")
    @Expose
    private double sellingPrice;

    @SerializedName("taxAmt")
    @Expose
    private double taxAmt;

    @SerializedName("vendor")
    @Expose
    private String vendor = "";

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public double getTaxAmt() {
        return taxAmt;
    }

    public void setTaxAmt(double taxAmt) {
        this.taxAmt = taxAmt;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public boolean isZoneValid() {
        return zoneValid;
    }

    public void setZoneValid(boolean zoneValid) {
        this.zoneValid = zoneValid;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Product_ getProduct() {
        return product;
    }

    public void setProduct(Product_ product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
