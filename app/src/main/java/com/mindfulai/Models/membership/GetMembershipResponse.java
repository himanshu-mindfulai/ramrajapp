package com.mindfulai.Models.membership;

public class GetMembershipResponse {
    private String _id;
    private String expireAt;
    private boolean expired;
    private String name;
    private double price;
    private String purchasedOn;
    private String startingFrom;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getExpiredAt() {
        return expireAt;
    }

    public void setExpiredAt(String expiredAt) {
        this.expireAt = expiredAt;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getPurchasedOn() {
        return purchasedOn;
    }

    public void setPurchasedOn(String purchasedOn) {
        this.purchasedOn = purchasedOn;
    }

    public String getStartingFrom() {
        return startingFrom;
    }

    public void setStartingFrom(String startingFrom) {
        this.startingFrom = startingFrom;
    }
}
