package com.hmmelton.releave.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by harrisonmelton on 11/3/16.
 * This is a model class that holds the information for 1 restroom.
 */
public class Restroom {

    double lat, lng;
    String name, address;
    boolean locked;

    public Restroom(double lat, double lng, String name, String address, boolean locked) {
        this.lat = lat;
        this.lng = lng;
        this.name = name;
        this.address = address;
    }

    public Restroom(JSONObject restroom) {
        try {
            this.lat = restroom.getDouble("lat");
            this.lng = restroom.getDouble("lng");
            this.name = restroom.getString("name");
            this.address = restroom.getString("address");
        } catch (JSONException e) {
            // TODO: handle error
        }
    }
}
