package com.mindfulai.Models;

import com.mindfulai.Models.varientsByCategory.Attribute;
import com.mindfulai.Models.varientsByCategory.WholeSalePriceModel;

import java.io.Serializable;
import java.util.ArrayList;

public class DifferentVarients implements Serializable {
    private String price;
    private float sellingPrice;
    private ArrayList<String> option_value;
    private String id;
    private Integer stock;
    private Integer minQty;
    private ArrayList<String> imagesList;
    private int inCart;
    private String tag;
    private boolean subscribable;
    private ArrayList<Attribute> attribute;
    private ArrayList<WholeSalePriceModel> wholeSalePriceModelArrayList;

    public DifferentVarients(String price, ArrayList<String> option_value, String id, Integer stock, float selling_price, Integer minQty, ArrayList<String> imagesList, int inCart, boolean subscribable, ArrayList<Attribute> attribute, ArrayList<WholeSalePriceModel> wholeSalePriceModelArrayList,String tag) {
        this.price = price;
        this.id = id;
        this.option_value = option_value;
        this.stock = stock;
        this.sellingPrice = selling_price;
        this.minQty = minQty;
        this.imagesList = imagesList;
        this.inCart = inCart;
        this.subscribable = subscribable;
        this.attribute = attribute;
        this.wholeSalePriceModelArrayList = wholeSalePriceModelArrayList;
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }


    public ArrayList<WholeSalePriceModel> getWholeSalePriceModelArrayList() {
        return wholeSalePriceModelArrayList;
    }

    public void setWholeSalePriceModelArrayList(ArrayList<WholeSalePriceModel> wholeSalePriceModelArrayList) {
        this.wholeSalePriceModelArrayList = wholeSalePriceModelArrayList;
    }

    public ArrayList<Attribute> getAttribute() {
        return attribute;
    }

    public void setAttribute(ArrayList<Attribute> attribute) {
        this.attribute = attribute;
    }

    public boolean isSubscribable() {
        return subscribable;
    }

    public void setSubscribable(boolean subscribable) {
        this.subscribable = subscribable;
    }

    public int getInCart() {
        return inCart;
    }

    public void setInCart(int inCart) {
        this.inCart = inCart;
    }

    public ArrayList<String> getImagesList() {
        return imagesList;
    }

    public void setImagesList(ArrayList<String> imagesList) {
        this.imagesList = imagesList;
    }

    public Integer getMinQty() {
        return minQty;
    }

    public void setMinQty(Integer minQty) {
        this.minQty = minQty;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public ArrayList<String> getOption_value() {
        return option_value;
    }

    public void setOption_value(ArrayList<String> option_value) {
        this.option_value = option_value;
    }

    public float getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(float sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

}
