package com.mindfulai.Models.flashbanner;

public class FlashBannerData {
    private String id;
    private String image;
    private BannerTarget target;
    private String type;

    public String getType() {
        return type;
    }

    public BannerTarget getTarget() {
        return target;
    }

    public String getID() { return id; }
    public void setID(String value) { this.id = value; }

    public String getImage() { return image; }
    public void setImage(String value) { this.image = value; }
}
