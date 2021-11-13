package com.mindfulai.Models;

import com.mindfulai.Activites.ProductsByVendorActivity;
import com.mindfulai.Models.varientsByCategory.Datum;

import java.util.ArrayList;
import java.util.HashMap;

public class VendorProductsByCategory {
    private String title;
    private ArrayList<Datum> products;
    private boolean selected;

    public VendorProductsByCategory(String title, ArrayList<Datum> products) {
        this.title = title;
        this.products = products;
        this.selected = true;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<Datum> getProducts() {
        return products;
    }

    public void setProducts(ArrayList<Datum> products) {
        this.products = products;
    }
}
