package com.mindfulai.Models.varientsByCategory;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Attribute implements Serializable {

    @SerializedName("attribute")
    @Expose
    private Attribute_ attribute;
    @SerializedName("option")
    @Expose
    private Option option;

    public Attribute() {
    }

    public Attribute_ getAttribute() {
        return attribute;
    }

    public void setAttribute(Attribute_ attribute) {
        this.attribute = attribute;
    }

    public Option getOption() {
        return option;
    }

    public void setOption(Option option) {
        this.option = option;
    }
}
