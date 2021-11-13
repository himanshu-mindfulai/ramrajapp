package com.mindfulai.Models.DeliveryMethod;

import java.util.ArrayList;

public class DeliveryMethodData {
    private String _id;
    private String name;
    private ArrayList<Zones> zones;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Zones> getZoneData() {
        return zones;
    }
}
