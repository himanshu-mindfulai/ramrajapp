package com.mindfulai.Models.varientsByCategory;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Option implements Serializable {

    @SerializedName("_id")
    @Expose
    private String id;
    @SerializedName("parent")
    @Expose
    private String parent;
    @SerializedName("value")
    @Expose
    private String value;

    public Option() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
