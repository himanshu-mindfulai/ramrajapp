package com.mindfulai.Models.subscription;

import com.mindfulai.Models.varientsByCategory.Attribute;
import com.mindfulai.Models.varientsByCategory.Images;

import java.util.ArrayList;

public class ProductTwoDataModel {
    private Images images;
    private String sku;
    private double price;
    private int minOrderQuantity;
    private double sellingPrice;
    private int stock;
    private ArrayList<Attribute> attributes;
    private ProductThreeDataModel product;

    public Images getImages() {
        return images;
    }

    public void setImages(Images images) {
        this.images = images;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
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

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public ArrayList<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(ArrayList<Attribute> attributes) {
        this.attributes = attributes;
    }

    public ProductThreeDataModel getProduct() {
        return product;
    }

    public void setProduct(ProductThreeDataModel product) {
        this.product = product;
    }
}
