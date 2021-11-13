package com.mindfulai.Models.subscription;

import com.mindfulai.Models.UserDataAddress;

public class SubscriptionDataModel {
    private UserDataAddress address;
    private String _id;
    private DaysDataModel days;
    private ProductOneDataModel product;
    private String status;

    public void setStatus(String status) {
        this.status = status;
    }

    public String isStatus() {
        return status;
    }

    public UserDataAddress getAddress() {
        return address;
    }

    public void setAddress(UserDataAddress address) {
        this.address = address;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public DaysDataModel getDays() {
        return days;
    }

    public void setDays(DaysDataModel days) {
        this.days = days;
    }

    public ProductOneDataModel getProduct() {
        return product;
    }

    public void setProduct(ProductOneDataModel product) {
        this.product = product;
    }
}